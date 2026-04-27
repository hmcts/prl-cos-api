package uk.gov.hmcts.reform.prl.mapper.bundle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.prl.config.BundleCategoryConfig;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.bundle.DocumentProperties;
import uk.gov.hmcts.reform.prl.models.bundle.FilterProperties;
import uk.gov.hmcts.reform.prl.models.bundle.FolderProperties;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.FM5_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ORDERS_SUBMITTED_WITH_APPLICATION;

@ExtendWith(MockitoExtension.class)
class BundleCreateRequestByCategoryMapperTest {

    public static final String AUTH_TOKEN = "AUTH_TOKEN";
    @Mock
    private CategoriesAndDocumentsHelper categoriesAndDocumentsHelper;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private BundleCategoryConfig bundleCategoryConfig;

    @InjectMocks
    private BundleCreateRequestByCategoryMapper bundleCreateRequestByCategoryMapper;

    @Test
    void mapCaseDataToBundleCreateRequest() {
        uk.gov.hmcts.reform.ccd.client.model.Document documents =
            new uk.gov.hmcts.reform.ccd.client.model
                .Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());

        Category subCategory = new Category(FM5_STATEMENTS, FM5_STATEMENTS, 3, List.of(documents), new ArrayList<>());
        Category applicantApplicationCategory = new Category(APPLICANT_APPLICATION, APPLICANT_APPLICATION, 4, List.of(documents), new ArrayList<>());
        Category orderCategory = new Category(ORDERS_SUBMITTED_WITH_APPLICATION, ORDERS_SUBMITTED_WITH_APPLICATION,
                                              5, List.of(documents), new ArrayList<>());
        Category category = new Category("parentCategoryId", "parentCategoryName", 2, List.of(documents), List.of(subCategory));

        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .applicantName("ApplicantFirstNameAndLastName")
            .build();

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(categoriesAndDocumentsHelper.getCategoriesAndDocuments(AUTH_TOKEN, c100CaseData))
            .thenReturn(List.of(category, subCategory, applicantApplicationCategory, orderCategory));

        FilterProperties fm5StatementsFilterProperties = FilterProperties.builder().value(FM5_STATEMENTS)
            .category(FM5_STATEMENTS).build();
        DocumentProperties documentProperties = DocumentProperties.builder().property("/data/allOtherDocuments")
            .filters(List.of(fm5StatementsFilterProperties)).build();
        FilterProperties applicantApplicationFilterProperties = FilterProperties.builder().value(APPLICANT_APPLICATION)
            .category(APPLICANT_APPLICATION).build();
        DocumentProperties applicationsDocumentProperties = DocumentProperties.builder().property("/data/applications")
            .filters(List.of(applicantApplicationFilterProperties)).build();
        FilterProperties ordersFilterProperties = FilterProperties.builder().value(ORDERS_SUBMITTED_WITH_APPLICATION)
            .category(ORDERS_SUBMITTED_WITH_APPLICATION).build();
        DocumentProperties ordersDocumentProperties = DocumentProperties.builder().property("/data/orders")
            .filters(List.of(ordersFilterProperties)).build();


        FolderProperties folderProperties = FolderProperties.builder().name("folder1")
            .documents(List.of(documentProperties, applicationsDocumentProperties, ordersDocumentProperties)).build();
        when(bundleCategoryConfig.getFolders()).thenReturn(List.of(folderProperties));

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestByCategoryMapper
            .mapCaseDataToBundleCreateRequest(c100CaseData, "eventI",
                                              Hearings.hearingsWith().build(), "sample.yaml");

        assertNotNull(bundleCreateRequest);

        // Should not contain police disclosures or medical records
        assertTrue(bundleCreateRequest.getCaseDetails().getCaseData().getData().getAllOtherDocuments().stream()
                       .map(Element::getValue)
                       .map(BundlingRequestDocument::getDocumentFileName)
                       .filter(fileName -> List.of("policeDisclosures", "medicalRecords", "anyOtherDocuments")
                           .contains(fileName)).toList().isEmpty());
    }
}
