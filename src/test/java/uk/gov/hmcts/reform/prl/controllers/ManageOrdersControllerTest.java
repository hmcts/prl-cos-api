package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioBeforeAEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrLegalAdvisorCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.AmendOrderService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum.noticeOfProceedingsParties;
import static uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum.standardDirectionsOrder;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class ManageOrdersControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ManageOrdersController manageOrdersController;

    @Mock
    private ObjectMapper objectMapper;

    private CaseDetails caseDetails;

    private CaseData caseData;

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Mock
    private ManageOrderEmailService manageOrderEmailService;

    @Mock
    private ManageOrderService manageOrderService;

    @Mock
    private UserService userService;

    @Mock
    private  DocumentLanguageService documentLanguageService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private UserDetails userDetails;

    @Mock
    private IdamClient idamClient;
    @Mock
    private AmendOrderService amendOrderService;

    @Mock
    private HearingDataService hearingDataService;

    @Mock
    private HearingService hearingService;

    @Mock
    private AllTabServiceImpl allTabService;

    @Mock
    @Qualifier("caseSummaryTab")
    CaseSummaryTabService caseSummaryTabService;

    PartyDetails applicant;

    PartyDetails respondent;

    @Mock
    AllTabServiceImpl tabService;

    @Mock
    RefDataUserService refDataUserService;

    @Mock
    RoleAssignmentService roleAssignmentService;

    @MockBean
    private SystemUserService systemUserService;

    private StartAllTabsUpdateDataContent startAllTabsUpdateDataContent;

    @Before
    public void setUp() {
        List<String> roles = new ArrayList();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .roles(roles)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        when(hearingDataService.populateHearingDynamicLists(Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any()))
            .thenReturn(HearingDataPrePopulatedDynamicLists.builder().build());

        when(hearingDataService.getHearingDataForOtherOrders(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(List.of(Element.<HearingData>builder().build()));

        when(hearingService.getHearings(Mockito.anyString(),Mockito.anyString())).thenReturn(Hearings.hearingsWith().build());

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();
        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();
        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);
        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);
        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);
        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);
        caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .manageOrders(ManageOrders.builder().markedToServeEmailNotification(Yes).build())
            .courtName("testcourt")
            .build();
        Map<String, Object> stringObjectMaps = caseData.toMap(new ObjectMapper());
        startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            stringObjectMaps,
            caseData,
            null
        );
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(any(), any(), any(), any(), any()))
            .thenReturn(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().build());

    }

    @Test
    public void testSubmitApplicationEventValidation() throws Exception {
        CaseData expectedCaseData = CaseData.builder()
            .id(12345L)
            .courtName("Horsham Court")
            .manageOrders(ManageOrders.builder().build())
            .uploadOrderDoc(Document.builder().build())
            .build();

        Map<String, Object> stringObjectMap = expectedCaseData.toMap(new ObjectMapper());

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("c21DraftFilename")
                                 .build())
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                   .id(12345L)
                                   .data(stringObjectMap)
                                   .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(expectedCaseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .populatePreviewOrderWhenOrderUploaded(authToken, s2sToken, callbackRequest);
        assertNotNull(callbackResponse);
    }

    @Test
    public void testPopulatePreviewOrderWhenOrderUploaded() throws Exception {
        CaseData expectedCaseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .uploadOrderDoc(Document.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blank)
            .dateOrderMade(LocalDate.now())
            .build();

        ObjectMapper objectMapper1 = new ObjectMapper();
        objectMapper1.findAndRegisterModules();

        Map<String, Object> stringObjectMap = expectedCaseData.toMap(objectMapper1);

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("c21DraftFilename")
                                 .build())
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(expectedCaseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .populatePreviewOrderWhenOrderUploaded(authToken,s2sToken,callbackRequest);
        assertNotNull(callbackResponse);
    }

    @Test
    public void testPopulatePreviewOrderWithError() throws Exception {
        CaseData expectedCaseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.justicesLegalAdviser).build())
            .uploadOrderDoc(Document.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blank)
            .dateOrderMade(LocalDate.now())
            .build();

        ObjectMapper objectMapper1 = new ObjectMapper();
        objectMapper1.findAndRegisterModules();

        Map<String, Object> stringObjectMap = expectedCaseData.toMap(objectMapper1);

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("c21DraftFilename")
                                 .build())
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(expectedCaseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .populatePreviewOrderWhenOrderUploaded(authToken,s2sToken,callbackRequest);
        assertNotNull(callbackResponse);
    }

    @Test
    public void testPopulatePreviewOrderWithErrorblank() throws Exception {
        CaseData expectedCaseData = CaseData.builder()
            .id(12345L)
            .justiceLegalAdviserFullName(" ")
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.justicesLegalAdviser).build())
            .uploadOrderDoc(Document.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blank)
            .dateOrderMade(LocalDate.now())
            .build();

        ObjectMapper objectMapper1 = new ObjectMapper();
        objectMapper1.findAndRegisterModules();

        Map<String, Object> stringObjectMap = expectedCaseData.toMap(objectMapper1);

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("c21DraftFilename")
                                 .build())
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(expectedCaseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .populatePreviewOrderWhenOrderUploaded(authToken,s2sToken,callbackRequest);
        assertNotNull(callbackResponse);
    }


    @Test
    public void testManageOrderApplicationEventValidation() throws Exception {

        CaseData expectedCaseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .uploadOrderDoc(Document.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("c21DraftFilename")
                                 .build())
            .uploadOrderDoc(Document.builder()
                                .documentUrl(generatedDocumentInfo.getUrl())
                                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                .documentHash(generatedDocumentInfo.getHashToken())
                                .documentFileName("c21DraftFilename")
                                .build())
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        Map<String, Object> stringObjectMap = expectedCaseData.toMap(new ObjectMapper());
        Map<String, String> dataFieldMap = new HashMap<>();
        dataFieldMap.put(PrlAppsConstants.TEMPLATE, "templateName");
        dataFieldMap.put(PrlAppsConstants.FILE_NAME, "fileName");

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(expectedCaseData);
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .populatePreviewOrderWhenOrderUploaded(authToken, s2sToken, callbackRequest);
        assertNotNull(callbackResponse);
    }

    @Test
    public void testManageOrderFL404bApplicationEventValidation() throws Exception {

        CaseData expectedCaseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .uploadOrderDoc(Document.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blank)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blank)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("fl404bDraftFilename")
                                 .build())
            .uploadOrderDoc(Document.builder()
                                .documentUrl(generatedDocumentInfo.getUrl())
                                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                .documentHash(generatedDocumentInfo.getHashToken())
                                .documentFileName("fl404bDraftFilename")
                                .build())
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        Map<String, Object> stringObjectMap = expectedCaseData.toMap(new ObjectMapper());
        Map<String, String> dataFieldMap = new HashMap<>();
        dataFieldMap.put(PrlAppsConstants.TEMPLATE, "templateName");
        dataFieldMap.put(PrlAppsConstants.FILE_NAME, "fileName");

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(expectedCaseData);
        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(documentLanguage);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .populatePreviewOrderWhenOrderUploaded(authToken, s2sToken, callbackRequest);
        assertNotNull(callbackResponse);
    }

    @Test
    public void testFetchFl401DataNoticeOfProceedings() {
        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().c21OrderOptions(C21OrderOptionsEnum.c21other).build())
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedings)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .isSdoSelected(No)
            .build();

        CaseData updatedCaseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().c21OrderOptions(C21OrderOptionsEnum.c21other).build())
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(ManageOrders.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .courtName("testCourt")
            .childrenList("Child 1: TestName\n")
            .selectedOrder(
                "Test Case 45678\\n\\nFamily Man ID: familyman12345\\n\\nFinancial compensation order following C79 "
                    + "enforcement application (C82)\\n\\n")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(null)
            .build();
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(manageOrderService.populateCustomOrderFields(any(CaseData.class), Mockito.any(CreateSelectOrderOptionsEnum.class)))
            .thenReturn(updatedCaseData);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(manageOrderService.populateHearingsDropdown(anyString(), any(CaseData.class))).thenReturn(dynamicList);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .prepopulateFL401CaseDetails("auth-test", s2sToken, callbackRequest);
        assertNotNull(callbackResponse);
    }

    @Test
    public void testNotPrepopulateFl401DataNoticeOfProceedingsIfC100() {
        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().id(UUID.randomUUID()).value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .children(listOfChildren)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedings)
            .build();

        CaseData updatedCaseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .manageOrders(ManageOrders.builder().build())
            .children(listOfChildren)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .childrenList("Child 1: TestName\n")
            .selectedOrder(
                "Test Case 45678\\n\\nFamily Man ID: familyman12345\\n\\nFinancial compensation order following C79 "
                    + "enforcement application (C82)\\n\\n")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                   .id(12345L)
                                   .data(stringObjectMap)
                                   .build())
            .build();
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(manageOrderService.populateCustomOrderFields(any(CaseData.class), Mockito.any(CreateSelectOrderOptionsEnum.class)))
            .thenReturn(updatedCaseData);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(manageOrderService.populateHearingsDropdown(anyString(), any(CaseData.class))).thenReturn(dynamicList);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .prepopulateFL401CaseDetails("auth-test", s2sToken, callbackRequest);
        assertNotNull(callbackResponse);

    }

    @Test
    public void testBlankOrderOrDirections() {

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                   .id(12345L)
                                   .data(stringObjectMap)
                                   .build())
            .build();
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(manageOrderService.populateHearingsDropdown(anyString(), any(CaseData.class))).thenReturn(dynamicList);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .prepopulateFL401CaseDetails("auth-test", s2sToken, callbackRequest);
        assertNotNull(callbackResponse);

    }

    @Test
    public void testChildArrangement() {

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                   .id(12345L)
                                   .data(stringObjectMap)
                                   .build())
            .build();
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(manageOrderService.populateHearingsDropdown(anyString(), any(CaseData.class))).thenReturn(dynamicList);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .prepopulateFL401CaseDetails("auth-test", s2sToken, callbackRequest);
        assertNotNull(callbackResponse);

    }

    @Test
    public void testParentalResponsability() {

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.parentalResponsibility)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                   .id(12345L)
                                   .data(stringObjectMap)
                                   .build())
            .build();
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(manageOrderService.populateHearingsDropdown(anyString(), any(CaseData.class))).thenReturn(dynamicList);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .prepopulateFL401CaseDetails("auth-test", s2sToken, callbackRequest);
        assertNotNull(callbackResponse);

    }

    @Test
    public void testSpecialGuardianship() {

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                   .id(12345L)
                                   .data(stringObjectMap)
                                   .build())
            .build();
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(manageOrderService.populateHearingsDropdown(anyString(), any(CaseData.class))).thenReturn(dynamicList);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .prepopulateFL401CaseDetails("auth-test", s2sToken, callbackRequest);
        assertNotNull(callbackResponse);

    }

    @Test
    public void testNoticeOfProcceedingsParties() {

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedingsParties)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                   .id(12345L)
                                   .data(stringObjectMap)
                                   .build())
            .build();
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(manageOrderService.populateHearingsDropdown(anyString(), any(CaseData.class))).thenReturn(dynamicList);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .prepopulateFL401CaseDetails("auth-test", s2sToken, callbackRequest);
        assertNotNull(callbackResponse);

    }

    @Test
    public void testNoticeOfProceedingsNonParties() {

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedingsNonParties)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                   .id(12345L)
                                   .data(stringObjectMap)
                                   .build())
            .build();
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(manageOrderService.populateHearingsDropdown(anyString(), any(CaseData.class))).thenReturn(dynamicList);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .prepopulateFL401CaseDetails("auth-test", s2sToken, callbackRequest);
        assertNotNull(callbackResponse);

    }

    @Test
    public void testAppointmentOfGuardian() {

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.appointmentOfGuardian)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                   .id(12345L)
                                   .data(stringObjectMap)
                                   .build())
            .build();
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(manageOrderService.populateHearingsDropdown(anyString(), any(CaseData.class))).thenReturn(dynamicList);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .prepopulateFL401CaseDetails("auth-test", s2sToken, callbackRequest);
        assertNotNull(callbackResponse);

    }

    @Test
    public void testStandardDirectionsOrder() {

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                   .id(12345L)
                                   .data(stringObjectMap)
                                   .build())
            .build();
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(manageOrderService.populateHearingsDropdown(anyString(), any(CaseData.class))).thenReturn(dynamicList);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .prepopulateFL401CaseDetails("auth-test", s2sToken, callbackRequest);
        assertNotNull(callbackResponse);

    }

    @Test
    public void testFL401DataWithHomeSituation() {
        ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
            .childFullName("TestName")
            .childsAge("23")
            .build();

        Element<ChildrenLiveAtAddress> wrappedChildren = Element.<ChildrenLiveAtAddress>builder().value(childrenLiveAtAddress).build();
        List<Element<ChildrenLiveAtAddress>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .home(Home.builder().children(listOfChildren).build())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .isSdoSelected(No)
            .build();

        CaseData updatedCaseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .home(Home.builder().children(listOfChildren).build())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .fl401FamilymanCaseNumber("12345")
            .childrenList("Child 1: TestName\n")
            .manageOrders(ManageOrders.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                   .id(12345L)
                                   .data(stringObjectMap)
                                   .build())
            .build();

        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(manageOrderService.populateCustomOrderFields(any(CaseData.class), Mockito.any(CreateSelectOrderOptionsEnum.class)))
            .thenReturn(updatedCaseData);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(manageOrderService.populateHearingsDropdown(anyString(), any(CaseData.class))).thenReturn(dynamicList);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = manageOrdersController.prepopulateFL401CaseDetails("auth-test", s2sToken, callbackRequest);
        assertNotNull(response);
    }

    @Test
    @Ignore
    public void testSubmitAmanageorderEmailValidation() throws Exception {

        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .state(State.CASE_ISSUED.getValue())
                             .build())
            .build();
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = new StartAllTabsUpdateDataContent(authToken,EventRequestData.builder().build(),
                                                StartEventResponse.builder().build(), stringObjectMap, caseData, null);
        when(allTabService.getStartAllTabsUpdate("12345")).thenReturn(startAllTabsUpdateDataContent);
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);
        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse
            = manageOrdersController.sendEmailNotificationOnClosingOrder(
            authToken,
            s2sToken,
            callbackRequest
        );
        verify(manageOrderEmailService, times(1))
            .sendEmailWhenOrderIsServed(anyString(), any(CaseData.class), anyMap());
    }

    @Test
    public void saveOrderDetailsTest() throws Exception {

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
            .build()).build();

        ManageOrders manageOrders = ManageOrders.builder()
            .nameOfLaToReviewOrder(dynamicList)
            .cafcassEmailAddress(listOfCafcassEmail)
            .isCaseWithdrawn(No)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().nameOfLaToReviewOrder(dynamicList).build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("PRL-ORDER-C21-COMMON.docx")
                                 .build())
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .previewOrderDoc(Document.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isTheOrderAboutAllChildren", No);
        stringObjectMap.put("isTheOrderAboutChildren", Yes);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        List<Element<OrderDetails>> orderDetailsList = List.of(Element.<OrderDetails>builder().value(
            OrderDetails.builder().build()).build());
        when(manageOrderService.addOrderDetailsAndReturnReverseSortedList(authToken,caseData))
            .thenReturn(Map.of("orderCollection", orderDetailsList));
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.saveOrderDetails(
            authToken,
            s2sToken,
            callbackRequest
        );
        // assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("previewOrderDoc"));
        assertEquals(orderDetailsList,aboutToStartOrSubmitCallbackResponse.getData().get("orderCollection"));
    }

    @Test
    public void saveOrderDetailsJudgeTest() throws Exception {

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
            .build()).build();

        ManageOrders manageOrders = ManageOrders.builder()
            .nameOfLaToReviewOrder(dynamicList)
            .cafcassEmailAddress(listOfCafcassEmail)
            .isCaseWithdrawn(No)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().amendOrderSelectCheckOptions(AmendOrderCheckEnum.noCheck)
                .nameOfJudgeToReviewOrder(JudicialUser.builder().idamId("123").build()).build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .previewOrderDoc(Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("PRL-ORDER-C21-COMMON.docx")
                .build())
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .previewOrderDoc(Document.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isTheOrderAboutAllChildren", No);
        stringObjectMap.put("isTheOrderAboutChildren", Yes);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        List<Element<OrderDetails>> orderDetailsList = List.of(Element.<OrderDetails>builder().value(
            OrderDetails.builder().build()).build());
        when(manageOrderService.addOrderDetailsAndReturnReverseSortedList(authToken,caseData))
            .thenReturn(Map.of("orderCollection", orderDetailsList));
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                .id(12345L)
                .data(stringObjectMap)
                .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.saveOrderDetails(
            authToken,
            s2sToken,
            callbackRequest
        );
        // assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("previewOrderDoc"));
        assertEquals(orderDetailsList,aboutToStartOrSubmitCallbackResponse.getData().get("orderCollection"));
    }

    @Test
    public void saveOrderDetailsNoOneApprovesTest() throws Exception {

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
            .build()).build();

        ManageOrders manageOrders = ManageOrders.builder()
            .nameOfLaToReviewOrder(dynamicList)
            .cafcassEmailAddress(listOfCafcassEmail)
            .isCaseWithdrawn(No)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().amendOrderSelectCheckOptions(AmendOrderCheckEnum.judgeOrLegalAdvisorCheck)
                .amendOrderSelectJudgeOrLa(JudgeOrLegalAdvisorCheckEnum.judge).build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .previewOrderDoc(Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("PRL-ORDER-C21-COMMON.docx")
                .build())
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .previewOrderDoc(Document.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isTheOrderAboutAllChildren", No);
        stringObjectMap.put("isTheOrderAboutChildren", Yes);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        List<Element<OrderDetails>> orderDetailsList = List.of(Element.<OrderDetails>builder().value(
            OrderDetails.builder().build()).build());
        when(manageOrderService.addOrderDetailsAndReturnReverseSortedList(authToken,caseData))
            .thenReturn(Map.of("orderCollection", orderDetailsList));
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                .id(12345L)
                .data(stringObjectMap)
                .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.saveOrderDetails(
            authToken,
            s2sToken,
            callbackRequest
        );
        // assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("previewOrderDoc"));
        assertEquals(orderDetailsList,aboutToStartOrSubmitCallbackResponse.getData().get("orderCollection"));
    }

    @Test
    public void populateHeaderTest() throws Exception {

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .previewOrderDoc(Document.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("manageOrderHeader1","test");
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(caseData)).thenReturn(stringObjectMap);
        when(manageOrderService.populateHeader(caseData))
            .thenReturn(stringObjectMap);
        List<DynamicListElement> elements = new ArrayList<>();
        when(hearingDataService.prePopulateHearingType(authToken)).thenReturn(elements);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.populateHeader(
            callbackRequest, authToken, s2sToken
        );
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("caseTypeOfApplication"));
    }

    @Test
    public void testShowPreviewOrderWhenOrderCreated() throws Exception {

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails =
            uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                .id(12345L)
                .data(stringObjectMap)
                .build();

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        manageOrdersController.showPreviewOrderWhenOrderCreated(
            authToken,
            s2sToken,
            callbackRequest
        );
        List<Element<AppointedGuardianFullName>> namesList = new ArrayList<>();
        verify(manageOrderService, times(1))
            .updateCaseDataWithAppointedGuardianNames(caseDetails, namesList);
    }


    @Test
    @Ignore
    public void testSubmitManageOrderCafacassEmailNotification() throws Exception {

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .state(State.CASE_ISSUED.getValue())
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassEmailAddress(listOfCafcassEmail)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();


        caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().markedToServeEmailNotification(Yes).build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("PRL-ORDER-C21-COMMON.docx")
                                 .build())
            .build();

        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .state(State.CASE_ISSUED.getValue())
                             .build())
            .build();
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authToken,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), stringObjectMap, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(any(), any(), any(), any(), any()))
            .thenReturn(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().build());
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.sendEmailNotificationOnClosingOrder(
            authToken,
            s2sToken,
            callbackRequest
        );
        verify(manageOrderEmailService, times(1))
            .sendEmailWhenOrderIsServed("Bearer TestAuthToken", caseData, stringObjectMap);
    }

    @Test
    public void testPopulateOrderToAmendDownloadLink() {

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassEmailAddress(listOfCafcassEmail)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();


        caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .manageOrdersOptions(ManageOrdersOptionsEnum.amendOrderUnderSlipRule)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("PRL-ORDER-C21-COMMON.docx")
                                 .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(manageOrderService.getOrderToAmendDownloadLink(caseData)).thenReturn(new HashMap<>());
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.populateOrderToAmendDownloadLink(
            authToken,
            s2sToken,
            callbackRequest
        );
        verify(manageOrderService, times(1))
            .getOrderToAmendDownloadLink(caseData);
    }

    @Test
    public void saveOrderDetailsTestWithResetChildOptions() throws Exception {

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
            .build()).build();

        ManageOrders manageOrders = ManageOrders.builder()
            .nameOfLaToReviewOrder(dynamicList)
            .cafcassEmailAddress(listOfCafcassEmail)
            .isCaseWithdrawn(Yes)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("PRL-ORDER-C21-COMMON.docx")
                                 .build())
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .previewOrderDoc(Document.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.amendOrderUnderSlipRule)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isTheOrderAboutAllChildren", Yes);
        stringObjectMap.put("isTheOrderAboutChildren", No);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        List<Element<OrderDetails>> orderDetailsList = List.of(Element.<OrderDetails>builder().value(
            OrderDetails.builder().build()).build());
        when(amendOrderService.updateOrder(caseData, authToken))
            .thenReturn(Map.of("orderCollection", orderDetailsList));
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.saveOrderDetails(
            authToken,
            s2sToken,
            callbackRequest
        );
        assertEquals(orderDetailsList,aboutToStartOrSubmitCallbackResponse.getData().get("orderCollection"));
    }

    @Test
    public void testManageOrderMidEvent() throws Exception {

        caseData = CaseData.builder()
            .id(12345L)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(Yes).build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isTheOrderAboutAllChildren", Yes);
        stringObjectMap.put("isTheOrderAboutChildren", No);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.manageOrderMidEvent(
            authToken,
            s2sToken,
            callbackRequest
        );
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData());
    }

    @Test
    public void testAddUploadOrder() throws Exception {

        ManageOrders manageOrders = ManageOrders.builder()
            .isCaseWithdrawn(Yes)
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("PRL-ORDER-C21-COMMON.docx")
                                 .build())
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .previewOrderDoc(Document.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.amendOrderUnderSlipRule)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(Yes).build())
            .build();

        List<Element<OrderDetails>> orderDetailsList = List.of(Element.<OrderDetails>builder().value(
            OrderDetails.builder().build()).build());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.addOrderDetailsAndReturnReverseSortedList(authToken, caseData))
            .thenReturn(Map.of("orderCollection", orderDetailsList));
        when(manageOrderService.populateHeader(caseData))
            .thenReturn(stringObjectMap);
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.whenToServeOrder(
            authToken,
            s2sToken,
            callbackRequest
        );
        assertEquals(Yes.getDisplayedValue(), aboutToStartOrSubmitCallbackResponse.getData().get("doYouWantToServeOrder"));
    }

    @Test
    public void testAddUploadOrderDoesntAmmendSlipRule() throws Exception {

        ManageOrders manageOrders = ManageOrders.builder()
            .isCaseWithdrawn(Yes)
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("PRL-ORDER-C21-COMMON.docx")
                                 .build())
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .previewOrderDoc(Document.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(Yes).build())
            .build();

        List<Element<OrderDetails>> orderDetailsList = List.of(Element.<OrderDetails>builder().value(
            OrderDetails.builder().build()).build());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.addOrderDetailsAndReturnReverseSortedList(authToken, caseData))
            .thenReturn(Map.of("orderCollection", orderDetailsList));
        when(manageOrderService.populateHeader(caseData))
            .thenReturn(stringObjectMap);
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.whenToServeOrder(
            authToken,
            s2sToken,
            callbackRequest
        );
        assertEquals(Yes.getDisplayedValue(), aboutToStartOrSubmitCallbackResponse.getData().get("doYouWantToServeOrder"));
    }

    @Test
    public void testAddUploadOrderDoesntNeedServing() throws Exception {

        ManageOrders manageOrders = ManageOrders.builder()
            .isCaseWithdrawn(Yes)
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("PRL-ORDER-C21-COMMON.docx")
                                 .build())
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .previewOrderDoc(Document.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(No).build())
            .build();

        List<Element<OrderDetails>> orderDetailsList = List.of(Element.<OrderDetails>builder().value(
            OrderDetails.builder().build()).build());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.addOrderDetailsAndReturnReverseSortedList(authToken, caseData))
            .thenReturn(Map.of("orderCollection", orderDetailsList));
        when(manageOrderService.populateHeader(caseData))
            .thenReturn(stringObjectMap);
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.whenToServeOrder(
            authToken,
            s2sToken,
            callbackRequest
        );
        assertEquals(No.getDisplayedValue(), aboutToStartOrSubmitCallbackResponse.getData().get("doYouWantToServeOrder"));
    }

    @Test
    public void testupdateManageOrdersisSdoSelectedNo() throws Exception {
        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedings)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .isSdoSelected(No)
            .build();

        CaseData updatedCaseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .createSelectOrderOptions(standardDirectionsOrder)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(ManageOrders.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .courtName("testCourt")
            .childrenList("Child 1: TestName\n")
            .selectedOrder(
                "Test Case 45678\\n\\nFamily Man ID: familyman12345\\n\\nFinancial compensation order following C79 "
                    + "enforcement application (C82)\\n\\n")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(null)
            .build();
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(manageOrderService.populateCustomOrderFields(any(CaseData.class), any(CreateSelectOrderOptionsEnum.class))).thenReturn(updatedCaseData);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(manageOrderService.populateHearingsDropdown(anyString(), any(CaseData.class))).thenReturn(dynamicList);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .prepopulateFL401CaseDetails(authToken, s2sToken,  callbackRequest);
        assertNotNull(callbackResponse);
    }

    @Test
    public void testupdateManageOrdersisSdoSelectedYes() throws Exception {
        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .parentalResponsibilityDetails("test")
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .createSelectOrderOptions(standardDirectionsOrder)
            .isSdoSelected(Yes)
            .build();

        CaseData updatedCaseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .createSelectOrderOptions(standardDirectionsOrder)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(ManageOrders.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .courtName("testCourt")
            .childrenList("Child 1: TestName\n")
            .selectedOrder(
                "Test Case 45678\\n\\nFamily Man ID: familyman12345\\n\\nFinancial compensation order following C79 "
                    + "enforcement application (C82)\\n\\n")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(null)
            .build();
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(manageOrderService.populateCustomOrderFields(any(CaseData.class), any(CreateSelectOrderOptionsEnum.class))).thenReturn(updatedCaseData);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(manageOrderService.populateHearingsDropdown(anyString(), any(CaseData.class))).thenReturn(dynamicList);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .prepopulateFL401CaseDetails(authToken, s2sToken,  callbackRequest);
        assertNotNull(callbackResponse);
    }

    @Test
    public void testExceptionForPopulatePreviewOrderWhenOrderUploaded() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(List.of(element(PartyDetails.builder().firstName("app").build())))
            .respondents(List.of(element(PartyDetails.builder().firstName("resp").build())))
            .children(List.of(element(Child.builder().firstName("ch").build())))
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().applicantName("asd").build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            manageOrdersController.populatePreviewOrderWhenOrderUploaded(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForPrepopulateFL401CaseDetails() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(List.of(element(PartyDetails.builder().firstName("app").build())))
            .respondents(List.of(element(PartyDetails.builder().firstName("resp").build())))
            .children(List.of(element(Child.builder().firstName("ch").build())))
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().applicantName("asd").build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            manageOrdersController.prepopulateFL401CaseDetails(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForPopulateHeader() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(List.of(element(PartyDetails.builder().firstName("app").build())))
            .respondents(List.of(element(PartyDetails.builder().firstName("resp").build())))
            .children(List.of(element(Child.builder().firstName("ch").build())))
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().applicantName("asd").build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            manageOrdersController.populateHeader(callbackRequest, authToken, s2sToken);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForSendEmailNotificationOnClosingOrder() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(List.of(element(PartyDetails.builder().firstName("app").build())))
            .respondents(List.of(element(PartyDetails.builder().firstName("resp").build())))
            .children(List.of(element(Child.builder().firstName("ch").build())))
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().applicantName("asd").build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            manageOrdersController.sendEmailNotificationOnClosingOrder(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForshowPreviewOrderWhenOrderCreated() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(List.of(element(PartyDetails.builder().firstName("app").build())))
            .respondents(List.of(element(PartyDetails.builder().firstName("resp").build())))
            .children(List.of(element(Child.builder().firstName("ch").build())))
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().applicantName("asd").build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            manageOrdersController.showPreviewOrderWhenOrderCreated(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForshowPreviewOrderWhenOrderCreatedWithHearingData() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(List.of(element(PartyDetails.builder().firstName("app").build())))
            .respondents(List.of(element(PartyDetails.builder().firstName("resp").build())))
            .children(List.of(element(Child.builder().firstName("ch").build())))
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().applicantName("asd").build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails =
            uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                .id(12345L)
                .data(stringObjectMap)
                .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        when(manageOrderService
                 .getHearingDataFromExistingHearingData(any(),any(),any()))
            .thenReturn(List.of(element(HearingData.builder().applicantName("asd").build())));
        assertNotNull(manageOrdersController.showPreviewOrderWhenOrderCreated(authToken, s2sToken, callbackRequest));

    }

    @Test
    public void testExceptionForpopulateOrderToAmendDownloadLink() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(List.of(element(PartyDetails.builder().firstName("app").build())))
            .respondents(List.of(element(PartyDetails.builder().firstName("resp").build())))
            .children(List.of(element(Child.builder().firstName("ch").build())))
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().applicantName("asd").build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            manageOrdersController.populateOrderToAmendDownloadLink(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForAddUploadOrder() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(List.of(element(PartyDetails.builder().firstName("app").build())))
            .respondents(List.of(element(PartyDetails.builder().firstName("resp").build())))
            .children(List.of(element(Child.builder().firstName("ch").build())))
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().applicantName("asd").build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            manageOrdersController.whenToServeOrder(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForManageOrderMidEvent() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(List.of(element(PartyDetails.builder().firstName("app").build())))
            .respondents(List.of(element(PartyDetails.builder().firstName("resp").build())))
            .children(List.of(element(Child.builder().firstName("ch").build())))
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().applicantName("asd").build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            manageOrdersController.manageOrderMidEvent(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForServeOrderMidEvent() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(List.of(element(PartyDetails.builder().firstName("app").build())))
            .respondents(List.of(element(PartyDetails.builder().firstName("resp").build())))
            .children(List.of(element(Child.builder().firstName("ch").build())))
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().applicantName("asd").build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            manageOrdersController.serveOrderMidEvent(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForprePopulateJudgeOrLegalAdviser() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(List.of(element(PartyDetails.builder().firstName("app").build())))
            .respondents(List.of(element(PartyDetails.builder().firstName("resp").build())))
            .children(List.of(element(Child.builder().firstName("ch").build())))
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().applicantName("asd").build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            manageOrdersController.prePopulateJudgeOrLegalAdviser(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testNoHearingDataValidation() throws Exception {
        CaseData caseData = CaseData.builder()
            .createSelectOrderOptions(noticeOfProceedingsParties)
            .manageOrders(ManageOrders.builder().build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.populatePreviewOrder(authToken, callbackRequest, caseData)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .validateAndPopulateHearingData(authToken, s2sToken, callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("Please provide at least one hearing details", callbackResponse.getErrors().get(0));
    }

    @Test
    public void testNoHearingDataSelectedValidation() throws Exception {
        CaseData caseData = CaseData.builder()
            .createSelectOrderOptions(noticeOfProceedingsParties)
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(List.of(element(HearingData.builder().build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.populatePreviewOrder(authToken, callbackRequest, caseData)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .validateAndPopulateHearingData(authToken, s2sToken, callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("Please provide at least one hearing details", callbackResponse.getErrors().get(0));
    }

    @Test
    public void testMoreThanOneHearingsSelectedValidation() throws Exception {
        HearingData hearingData1 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedByListingTeam)
            .build();
        HearingData hearingData2 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .build();
        CaseData caseData = CaseData.builder()
            .createSelectOrderOptions(noticeOfProceedingsParties)
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(List.of(element(hearingData1), element(hearingData2))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.populatePreviewOrder(authToken, callbackRequest, caseData)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .validateAndPopulateHearingData(authToken, s2sToken, callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("Only one hearing can be created", callbackResponse.getErrors().get(0));
    }

    @Test
    public void testHearingTypeAndEstimatedTimingsValidations() throws Exception {
        HearingData hearingData = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .hearingEstimatedDays("ABC")
            .hearingEstimatedHours("DEF")
            .hearingEstimatedMinutes("XYZ")
            .build();
        CaseData caseData = CaseData.builder()
            .createSelectOrderOptions(noticeOfProceedingsParties)
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(List.of(element(hearingData))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.populatePreviewOrder(authToken, callbackRequest, caseData)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .validateAndPopulateHearingData(authToken, s2sToken, callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("You must select a hearing type", callbackResponse.getErrors().get(0));
        assertEquals("Please enter numeric value for Hearing estimated days", callbackResponse.getErrors().get(1));
        assertEquals("Please enter numeric value for Hearing estimated hours", callbackResponse.getErrors().get(2));
        assertEquals("Please enter numeric value for Hearing estimated minutes", callbackResponse.getErrors().get(3));
    }

    @Test
    public void testValidateAndPopulateHearingData() throws Exception {
        CaseData expectedCaseData = CaseData.builder()
            .id(12345L)
            .courtName("Horsham Court")
            .manageOrders(ManageOrders.builder().build())
            .uploadOrderDoc(Document.builder().build())
            .build();

        Map<String, Object> stringObjectMap = expectedCaseData.toMap(new ObjectMapper());

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("c21DraftFilename")
                                 .build())
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                   .id(12345L)
                                   .data(stringObjectMap)
                                   .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(expectedCaseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .validateAndPopulateHearingData(authToken, s2sToken, callbackRequest);

        assertNotNull(callbackResponse);
    }

    @Test
    public void saveOrderDetailsTestForServedSavedOrder() throws Exception {

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        List<Element<HearingData>> hearingElementList = new ArrayList<>();
        hearingElementList.add(element(HearingData.builder().build()));

        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
            .build()).build();

        ManageOrders manageOrders = ManageOrders.builder()
            .nameOfLaToReviewOrder(dynamicList)
            .cafcassEmailAddress(listOfCafcassEmail)
            .isCaseWithdrawn(YesOrNo.No)
            .ordersHearingDetails(hearingElementList)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("PRL-ORDER-C21-COMMON.docx")
                                 .build())
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .previewOrderDoc(Document.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isTheOrderAboutAllChildren", YesOrNo.No);
        stringObjectMap.put("isTheOrderAboutChildren", YesOrNo.Yes);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        List<Element<OrderDetails>> orderDetailsList = List.of(Element.<OrderDetails>builder().value(
            OrderDetails.builder().build()).build());
        when(manageOrderService.addOrderDetailsAndReturnReverseSortedList(authToken,caseData))
            .thenReturn(Map.of("orderCollection", orderDetailsList));
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(hearingDataService.getHearingDataForSelectedHearing(Mockito.any(),Mockito.any(),Mockito.anyString()))
            .thenReturn(hearingElementList);

        when(hearingService.getHearings(Mockito.anyString(),Mockito.anyString())).thenReturn(Hearings.hearingsWith().build());
        when(manageOrderService.serveOrder(Mockito.any(), Mockito.any())).thenReturn(orderDetailsList);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.saveOrderDetails(
            authToken,
            s2sToken,
            callbackRequest
        );
        // assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("previewOrderDoc"));
        assertEquals(orderDetailsList,aboutToStartOrSubmitCallbackResponse.getData().get("orderCollection"));
    }

    @Test
    public void saveOrderDetailsTestForUploadOrder() throws Exception {

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        List<Element<HearingData>> hearingElementList = new ArrayList<>();
        hearingElementList.add(element(HearingData.builder().build()));

        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
            .build()).build();

        ManageOrders manageOrders = ManageOrders.builder()
            .nameOfLaToReviewOrder(dynamicList)
            .cafcassEmailAddress(listOfCafcassEmail)
            .isCaseWithdrawn(YesOrNo.No)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("PRL-ORDER-C21-COMMON.docx")
                                 .build())
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .previewOrderDoc(Document.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.uploadAnOrder)
            .createSelectOrderOptions(standardDirectionsOrder)
            .standardDirectionOrder(StandardDirectionOrder.builder().build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isTheOrderAboutAllChildren", YesOrNo.No);
        stringObjectMap.put("isTheOrderAboutChildren", YesOrNo.Yes);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        List<Element<OrderDetails>> orderDetailsList = List.of(Element.<OrderDetails>builder().value(
            OrderDetails.builder().build()).build());
        when(manageOrderService.addOrderDetailsAndReturnReverseSortedList(authToken,caseData))
            .thenReturn(Map.of("orderCollection", orderDetailsList));
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(manageOrderService.setHearingDataForSdo(any(),any(), anyString()))
            .thenReturn(caseData);
        when(hearingService.getHearings(Mockito.anyString(),Mockito.anyString())).thenReturn(Hearings.hearingsWith().build());
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.saveOrderDetails(
            authToken,
            s2sToken,
            callbackRequest
        );
        // assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("previewOrderDoc"));
        assertEquals(orderDetailsList,aboutToStartOrSubmitCallbackResponse.getData().get("orderCollection"));
    }

    @Test
    public void testHearingTypeAndEstimatedTimingsValidationsForSdo() throws Exception {
        HearingData hearingData = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .hearingEstimatedDays("ABC")
            .hearingEstimatedHours("DEF")
            .hearingEstimatedMinutes("XYZ")
            .build();
        CaseData caseData = CaseData.builder()
            .createSelectOrderOptions(standardDirectionsOrder)
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(List.of(element(hearingData))).build())
            .standardDirectionOrder(StandardDirectionOrder.builder()
                                        .sdoUrgentHearingDetails(HearingData.builder()
                                                                     .hearingEstimatedHours("aa")
                                                                     .build())
                                        .sdoHearingsAndNextStepsList(List.of(SdoHearingsAndNextStepsEnum.urgentHearing))
                                        .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.populatePreviewOrder(authToken, callbackRequest, caseData)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .validateAndPopulateHearingData(authToken, s2sToken, callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals("Please enter numeric value for Hearing estimated hours", callbackResponse.getErrors().get(0));
    }

    @Test
    public void testManageOrderApplication() throws Exception {

        CaseData expectedCaseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.magistrate).build())
            .magistrateLastName(new ArrayList<>())
            .uploadOrderDoc(Document.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("c21DraftFilename")
                                 .build())
            .uploadOrderDoc(Document.builder()
                                .documentUrl(generatedDocumentInfo.getUrl())
                                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                .documentHash(generatedDocumentInfo.getHashToken())
                                .documentFileName("c21DraftFilename")
                                .build())
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        Map<String, Object> stringObjectMap = expectedCaseData.toMap(new ObjectMapper());
        Map<String, String> dataFieldMap = new HashMap<>();
        dataFieldMap.put(PrlAppsConstants.TEMPLATE, "templateName");
        dataFieldMap.put(PrlAppsConstants.FILE_NAME, "fileName");

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(expectedCaseData);
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .populatePreviewOrderWhenOrderUploaded(authToken, s2sToken, callbackRequest);
        assertNotNull(callbackResponse);
    }

    @Test
    public void testServeOrderMidEvent() {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(List.of(element(PartyDetails.builder().firstName("app").build())))
            .respondents(List.of(element(PartyDetails.builder().firstName("resp").build())))
            .children(List.of(element(Child.builder().firstName("ch").build())))
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().applicantName("asd").build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Mockito.when(manageOrderService.serveOrderMidEvent(any())).thenReturn(new HashMap<>());
        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        assertNotNull(manageOrdersController.serveOrderMidEvent(authToken, s2sToken, callbackRequest));
    }

    @Test
    public void testPrePopulateJudgeOrLegalAdviser() {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(List.of(element(PartyDetails.builder().firstName("app").build())))
            .respondents(List.of(element(PartyDetails.builder().firstName("resp").build())))
            .children(List.of(element(Child.builder().firstName("ch").build())))
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().applicantName("asd").build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        Mockito.when(refDataUserService.getLegalAdvisorList()).thenReturn(List.of(DynamicListElement.builder().build()));
        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        assertNotNull(manageOrdersController.prePopulateJudgeOrLegalAdviser(authToken, s2sToken, callbackRequest));
    }

    @Test
    public void testExceptionForPrePopulateJudgeOrLegalAdviser() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(List.of(element(PartyDetails.builder().firstName("app").build())))
            .respondents(List.of(element(PartyDetails.builder().firstName("resp").build())))
            .children(List.of(element(Child.builder().firstName("ch").build())))
            .courtName("testcourt")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .manageOrders(ManageOrders.builder().ordersHearingDetails(List.of(element(HearingData.builder().applicantName("asd").build()))).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            manageOrdersController.prePopulateJudgeOrLegalAdviser(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testPopulatePreviewOrderWhenOccupationOrderCreated() throws Exception {
        CaseData expectedCaseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder()
                              .fl404CustomFields(FL404.builder()
                                                     .build()).build())
            .uploadOrderDoc(Document.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.occupation)
            .dateOrderMade(LocalDate.now())
            .build();

        ObjectMapper objectMapper1 = new ObjectMapper();
        objectMapper1.findAndRegisterModules();

        Map<String, Object> stringObjectMap = expectedCaseData.toMap(objectMapper1);

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.occupation)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("c21DraftFilename")
                                 .build())
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(expectedCaseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .populatePreviewOrderWhenOrderUploaded(authToken,s2sToken,callbackRequest);
        assertNotNull(callbackResponse);
    }

    @Test
    public void testPopulatePreviewOrderWhenOccupationOrderAndAppOrRespPresent() throws Exception {
        CaseData expectedCaseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder()
                              .fl404CustomFields(FL404.builder()
                                                     .fl404bApplicantIsEntitledToOccupy(List.of("test"))
                                                     .build()).build())
            .uploadOrderDoc(Document.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.occupation)
            .dateOrderMade(LocalDate.now())
            .build();

        ObjectMapper objectMapper1 = new ObjectMapper();
        objectMapper1.findAndRegisterModules();

        Map<String, Object> stringObjectMap = expectedCaseData.toMap(objectMapper1);

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.occupation)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("c21DraftFilename")
                                 .build())
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(expectedCaseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .populatePreviewOrderWhenOrderUploaded(authToken,s2sToken,callbackRequest);
        assertNotNull(callbackResponse);
    }

    @Test
    public void saveOrderDetailsTestWithHearing() throws Exception {

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
            .build()).build();

        ManageOrders manageOrders = ManageOrders.builder()
            .nameOfLaToReviewOrder(dynamicList)
            .cafcassEmailAddress(listOfCafcassEmail)
            .isCaseWithdrawn(No)
            .c21OrderOptions(C21OrderOptionsEnum.c21other)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("PRL-ORDER-C21-COMMON.docx")
                                 .build())
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .previewOrderDoc(Document.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isTheOrderAboutAllChildren", No);
        stringObjectMap.put("isTheOrderAboutChildren", Yes);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        List<Element<OrderDetails>> orderDetailsList = List.of(Element.<OrderDetails>builder().value(
            OrderDetails.builder().build()).build());
        when(manageOrderService.addOrderDetailsAndReturnReverseSortedList(authToken,caseData))
            .thenReturn(Map.of("orderCollection", orderDetailsList));
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.saveOrderDetails(
            authToken,
            s2sToken,
            callbackRequest
        );
        assertEquals(orderDetailsList,aboutToStartOrSubmitCallbackResponse.getData().get("orderCollection"));
    }

    @Test
    public void saveOrderDetailsTestWithSdo() throws Exception {

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
            .build()).build();

        ManageOrders manageOrders = ManageOrders.builder()
            .nameOfLaToReviewOrder(dynamicList)
            .cafcassEmailAddress(listOfCafcassEmail)
            .isCaseWithdrawn(No)
            .c21OrderOptions(C21OrderOptionsEnum.c21other)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("PRL-ORDER-C21-COMMON.docx")
                                 .build())
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .previewOrderDoc(Document.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .createSelectOrderOptions(standardDirectionsOrder)

            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isTheOrderAboutAllChildren", No);
        stringObjectMap.put("isTheOrderAboutChildren", Yes);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        List<Element<OrderDetails>> orderDetailsList = List.of(Element.<OrderDetails>builder().value(
            OrderDetails.builder().build()).build());
        when(manageOrderService.addOrderDetailsAndReturnReverseSortedList(authToken,caseData))
            .thenReturn(Map.of("orderCollection", orderDetailsList));
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(manageOrderService.setHearingDataForSdo(any(),any(),any())).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.saveOrderDetails(
            authToken,
            s2sToken,
            callbackRequest
        );
        assertEquals(orderDetailsList,aboutToStartOrSubmitCallbackResponse.getData().get("orderCollection"));
    }


    @Test
    public void saveOrderDetailsTestFailedToAutherisation() throws Exception {

        applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .email("applicant@tests.com")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        respondent = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .canYouProvideEmailAddress(Yes)
            .email("respondent@tests.com")
            .isEmailAddressConfidential(No)
            .isAddressConfidential(No)
            .solicitorEmail("test@test.com")
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassEmailAddress(listOfCafcassEmail)
            .isCaseWithdrawn(No)
            .c21OrderOptions(C21OrderOptionsEnum.c21other)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .courtName("testcourt")
            .manageOrders(manageOrders)
            .previewOrderDoc(Document.builder()
                                 .documentUrl(generatedDocumentInfo.getUrl())
                                 .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                 .documentHash(generatedDocumentInfo.getHashToken())
                                 .documentFileName("PRL-ORDER-C21-COMMON.docx")
                                 .build())
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .previewOrderDoc(Document.builder().build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .createSelectOrderOptions(standardDirectionsOrder)

            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isTheOrderAboutAllChildren", No);
        stringObjectMap.put("isTheOrderAboutChildren", Yes);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        List<Element<OrderDetails>> orderDetailsList = List.of(Element.<OrderDetails>builder().value(
            OrderDetails.builder().build()).build());
        when(manageOrderService.addOrderDetailsAndReturnReverseSortedList(authToken,caseData))
            .thenReturn(Map.of("orderCollection", orderDetailsList));
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData))
            .thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(false);
        when(manageOrderService.setHearingDataForSdo(any(),any(),any())).thenReturn(caseData);
        assertExpectedException(() -> {
            manageOrdersController.saveOrderDetails(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    @Test
    public void testNoHearingDataValidationFailedToAutherisation() throws Exception {
        CaseData caseData = CaseData.builder()
            .createSelectOrderOptions(noticeOfProceedingsParties)
            .manageOrders(ManageOrders.builder().build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);

        assertExpectedException(() -> {
            manageOrdersController.validateAndPopulateHearingData(authToken, s2sToken, callbackRequest); }, RuntimeException.class, "Invalid Client");

    }

    @Test
    public void testAddressValidationError() throws Exception {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();
        List<String> errors = new ArrayList<>();
        errors.add("This order cannot be served by post until the respondent's " + "address is given.");
        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response  = AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors).build();
        when(manageOrderService.validateRespondentLipAndOtherPersonAddress(callbackRequest)).thenReturn(response);

        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .validateRespondentAndOtherPersonAddress(authToken, s2sToken, callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertEquals(errors.get(0), callbackResponse.getErrors().get(0));
    }

    @Test
    public void testCaseDataWhenNoValidationErrorReturned() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(123L)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response  = AboutToStartOrSubmitCallbackResponse.builder()
            .data(stringObjectMap).build();
        when(manageOrderService.validateRespondentLipAndOtherPersonAddress(callbackRequest)).thenReturn(response);
        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(true);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.validateRespondentLipAndOtherPersonAddress(callbackRequest)).thenReturn(response);

        AboutToStartOrSubmitCallbackResponse callbackResponse = manageOrdersController
            .validateRespondentAndOtherPersonAddress(authToken, s2sToken, callbackRequest);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getData());
        assertNotNull(callbackResponse.getData().get("id"));
        assertEquals("123", callbackResponse.getData().get("id").toString());
    }

    @Test
    public void testValidateAddressFailedToAuthorisation() throws Exception {
        CaseData caseData = CaseData.builder()
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);

        assertExpectedException(() -> {
            manageOrdersController
                .validateRespondentAndOtherPersonAddress(authToken, s2sToken, callbackRequest); },
                                RuntimeException.class, "Invalid Client");

    }

    @Test
    @Ignore ("Need to test and confirm for sendEmailNotificationOnClosingOrder")
    public void testSendEmailNotificationOnClosingOrder() throws Exception {
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .state(State.CASE_ISSUED.getValue())
                             .build())
            .build();
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(any(), any(), any(), any(), any()))
            .thenReturn(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().build());
        when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );
        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse
            = manageOrdersController.sendEmailNotificationOnClosingOrder(
            authToken,
            s2sToken,
            callbackRequest
        );
        verify(manageOrderEmailService, times(1))
            .sendEmailWhenOrderIsServed(anyString(), any(CaseData.class), anyMap());
    }
}
