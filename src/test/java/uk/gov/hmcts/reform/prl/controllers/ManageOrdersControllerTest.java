package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.AmendOrderService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.HearingDataService;
import uk.gov.hmcts.reform.prl.services.ManageOrderEmailService;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class ManageOrdersControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ManageOrdersController manageOrdersController;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CaseDetails caseDetails;

    @Mock
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
    private DynamicMultiSelectListService dynamicMultiSelectListService;

    @Mock
    private IdamClient idamClient;
    @Mock
    private AmendOrderService amendOrderService;

    @Mock
    private HearingDataService hearingDataService;

    @Mock
    @Qualifier("caseSummaryTab")
    CaseSummaryTabService caseSummaryTabService;

    PartyDetails applicant;
    PartyDetails respondent;
    Map<String, Object> summaryTabFields;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
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

        summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5");
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
    public void testFetchChildrenNamesList() {
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
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .children(listOfChildren)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.generalForm)
            .build();

        CaseData updatedCaseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .children(listOfChildren)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .fl401FamilymanCaseNumber("12345")
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
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(caseData)).thenReturn(stringObjectMap);
        when(manageOrderService.populateCustomOrderFields(any(CaseData.class))).thenReturn(updatedCaseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        CallbackResponse callbackResponse = manageOrdersController.fetchOrderDetails(authToken, s2sToken,callbackRequest);
        assertNotNull(callbackResponse);
    }

    @Test
    public void testFetchChildrenNamesListForNoticeOfProceedings() {
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
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .children(listOfChildren)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedings)
            .build();

        CaseData updatedCaseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .children(listOfChildren)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .fl401FamilymanCaseNumber("12345")
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
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(manageOrderService.populateCustomOrderFields(any(CaseData.class))).thenReturn(updatedCaseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        CallbackResponse callbackResponse = manageOrdersController.fetchOrderDetails(authToken,s2sToken,callbackRequest);
        assertEquals("Child 1: TestName\n", callbackResponse.getData().getChildrenList());
        assertEquals(
            "Test Case 45678\\n\\nFamily Man ID: familyman12345\\n\\nFinancial compensation order following C79 enforcement application (C82)\\n\\n",
            callbackResponse.getData().getSelectedOrder());
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
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .courtName("testCourt")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedings)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .build();

        CaseData updatedCaseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder().build())
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
        when(manageOrderService.populateCustomOrderFields(any(CaseData.class))).thenReturn(updatedCaseData);
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
        when(manageOrderService.populateCustomOrderFields(any(CaseData.class))).thenReturn(updatedCaseData);
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
        when(manageOrderService.populateCustomOrderFields(any(CaseData.class))).thenReturn(updatedCaseData);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(manageOrderService.populateHearingsDropdown(anyString(), any(CaseData.class))).thenReturn(dynamicList);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = manageOrdersController.prepopulateFL401CaseDetails("auth-test", s2sToken, callbackRequest);
        assertNotNull(response);
    }

    @Test
    public void testFetchChildrenNamesListWithHomeSituation() {
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
            .home(Home.builder().children(listOfChildren).build())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        CaseData updatedCaseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .home(Home.builder().children(listOfChildren).build())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .fl401FamilymanCaseNumber("12345")
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
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(manageOrderService.getUpdatedCaseData(any(CaseData.class))).thenReturn(stringObjectMap);
        when(manageOrderService.populateCustomOrderFields(any(CaseData.class))).thenReturn(updatedCaseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        CallbackResponse callbackResponse = manageOrdersController.fetchOrderDetails(authToken,s2sToken,callbackRequest);
        assertEquals("Child 1: TestName\n", callbackResponse.getData().getChildrenList());
        assertEquals(
            "Test Case 45678\\n\\nFamily Man ID: familyman12345\\n\\nFinancial compensation order following C79 enforcement application (C82)\\n\\n",
            callbackResponse.getData().getSelectedOrder());
    }

    @Test
    public void testSubmitAmanageorderEmailValidation() throws Exception {
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

        caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            .children(listOfChildren)
            .manageOrders(ManageOrders.builder().markedToServeEmailNotification(YesOrNo.Yes)
                              .isFinalOrderIssuedForAllChildren(YesOrNo.No)
                              .build())
            .courtName("testcourt")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .state(State.CASE_ISSUED.getValue())
                             .build())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.sendEmailNotificationOnClosingOrder(
            authToken,
            s2sToken,
            callbackRequest
        );
        verify(manageOrderEmailService, times(1))
            .sendEmailWhenOrderIsServed(callbackRequest.getCaseDetails());
    }

    @Test
    public void saveOrderDetailsTest() throws Exception {

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
            .isFinalOrderIssued(YesOrNo.Yes)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        ManageOrders manageOrders = ManageOrders.builder()
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
            .manageOrders(manageOrders)
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
        stringObjectMap.put("isTheOrderAboutAllChildren", YesOrNo.No);
        stringObjectMap.put("isTheOrderAboutChildren", YesOrNo.Yes);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        List<Element<OrderDetails>> orderDetailsList = List.of(Element.<OrderDetails>builder().value(
            OrderDetails.builder().build()).build());
        when(manageOrderService.addOrderDetailsAndReturnReverseSortedList(authToken,caseData))
            .thenReturn(Map.of("orderCollection", orderDetailsList));
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(manageOrderService.getAllChildrenFinalOrderIssuedStatus(caseData)).thenReturn(YesOrNo.Yes);
        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .state(State.CASE_ISSUED.getValue())
                             .createdDate(LocalDateTime.now())
                             .lastModified(LocalDateTime.now())
                             .build())
            .build();
        when(CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
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
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("manageOrderHeader1"));
    }

    @Test
    public void testShowPreviewOrderWhenOrderCreated() throws Exception {

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
    public void testSubmitManageOrderCafacassEmailNotification() throws Exception {

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
            .manageOrders(ManageOrders.builder().markedToServeEmailNotification(YesOrNo.Yes)
                              .isFinalOrderIssuedForAllChildren(YesOrNo.Yes)
                              .build())
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

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .state(State.ALL_FINAL_ORDERS_ISSUED.getValue())
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        AboutToStartOrSubmitCallbackResponse callbackResponse  = manageOrdersController.sendEmailNotificationOnClosingOrder(
            authToken,
            s2sToken,
            callbackRequest
        );
        verify(manageOrderEmailService, times(1))
            .sendEmailWhenOrderIsServed(callbackRequest.getCaseDetails());
    }

    @Test
    public void testPopulateOrderToAmendDownloadLink() {

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
            .isFinalOrderIssued(YesOrNo.Yes)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        String cafcassEmail = "testing@cafcass.com";

        Element<String> wrappedCafcass = Element.<String>builder().value(cafcassEmail).build();
        List<Element<String>> listOfCafcassEmail = Collections.singletonList(wrappedCafcass);

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassEmailAddress(listOfCafcassEmail)
            .isCaseWithdrawn(YesOrNo.Yes)
            .build();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(ManageOrders.builder()
                              .isCaseWithdrawn(YesOrNo.Yes)
                              .build())
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
        stringObjectMap.put("isTheOrderAboutAllChildren", YesOrNo.Yes);
        stringObjectMap.put("isTheOrderAboutChildren", YesOrNo.No);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        List<Element<OrderDetails>> orderDetailsList = List.of(Element.<OrderDetails>builder().value(
            OrderDetails.builder().build()).build());
        when(amendOrderService.updateOrder(caseData, authToken))
            .thenReturn(Map.of("orderCollection", orderDetailsList));
        when(manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(manageOrderService.getAllChildrenFinalOrderIssuedStatus(caseData)).thenReturn(YesOrNo.Yes);
        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .state(State.ALL_FINAL_ORDERS_ISSUED.getValue())
                             .data(stringObjectMap)
                             .createdDate(LocalDateTime.now())
                             .lastModified(LocalDateTime.now())
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
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isTheOrderAboutAllChildren", YesOrNo.Yes);
        stringObjectMap.put("isTheOrderAboutChildren", YesOrNo.No);
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
        assertEquals(YesOrNo.Yes,aboutToStartOrSubmitCallbackResponse.getData().get("ordersNeedToBeServed"));
    }

    @Test
    public void testAddUploadOrder() throws Exception {

        ManageOrders manageOrders = ManageOrders.builder()
            .isCaseWithdrawn(YesOrNo.Yes)
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
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
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
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.addUploadOrder(
            authToken,
            s2sToken,
            callbackRequest
        );
        assertEquals(YesOrNo.Yes,aboutToStartOrSubmitCallbackResponse.getData().get("ordersNeedToBeServed"));
    }

    @Test
    public void testAddUploadOrderDoesntAmmendSlipRule() throws Exception {

        ManageOrders manageOrders = ManageOrders.builder()
            .isCaseWithdrawn(YesOrNo.Yes)
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
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
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
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.addUploadOrder(
            authToken,
            s2sToken,
            callbackRequest
        );
        assertEquals(YesOrNo.Yes,aboutToStartOrSubmitCallbackResponse.getData().get("ordersNeedToBeServed"));
    }

    @Test
    public void testAddUploadOrderDoesntNeedServing() throws Exception {

        ManageOrders manageOrders = ManageOrders.builder()
            .isCaseWithdrawn(YesOrNo.Yes)
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
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.No).build())
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
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = manageOrdersController.addUploadOrder(
            authToken,
            s2sToken,
            callbackRequest
        );
        assertEquals(YesOrNo.No,aboutToStartOrSubmitCallbackResponse.getData().get("ordersNeedToBeServed"));
    }

    @Test
    public void testExceptionForPopulatePreviewOrderWhenOrderUploaded() throws Exception {

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
    public void testExceptionForpopulateOrderToAmendDownloadLink() throws Exception {

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
            manageOrdersController.addUploadOrder(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForManageOrderMidEvent() throws Exception {

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
}
