package uk.gov.hmcts.reform.prl.mapper.bundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class BundleCreateRequestMapperTest {
    @InjectMocks
    private BundleCreateRequestMapper bundleCreateRequestMapper;

    @Test
    public void testBundleCreateRequestMapper() {
        List<FurtherEvidence> furtherEvidences = new ArrayList<>();
        furtherEvidences.add(FurtherEvidence.builder().typeOfDocumentFurtherEvidence(FurtherEvidenceDocumentType.consentOrder)
            .documentFurtherEvidence(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Sample1.pdf").build())
            .restrictCheckboxFurtherEvidence(new ArrayList<>()).build());

        List<OtherDocuments> otherDocuments = new ArrayList<>();
        otherDocuments.add(OtherDocuments.builder().documentName("Application docu")
            .documentOther(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Sample2.pdf").build()).documentTypeOther(
                DocTypeOtherDocumentsEnum.applicantStatement).restrictCheckboxOtherDocuments(new ArrayList<>()).build());
        Document document = Document.builder().documentUrl("url").documentBinaryUrl("Url").documentFileName("fileName").build();

        OrderDetails orderDetails = OrderDetails.builder().orderDocument(document).orderType("ChildArrangement").build();
        List<OrderDetails> orderDetailsList = new ArrayList<>();
        orderDetailsList.add(orderDetails);
        CaseData caseData = CaseData.builder()
            .id(123456789123L)
            .welshLanguageRequirement(No)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .otherDocuments(ElementUtils.wrapElements(otherDocuments))
            .furtherEvidences(ElementUtils.wrapElements(furtherEvidences))
            .finalDocument(document)
            .orderCollection(ElementUtils.wrapElements(orderDetailsList))
            .build();

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(caseData,"eventI","sample.yaml");
        assertNotNull(bundleCreateRequest);
    }
}
