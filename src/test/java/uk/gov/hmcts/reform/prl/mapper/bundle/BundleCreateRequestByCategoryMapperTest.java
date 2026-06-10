package uk.gov.hmcts.reform.prl.mapper.bundle;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICATIONS_WITHIN_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.FM5_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.ORDERS_SUBMITTED_WITH_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_CLOSED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_SUBMITTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONFIDENTIAL;

@ExtendWith(MockitoExtension.class)
class BundleCreateRequestByCategoryMapperTest {

    public static final String AUTH_TOKEN = "AUTH_TOKEN";
    @Mock
    private CategoriesAndDocumentsHelper categoriesAndDocumentsHelper;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private BundleCategoryConfig bundleCategoryConfig;

    private HearingDetailsMapperUtil hearingDetailsMapperUtil;

    @InjectMocks
    private BundleCreateRequestByCategoryMapper bundleCreateRequestByCategoryMapper;

    @BeforeEach
    void setUp() {
        hearingDetailsMapperUtil = new HearingDetailsMapperUtil();
        bundleCreateRequestByCategoryMapper = new BundleCreateRequestByCategoryMapper(categoriesAndDocumentsHelper,
                                                                                      systemUserService,
                                                                                      bundleCategoryConfig,
                                                                                      hearingDetailsMapperUtil);
    }

    @Test
    void mapCaseDataToBundleCreateRequest() {
        uk.gov.hmcts.reform.ccd.client.model.Document documents =
            new uk.gov.hmcts.reform.ccd.client.model
                .Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());

        Document awpDocument = Document.builder().categoryId(APPLICATIONS_WITHIN_PROCEEDINGS).build();

        Category subCategory = new Category(FM5_STATEMENTS, FM5_STATEMENTS, 3, List.of(documents), new ArrayList<>());
        Category applicantApplicationCategory = new Category(APPLICANT_APPLICATION, APPLICANT_APPLICATION, 4, List.of(documents), new ArrayList<>());
        Category orderCategory = new Category(ORDERS_SUBMITTED_WITH_APPLICATION, ORDERS_SUBMITTED_WITH_APPLICATION,
                                              5, List.of(documents), new ArrayList<>());
        Category awpCategory = new Category(APPLICATIONS_WITHIN_PROCEEDINGS, APPLICATIONS_WITHIN_PROCEEDINGS,
                                              6, List.of(documents), new ArrayList<>());
        Category category = new Category("parentCategoryId", "parentCategoryName", 2, List.of(documents), List.of(subCategory));



        ResponseDocuments responseDocuments = ResponseDocuments.builder().citizenDocument(Document.builder().build()).build();
        AdditionalApplicationsBundle additionalApplicationsBundleWithSubmittedState = AdditionalApplicationsBundle.builder()
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                         .applicationStatus(AWP_STATUS_SUBMITTED)
                                         .finalDocument(List.of(ElementUtils.element(awpDocument)))
                                         .supportingEvidenceBundle(List.of(
                                             ElementUtils.element(SupportingEvidenceBundle.builder()
                                                                      .document(awpDocument).build())))
                                         .build())
            .build();
        AdditionalApplicationsBundle additionalApplicationsBundleWithCloseState = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(C2DocumentBundle.builder()
                                  .applicationStatus(AWP_STATUS_CLOSED)
                                  .finalDocument(List.of(ElementUtils.element(awpDocument)))
                                  .supportingEvidenceBundle(List.of(
                                      ElementUtils.element(SupportingEvidenceBundle.builder()
                                                               .document(awpDocument).build())))
                                  .build())
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                         .applicationStatus(AWP_STATUS_CLOSED)
                                         .finalDocument(List.of(ElementUtils.element(awpDocument)))
                                         .supportingEvidenceBundle(List.of(
                                             ElementUtils.element(SupportingEvidenceBundle.builder()
                                                                      .document(awpDocument).build())))
                                         .build())
            .build();
        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .applicantName("ApplicantFirstNameAndLastName")
            .citizenResponseC7DocumentList(List.of(Element.<ResponseDocuments>builder().id(UUID.randomUUID())
                                                       .value(responseDocuments).build()))
            .additionalApplicationsBundle(List.of(ElementUtils.element(additionalApplicationsBundleWithSubmittedState),
                                                  ElementUtils.element(additionalApplicationsBundleWithCloseState)))
            .build();

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(categoriesAndDocumentsHelper.getCategoriesAndDocuments(AUTH_TOKEN, c100CaseData))
            .thenReturn(List.of(category, subCategory, applicantApplicationCategory, orderCategory, awpCategory));

        FilterProperties fm5StatementsFilterProperties = FilterProperties.builder().value(FM5_STATEMENTS)
            .category(FM5_STATEMENTS).build();
        FilterProperties awpFilterProperties = FilterProperties.builder().value("applicantAWPDocuments")
            .category(APPLICATIONS_WITHIN_PROCEEDINGS).build();
        DocumentProperties documentProperties = DocumentProperties.builder().property("/data/allOtherDocuments")
            .filters(List.of(fm5StatementsFilterProperties, awpFilterProperties)).build();
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
        List<String> allOtherDocs = bundleCreateRequest.getCaseDetails().getCaseData().getData().getAllOtherDocuments().stream()
            .map(Element::getValue)
            .map(BundlingRequestDocument::getDocumentFileName).toList();

        assertEquals(5,allOtherDocs.size());


    }

    @Test
    void mapCaseDataToBundleCreateRequestWhenAwpInNonBundleCategory() {
        uk.gov.hmcts.reform.ccd.client.model.Document documents =
            new uk.gov.hmcts.reform.ccd.client.model
                .Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());

        Document awpDocument = Document.builder().categoryId(CONFIDENTIAL).build();

        Category subCategory = new Category(FM5_STATEMENTS, FM5_STATEMENTS, 3, List.of(documents), new ArrayList<>());
        Category applicantApplicationCategory = new Category(APPLICANT_APPLICATION, APPLICANT_APPLICATION, 4, List.of(documents), new ArrayList<>());
        Category orderCategory = new Category(ORDERS_SUBMITTED_WITH_APPLICATION, ORDERS_SUBMITTED_WITH_APPLICATION,
                                              5, List.of(documents), new ArrayList<>());
        Category awpCategory = new Category(APPLICATIONS_WITHIN_PROCEEDINGS, APPLICATIONS_WITHIN_PROCEEDINGS,
                                            6, List.of(documents), new ArrayList<>());
        Category category = new Category("parentCategoryId", "parentCategoryName", 2, List.of(documents), List.of(subCategory));



        ResponseDocuments responseDocuments = ResponseDocuments.builder().citizenDocument(Document.builder().build()).build();
        AdditionalApplicationsBundle additionalApplicationsBundleWithSubmittedState = AdditionalApplicationsBundle.builder()
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                         .applicationStatus(AWP_STATUS_SUBMITTED)
                                         .finalDocument(List.of(ElementUtils.element(awpDocument)))
                                         .supportingEvidenceBundle(List.of(
                                             ElementUtils.element(SupportingEvidenceBundle.builder()
                                                                      .document(awpDocument).build())))
                                         .build())
            .build();
        AdditionalApplicationsBundle additionalApplicationsBundleWithCloseState = AdditionalApplicationsBundle.builder()
            .c2DocumentBundle(C2DocumentBundle.builder()
                                  .applicationStatus(AWP_STATUS_CLOSED)
                                  .finalDocument(List.of(ElementUtils.element(awpDocument)))
                                  .supportingEvidenceBundle(List.of(
                                      ElementUtils.element(SupportingEvidenceBundle.builder()
                                                               .document(awpDocument).build())))
                                  .build())
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                         .applicationStatus(AWP_STATUS_CLOSED)
                                         .finalDocument(List.of(ElementUtils.element(awpDocument)))
                                         .supportingEvidenceBundle(List.of(
                                             ElementUtils.element(SupportingEvidenceBundle.builder()
                                                                      .document(awpDocument).build())))
                                         .build())
            .build();
        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .applicantName("ApplicantFirstNameAndLastName")
            .citizenResponseC7DocumentList(List.of(Element.<ResponseDocuments>builder().id(UUID.randomUUID())
                                                       .value(responseDocuments).build()))
            .additionalApplicationsBundle(List.of(ElementUtils.element(additionalApplicationsBundleWithSubmittedState),
                                                  ElementUtils.element(additionalApplicationsBundleWithCloseState)))
            .build();

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(categoriesAndDocumentsHelper.getCategoriesAndDocuments(AUTH_TOKEN, c100CaseData))
            .thenReturn(List.of(category, subCategory, applicantApplicationCategory, orderCategory, awpCategory));

        FilterProperties fm5StatementsFilterProperties = FilterProperties.builder().value(FM5_STATEMENTS)
            .category(FM5_STATEMENTS).build();
        FilterProperties awpFilterProperties = FilterProperties.builder().value("applicantAWPDocuments")
            .category(APPLICATIONS_WITHIN_PROCEEDINGS).build();
        DocumentProperties documentProperties = DocumentProperties.builder().property("/data/allOtherDocuments")
            .filters(List.of(fm5StatementsFilterProperties, awpFilterProperties)).build();
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
        List<String> allOtherDocs = bundleCreateRequest.getCaseDetails().getCaseData().getData().getAllOtherDocuments().stream()
            .map(Element::getValue)
            .map(BundlingRequestDocument::getDocumentFileName).toList();

        assertEquals(1,allOtherDocs.size());

    }


    @Test
    void testMapHearingDetailsWithNullHearings() {
        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .applicantName("ApplicantFirstNameAndLastName")
            .build();

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(categoriesAndDocumentsHelper.getCategoriesAndDocuments(AUTH_TOKEN, c100CaseData))
            .thenReturn(new ArrayList<>());
        when(bundleCategoryConfig.getFolders()).thenReturn(new ArrayList<>());

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestByCategoryMapper
            .mapCaseDataToBundleCreateRequest(c100CaseData, "eventId", null, "sample.yaml");

        assertNotNull(bundleCreateRequest);
        assertTrue(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime() == null
                   || bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime().isEmpty());
        assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingJudgeName());
        assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingVenueAddress());
    }

    @Test
    void testMapHearingDetailsWithEmptyHearings() {
        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .applicantName("ApplicantFirstNameAndLastName")
            .build();

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(categoriesAndDocumentsHelper.getCategoriesAndDocuments(AUTH_TOKEN, c100CaseData))
            .thenReturn(new ArrayList<>());
        when(bundleCategoryConfig.getFolders()).thenReturn(new ArrayList<>());

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestByCategoryMapper
            .mapCaseDataToBundleCreateRequest(c100CaseData, "eventId",
                Hearings.hearingsWith().caseHearings(new ArrayList<>()).build(), "sample.yaml");

        assertNotNull(bundleCreateRequest);
        assertTrue(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime() == null
                   || bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime().isEmpty());
        assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingJudgeName());
        assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingVenueAddress());
    }

    @Test
    void testMapHearingDetailsWithNoListedHearings() {
        final CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .applicantName("ApplicantFirstNameAndLastName")
            .build();

        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith().hmcStatus("ADJOURNED").build());
        caseHearings.add(CaseHearing.caseHearingWith().hmcStatus("CANCELLED").build());

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(categoriesAndDocumentsHelper.getCategoriesAndDocuments(AUTH_TOKEN, c100CaseData))
            .thenReturn(new ArrayList<>());
        when(bundleCategoryConfig.getFolders()).thenReturn(new ArrayList<>());

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestByCategoryMapper
            .mapCaseDataToBundleCreateRequest(c100CaseData, "eventId",
                Hearings.hearingsWith().caseHearings(caseHearings).build(), "sample.yaml");

        assertNotNull(bundleCreateRequest);
        assertTrue(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime() == null
                   || bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime().isEmpty());
        assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingJudgeName());
        assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingVenueAddress());
    }

    @Test
    void testMapHearingDetailsWithEmptyHearingDaySchedule() {
        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .applicantName("ApplicantFirstNameAndLastName")
            .build();

        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .hearingDaySchedule(new ArrayList<>())
            .build());

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(categoriesAndDocumentsHelper.getCategoriesAndDocuments(AUTH_TOKEN, c100CaseData))
            .thenReturn(new ArrayList<>());
        when(bundleCategoryConfig.getFolders()).thenReturn(new ArrayList<>());

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestByCategoryMapper
            .mapCaseDataToBundleCreateRequest(c100CaseData, "eventId",
                Hearings.hearingsWith().caseHearings(caseHearings).build(), "sample.yaml");

        assertNotNull(bundleCreateRequest);
        assertTrue(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime() == null
                   || bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime().isEmpty());
        assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingJudgeName());
        assertNull(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingVenueAddress());
    }

    @Test
    void testMapHearingDetailsWithCompleteHearingInformation() {
        final CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .applicantName("ApplicantFirstNameAndLastName")
            .build();

        LocalDateTime hearingDateTime = LocalDateTime.of(2025, 3, 15, 10, 30);
        List<HearingDaySchedule> hearingDaySchedules = new ArrayList<>();
        hearingDaySchedules.add(HearingDaySchedule.hearingDayScheduleWith()
            .hearingJudgeId("judge123")
            .hearingJudgeName("Judge John Smith")
            .hearingVenueId("venue123")
            .hearingVenueName("Manchester Crown Court")
            .hearingVenueAddress("Judicial Building, Bridge Street, Manchester M2 1RB")
            .hearingStartDateTime(hearingDateTime)
            .build());

        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .hearingDaySchedule(hearingDaySchedules)
            .build());

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(categoriesAndDocumentsHelper.getCategoriesAndDocuments(AUTH_TOKEN, c100CaseData))
            .thenReturn(new ArrayList<>());
        when(bundleCategoryConfig.getFolders()).thenReturn(new ArrayList<>());

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestByCategoryMapper
            .mapCaseDataToBundleCreateRequest(c100CaseData, "eventId",
                Hearings.hearingsWith().caseHearings(caseHearings).build(), "sample.yaml");

        assertNotNull(bundleCreateRequest);
        assertEquals("Judge John Smith", bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingJudgeName());
        assertTrue(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime().contains("15 Mar 2025"));
        assertTrue(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime().contains("AM"));
        assertEquals("Manchester Crown Court" + "\n" + "Judicial Building, Bridge Street, Manchester M2 1RB",
            bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingVenueAddress());
    }

    @Test
    void testMapHearingDetailsWithVenueAddressOnly() {
        final CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .applicantName("ApplicantFirstNameAndLastName")
            .build();

        List<HearingDaySchedule> hearingDaySchedules = new ArrayList<>();
        hearingDaySchedules.add(HearingDaySchedule.hearingDayScheduleWith()
            .hearingJudgeName("Judge Jane Doe")
            .hearingVenueName(null)
            .hearingVenueAddress("123 Court Street, London SW1A 1AA")
            .hearingStartDateTime(LocalDateTime.of(2025, 4, 20, 14, 0))
            .build());

        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .hearingDaySchedule(hearingDaySchedules)
            .build());

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(categoriesAndDocumentsHelper.getCategoriesAndDocuments(AUTH_TOKEN, c100CaseData))
            .thenReturn(new ArrayList<>());
        when(bundleCategoryConfig.getFolders()).thenReturn(new ArrayList<>());

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestByCategoryMapper
            .mapCaseDataToBundleCreateRequest(c100CaseData, "eventId",
                Hearings.hearingsWith().caseHearings(caseHearings).build(), "sample.yaml");

        assertNotNull(bundleCreateRequest);
        assertEquals("Judge Jane Doe", bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingJudgeName());
        assertEquals("123 Court Street, London SW1A 1AA",
            bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingVenueAddress());
        assertTrue(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime().contains("20 Apr 2025"));
    }

    @Test
    void testMapHearingDetailsWithNullHearingStartDateTime() {
        final CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .applicantName("ApplicantFirstNameAndLastName")
            .build();

        List<HearingDaySchedule> hearingDaySchedules = new ArrayList<>();
        hearingDaySchedules.add(HearingDaySchedule.hearingDayScheduleWith()
            .hearingJudgeName("Judge Bob Wilson")
            .hearingVenueName("Bristol County Court")
            .hearingVenueAddress("Small Street, Bristol BS1 1DA")
            .hearingStartDateTime(null)
            .build());

        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .hearingDaySchedule(hearingDaySchedules)
            .build());

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(categoriesAndDocumentsHelper.getCategoriesAndDocuments(AUTH_TOKEN, c100CaseData))
            .thenReturn(new ArrayList<>());
        when(bundleCategoryConfig.getFolders()).thenReturn(new ArrayList<>());

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestByCategoryMapper
            .mapCaseDataToBundleCreateRequest(c100CaseData, "eventId",
                Hearings.hearingsWith().caseHearings(caseHearings).build(), "sample.yaml");

        assertNotNull(bundleCreateRequest);
        assertEquals("Judge Bob Wilson", bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingJudgeName());
        assertEquals("Bristol County Court" + "\n" + "Small Street, Bristol BS1 1DA",
            bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingVenueAddress());
        assertTrue(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime().isEmpty());
    }

    @Test
    void testMapHearingDetailsSelectsFirstListedHearingWhenMultipleExist() {
        final CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .applicantName("ApplicantFirstNameAndLastName")
            .build();

        LocalDateTime firstHearingDateTime = LocalDateTime.of(2025, 5, 10, 9, 0);
        LocalDateTime secondHearingDateTime = LocalDateTime.of(2025, 6, 15, 14, 0);

        List<HearingDaySchedule> firstHearingSchedules = new ArrayList<>();
        firstHearingSchedules.add(HearingDaySchedule.hearingDayScheduleWith()
            .hearingJudgeName("Judge Alice Brown")
            .hearingVenueName("Liverpool Court")
            .hearingVenueAddress("31 Whitechapel, Liverpool L1 6DS")
            .hearingStartDateTime(firstHearingDateTime)
            .build());

        List<HearingDaySchedule> secondHearingSchedules = new ArrayList<>();
        secondHearingSchedules.add(HearingDaySchedule.hearingDayScheduleWith()
            .hearingJudgeName("Judge Charles Davis")
            .hearingVenueName("Leeds Court")
            .hearingVenueAddress("Leeds Court Address")
            .hearingStartDateTime(secondHearingDateTime)
            .build());

        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .hearingDaySchedule(firstHearingSchedules)
            .build());
        caseHearings.add(CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .hearingDaySchedule(secondHearingSchedules)
            .build());

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(categoriesAndDocumentsHelper.getCategoriesAndDocuments(AUTH_TOKEN, c100CaseData))
            .thenReturn(new ArrayList<>());
        when(bundleCategoryConfig.getFolders()).thenReturn(new ArrayList<>());

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestByCategoryMapper
            .mapCaseDataToBundleCreateRequest(c100CaseData, "eventId",
                Hearings.hearingsWith().caseHearings(caseHearings).build(), "sample.yaml");

        assertNotNull(bundleCreateRequest);
        // Should map the first LISTED hearing
        assertEquals("Judge Alice Brown", bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingJudgeName());
        assertEquals("Liverpool Court" + "\n" + "31 Whitechapel, Liverpool L1 6DS",
            bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingVenueAddress());
        assertTrue(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime().contains("10 May 2025"));
    }

    @Test
    void testMapHearingDetailsWithMultipleHearingDaySchedules() {
        LocalDateTime firstDayDateTime = LocalDateTime.of(2025, 7, 5, 10, 0);
        LocalDateTime secondDayDateTime = LocalDateTime.of(2025, 7, 6, 11, 0);

        List<HearingDaySchedule> hearingDaySchedules = new ArrayList<>();
        hearingDaySchedules.add(HearingDaySchedule.hearingDayScheduleWith()
            .hearingJudgeName("Judge Emma Wilson")
            .hearingVenueName("London Central Court")
            .hearingVenueAddress("Central London Address")
            .hearingStartDateTime(firstDayDateTime)
            .build());
        hearingDaySchedules.add(HearingDaySchedule.hearingDayScheduleWith()
            .hearingJudgeName("Judge Francis Graham")
            .hearingVenueName("North London Court")
            .hearingVenueAddress("North London Address")
            .hearingStartDateTime(secondDayDateTime)
            .build());

        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .hearingDaySchedule(hearingDaySchedules)
            .build());

        CaseData c100CaseData = CaseData.builder()
            .id(123456789123L)
            .applicantName("ApplicantFirstNameAndLastName")
            .build();

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(categoriesAndDocumentsHelper.getCategoriesAndDocuments(AUTH_TOKEN, c100CaseData))
            .thenReturn(new ArrayList<>());
        when(bundleCategoryConfig.getFolders()).thenReturn(new ArrayList<>());

        BundleCreateRequest bundleCreateRequest = bundleCreateRequestByCategoryMapper
            .mapCaseDataToBundleCreateRequest(c100CaseData, "eventId",
                Hearings.hearingsWith().caseHearings(caseHearings).build(), "sample.yaml");

        assertNotNull(bundleCreateRequest);
        // Should map the first day schedule
        assertEquals("Judge Emma Wilson", bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingJudgeName());
        assertEquals("London Central Court" + "\n" + "Central London Address",
            bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingVenueAddress());
        assertTrue(bundleCreateRequest.getCaseDetails().getCaseData().getData().getHearingDetails().getHearingDateAndTime().contains("5 Jul 2025"));
    }

}
