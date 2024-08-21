package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.editandapprove.OrderApprovalDecisionsForCourtAdminOrderEnum;
import uk.gov.hmcts.reform.prl.enums.editandapprove.OrderApprovalDecisionsForSolicitorOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DomesticAbuseOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.FcOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrdersOptionEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrganisationOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServeOtherPartiesOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.WithDrawTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoFurtherInstructionsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.SdoDetails;
import uk.gov.hmcts.reform.prl.models.ServeOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.ServeOrgDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AdditionalOrderDocument;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingDataFromTabToDocmosis;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_IS_ORDER_APPROVED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_ORDER_NAME_ADMIN_CREATED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_ORDER_NAME_JUDGE_CREATED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_WHO_APPROVED_THE_ORDER;
import static uk.gov.hmcts.reform.prl.enums.Event.MANAGE_ORDERS;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.prohibitedStepsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;
import static uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum.amendOrderUnderSlipRule;
import static uk.gov.hmcts.reform.prl.services.ManageOrderService.CHILD_OPTION;
import static uk.gov.hmcts.reform.prl.services.ManageOrderService.SDO_FACT_FINDING_FLAG;
import static uk.gov.hmcts.reform.prl.services.ManageOrderService.VALIDATION_ADDRESS_ERROR_OTHER_PARTY;
import static uk.gov.hmcts.reform.prl.services.ManageOrderService.VALIDATION_ADDRESS_ERROR_RESPONDENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ManageOrderServiceTest {


    @InjectMocks
    private ManageOrderService manageOrderService;

    @Mock
    private DgsService dgsService;

    @Mock
    private WelshCourtEmail welshCourtEmail;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private Time dateTime;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ElementUtils elementUtils;

    @Mock
    private DocumentLanguageService documentLanguageService;

    @Mock
    DynamicMultiSelectListService dynamicMultiSelectListService;

    private DynamicList dynamicList;
    private DynamicMultiSelectList dynamicMultiSelectList;
    private UUID uuid;
    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";
    private ManageOrders manageOrders;

    @Mock
    private UserService userService;

    @Mock
    private RoleAssignmentApi roleAssignmentApi;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private LocalDateTime now;
    @Mock
    private HearingService hearingService;

    @Mock
    private HearingDataService hearingDataService;

    @Mock
    private RefDataUserService refDataUserService;


    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setup() {
        now = dateTime.now();
        DynamicListElement dynamicListElement = DynamicListElement.builder().code(TEST_UUID).label(" ").build();
        dynamicList = DynamicList.builder()
            .listItems(List.of(dynamicListElement))
            .value(dynamicListElement)
            .build();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code(TEST_UUID)
            .label("test")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();
        List<Element<HearingData>> hearingDataList  = new ArrayList<>();
        HearingData hearingdata = HearingData.builder()
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata));
        manageOrders = ManageOrders.builder()
            .withdrawnOrRefusedOrder(WithDrawTypeOfOrderEnum.withdrawnApplication)
            .isCaseWithdrawn(YesOrNo.No)
            .ordersHearingDetails(hearingDataList)
            .childOption(
                dynamicMultiSelectList
            )
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        uuid = UUID.fromString(TEST_UUID);
        when(elementUtils.getDynamicListSelectedValue(Mockito.any(), Mockito.any())).thenReturn(uuid);
        when(dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(Mockito.any(CaseData.class)))
            .thenReturn(dynamicMultiSelectList);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(dynamicMultiSelectListService.getStringFromDynamicMultiSelectList(Mockito.any(DynamicMultiSelectList.class)))
            .thenReturn("testChild");
        when(userService.getUserByUserId(Mockito.anyString(), Mockito.anyString())).thenReturn(UserDetails.builder()
                                                                                                   .forename("")
                                                                                                   .surname("")
                                                                                                   .build());
        ReflectionTestUtils.setField(manageOrderService, "hearingStatusesToFilter", "COMPLETED, AWAITING_ACTUALS");
    }

    @Test
    public void getUpdatedCaseDataCaTest() {
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
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .children(listOfChildren)
            .manageOrders(manageOrders)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        Map<String, Object> caseDataUpdated = manageOrderService.getUpdatedCaseData(caseData);
        assertEquals("testChild", caseDataUpdated.get("childrenList"));
        assertNotNull(caseDataUpdated.get("selectedOrder"));
    }

    @Test
    public void getUpdatedCaseDataFl401() {

        ApplicantChild child = ApplicantChild.builder()
            .fullName("TestName")
            .build();

        Element<ApplicantChild> wrappedChildren = Element.<ApplicantChild>builder().value(child).build();
        List<Element<ApplicantChild>> listOfChildren = Collections.singletonList(wrappedChildren);

        ChildrenLiveAtAddress homeChild = ChildrenLiveAtAddress.builder()
            .childFullName("Test Child Name")
            .build();

        Element<ChildrenLiveAtAddress> wrappedHomeChildren = Element.<ChildrenLiveAtAddress>builder().value(homeChild).build();
        List<Element<ChildrenLiveAtAddress>> listOfHomeChildren = Collections.singletonList(wrappedHomeChildren);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .applicantChildDetails(listOfChildren)
            .manageOrders(manageOrders)
            .home(Home.builder()
                      .children(listOfHomeChildren)
                      .build())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        Map<String, Object> caseDataUpdated = manageOrderService.getUpdatedCaseData(caseData);
        assertEquals("testChild", caseDataUpdated.get("childrenList"));
        assertNotNull(caseDataUpdated.get("selectedOrder"));
    }

    @Test
    public void testPupulateHeader() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .familymanCaseNumber("familyman6789")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        Map<String, Object> responseMap = manageOrderService.populateHeader(caseData);

        assertNotNull(responseMap);
        assertNotNull(responseMap.get(CASE_TYPE_OF_APPLICATION));
        assertEquals(FL401_CASE_TYPE, responseMap.get(CASE_TYPE_OF_APPLICATION));
    }


    @Test
    public void whenFl404bOrder_thenPopulateCustomFields() {
        CaseData caseData = CaseData.builder()
            .id(12345674L)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blank)
            .courtName("Court name")
            .childArrangementOrders(ChildArrangementOrdersEnum.authorityC31)
            .applicantsFL401(PartyDetails.builder()
                                 .firstName("app")
                                 .lastName("testLast")
                                 .solicitorReference("test test")
                                 .representativeLastName("test")
                                 .representativeFirstName("test")
                                 .build())
            .manageOrders(ManageOrders.builder()
                              .recitalsOrPreamble("test")
                              .isCaseWithdrawn(YesOrNo.Yes)
                              .fl404CustomFields(FL404.builder().build())
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
                              .orderDirections("test")
                              .furtherDirectionsIfRequired("test")
                              .build())
            .respondentsFL401(PartyDetails.builder()
                                  .firstName("resp")
                                  .lastName("testLast")
                                  .solicitorReference("test test")
                                  .dateOfBirth(LocalDate.of(1990, 10, 20))
                                  .representativeLastName("test")
                                  .representativeFirstName("test")
                                  .address(Address.builder()
                                               .addressLine1("add1")
                                               .postCode("n145kk")
                                               .build())
                                  .build())
            .build();

        FL404 expectedDetails = FL404.builder()
            .fl404bApplicantName("app testLast")
            .fl404bCaseNumber("12345674")
            .fl404bCourtName("Court name")
            .fl404bRespondentName("resp testLast")
            .fl404bRespondentReference("test test")
            .fl404bApplicantReference("test test")
            .fl404bRespondentAddress(Address.builder()
                                         .addressLine1("add1")
                                         .postCode("n145kk")
                                         .build())
            .fl404bRespondentDob(LocalDate.of(1990, 10, 20))
            .build();

        CaseData updatedCaseData = manageOrderService.populateCustomOrderFields(caseData, CreateSelectOrderOptionsEnum.blank);

        assertEquals(updatedCaseData.getManageOrders().getFl404CustomFields(), expectedDetails);


    }

    @Test
    public void whenFl402Order_thenPopulateCustomFields() {
        ManageOrders expectedDetails = ManageOrders.builder()
            .manageOrdersFl402CaseNo("12345674")
            .manageOrdersFl402CourtName("Court name")
            .manageOrdersFl402Applicant("app testLast")
            .manageOrdersFl402ApplicantRef("test test1")
            .orderDirections("order dir")
            .furtherDirectionsIfRequired("fur dir")
            .recitalsOrPreamble("reci")
            .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.justicesClerk)
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345674L)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blank)
            .manageOrders(expectedDetails)
            .courtName("Court name")
            .childArrangementOrders(ChildArrangementOrdersEnum.authorityC31)
            .applicantsFL401(PartyDetails.builder()
                                 .firstName("app")
                                 .lastName("testLast")
                                 .representativeFirstName("test")
                                 .representativeLastName("test1")
                                 .build())
            .respondentsFL401(PartyDetails.builder()
                                  .firstName("resp")
                                  .lastName("testLast")
                                  .dateOfBirth(LocalDate.of(1990, 10, 20))
                                  .address(Address.builder()
                                               .addressLine1("add1")
                                               .postCode("n145kk")
                                               .build())
                                  .build())
            .build();


        CaseData updatedCaseData = manageOrderService.getFL402FormData(caseData);

        assertEquals(updatedCaseData.getManageOrders(), expectedDetails);

    }


    @Test
    public void testPopulatePreviewOrderFromCaSpecificOrder() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        ReflectionTestUtils.setField(manageOrderService, "c43WelshDraftTemplate", "c43WelshDraftTemplate");
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        Map<String, Object> caseDataUpdated = manageOrderService.getCaseData("test token", caseData,
                                                         CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));
    }

    @Test
    public void testPopulatePreviewOrderFromBlankOrderWithdrawn() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.blankOrderOrDirections
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));
    }

    @Test
    public void testPopulatePreviewOrderFromAppointmentOfGuardian() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.appointmentOfGuardian)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.appointmentOfGuardian
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));
    }

    @Test
    public void testPopulatePreviewOrderFromParentalResponsibility() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.parentalResponsibility)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.parentalResponsibility
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));
    }

    @Test
    public void testPopulatePreviewOrderFromTransferOfCase() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.transferOfCaseToAnotherCourt)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.transferOfCaseToAnotherCourt
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));
    }

    @Test
    public void testPopulatePreviewOrderFromSpecificGuardian() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.specialGuardianShip
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));
    }

    @Test
    public void testPopulatePreviewOrderFromPowerOfArrest() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.powerOfArrest)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.powerOfArrest
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));
    }


    @Test
    public void testPopulateHeaderC100Test() {
        CaseData.CaseDataBuilder builder = CaseData.builder();
        builder.id(12345L);
        builder.caseTypeOfApplication(C100_CASE_TYPE);
        builder.applicantCaseName("Test Case 45678");
        builder.manageOrdersOptions(amendOrderUnderSlipRule);
        builder.orderCollection(List.of(element(OrderDetails.builder()
                                                    .orderType("other")
                                                    .otherDetails(OtherOrderDetails.builder()
                                                                      .orderCreatedDate("10-Feb-2023").build()).build())));
        builder.childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82);
        CaseData caseData = builder
            .build();

        Map<String, Object> responseMap = manageOrderService.populateHeader(caseData);

        assertNotNull(responseMap);
        assertNotNull(responseMap.get(CASE_TYPE_OF_APPLICATION));
        assertEquals(C100_CASE_TYPE, responseMap.get(CASE_TYPE_OF_APPLICATION));
    }

    @Test
    public void testPopulateHeaderC100TestWithCafcass() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .isCafcass(YesOrNo.Yes)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(element(OrderDetails.builder()
                                                 .orderType("other")
                                                 .otherDetails(OtherOrderDetails.builder()
                                                                   .orderCreatedDate("10-Feb-2023").build()).build())))
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        Map<String, Object> responseMap = manageOrderService.populateHeader(caseData);

        assertNotNull(responseMap);
        assertNotNull(responseMap.get(CASE_TYPE_OF_APPLICATION));
        assertEquals(C100_CASE_TYPE, responseMap.get(CASE_TYPE_OF_APPLICATION));
    }

    @Test
    public void testPopulateHeaderC100TestWithRegionId() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseManagementLocation(CaseManagementLocation.builder().regionId("1").build())
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(element(OrderDetails.builder()
                                                 .orderType("other")
                                                 .otherDetails(OtherOrderDetails.builder()
                                                                   .orderCreatedDate("10-Feb-2023").build()).build())))
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        Map<String, Object> responseMap = manageOrderService.populateHeader(caseData);

        assertNotNull(responseMap);
        assertNotNull(responseMap.get(CASE_TYPE_OF_APPLICATION));
        assertEquals(C100_CASE_TYPE, responseMap.get(CASE_TYPE_OF_APPLICATION));
    }

    @Test
    public void testPopulateHeaderNoFl401FamilyManTest() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        Map<String, Object> responseMap = manageOrderService.populateHeader(caseData);

        assertNotNull(responseMap);
        assertNotNull(responseMap.get(CASE_TYPE_OF_APPLICATION));
        assertEquals(FL401_CASE_TYPE, responseMap.get(CASE_TYPE_OF_APPLICATION));
    }

    @Test
    public void testPopulatePreviewOrderBlankOrderFromCaseData() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .manageOrders(manageOrders)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.blankOrderOrDirections
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));
    }

    @Test
    public void testPopulatePreviewOrderCAorderFromCaseData() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData,
                                                         CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));

    }

    @Test
    public void testPopulatePreviewOrderSpecificGuardianOrderFromCaseData() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.specialGuardianShip
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));

    }

    @Test
    public void testPopulatePreviewOrderFromCaseData() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .manageOrders(manageOrders)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.blankOrderOrDirections
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));

    }

    @Test
    public void testPopulateFinalOrderFromCaseData() throws Exception {

        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder().forename("test")
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());


        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<OrderRecipientsEnum> recipientList = new ArrayList<>();
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        recipientList.add(OrderRecipientsEnum.applicantOrApplicantSolicitor);
        recipientList.add(OrderRecipientsEnum.respondentOrRespondentSolicitor);

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        ReflectionTestUtils.setField(manageOrderService, "c21Template", "c21-template");

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(StandardDirectionOrder.builder().build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            .dateOrderMade(LocalDate.now())
            .orderRecipients(recipientList)
            .applicants(partyDetails)
            .respondents(partyDetails)
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .doesOrderClosesCase(YesOrNo.Yes)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .manageOrders(manageOrders)
            .build();

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));

    }

    @Test
    public void testPopulateFinalOrderFromCaseDataWithNoCheck() throws Exception {

        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder().forename("test")
                                                                     .roles(List.of(Roles.COURT_ADMIN.getValue())).build());


        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<OrderRecipientsEnum> recipientList = new ArrayList<>();
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        recipientList.add(OrderRecipientsEnum.applicantOrApplicantSolicitor);
        recipientList.add(OrderRecipientsEnum.respondentOrRespondentSolicitor);

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        ReflectionTestUtils.setField(manageOrderService, "c21Template", "c21-template");
        manageOrders = ManageOrders.builder()
            .withdrawnOrRefusedOrder(WithDrawTypeOfOrderEnum.withdrawnApplication)
            .isCaseWithdrawn(YesOrNo.No)
            .amendOrderSelectCheckOptions(AmendOrderCheckEnum.noCheck)
            .childOption(
                dynamicMultiSelectList
            )
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(StandardDirectionOrder.builder().build())
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                                        .sdoHearingsAndNextStepsList(List.of(SdoHearingsAndNextStepsEnum.factFindingHearing))
                                        .build())
            .fl401FamilymanCaseNumber("familyman12345")
            .dateOrderMade(LocalDate.now())
            .orderRecipients(recipientList)
            .applicants(partyDetails)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .respondents(partyDetails)
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .doesOrderClosesCase(YesOrNo.Yes)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .manageOrders(manageOrders)
            .build();

        ReflectionTestUtils.setField(manageOrderService, "sdoWelshTemplate", "sdo-WEL-Template");

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));

    }

    @Test
    public void testPopulateFinalWelshOrderFromCaseData() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<OrderRecipientsEnum> recipientList = new ArrayList<>();
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        recipientList.add(OrderRecipientsEnum.applicantOrApplicantSolicitor);
        recipientList.add(OrderRecipientsEnum.respondentOrRespondentSolicitor);

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        ReflectionTestUtils.setField(manageOrderService, "c21Template", "c21-template");
        ReflectionTestUtils.setField(manageOrderService, "c21WelshTemplate", "c21-WEL-template");

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .dateOrderMade(LocalDate.now())
            .orderRecipients(recipientList)
            .applicants(partyDetails)
            .respondents(partyDetails)
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .doesOrderClosesCase(YesOrNo.Yes)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .manageOrders(manageOrders)
            .build();

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));

    }

    @Test
    public void testPopulateFinalUploadOrderFromCaseData() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .doesOrderClosesCase(YesOrNo.Yes)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .manageOrders(ManageOrders.builder().build())
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));

    }


    @Test
    public void testPopulateFinalUploadOrderFromCaseDataWithMultipleOrders() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .caseTypeOfApplication("FL401")
            .applicantsFL401(PartyDetails.builder().firstName("firstname")
                                 .lastName("lastname")
                                 .representativeFirstName("firstname")
                                 .representativeLastName("lastname")
                                 .build())
            .respondentsFL401(PartyDetails.builder().firstName("firstname")
                                  .lastName("lastname")
                                  .representativeFirstName("firstname")
                                  .representativeLastName("lastname")
                                  .build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(new ArrayList<>())
            .dateOrderMade(LocalDate.now())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .doesOrderClosesCase(YesOrNo.Yes)
            .manageOrders(manageOrders)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));

    }

    @Test
    public void testUpdateCaseDataWithAppointedGuardianNames() {

        List<Element<AppointedGuardianFullName>> namesList = new ArrayList<>();
        AppointedGuardianFullName appointedGuardianFullName = AppointedGuardianFullName.builder()
            .guardianFullName("Full Name")
            .build();

        Element<AppointedGuardianFullName> wrappedName = Element.<AppointedGuardianFullName>builder().value(
            appointedGuardianFullName).build();
        List<Element<AppointedGuardianFullName>> caseDataNameList = Collections.singletonList(wrappedName);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .appointedGuardianName(caseDataNameList)
            .build();

        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails =
            uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        manageOrderService.updateCaseDataWithAppointedGuardianNames(caseDetails, namesList);
        assertEquals("Full Name", caseDataNameList.get(0).getValue().getGuardianFullName());
    }


    @Test
    public void testPopulateFinalOrderFromCaseDataFL401() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<OrderRecipientsEnum> recipientList = new ArrayList<>();
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .firstName("")
            .lastName("")
            .dateOfBirth(LocalDate.now())
            .address(Address.builder().build())
            .solicitorReference("123")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("test")
            .representativeLastName("test")
            .build();

        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        recipientList.add(OrderRecipientsEnum.applicantOrApplicantSolicitor);
        recipientList.add(OrderRecipientsEnum.respondentOrRespondentSolicitor);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .applicantsFL401(PartyDetails.builder().firstName("firstname")
                                 .lastName("lastname")
                                 .representativeFirstName("firstname")
                                 .representativeLastName("lastname")
                                 .build())
            .respondentsFL401(PartyDetails.builder().firstName("firstname")
                                  .lastName("lastname")
                                  .representativeFirstName("firstname")
                                  .representativeLastName("lastname")
                                  .build())

            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .dateOrderMade(LocalDate.now())
            .orderRecipients(recipientList)
            .applicants(partyDetails)
            .respondents(partyDetails)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .doesOrderClosesCase(YesOrNo.Yes)
            .manageOrders(manageOrders)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));
    }


    @Test
    public void testPopulateFinalOrderFromCaseDataFL401ForMultipleOrders() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .dateOfBirth(LocalDate.now())
            .address(Address.builder().build())
            .solicitorReference("123")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("test")
            .representativeLastName("test")
            .build();

        PartyDetails respPartyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .dateOfBirth(LocalDate.now())
            .address(Address.builder().build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .applicantsFL401(partyDetails)
            .respondentsFL401(respPartyDetails)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(new ArrayList<>())
            .dateOrderMade(LocalDate.now())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .doesOrderClosesCase(YesOrNo.Yes)
            .manageOrders(manageOrders)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));

    }

    @Test
    public void testPopulatePreviewOrderFromCaseDataCaseAmendDischargedVariedFl404b() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.amendDischargedVaried)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.amendDischargedVaried
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));

    }

    @Test
    public void testPopulatePreviewOrderFromCaseDataCaseBlankOrderFl404b() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blank)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData, CreateSelectOrderOptionsEnum.blank);

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));

    }

    @Test
    public void testPopulatePreviewOrderFromCaseDataNonMolestationOrderFl404a() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.nonMolestation
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));

    }

    @Test
    public void testPopulatePreviewOrderFromCaseDataNonMolestationOrderFl404aForC100() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        List<OrderRecipientsEnum> recipientList = new ArrayList<>();
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .firstName("Test")
            .lastName("Test")
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicants(partyDetails)
            .respondents(partyDetails)
            .applicantCaseName("Test Case 45678")
            .manageOrders(ManageOrders.builder().fl404CustomFields(FL404.builder().build()).build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .fl401FamilymanCaseNumber("familyman12345")
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        CaseData caseData2 = manageOrderService.populateCustomOrderFields(
            caseData,
            CreateSelectOrderOptionsEnum.nonMolestation
        );

        assertNotNull(caseData2.getManageOrders().getFl404CustomFields().getFl404bApplicantName());

    }

    @Test
    public void testPopulatePreviewOrderFromCaseDataGeneralN117() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.generalForm)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.generalForm
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));

    }

    @Test
    public void testPopulatePreviewOrderFromCaseDataNoticeOfProceedingsFl402() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedings)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.noticeOfProceedings
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));

    }

    @Test
    public void testPopulatePreviewOrderFromSdo() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                                        .sdoHearingsAndNextStepsList(List.of(SdoHearingsAndNextStepsEnum.factFindingHearing))
                                        .sdoAllocateOrReserveJudgeName(JudicialUser.builder().idamId("").build()).build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .build();

        when(dgsService.generateDocument(anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        Map<String, Object> caseDataUpdated = manageOrderService.getCaseData(
            "test token",
            caseData,
            CreateSelectOrderOptionsEnum.standardDirectionsOrder
        );

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));
    }

    @Test
    public void testPopulateFinalOrderFromCaseDataCaseAmendDischargedVariedFl404b() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<OrderRecipientsEnum> recipientList = new ArrayList<>();
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        recipientList.add(OrderRecipientsEnum.applicantOrApplicantSolicitor);
        recipientList.add(OrderRecipientsEnum.respondentOrRespondentSolicitor);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.amendDischargedVaried)
            .fl401FamilymanCaseNumber("familyman12345")
            .dateOrderMade(LocalDate.now())
            .orderRecipients(recipientList)
            .applicants(partyDetails)
            .respondents(partyDetails)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .doesOrderClosesCase(YesOrNo.Yes)
            .manageOrders(manageOrders)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));

    }


    @Test
    public void testAddOrderDetailsAndReturnReverseSortedListWithNullOrgName() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<OrderRecipientsEnum> recipientList = new ArrayList<>();
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName(null).build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        recipientList.add(OrderRecipientsEnum.applicantOrApplicantSolicitor);
        recipientList.add(OrderRecipientsEnum.respondentOrRespondentSolicitor);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .dateOrderMade(LocalDate.now())
            .orderRecipients(recipientList)
            .applicants(partyDetails)
            .respondents(partyDetails)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .doesOrderClosesCase(YesOrNo.Yes)
            .manageOrders(manageOrders)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));

    }

    @Test
    public void testPopulateHeader() {

        List<OrderRecipientsEnum> recipientList = new ArrayList<>();
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName(null).build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        recipientList.add(OrderRecipientsEnum.applicantOrApplicantSolicitor);
        recipientList.add(OrderRecipientsEnum.respondentOrRespondentSolicitor);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .dateOrderMade(LocalDate.now())
            .orderRecipients(recipientList)
            .applicants(partyDetails)
            .respondents(partyDetails)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        Map<String, Object> responseMap = manageOrderService.populateHeader(caseData);

        assertNotNull(responseMap);
        assertNotNull(responseMap.get(CASE_TYPE_OF_APPLICATION));
        assertEquals(C100_CASE_TYPE, responseMap.get(CASE_TYPE_OF_APPLICATION));
    }

    @Test
    public void testPopulateHeaderWithCafcass() {

        List<OrderRecipientsEnum> recipientList = new ArrayList<>();
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName(null).build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        recipientList.add(OrderRecipientsEnum.applicantOrApplicantSolicitor);
        recipientList.add(OrderRecipientsEnum.respondentOrRespondentSolicitor);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .isCafcass(YesOrNo.Yes)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .dateOrderMade(LocalDate.now())
            .orderRecipients(recipientList)
            .applicants(partyDetails)
            .respondents(partyDetails)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        Map<String, Object> responseMap = manageOrderService.populateHeader(caseData);

        assertNotNull(responseMap);
        assertNotNull(responseMap.get(CASE_TYPE_OF_APPLICATION));
        assertEquals(C100_CASE_TYPE, responseMap.get(CASE_TYPE_OF_APPLICATION));
    }


    @Test
    public void testPopulateFinalOrderFromCaseDataCaseOccupationOrder() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<OrderRecipientsEnum> recipientList = new ArrayList<>();
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        recipientList.add(OrderRecipientsEnum.applicantOrApplicantSolicitor);
        recipientList.add(OrderRecipientsEnum.respondentOrRespondentSolicitor);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.occupation)
            .fl401FamilymanCaseNumber("familyman12345")
            .dateOrderMade(LocalDate.now())
            .orderRecipients(recipientList)
            .applicants(partyDetails)
            .respondents(partyDetails)
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .doesOrderClosesCase(YesOrNo.Yes)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .manageOrders(manageOrders)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));
    }

    @Test
    public void testgetOrderToAmendDownloadLink() {

        CaseData caseData = CaseData.builder()
            .orderCollection(List.of(Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                        .builder()
                                                                                        .orderDocument(Document
                                                                                                           .builder()
                                                                                                           .build())
                                                                                        .build()).build()))
            .manageOrders(ManageOrders.builder().amendOrderDynamicList(dynamicList).build())
            .build();
        assertNotNull(manageOrderService.getOrderToAmendDownloadLink(caseData));
    }

    @Test
    public void testpopulateCustomOrderFieldsGeneralForm() {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("fn")
            .lastName("ln")
            .dateOfBirth(LocalDate.now())
            .address(Address.builder().build())
            .solicitorReference("solRef")
            .build();
        CaseData caseData = CaseData.builder()
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .manageOrders(ManageOrders.builder().isTheOrderByConsent(YesOrNo.Yes).build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.generalForm).build();
        CaseData caseDataUpdated = manageOrderService.populateCustomOrderFields(caseData, CreateSelectOrderOptionsEnum.generalForm);
        assertNotNull(caseDataUpdated);
        assertNotNull(caseDataUpdated.getManageOrders().getManageOrdersApplicantReference());
        assertNotNull(caseDataUpdated.getManageOrders().getManageOrdersRespondentReference());
    }

    @Test
    public void testpopulateCustomOrderFieldsNoticeOfProceedings() {
        ManageOrders expectedDetails = ManageOrders.builder()
            .manageOrdersFl402CaseNo("12345674")
            .manageOrdersFl402CourtName("Court name")
            .manageOrdersFl402Applicant("app testLast")
            .manageOrdersFl402ApplicantRef("test test1")
            .orderDirections("order dir")
            .furtherDirectionsIfRequired("fur dir")
            .recitalsOrPreamble("reci")
            .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.justicesClerk)
            .build();
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .dateOfBirth(LocalDate.now())
            .address(Address.builder().build())
            .build();
        CaseData caseData = CaseData.builder()
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .manageOrders(expectedDetails)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedings).build();
        assertNotNull(manageOrderService.populateCustomOrderFields(caseData, CreateSelectOrderOptionsEnum.noticeOfProceedings));
    }

    @Test
    public void testServeOrderCA() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();


        List<DynamicMultiselectListElement> elements = new ArrayList<>();
        DynamicMultiselectListElement element = DynamicMultiselectListElement.builder()
            .code("1234")
            .label("test label").build();
        elements.add(element);
        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .childArrangementsOrdersToIssue(List.of(childArrangementsOrder,prohibitedStepsOrder))
            .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .recipientsOptions(DynamicMultiSelectList.builder()
                                   .listItems(elements)
                                   .build())
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                                                       .emailInformation(EmailInformation.builder().emailName("").build())
                                                       .build())))
            .childOption(DynamicMultiSelectList.builder()
                             .listItems(elements)
                             .build())
            .otherParties(DynamicMultiSelectList.builder()
                              .listItems(elements)
                              .build())
            .serveToRespondentOptions(YesOrNo.Yes)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .build();

        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                          .builder()
                                                                                          .orderDocument(Document
                                                                                                             .builder()
                                                                                                             .build())
                                                                                          .dateCreated(now)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .dateOrderMade(LocalDate.now())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));
    }

    @Test
    public void testServeOrderCaCafcassServedOptionsYes() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        ManageOrders manageOrders = ManageOrders.builder()
            .otherParties(dynamicMultiSelectList)
            .cafcassServedOptions(YesOrNo.Yes)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveToRespondentOptions(YesOrNo.Yes)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                                                       .emailInformation(EmailInformation.builder().emailName("").build())
                                                       .build())))
            .build();

        OrderDetails orderDetails = OrderDetails.builder().typeOfOrder("kkkkk").dateCreated(LocalDateTime.now()).build();
        Element<OrderDetails> orders1 = element(orderDetails);
        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                          .builder()
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .orderDocument(Document
                                                                                                             .builder()
                                                                                                             .build())
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .dateOrderMade(LocalDate.now())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));
    }

    @Test
    public void testServeOrderCaCafcassServedOptionsYesAndOtherPartiesOnlyC47a() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        ManageOrders manageOrders = ManageOrders.builder()
            .otherParties(dynamicMultiSelectList)
            .cafcassServedOptions(YesOrNo.Yes)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                                                       .emailInformation(EmailInformation.builder().emailName("").build())
                                                       .build())))
            .serveToRespondentOptions(YesOrNo.Yes)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .build();

        OrderDetails orderDetails = OrderDetails.builder().typeOfOrder("kkkkk").dateCreated(LocalDateTime.now()).build();
        Element<OrderDetails> orders1 = element(orderDetails);
        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                          .builder()
                                                                                          .orderType("null")
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .orderDocument(Document
                                                                                                             .builder()
                                                                                                             .build())
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .dateOrderMade(LocalDate.now())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));
    }

    @Test
    public void testServeOrderDA() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                          .builder()
                                                                                          .orderDocument(Document
                                                                                                             .builder()
                                                                                                             .build())
                                                                                          .dateCreated(now)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);
        List<DynamicMultiselectListElement> elements = new ArrayList<>();
        DynamicMultiselectListElement element = DynamicMultiselectListElement.builder()
            .code("1234")
            .label("test label").build();
        elements.add(element);
        ManageOrders manageOrders = ManageOrders.builder()
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .servingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .recipientsOptions(DynamicMultiSelectList.builder()
                                   .listItems(elements)
                                   .build())
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.post)
                                                       .postalInformation(PostalInformation.builder().postalName("").build())
                                                       .build())))
            .childOption(DynamicMultiSelectList.builder()
                             .listItems(elements)
                             .build())
            .otherParties(DynamicMultiSelectList.builder()
                              .listItems(elements)
                              .build())
            .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
            .build();

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .dateOfBirth(LocalDate.now())
            .address(Address.builder().build())
            .solicitorReference("123")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("test")
            .representativeLastName("test")
            .build();

        PartyDetails respPartyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .dateOfBirth(LocalDate.now())
            .address(Address.builder().build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantsFL401(partyDetails)
            .respondentsFL401(respPartyDetails)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .dateOrderMade(LocalDate.now())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));
    }

    @Test
    public void testgetOrderTemplateAndFileForSdo() {
        Map<String, String> fieldsMap = manageOrderService.getOrderTemplateAndFile(CreateSelectOrderOptionsEnum.standardDirectionsOrder);
        assertTrue(fieldsMap.containsKey(PrlAppsConstants.FILE_NAME));
    }

    @Test
    public void testgetOrderTemplateAndFileForNoticeOfProceedingsParties() {
        Map<String, String> fieldsMap = manageOrderService.getOrderTemplateAndFile(CreateSelectOrderOptionsEnum.noticeOfProceedingsParties);
        assertTrue(fieldsMap.containsKey(PrlAppsConstants.FILE_NAME));
    }

    @Test
    public void testgetOrderTemplateAndFileForNoticeOfProceedingsNonParties() {
        Map<String, String> fieldsMap = manageOrderService.getOrderTemplateAndFile(CreateSelectOrderOptionsEnum.noticeOfProceedingsNonParties);
        assertTrue(fieldsMap.containsKey(PrlAppsConstants.FILE_NAME));
    }

    @Test
    public void testgetOrderTemplateAndFileForOccupation() {
        Map<String, String> fieldsMap = manageOrderService.getOrderTemplateAndFile(CreateSelectOrderOptionsEnum.occupation);
        assertTrue(fieldsMap.containsKey(PrlAppsConstants.FILE_NAME));
    }

    @Test
    public void testgetOrderTemplateAndFileForDoi() {
        Map<String, String> fieldsMap = manageOrderService.getOrderTemplateAndFile(CreateSelectOrderOptionsEnum.directionOnIssue);
        assertTrue(fieldsMap.containsKey(PrlAppsConstants.FILE_NAME));
    }

    @Test
    public void testpopulateCustomOrderFieldsForUploadAnOrder() {
        ManageOrders expectedDetails = ManageOrders.builder()
            .manageOrdersFl402CaseNo("12345674")
            .manageOrdersFl402CourtName("Court name")
            .manageOrdersFl402Applicant("app testLast")
            .manageOrdersFl402ApplicantRef("test test1")
            .orderDirections("order dir")
            .furtherDirectionsIfRequired("fur dir")
            .recitalsOrPreamble("reci")
            .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.justicesClerk)
            .build();
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .dateOfBirth(LocalDate.now())
            .address(Address.builder().build())
            .build();
        CaseData caseData = CaseData.builder()
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .manageOrders(expectedDetails)
            .manageOrdersOptions(ManageOrdersOptionsEnum.uploadAnOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blank)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82).build();
        assertNotNull(manageOrderService.populateCustomOrderFields(caseData, CreateSelectOrderOptionsEnum.blank));
    }

    @Test
    public void testGetAllRecipientsForC100() {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .dateOfBirth(LocalDate.now())
            .address(Address.builder().build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .respondents(List.of(partyDetailsElement))
            .build();
        assertEquals("test (Respondent's Solicitor)\n", manageOrderService.getAllRecipients(caseData));
    }

    @Test
    public void testGetAllRecipientsForFL401() {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .dateOfBirth(LocalDate.now())
            .address(Address.builder().build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("test")
            .representativeLastName("test")
            .build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .orderRecipients(List.of(OrderRecipientsEnum.respondentOrRespondentSolicitor))
            .respondentsFL401(partyDetails)
            .build();
        assertEquals("test test\n", manageOrderService.getAllRecipients(caseData));
    }

    @Test
    public void testGetAllRecipientsForC100ApplicantOrApplicantSolicitor() {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .dateOfBirth(LocalDate.now())
            .address(Address.builder().build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(partyDetails);
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            .applicants(List.of(partyDetailsElement))
            .build();
        assertEquals("test (Applicant's Solicitor)\n", manageOrderService.getAllRecipients(caseData));
    }

    @Test
    public void testGetAllRecipientsForFL401ApplicantOrApplicantSolicitor() {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .dateOfBirth(LocalDate.now())
            .address(Address.builder().build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("test")
            .representativeLastName("test")
            .build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .orderRecipients(List.of(OrderRecipientsEnum.applicantOrApplicantSolicitor))
            .applicantsFL401(partyDetails)
            .build();
        assertEquals("test\n", manageOrderService.getAllRecipients(caseData));
    }

    @Test
    public void testGetSelectedOrderInfoForUploadDomesticAbuseOrders() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .domesticAbuseOrders(DomesticAbuseOrdersEnum.blankOrder)
            .build();
        assertEquals("Blank order (FL404B)", manageOrderService.getSelectedOrderInfoForUpload(caseData));
    }

    @Test
    public void testGetSelectedOrderInfoForUploadFcOrders() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .fcOrders(FcOrdersEnum.summonToAppearToCourt)
            .build();
        assertEquals("Summons to appear at court (FC601)", manageOrderService.getSelectedOrderInfoForUpload(caseData));
    }

    @Test
    public void testGetSelectedOrderInfoForUploadOtherOrdersOption() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .otherOrdersOption(OtherOrdersOptionEnum.other)
            .nameOfOrder("test")
            .build();
        assertEquals("Other : test", manageOrderService.getSelectedOrderInfoForUpload(caseData));
    }

    @Test
    public void testGetSelectedOrderInForUpload() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .build();
        assertEquals("", manageOrderService.getSelectedOrderInfoForUpload(caseData));
    }

    @Test
    public void testGetSelectedOrderIdForUploadDomesticAbuseOrders() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .domesticAbuseOrders(DomesticAbuseOrdersEnum.blankOrder)
            .build();
        assertEquals("blankOrder", manageOrderService.getSelectedOrderIdForUpload(caseData));
    }

    @Test
    public void testGetSelectedOrderIdForUploadFcOrders() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .fcOrders(FcOrdersEnum.summonToAppearToCourt)
            .build();
        assertEquals("summonToAppearToCourt", manageOrderService.getSelectedOrderIdForUpload(caseData));
    }

    @Test
    public void testGetSelectedOrderIdForUploadOtherOrdersOption() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .otherOrdersOption(OtherOrdersOptionEnum.other)
            .nameOfOrder("test")
            .build();
        assertEquals("other : test", manageOrderService.getSelectedOrderIdForUpload(caseData));
    }

    @Test
    public void testGetSelectedOrderIdForUpload() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .build();
        assertEquals("", manageOrderService.getSelectedOrderIdForUpload(caseData));
    }

    @Test
    public void testGetSelectedOrderIdForUploadforChildArrOrder() {
        CaseData caseData = CaseData.builder()
            .childArrangementOrders(ChildArrangementOrdersEnum.blankOrderOrDirections)
            .caseTypeOfApplication("FL401")
            .build();
        assertEquals("blankOrderOrDirections", manageOrderService.getSelectedOrderIdForUpload(caseData));
    }

    @Test
    public void testPopulateDraftOrderForJudge() throws Exception {
        when(userService.getUserDetails(Mockito.anyString()))
            .thenReturn(UserDetails.builder().roles(List.of(Roles.JUDGE.getValue())).build());

        when(dateTime.now()).thenReturn(LocalDateTime.now());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .doesOrderClosesCase(YesOrNo.Yes)
            .manageOrdersOptions(ManageOrdersOptionsEnum.uploadAnOrder)
            .fcOrders(FcOrdersEnum.warrantOfCommittal)
            .judgeOrMagistratesLastName("test")
            .dateOrderMade(LocalDate.now())
            .manageOrders(ManageOrders.builder()
                              .recitalsOrPreamble("test")
                              .isCaseWithdrawn(YesOrNo.Yes)
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
                              .orderDirections("test")
                              .furtherDirectionsIfRequired("test")
                              .childOption(dynamicMultiSelectList)
                              .build())
            .build();
        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData).get(
            "draftOrderCollection"));
    }


    @Test
    public void testOrderStatusCreatedByAdmin() {
        String status = manageOrderService.getOrderStatus("createAnOrder", "COURT_ADMIN", null, null);
        assertEquals(OrderStatusEnum.createdByCA.getDisplayedValue(), status);
    }

    @Test
    public void testOrderStatusCreatedByJudge() {
        String status = manageOrderService.getOrderStatus("createAnOrder", "JUDGE", null, " ");
        assertEquals(OrderStatusEnum.createdByJudge.getDisplayedValue(), status);
    }

    @Test
    public void testOrderStatusCreatedBySolicitor() {
        String status = manageOrderService.getOrderStatus("draftAnOrder", "SOLICITOR", null, "");
        assertEquals(OrderStatusEnum.draftedByLR.getDisplayedValue(), status);
    }

    @Test
    public void testOrderStatusReviewByAdminAlreadyReviewedByJudge() {
        String status = manageOrderService.getOrderStatus(
            "createAnOrder",
            "COURT_ADMIN",
            "adminEditAndApproveAnOrder",
            "Reviewed by Judge"
        );
        assertEquals(OrderStatusEnum.reviewedByJudge.getDisplayedValue(), status);
    }

    @Test
    public void testOrderStatusReviewByAdminNotReviewedByJudge() {
        String status = manageOrderService.getOrderStatus(
            "createAnOrder",
            "COURT_ADMIN",
            "adminEditAndApproveAnOrder",
            "Created by Admin"
        );
        assertEquals(OrderStatusEnum.reviewedByCA.getDisplayedValue(), status);
    }

    @Test
    public void testOrderStatusReviewByJudge() {
        String status = manageOrderService.getOrderStatus(
            "createAnOrder",
            "JUDGE",
            "editAndApproveAnOrder",
            "Created by Admin"
        );
        assertEquals(OrderStatusEnum.reviewedByJudge.getDisplayedValue(), status);
    }

    @Test
    public void testOrderStatusReviewByManager() {
        String status = manageOrderService.getOrderStatus(
            "createAnOrder",
            "COURT_ADMIN",
            "editAndApproveAnOrder",
            "Created by Admin"
        );
        assertEquals(OrderStatusEnum.reviewedByManager.getDisplayedValue(), status);
    }

    @Test
    public void testPopulateFinalUploadOrder() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.COURT_ADMIN.getValue())).build());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .doesOrderClosesCase(YesOrNo.Yes)
            .manageOrdersOptions(ManageOrdersOptionsEnum.uploadAnOrder)
            .manageOrders(ManageOrders.builder().build())
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .build();
        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));

    }

    @Test
    public void testPopulateFinalUploadOrderWithDateOrderMade() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.COURT_ADMIN.getValue())).build());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .dateOrderMade(LocalDate.now())
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .doesOrderClosesCase(YesOrNo.Yes)
            .manageOrdersOptions(ManageOrdersOptionsEnum.uploadAnOrder)
            .manageOrders(ManageOrders.builder().build())
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .build();
        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));

    }

    @Test
    public void testGetLoggedInUserTypeSolicitor() {
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.SOLICITOR.getValue())).build());
        assertEquals(UserRoles.SOLICITOR.name(), manageOrderService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeCitizen() {
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.CITIZEN.getValue())).build());
        assertEquals(UserRoles.CITIZEN.name(), manageOrderService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeSystemUpdate() {
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.SYSTEM_UPDATE.getValue())).build());
        assertEquals(UserRoles.SYSTEM_UPDATE.name(), manageOrderService.getLoggedInUserType("test"));
    }


    @Test
    public void testGetLoggedInUserTypeCourtAdminFromAmRoleAssignment() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(
            "hearing-centre-admin");
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .id("123")
                                                                     .roles(List.of(Roles.LEGAL_ADVISER.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(
            roleAssignmentServiceResponse);
        assertEquals(UserRoles.COURT_ADMIN.name(), manageOrderService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeSolicitorFromIdam() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(
            "caseworker-privatelaw-solicitor");
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .id("123")
                                                                     .roles(List.of(Roles.SOLICITOR.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(
            roleAssignmentServiceResponse);
        assertEquals(UserRoles.SOLICITOR.name(), manageOrderService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeJudgeFromAmRoleAssignment() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse("allocated-magistrate");
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
            .id("123")
                                                                     .roles(List.of(Roles.LEGAL_ADVISER.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(roleAssignmentServiceResponse);
        assertEquals(UserRoles.JUDGE.name(), manageOrderService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeForSystemUpdateFromIdam() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse(
            "caseworker-privatelaw-systemupdate");
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .id("123")
                                                                     .roles(List.of(Roles.SYSTEM_UPDATE.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(
            roleAssignmentServiceResponse);
        assertEquals(UserRoles.SYSTEM_UPDATE.name(), manageOrderService.getLoggedInUserType("test"));
    }

    @Test
    public void testGetLoggedInUserTypeForCitizenFromIdam() {
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = setAndGetRoleAssignmentServiceResponse("citizen");
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .id("123")
                                                                     .roles(List.of(Roles.CITIZEN.getValue())).build());
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(true);

        when(roleAssignmentApi.getRoleAssignments("test", authTokenGenerator.generate(), null, "123")).thenReturn(
            roleAssignmentServiceResponse);
        assertEquals(UserRoles.CITIZEN.name(), manageOrderService.getLoggedInUserType("test"));
    }

    private RoleAssignmentServiceResponse setAndGetRoleAssignmentServiceResponse(String roleName) {
        List<RoleAssignmentResponse> listOfRoleAssignmentResponses = new ArrayList<>();
        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName(roleName);
        listOfRoleAssignmentResponses.add(roleAssignmentResponse);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = new RoleAssignmentServiceResponse();
        roleAssignmentServiceResponse.setRoleAssignmentResponse(listOfRoleAssignmentResponses);
        return roleAssignmentServiceResponse;
    }


    @Test
    public void testServeOrderCaWithRecipients() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code("test")
            .label("test")
            .build();
        DynamicMultiSelectList dummyDynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(
                dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .serveOrderDynamicList(dummyDynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveToRespondentOptions(YesOrNo.No)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .recipientsOptions(dynamicMultiSelectList)
            .otherParties(dynamicMultiSelectList)
            .build();

        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                          .builder()
                                                                                          .orderDocument(Document
                                                                                                             .builder()
                                                                                                             .build())
                                                                                          .dateCreated(now)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .dateOrderMade(LocalDate.now())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();


        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));

    }

    @Test
    public void testServeOrderCaWithRecipientsOptionsOnlyC47a() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code("test")
            .label("test")
            .build();
        DynamicMultiSelectList dummyDynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(
                dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveToRespondentOptions(YesOrNo.No)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                                                       .emailInformation(EmailInformation.builder().emailAddress("").build())
                                                       .build())))
            .recipientsOptions(dummyDynamicMultiSelectList)
            .otherParties(dummyDynamicMultiSelectList)
            .emailInformationCA(List.of(Element.<EmailInformation>builder()
                                            .value(EmailInformation.builder().emailAddress("test").build()).build()))
            .postalInformationCA(List.of(Element.<PostalInformation>builder()
                                             .value(PostalInformation.builder().postalAddress(
                                                 Address.builder().postCode("NE65LA").build()).build()).build()))
            .isTheOrderAboutAllChildren(YesOrNo.No)
            .childOption(dummyDynamicMultiSelectList)
            .build();

        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                          .builder()
                                                                                          .orderDocument(Document
                                                                                                             .builder()
                                                                                                             .build())
                                                                                          .dateCreated(now)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .dateOrderMade(LocalDate.now())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));
    }

    @Test
    public void testCheckOnlyC47aOrderSelectedToServeForC47A() {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code("test")
            .label("testC47A")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();
        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveToRespondentOptions(YesOrNo.No)
            .serveOrgDetailsList(List.of(element(ServeOrgDetails.builder().serveByPostOrEmail(DeliveryByEnum.email)
                                                       .emailInformation(EmailInformation.builder().emailName("").build())
                                                       .build())))
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .build();
        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid)
            .value(OrderDetails.builder()
                              .orderDocument(Document.builder().build())
                              .dateCreated(now)
                              .orderTypeId(TEST_UUID)
                              .otherDetails(OtherOrderDetails.builder().build())
                              .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .applicantsFL401(PartyDetails.builder().doTheyHaveLegalRepresentation(YesNoDontKnow.yes).build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        Map<String, Object> stringObjectMap = manageOrderService.serveOrderMidEvent(callbackRequest);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }

    @Test
    public void testCheckOnlyC47aOrderSelectedToServeForC21() {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code("test")
            .label("testC21")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveToRespondentOptions(YesOrNo.No)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .build();

        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                          .builder()
                                                                                          .orderDocument(Document
                                                                                                             .builder()
                                                                                                             .build())
                                                                                          .dateCreated(now)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .applicants(List.of(element(PartyDetails.builder().solicitorEmail("test").doTheyHaveLegalRepresentation(YesNoDontKnow.no).build())))
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        String courtEmail = "test1@test.com";
        when(welshCourtEmail.populateCafcassCymruEmailInManageOrders(Mockito.any())).thenReturn(courtEmail);

        Map<String, Object> stringObjectMap = manageOrderService.serveOrderMidEvent(callbackRequest);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }

    @Test
    public void testPopulatePreviewOrder() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code("test")
            .label("testC21")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();

        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                          .builder()
                                                                                          .orderDocument(Document
                                                                                                             .builder()
                                                                                                             .build())
                                                                                          .dateCreated(now)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("courtName", "test");


        when(hearingService.getHearings(Mockito.anyString(),Mockito.anyString())).thenReturn(Hearings.hearingsWith().build());
        when(hearingDataService.populateHearingDynamicLists(Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any()))
            .thenReturn(HearingDataPrePopulatedDynamicLists.builder().build());
        when(hearingDataService.getHearingDataForOtherOrders(Mockito.any(),Mockito.any(),Mockito.any()))
            .thenReturn(List.of(Element.<HearingData>builder().build()));
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveToRespondentOptions(YesOrNo.No)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .build();

        ReflectionTestUtils.setField(manageOrderService, "c21DraftWelshTemplate", "c21DraftWelshTemplate");
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails
            = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().data(stringObjectMap).build();

        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetailsBefore
            = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().data(stringObjectMap).build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .dateOrderMade(LocalDate.now())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .appointedGuardianName(null)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .build();
        Map<String, Object> result = manageOrderService.populatePreviewOrder("test", callbackRequest, caseData);
        Assert.assertTrue(!result.isEmpty());
    }

    @Test
    public void testPopulateFinalUploadOrderFromCaseDataWithMultipleOrdersForWelsh() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .caseTypeOfApplication("FL401")
            .applicantsFL401(PartyDetails.builder().firstName("firstname")
                                 .lastName("lastname")
                                 .representativeFirstName("firstname")
                                 .representativeLastName("lastname")
                                 .build())
            .respondentsFL401(PartyDetails.builder().firstName("firstname")
                                  .lastName("lastname")
                                  .representativeFirstName("firstname")
                                  .representativeLastName("lastname")
                                  .build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(new ArrayList<>())
            .dateOrderMade(LocalDate.now())
            .selectTypeOfOrder(SelectTypeOfOrderEnum.interim)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .manageOrders(manageOrders.toBuilder()
                              .amendOrderSelectCheckOptions(AmendOrderCheckEnum.noCheck)
                              .build())
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.No).build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .build();

        ReflectionTestUtils.setField(manageOrderService, "c21WelshTemplate", "c21-WEL-template");

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));
    }

    @Test
    public void testHearingsDropdownWhenNoHearings() {
        //when
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Test")
            .manageOrders(ManageOrders.builder().hearingsType(dynamicList).build())
            .build();

        //mocks
        when(hearingService.getHearings(authToken, "123")).thenReturn(Hearings.hearingsWith().build());

        //invoke
        DynamicList dynamicList1 = manageOrderService.populateHearingsDropdown(authToken, caseData);

        //asserts
        assertNotNull(caseData.getManageOrders().getHearingsType());
        assertNotNull(dynamicList1.getListItems());
    }

    @Test
    public void testHearingsDropdownWhenNoCompletedHearings() {
        //when
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Test")
            .manageOrders(ManageOrders.builder().hearingsType(dynamicList).build())
            .build();
        CaseHearing caseHearing = CaseHearing.caseHearingWith().hmcStatus("CANCELLED").hearingID(123456L).build();
        Hearings hearings = Hearings.hearingsWith()
            .caseRef("123")
            .hmctsServiceCode("ABA5")
            .caseHearings(Collections.singletonList(caseHearing))
            .build();

        //mocks
        when(hearingService.getHearings(authToken, "123")).thenReturn(hearings);

        //invoke
        DynamicList dynamicList1 = manageOrderService.populateHearingsDropdown(authToken, caseData);

        //asserts
        assertNotNull(caseData.getManageOrders().getHearingsType());
        assertNotNull(dynamicList1.getListItems());
    }

    @Test
    public void testHearingsDropdownWhenNoHearingDate() {
        //when
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Test")
            .manageOrders(ManageOrders.builder().hearingsType(dynamicList).build())
            .build();
        CaseHearing caseHearing = CaseHearing.caseHearingWith().hmcStatus(PrlAppsConstants.HMC_STATUS_COMPLETED)
            .hearingID(123456L).hearingDaySchedule(null).build();
        Hearings hearings = Hearings.hearingsWith()
            .caseRef("123")
            .hmctsServiceCode("ABA5")
            .caseHearings(Collections.singletonList(caseHearing))
            .build();

        //mocks
        when(hearingService.getHearings(authToken, "123")).thenReturn(hearings);

        //invoke
        DynamicList dynamicList1 = manageOrderService.populateHearingsDropdown(authToken, caseData);

        //asserts
        assertNotNull(caseData.getManageOrders().getHearingsType());
        assertNotNull(dynamicList1.getListItems());
    }

    @Test
    public void testHearingsDropdownWhenCompletedHearingsAvailable() {
        //when
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Test")
            .manageOrders(ManageOrders.builder().hearingsType(dynamicList).build())
            .build();
        CaseHearing caseHearing = CaseHearing.caseHearingWith()
            .hmcStatus(PrlAppsConstants.HMC_STATUS_COMPLETED)
            .hearingID(123456L)
            .hearingDaySchedule(Arrays.asList(
                HearingDaySchedule.hearingDayScheduleWith().hearingStartDateTime(LocalDateTime.now().minusDays(30)).build(),
                HearingDaySchedule.hearingDayScheduleWith().hearingStartDateTime(LocalDateTime.now().minusDays(15)).build()
            ))
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseRef("123")
            .hmctsServiceCode("ABA5")
            .caseHearings(Collections.singletonList(caseHearing))
            .build();

        //mocks
        when(hearingService.getHearings(authToken, "123")).thenReturn(hearings);

        //invoke
        DynamicList dynamicList1 = manageOrderService.populateHearingsDropdown(authToken, caseData);

        //asserts
        assertNotNull(caseData.getManageOrders().getHearingsType());
    }

    @Test
    public void testHearingsDropdownWhenMultipleHearingsAvailable() {
        //when
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Test")
            .manageOrders(ManageOrders.builder()
                              .hearingsType(dynamicList)
                              .build())
            .build();
        CaseHearing caseHearing1 = CaseHearing.caseHearingWith()
            .hmcStatus(PrlAppsConstants.HMC_STATUS_COMPLETED)
            .hearingID(12345L)
            .hearingDaySchedule(Arrays.asList(
                HearingDaySchedule.hearingDayScheduleWith().hearingStartDateTime(LocalDateTime.now().minusDays(30)).build(),
                HearingDaySchedule.hearingDayScheduleWith().hearingStartDateTime(LocalDateTime.now().minusDays(15)).build()
            ))
            .build();
        CaseHearing caseHearing2 = CaseHearing.caseHearingWith()
            .hmcStatus(PrlAppsConstants.HMC_STATUS_COMPLETED)
            .hearingID(67890L)
            .hearingDaySchedule(Collections.singletonList(
                HearingDaySchedule.hearingDayScheduleWith().hearingStartDateTime(LocalDateTime.now().minusDays(5)).build()
            ))
            .build();
        CaseHearing caseHearing3 = CaseHearing.caseHearingWith()
            .hmcStatus(PrlAppsConstants.HMC_STATUS_COMPLETED)
            .hearingID(98765L)
            .hearingDaySchedule(Collections.singletonList(
                HearingDaySchedule.hearingDayScheduleWith().hearingStartDateTime(null).build()))
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseRef("123")
            .hmctsServiceCode("ABA5")
            .caseHearings(Arrays.asList(caseHearing1, caseHearing2, caseHearing3))
            .build();

        //mocks
        when(hearingService.getHearings(authToken, "123")).thenReturn(hearings);

        //invoke
        DynamicList dynamicList1 = manageOrderService.populateHearingsDropdown(authToken, caseData);

        //asserts
        assertNotNull(caseData.getManageOrders().getHearingsType());
        assertNotNull(dynamicList1.getListItems());
    }


    @Test
    public void testResetChildOptions() {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code("test")
            .label("testC47A")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveToRespondentOptions(YesOrNo.No)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .build();

        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                          .builder()
                                                                                          .orderDocument(Document
                                                                                                             .builder()
                                                                                                             .build())
                                                                                          .dateCreated(now)
                                                                                          .isTheOrderAboutChildren(YesOrNo.Yes)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        manageOrderService.resetChildOptions(callbackRequest);
        Assert.assertEquals(null,callbackRequest.getCaseDetails().getData().get(CHILD_OPTION));
    }

    @Test
    public void testPopulateJudgeNameForFinalDoc() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<OrderRecipientsEnum> recipientList = new ArrayList<>();
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        recipientList.add(OrderRecipientsEnum.applicantOrApplicantSolicitor);
        recipientList.add(OrderRecipientsEnum.respondentOrRespondentSolicitor);

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        ReflectionTestUtils.setField(manageOrderService, "c21Template", "c21-template");
        manageOrders = manageOrders.toBuilder()
            .amendOrderSelectCheckOptions(AmendOrderCheckEnum.noCheck).build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(StandardDirectionOrder.builder().build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            .dateOrderMade(LocalDate.now())
            .orderRecipients(recipientList)
            .applicants(partyDetails)
            .respondents(partyDetails)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .doesOrderClosesCase(YesOrNo.Yes)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.No).build())
            .manageOrders(manageOrders)
            .build();

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));

    }

    @Test
    public void testGetJudgeFullName() {
        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoAllocateOrReserveJudgeName(JudicialUser.builder().idamId("1234").personalCode("ABC").build())
            .sdoLocalAuthorityList(List.of(SdoLocalAuthorityEnum.localAuthorityLetter))
            .sdoFurtherList(List.of(SdoFurtherInstructionsEnum.newDirection))
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(standardDirectionOrder)
            .build();
        List<JudicialUsersApiResponse> judicialUsersApiResponses = new ArrayList<>();
        JudicialUsersApiResponse judicialUsersApiResponse = JudicialUsersApiResponse.builder()
            .fullName("Test")
            .personalCode("test")
            .build();
        judicialUsersApiResponses.add(judicialUsersApiResponse);
        JudicialUsersApiRequest judicialUsersApiRequest = JudicialUsersApiRequest.builder()
            .personalCode(new String[]{"test"}).build();
        when(refDataUserService.getAllJudicialUserDetails(judicialUsersApiRequest)).thenReturn(judicialUsersApiResponses);
        String name = manageOrderService.getJudgeFullName(JudicialUser.builder().idamId("1234").personalCode("test").build());
        assertEquals("Test", name);
    }


    @Test
    public void testResetChildOptionsWithIsTheOderAboutAllChildren() {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code("test")
            .label("testC47A")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .isTheOrderAboutAllChildren(YesOrNo.Yes)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveToRespondentOptions(YesOrNo.No)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .build();

        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                          .builder()
                                                                                          .orderDocument(Document
                                                                                                             .builder()
                                                                                                             .build())
                                                                                          .dateCreated(now)
                                                                                          .isTheOrderAboutChildren(YesOrNo.Yes)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        manageOrderService.resetChildOptions(callbackRequest);
        Assert.assertNotNull(null,callbackRequest.getCaseDetails().getData().get(CHILD_OPTION));
    }

    @Test
    public void testSetChildOptionsIfOrderAboutAllChildrenYes() {

        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code("test")
            .label("testC21")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();

        ManageOrders manageOrders = ManageOrders.builder()
            .isTheOrderAboutAllChildren(YesOrNo.Yes)
            .cafcassCymruServedOptions(YesOrNo.No)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveToRespondentOptions(YesOrNo.No)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .build();

        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                          .builder()
                                                                                          .orderDocument(Document
                                                                                                             .builder()
                                                                                                             .build())
                                                                                          .dateCreated(now)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        DynamicMultiselectListElement dynamicMultiselectListElementUpdated = DynamicMultiselectListElement.builder()
            .code("123")
            .label("John (Child 1)")
            .build();
        when(dynamicMultiSelectListService.getChildrenMultiSelectList(caseData)).thenReturn(List.of(dynamicMultiselectListElementUpdated));

        CaseData caseData1 = manageOrderService.setChildOptionsIfOrderAboutAllChildrenYes(caseData);
        Assert.assertEquals("John (Child 1)",caseData1.getManageOrders().getChildOption().getListItems().get(0).getLabel());
    }

    @Test
    public void testSetMarkedToServeEmailNotificationWithOrdersNeedToBeServedYes() {

        ManageOrders manageOrders = ManageOrders.builder()
            .ordersNeedToBeServed(YesOrNo.Yes)
            .serveToRespondentOptions(YesOrNo.No)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageOrderService.setMarkedToServeEmailNotification(caseData,caseDataUpdated);
        Assert.assertEquals(YesOrNo.Yes,caseDataUpdated.get("markedToServeEmailNotification"));

    }

    @Test
    public void testSetMarkedToServeEmailNotificationWithOrdersNeedToBeServedNo() {

        ManageOrders manageOrders = ManageOrders.builder()
            .ordersNeedToBeServed(YesOrNo.No)
            .serveToRespondentOptions(YesOrNo.No)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageOrderService.setMarkedToServeEmailNotification(caseData,caseDataUpdated);
        Assert.assertEquals(YesOrNo.Yes,caseDataUpdated.get("markedToServeEmailNotification"));

    }

    @Test
    public void testCleanUpSelectedManageOrderOptions() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("manageOrdersOptions","manageOrdersOptions");
        manageOrderService.cleanUpSelectedManageOrderOptions(caseDataUpdated);
        Assert.assertNull(caseDataUpdated.get("manageOrdersOptions"));

    }

    @Test
    public void testServeFinalOrderC43() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();


        List<DynamicMultiselectListElement> elements = new ArrayList<>();
        DynamicMultiselectListElement element = DynamicMultiselectListElement.builder()
            .code("1234")
            .label("test label").build();
        elements.add(element);
        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .childArrangementsOrdersToIssue(List.of(childArrangementsOrder,prohibitedStepsOrder))
            .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
            .amendOrderSelectCheckOptions(AmendOrderCheckEnum.noCheck)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .recipientsOptions(DynamicMultiSelectList.builder()
                                   .listItems(elements)
                                   .build())
            .childOption(DynamicMultiSelectList.builder()
                             .listItems(elements)
                             .build())
            .otherParties(DynamicMultiSelectList.builder()
                              .listItems(elements)
                              .build())
            .serveToRespondentOptions(YesOrNo.Yes)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .build();


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            .dateOrderMade(LocalDate.now())
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .manageOrders(manageOrders)
            .build();


        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));

    }

    @Test
    public void testSaveAdditionalOrderDocuments() {
        //Given
        Document document1 = Document.builder().documentFileName("abc.pdf").build();
        Document document2 = Document.builder().documentFileName("xyz.pdf").build();
        manageOrders = manageOrders.toBuilder()
            .serveOrderAdditionalDocuments(List.of(element(document1), element(document2)))
            .serveOrderDynamicList(DynamicMultiSelectList.builder()
                                       .value(List.of(
                                           DynamicMultiselectListElement.builder()
                                               .code("123")
                                               .label("test order")
                                               .build()))
                                       .build())
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();

        when(userService.getUserDetails(anyString())).thenReturn(
            UserDetails.builder().forename("testFN").surname("testLN").build());
        when(dateTime.now()).thenReturn(LocalDateTime.now());

        //When
        manageOrderService.saveAdditionalOrderDocuments(authToken, caseData, caseDataUpdated);

        //Then
        assertNotNull(caseDataUpdated.get("additionalOrderDocuments"));
        List<Element<AdditionalOrderDocument>> additionalOrderDocuments =
            (List<Element<AdditionalOrderDocument>>) caseDataUpdated.get("additionalOrderDocuments");
        assertEquals(2, additionalOrderDocuments.get(0).getValue().getAdditionalDocuments().size());
    }

    @Test
    public void testSkipSaveAdditionalOrderDocumentsIfEmpty() {
        //Given
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();

        when(userService.getUserDetails(anyString())).thenReturn(
            UserDetails.builder().forename("testFN").surname("testLN").build());

        //When
        manageOrderService.saveAdditionalOrderDocuments(authToken, caseData, caseDataUpdated);

        //Then
        assertNull(caseDataUpdated.get("additionalOrderDocuments"));
    }

    @Test
    public void testGetGeneratedDocument() {
        GeneratedDocumentInfo generatedDocumentInfo1 = GeneratedDocumentInfo.builder().build();
        assertNotNull(manageOrderService.getGeneratedDocument(
            generatedDocumentInfo1, true, new HashMap<>())
        );
    }

    @Test
    public void testCleanUpServeOrderOptions() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("serveOrderAdditionalDocuments","serveOrderAdditionalDocuments");
        manageOrderService.cleanUpServeOrderOptions(caseDataUpdated);
        Assert.assertNull(caseDataUpdated.get("serveOrderAdditionalDocuments"));
    }

    @Test
    public void testSetFieldsForWaTaskForJudgeCreateOrder() {
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .doesOrderClosesCase(YesOrNo.Yes)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .manageOrders(ManageOrders.builder().build())
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .build();
        Map<String, Object> response = manageOrderService.setFieldsForWaTask("test token", caseData, "eventId");
        assertNotNull(response);
        assertTrue(response.containsKey(WA_ORDER_NAME_JUDGE_CREATED));
        assertNotNull(response.get(WA_ORDER_NAME_JUDGE_CREATED));
    }

    @Test
    public void testSetFieldsForWaTaskForCourtAdminCreateOrder() {
        when(dateTime.now()).thenReturn(LocalDateTime.now());
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.COURT_ADMIN.getValue())).build());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .doesOrderClosesCase(YesOrNo.Yes)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .manageOrders(ManageOrders.builder().build())
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .build();
        Map<String, Object> response = manageOrderService.setFieldsForWaTask("test token", caseData, "eventId");
        assertNotNull(response);
        assertTrue(response.containsKey(WA_ORDER_NAME_ADMIN_CREATED));
        assertNotNull(response.get(WA_ORDER_NAME_ADMIN_CREATED));
    }

    @Test
    public void testSetFieldsForWaTaskForUploadOrder() {
        when(dateTime.now()).thenReturn(LocalDateTime.now());

        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .doesOrderClosesCase(YesOrNo.Yes)
            .manageOrdersOptions(ManageOrdersOptionsEnum.uploadAnOrder)
            .manageOrders(ManageOrders.builder().build())
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .build();
        Map<String, Object> response = manageOrderService.setFieldsForWaTask("test token", caseData, "eventId");
        assertNotNull(response);
        assertTrue(response.containsKey(WA_ORDER_NAME_JUDGE_CREATED));
        assertNotNull(response.get(WA_ORDER_NAME_JUDGE_CREATED));
    }

    @Test
    public void testServeOrderC100() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();


        List<DynamicMultiselectListElement> elements = new ArrayList<>();
        DynamicMultiselectListElement element = DynamicMultiselectListElement.builder()
            .code("1234")
            .label("test label").build();
        elements.add(element);
        List<Element<ServeOrgDetails>> serveOrgDetailsList = new ArrayList<>();
        serveOrgDetailsList.add(element(ServeOrgDetails.builder()
                                            .serveByPostOrEmail(DeliveryByEnum.post)
                                            .postalInformation(PostalInformation.builder()
                                                                   .postalAddress(Address.builder().build()).build())
                                            .build()));

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .childArrangementsOrdersToIssue(List.of(childArrangementsOrder,prohibitedStepsOrder))
            .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrgDetailsList(serveOrgDetailsList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .recipientsOptions(DynamicMultiSelectList.builder()
                                   .listItems(elements)
                                   .build())
            .childOption(DynamicMultiSelectList.builder()
                             .listItems(elements)
                             .build())
            .otherParties(DynamicMultiSelectList.builder()
                              .listItems(elements)
                              .build())
            .serveToRespondentOptions(YesOrNo.Yes)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .deliveryByOptionsCA(DeliveryByEnum.post)
            .emailInformationCA(List.of(Element.<EmailInformation>builder()
                                            .value(EmailInformation.builder().emailAddress("test").build()).build()))
            .postalInformationCA(List.of(Element.<PostalInformation>builder()
                                             .value(PostalInformation.builder().postalAddress(
                                                 Address.builder().postCode("NE65LA").build()).build()).build()))
            .build();

        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                          .builder()
                                                                                          .orderDocument(Document
                                                                                                             .builder()
                                                                                                             .build())
                                                                                          .dateCreated(now)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder().firstName("first").lastName("lastname")
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(partyDetails)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .dateOrderMade(LocalDate.now())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();


        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        assertNotNull(manageOrderService.serveOrder(caseData,orderList));

    }

    @Test
    public void testGetAdditionalRequirementsForHearingReqForOtherOrderDraft() {
        List<Element<HearingData>> ordersHearingDetails = new ArrayList<>();
        HearingData hearingData = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .additionalDetailsForHearingDateOptions("aaaa")
            .build();
        HearingData hearingData1 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedByListingTeam)
            .additionalDetailsForHearingDateOptions("bbbb")
            .build();
        HearingData hearingData2 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .additionalDetailsForHearingDateOptions("cccc")
            .build();
        ordersHearingDetails.add(element(hearingData));
        ordersHearingDetails.add(element(hearingData1));
        ordersHearingDetails.add(element(hearingData2));

        String response = manageOrderService.getAdditionalRequirementsForHearingReq(
            ordersHearingDetails,
            true,
            null,
            CreateSelectOrderOptionsEnum.blankOrderOrDirections,
            C21OrderOptionsEnum.c21other
        );
        assertNotNull(response);
        assertEquals("bbbb, cccc", response);
    }

    @Test
    public void testGetAdditionalRequirementsForHearingReqForOtherOrderFinal() {
        List<Element<HearingData>> ordersHearingDetails = new ArrayList<>();
        HearingData hearingData = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .additionalDetailsForHearingDateOptions("aaaa")
            .build();
        HearingData hearingData1 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedByListingTeam)
            .additionalDetailsForHearingDateOptions("bbbb")
            .build();
        HearingData hearingData2 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .additionalDetailsForHearingDateOptions("cccc")
            .build();
        ordersHearingDetails.add(element(hearingData));
        ordersHearingDetails.add(element(hearingData1));
        ordersHearingDetails.add(element(hearingData2));

        String response = manageOrderService.getAdditionalRequirementsForHearingReq(
            ordersHearingDetails,
            false,
            null,
            CreateSelectOrderOptionsEnum.blankOrderOrDirections,
            C21OrderOptionsEnum.c21other
        );
        assertNotNull(response);
        assertEquals("cccc", response);
    }

    @Test
    public void testGetAdditionalRequirementsForHearingReqForSdoDraft() {
        List<Element<HearingData>> ordersHearingDetails = new ArrayList<>();
        HearingData hearingData = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .additionalDetailsForHearingDateOptions("aaaa")
            .build();
        HearingData hearingData1 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedByListingTeam)
            .additionalDetailsForHearingDateOptions("bbbb")
            .build();
        HearingData hearingData2 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .additionalDetailsForHearingDateOptions("cccc")
            .build();

        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoUrgentHearingDetails(hearingData)
            .sdoFhdraHearingDetails(hearingData1)
            .sdoDraHearingDetails(hearingData2)
            .sdoSettlementHearingDetails(hearingData)
            .sdoPermissionHearingDetails(hearingData)
            .sdoSecondHearingDetails(hearingData)
            .sdoDirectionsForFactFindingHearingDetails(hearingData)
            .build();


        String response = manageOrderService.getAdditionalRequirementsForHearingReq(
            ordersHearingDetails,
            true,
            standardDirectionOrder,
            CreateSelectOrderOptionsEnum.standardDirectionsOrder,
            null
        );
        assertNotNull(response);
        assertEquals("bbbb, cccc", response);
    }

    @Test
    public void testGetAdditionalRequirementsForHearingReqForSdoFinal() {
        List<Element<HearingData>> ordersHearingDetails = new ArrayList<>();
        HearingData hearingData = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .additionalDetailsForHearingDateOptions("aaaa")
            .build();
        HearingData hearingData1 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedByListingTeam)
            .additionalDetailsForHearingDateOptions("bbbb")
            .build();
        HearingData hearingData2 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .additionalDetailsForHearingDateOptions("cccc")
            .build();

        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoUrgentHearingDetails(hearingData)
            .sdoFhdraHearingDetails(hearingData1)
            .sdoDraHearingDetails(hearingData2)
            .sdoSettlementHearingDetails(hearingData)
            .sdoPermissionHearingDetails(hearingData)
            .sdoSecondHearingDetails(hearingData)
            .build();

        String response = manageOrderService.getAdditionalRequirementsForHearingReq(
            ordersHearingDetails,
            false,
            standardDirectionOrder,
            CreateSelectOrderOptionsEnum.standardDirectionsOrder,
            null
        );
        assertNotNull(response);
        assertEquals("cccc", response);
    }

    @Test
    public void testServeOrder() {
        List<DynamicMultiselectListElement> dynamicMultiselectListElements = new ArrayList<>();
        DynamicMultiselectListElement partyDynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code("00000000-0000-0000-0000-000000000000")
            .label("John Doe")
            .build();
        dynamicMultiselectListElements.add(partyDynamicMultiselectListElement);
        DynamicMultiSelectList partyDynamicMultiSelectList = DynamicMultiSelectList.builder()
            .listItems(dynamicMultiselectListElements)
            .value(dynamicMultiselectListElements)
            .build();

        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
            .builder()
            .orderDocument(Document
                .builder()
                .build())
            .dateCreated(now)
            .orderTypeId("00000000-0000-0000-0000-000000000000")
            .otherDetails(
                OtherOrderDetails.builder().build())
            .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .doesOrderClosesCase(YesOrNo.Yes)
            .manageOrdersOptions(ManageOrdersOptionsEnum.uploadAnOrder)
            .manageOrders(ManageOrders.builder().serveOrderDynamicList(partyDynamicMultiSelectList)
                .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other)).build())
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .applicantsFL401(PartyDetails.builder().build())
            .build();

        List<Element<OrderDetails>> listOfOrders = manageOrderService.serveOrder(caseData, orderList);
        assertNotNull(listOfOrders);
    }

    @Test
    public void testServeFL401Order() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();


        List<DynamicMultiselectListElement> elements = new ArrayList<>();
        DynamicMultiselectListElement element = DynamicMultiselectListElement.builder()
            .code("1234")
            .label("test label").build();
        elements.add(element);
        ManageOrders manageOrders = ManageOrders.builder()
            .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
            .cafcassCymruServedOptions(YesOrNo.No)
            .childArrangementsOrdersToIssue(List.of(childArrangementsOrder,prohibitedStepsOrder))
            .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .recipientsOptions(DynamicMultiSelectList.builder()
                                   .listItems(elements)
                                   .build())
            .childOption(DynamicMultiSelectList.builder()
                             .listItems(elements)
                             .build())
            .otherParties(DynamicMultiSelectList.builder()
                              .listItems(elements)
                              .build())
            .serveToRespondentOptions(YesOrNo.Yes)
            .servingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .deliveryByOptionsCA(DeliveryByEnum.post)
            .emailInformationDA(List.of(Element.<EmailInformation>builder()
                                            .value(EmailInformation.builder().emailAddress("test").build()).build()))
            .postalInformationDA(List.of(Element.<PostalInformation>builder()
                                             .value(PostalInformation.builder().postalAddress(
                                                 Address.builder().postCode("NE65LA").build()).build()).build()))
            .build();

        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                          .builder()
                                                                                          .orderDocument(Document
                                                                                                             .builder()
                                                                                                             .build())
                                                                                          .dateCreated(now)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder().firstName("first").lastName("lastname")
            .representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantsFL401(details)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .dateOrderMade(LocalDate.now())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();


        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        assertNotNull(manageOrderService.serveOrder(caseData,orderList));

    }

    @Test
    public void testPopulateServeOrderDetails() {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .familymanCaseNumber("familyman6789")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        Map<String, Object> responseMap = manageOrderService.populateHeader(caseData);
        manageOrderService.populateServeOrderDetails(caseData,responseMap);
        assertNotNull(responseMap);
        assertNotNull(responseMap.get(CASE_TYPE_OF_APPLICATION));
        assertEquals(FL401_CASE_TYPE, responseMap.get(CASE_TYPE_OF_APPLICATION));
    }

    @Test
    public void givenRespondentLipNonPersonalServicePrefPostShouldGiveErrorIfAddressIsNotPresent() {
        List<Element<PartyDetails>> respondents =
            List.of(ElementUtils.element(UUID.fromString("e406bcc3-3c91-45db-9dcc-3a5c14930851"),PartyDetails.builder()
                .contactPreferences(ContactPreferences.post)
                .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                    .build()));
        List<Element<PartyDetails>> otherParties =
            List.of(ElementUtils.element(UUID.fromString("6bb5e9ac-df97-4593-8b22-3969dc0bb4e1"),PartyDetails.builder()
                    .address(Address.builder().addressLine1("test address").build())
                .build()));
        CaseData caseData = getCaseData();
        caseData = caseData.toBuilder().respondents(respondents)
            .othersToNotify(otherParties)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response
            = manageOrderService.validateRespondentLipAndOtherPersonAddress(callbackRequest);
        assertEquals(1,response.getErrors().size());
        assertTrue(response.getErrors().contains(VALIDATION_ADDRESS_ERROR_RESPONDENT));
    }

    @Test
    public void givenRespondentLipNonPersonalServiceContactPrefNotPresentShouldGiveErrorIfAddressIsNotPresent() {
        List<Element<PartyDetails>> respondents =
            List.of(ElementUtils.element(UUID.fromString("e406bcc3-3c91-45db-9dcc-3a5c14930851"),PartyDetails.builder()
                    .email("test@test.com")
                .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                    .build()));
        List<Element<PartyDetails>> otherParties =
            List.of(ElementUtils.element(UUID.fromString("6bb5e9ac-df97-4593-8b22-3969dc0bb4e1"),PartyDetails.builder()
                .address(Address.builder().addressLine1("test address").build())
                .build()));
        CaseData caseData = getCaseData();
        caseData = caseData.toBuilder().respondents(respondents)
            .taskListVersion(TASK_LIST_VERSION_V2)
            .otherPartyInTheCaseRevised(otherParties).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response
            = manageOrderService.validateRespondentLipAndOtherPersonAddress(callbackRequest);
        assertEquals(1,response.getErrors().size());
        assertTrue(response.getErrors().contains(VALIDATION_ADDRESS_ERROR_RESPONDENT));
    }

    @Test
    public void givenRespondentLipNonPersonalServiceEmailAddressNotPresentShouldGiveErrorIfAddressIsNotPresent() {
        List<Element<PartyDetails>> respondents =
            List.of(ElementUtils.element(UUID.fromString("e406bcc3-3c91-45db-9dcc-3a5c14930851"),PartyDetails.builder()
                .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                    .contactPreferences(ContactPreferences.post)
                    .build()));
        List<Element<PartyDetails>> otherParties =
            List.of(ElementUtils.element(UUID.fromString("6bb5e9ac-df97-4593-8b22-3969dc0bb4e1"),PartyDetails.builder()
                .address(Address.builder().addressLine1("test address").build())
                .build()));
        CaseData caseData = getCaseData();
        caseData = caseData.toBuilder().respondents(respondents)
            .taskListVersion(TASK_LIST_VERSION_V2)
            .otherPartyInTheCaseRevised(otherParties).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response
            = manageOrderService.validateRespondentLipAndOtherPersonAddress(callbackRequest);
        assertEquals(1,response.getErrors().size());
        assertTrue(response.getErrors().contains(VALIDATION_ADDRESS_ERROR_RESPONDENT));
    }

    @Test
    public void givenRespondenNonPersonalServiceIfRepresentedShouldNotGiveErrorIfAddressIsNotPresent() {
        List<Element<PartyDetails>> respondents =
            List.of(ElementUtils.element(UUID.fromString("e406bcc3-3c91-45db-9dcc-3a5c14930851"),PartyDetails.builder()
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .contactPreferences(ContactPreferences.post)
                .build()));
        List<Element<PartyDetails>> otherParties =
            List.of(ElementUtils.element(UUID.fromString("6bb5e9ac-df97-4593-8b22-3969dc0bb4e1"),PartyDetails.builder()
                .address(Address.builder().addressLine1("test address").build())
                .build()));
        CaseData caseData = getCaseData();
        caseData = caseData.toBuilder()
            .taskListVersion(TASK_LIST_VERSION_V2)
            .id(123L).respondents(respondents)
            .otherPartyInTheCaseRevised(otherParties).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response
            = manageOrderService.validateRespondentLipAndOtherPersonAddress(callbackRequest);
        assertNotNull(response.getData());
        assertNotNull(response.getData().get("id"));
        assertEquals("123", response.getData().get("id").toString());

    }

    @Test
    public void givenRespondentLipNonPersonalServiceShouldNotGiveErrorIfAddressIsPresent() {
        List<Element<PartyDetails>> respondents =
            List.of(ElementUtils.element(UUID.fromString("e406bcc3-3c91-45db-9dcc-3a5c14930851"),PartyDetails.builder()
                    .email("test@test.com")
                    .address(Address.builder().addressLine1("test address").build())
                .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                    .build()));
        CaseData caseData = getCaseData();
        caseData = caseData.toBuilder()
            .id(123L).respondents(respondents)
            .manageOrders(caseData.getManageOrders().toBuilder()
                              .otherParties(null)
                              .serveToRespondentOptions(YesOrNo.Yes).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response
            = manageOrderService.validateRespondentLipAndOtherPersonAddress(callbackRequest);
        assertNotNull(response.getData());
        assertNotNull(response.getData().get("id"));
        assertEquals("123", response.getData().get("id").toString());
    }

    @Test
    public void givenRespondentLipPersonalServiceShouldNotGiveError() {
        List<Element<PartyDetails>> respondents =
            List.of(ElementUtils.element(UUID.fromString("e406bcc3-3c91-45db-9dcc-3a5c14930851"),PartyDetails.builder()
                .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                .contactPreferences(ContactPreferences.post)
                .build()));
        CaseData caseData = getCaseData();
        caseData = caseData.toBuilder()
            .id(123L).respondents(respondents)
            .taskListVersion(TASK_LIST_VERSION_V2)
            .manageOrders(caseData.getManageOrders().toBuilder()
                              .otherParties(null)
                              .serveToRespondentOptions(YesOrNo.Yes).build()).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response
            = manageOrderService.validateRespondentLipAndOtherPersonAddress(callbackRequest);
        assertNotNull(response.getData());
        assertNotNull(response.getData().get("id"));
        assertEquals("123", response.getData().get("id").toString());
    }

    @Test
    public void givenOtherPartyNonPersonalServiceShouldGiveErrorIfAddressIsNotPresent() {
        List<Element<PartyDetails>> respondents =
            List.of(ElementUtils.element(UUID.fromString("e406bcc3-3c91-45db-9dcc-3a5c14930851"),PartyDetails.builder()
                .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                .contactPreferences(ContactPreferences.post)
                .build()));
        List<Element<PartyDetails>> otherParties =
            List.of(ElementUtils.element(UUID.fromString("6bb5e9ac-df97-4593-8b22-3969dc0bb4e1"),PartyDetails.builder()
                .build()));
        CaseData caseData = getCaseData();
        caseData = caseData.toBuilder().respondents(respondents)
            .taskListVersion(TASK_LIST_VERSION_V2)
            .otherPartyInTheCaseRevised(otherParties)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response
            = manageOrderService.validateRespondentLipAndOtherPersonAddress(callbackRequest);
        assertEquals(2,response.getErrors().size());
        assertTrue(response.getErrors().contains(VALIDATION_ADDRESS_ERROR_RESPONDENT));
        assertTrue(response.getErrors().contains(VALIDATION_ADDRESS_ERROR_OTHER_PARTY));
    }

    @Test
    public void givenOtherPartyPersonalServiceShouldGiveErrorIfAddressIsNotPresent() {
        List<Element<PartyDetails>> respondents =
            List.of(ElementUtils.element(UUID.fromString("e406bcc3-3c91-45db-9dcc-3a5c14930851"), PartyDetails.builder()
                .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                .contactPreferences(ContactPreferences.post)
                .build()));
        List<Element<PartyDetails>> otherParties =
            List.of(ElementUtils.element(UUID.fromString("6bb5e9ac-df97-4593-8b22-3969dc0bb4e1"), PartyDetails.builder()
                .build()));
        CaseData caseData = getCaseData();
        caseData = caseData.toBuilder().respondents(respondents)
            .taskListVersion(TASK_LIST_VERSION_V2)
            .otherPartyInTheCaseRevised(otherParties)
            .manageOrders(caseData.getManageOrders().toBuilder()
                              .serveToRespondentOptions(YesOrNo.Yes).build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response
            = manageOrderService.validateRespondentLipAndOtherPersonAddress(callbackRequest);
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().contains(VALIDATION_ADDRESS_ERROR_OTHER_PARTY));
    }

    @Test
    public void givenOtherPartyShouldNotGiveErrorIfAddressIsPresent() {
        List<Element<PartyDetails>> respondents =
            List.of(ElementUtils.element(UUID.fromString("e406bcc3-3c91-45db-9dcc-3a5c14930851"), PartyDetails.builder()
                .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                .contactPreferences(ContactPreferences.post)
                .address(Address.builder()
                             .addressLine1("test address").build())
                .build()));
        List<Element<PartyDetails>> otherParties =
            List.of(ElementUtils.element(UUID.fromString("6bb5e9ac-df97-4593-8b22-3969dc0bb4e1"), PartyDetails.builder()
                .address(Address.builder().addressLine1("test address").build())
                .build()));
        CaseData caseData = getCaseData();
        caseData = caseData.toBuilder()
            .id(123L).respondents(respondents)
            .taskListVersion(TASK_LIST_VERSION_V2)
            .otherPartyInTheCaseRevised(otherParties)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response
            = manageOrderService.validateRespondentLipAndOtherPersonAddress(callbackRequest);
        assertNotNull(response.getData());
        assertNotNull(response.getData().get("id"));
        assertEquals("123", response.getData().get("id").toString());
    }

    private CaseData getCaseData() {
        List<DynamicMultiselectListElement> elementList = new ArrayList<>();
        elementList.add(DynamicMultiselectListElement.builder()
                            .code("0016cff5-17f0-42af-bea4-0b04ca74c9ec")
                            .label("Jeremy Anderson (Applicant 2)")
                            .build());
        elementList.add(DynamicMultiselectListElement.builder()
                            .code("7e9028ba-534c-42bb-bb04-e67b6551b4bd")
                            .label("Martina Graham (Applicant 3)")
                            .build());
        elementList.add(DynamicMultiselectListElement.builder()
                            .code("e406bcc3-3c91-45db-9dcc-3a5c14930851")
                            .label("Mary Richards (Respondent 1)")
                            .build());

        List<DynamicMultiselectListElement> otherPartiesList = new ArrayList<>();
        otherPartiesList.add(DynamicMultiselectListElement.builder()
                                 .code("6bb5e9ac-df97-4593-8b22-3969dc0bb4e1")
                                 .label("Sam Nolan")
                                 .build());
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().serveToRespondentOptions(YesOrNo.No)
                              .recipientsOptions(DynamicMultiSelectList.builder()
                                                     .value(elementList)
                                                     .listItems(elementList)
                                                     .build())
                              .otherParties(DynamicMultiSelectList.builder()
                                                .value(otherPartiesList)
                                                .listItems(otherPartiesList)
                                                .build())
                              .build())
            .build();
        return caseData;
    }

    @Test
    public void testWaSetHearingOptionDetailsForTask_whenDoYouWantToEditOrderYes() {

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata));
        manageOrders.setOrdersHearingDetails(hearingDataList);
        manageOrders.setWhatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe);

        DraftOrder draftOrder = DraftOrder.builder().manageOrderHearingDetails(hearingDataList).build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();

        Element<DraftOrder> draftOrderElement = element(uuid, draftOrder);
        draftOrderCollection.add(draftOrderElement);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .doYouWantToEditTheOrder(YesOrNo.Yes)
            .draftOrderCollection(draftOrderCollection)
            .manageOrders(manageOrders).build();

        //when(manageOrderService.isOrderEdited(caseData, Event.EDIT_AND_APPROVE_ORDER.getId(), false)).thenReturn(true);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageOrderService.setHearingOptionDetailsForTask(
            caseData,
            caseDataUpdated,
            Event.EDIT_AND_APPROVE_ORDER.getId(),
            "JUDGE",
            uuid.toString()
        );
        assertEquals(
            HearingDateConfirmOptionEnum.dateToBeFixed.toString(),
            caseDataUpdated.get("hearingOptionSelected")
        );
        assertEquals("No", caseDataUpdated.get("isMultipleHearingSelected"));
        assertEquals("Yes", caseDataUpdated.get("isHearingTaskNeeded"));
        assertEquals("Yes", caseDataUpdated.get(WA_IS_ORDER_APPROVED));
        assertEquals("JUDGE", caseDataUpdated.get(WA_WHO_APPROVED_THE_ORDER));
    }


    @Test
    public void testWaSetHearingOptionDetailsForTask_whenDoYouWantToEditOrderNo() {

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata));

        manageOrders.setOrdersHearingDetails(hearingDataList);
        manageOrders.setWhatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.sendToAdminToServe);

        DraftOrder draftOrder = DraftOrder.builder().manageOrderHearingDetails(hearingDataList).build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();

        Element<DraftOrder> draftOrderElement = element(uuid, draftOrder);
        draftOrderCollection.add(draftOrderElement);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .doYouWantToEditTheOrder(YesOrNo.No)
            .draftOrderCollection(draftOrderCollection)
            .manageOrders(manageOrders).build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageOrderService.setHearingOptionDetailsForTask(caseData,
                                                          caseDataUpdated,
                                                          Event.EDIT_AND_APPROVE_ORDER.getId(),
                                                          "JUDGE", uuid.toString());
        assertEquals(
            HearingDateConfirmOptionEnum.dateReservedWithListAssit.toString(),
            caseDataUpdated.get("hearingOptionSelected")
        );
        assertEquals("No", caseDataUpdated.get("isMultipleHearingSelected"));
        assertEquals("Yes", caseDataUpdated.get("isHearingTaskNeeded"));
        assertEquals("Yes", caseDataUpdated.get(WA_IS_ORDER_APPROVED));
        assertEquals("JUDGE", caseDataUpdated.get(WA_WHO_APPROVED_THE_ORDER));
    }

    @Test
    public void testWaSetHearingOptionDetailsForTask_whenManagerOrdersJourney() {

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata));

        manageOrders.setOrdersHearingDetails(hearingDataList);
        manageOrders.setWhatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.sendToAdminToServe);

        DraftOrder draftOrder = DraftOrder.builder().manageOrderHearingDetails(hearingDataList).build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();

        Element<DraftOrder> draftOrderElement = element(uuid, draftOrder);
        draftOrderCollection.add(draftOrderElement);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .doYouWantToEditTheOrder(YesOrNo.No)
            .draftOrderCollection(draftOrderCollection)
            .manageOrders(manageOrders).build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        //when(manageOrderService.isOrderEdited(caseData,Event.EDIT_AND_APPROVE_ORDER.getId(),false)).thenReturn(false);
        manageOrderService.setHearingOptionDetailsForTask(caseData,
                                                          caseDataUpdated,
                                                          Event.MANAGE_ORDERS.getId(),
                                                          "JUDGE", null);
        assertEquals(
            HearingDateConfirmOptionEnum.dateReservedWithListAssit.toString(),
            caseDataUpdated.get("hearingOptionSelected")
        );
        assertEquals("No", caseDataUpdated.get("isMultipleHearingSelected"));
        assertEquals("Yes", caseDataUpdated.get("isHearingTaskNeeded"));
        assertNull(caseDataUpdated.get(WA_IS_ORDER_APPROVED));
        assertNull(caseDataUpdated.get(WA_WHO_APPROVED_THE_ORDER));
    }

    @Test
    public void testWaIsHearingTaskNeeded_whenRejected_thenHearingTaskNeeded_shuldbe_No() {

        Map<String, Object> caseDataUpdated = new HashMap<>();

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata));
        manageOrders.setOrdersHearingDetails(hearingDataList);

        String isOrderApproved = "No";
        String eventId = Event.EDIT_AND_APPROVE_ORDER.getId();
        AmendOrderCheckEnum amendOrderCheck = AmendOrderCheckEnum.judgeOrLegalAdvisorCheck;

        manageOrderService.setIsHearingTaskNeeded(
            manageOrders.getOrdersHearingDetails(),
            caseDataUpdated,
            isOrderApproved,
            amendOrderCheck,
            eventId
        );
        assertEquals("No", caseDataUpdated.get("isHearingTaskNeeded"));
    }

    @Test
    public void testWaIsHearingTaskNeeded_whenJudgeLaReviewRequired_thenHearingTaskNeeded_shuldbe_No() {

        Map<String, Object> caseDataUpdated = new HashMap<>();

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata));
        manageOrders.setOrdersHearingDetails(hearingDataList);

        String isOrderApproved = "Yes";
        String eventId = Event.MANAGE_ORDERS.getId();
        AmendOrderCheckEnum amendOrderCheck = AmendOrderCheckEnum.judgeOrLegalAdvisorCheck;

        manageOrderService.setIsHearingTaskNeeded(
            manageOrders.getOrdersHearingDetails(),
            caseDataUpdated,
            isOrderApproved,
            amendOrderCheck,
            eventId
        );
        assertEquals("No", caseDataUpdated.get("isHearingTaskNeeded"));
    }

    @Test
    public void testWaIsHearingTaskNeeded_whenManagerReviewRequired_thenHearingTaskNeeded_shuldbe_No() {

        Map<String, Object> caseDataUpdated = new HashMap<>();

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata));
        manageOrders.setOrdersHearingDetails(hearingDataList);

        String isOrderApproved = "Yes";
        String eventId = Event.MANAGE_ORDERS.getId();
        AmendOrderCheckEnum amendOrderCheck = AmendOrderCheckEnum.managerCheck;

        manageOrderService.setIsHearingTaskNeeded(
            manageOrders.getOrdersHearingDetails(),
            caseDataUpdated,
            isOrderApproved,
            amendOrderCheck,
            eventId
        );
        assertEquals("No", caseDataUpdated.get("isHearingTaskNeeded"));
    }

    @Test
    public void testUpdateOrderFieldsForDocmosis() {

        DraftOrder draftOrder = DraftOrder.builder().judgeOrMagistratesLastName("testJudge").build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .doYouWantToEditTheOrder(YesOrNo.No)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();

        CaseData response = manageOrderService.updateOrderFieldsForDocmosis(draftOrder, caseData);
        assertEquals("testJudge", response.getJudgeOrMagistratesLastName());

    }

    @Test
    public void testHandleFetchOrderDetails() {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .isSdoSelected(YesOrNo.Yes)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .applicants(List.of(element(PartyDetails.builder().doTheyHaveLegalRepresentation(YesNoDontKnow.no).build())))
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        Map<String, Object> caseDataUpdated = manageOrderService.handleFetchOrderDetails("testAuth", callbackRequest);
        assertEquals(YesOrNo.Yes, caseDataUpdated.get("isSdoSelected"));

    }


    @Test
    public void testGetHearingDataFromExistingHearingData() {

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata = HearingData.builder()
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata));

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(hearingDataService.getHearingDataForOtherOrders(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(List.of(Element.<HearingData>builder().build()));

        List<Element<HearingData>> hearingDataListResp
            = manageOrderService.getHearingDataFromExistingHearingData("testAuth", hearingDataList, caseData);

        assertNotNull(hearingDataListResp);
    }

    @Test
    public void testPopulateFinalOrderFromCaseDataForUploadAnOrder() throws Exception {

        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder().forename("test")
                                                                     .roles(List.of(Roles.COURT_ADMIN.getValue())).build());


        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<OrderRecipientsEnum> recipientList = new ArrayList<>();
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        recipientList.add(OrderRecipientsEnum.applicantOrApplicantSolicitor);
        recipientList.add(OrderRecipientsEnum.respondentOrRespondentSolicitor);

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        ReflectionTestUtils.setField(manageOrderService, "c21Template", "c21-template");

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(StandardDirectionOrder.builder().build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            //.dateOrderMade(LocalDate.now())
            .approvalDate(LocalDate.now())
            .orderRecipients(recipientList)
            .applicants(partyDetails)
            .respondents(partyDetails)
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .doesOrderClosesCase(YesOrNo.Yes)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.uploadAnOrder)
            .manageOrders(manageOrders.toBuilder()
                              .amendOrderSelectCheckOptions(AmendOrderCheckEnum.noCheck)
                              .serveOrderDynamicList(dynamicMultiSelectList)
                              .build())
            .build();
        Map<String, Object> response = manageOrderService.addOrderDetailsAndReturnReverseSortedList(
            "test token",
            caseData
        );
        List<Element<OrderDetails>> orderCollection = (List<Element<OrderDetails>>) response.get("orderCollection");

        assertEquals("Financial compensation order following C79 enforcement application (C82)",
                     orderCollection.get(0).getValue().getOrderTypeId());
        assertNotNull(response);

    }

    @Test
    public void testGenerateOrderDocumentFromDocmosis() throws Exception {
        ReflectionTestUtils.setField(manageOrderService, "sdoWelshDraftTemplate", "sdoWelshDraftTemplate");
        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata = HearingData.builder()
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata));

        manageOrders = ManageOrders.builder()
            .withdrawnOrRefusedOrder(WithDrawTypeOfOrderEnum.withdrawnApplication)
            .isCaseWithdrawn(YesOrNo.No)
            .ordersHearingDetails(hearingDataList)
            .childOption(
                dynamicMultiSelectList
            )
            .build();

        StandardDirectionOrder cafcass = StandardDirectionOrder.builder().sdoNewPartnerPartiesCafcass(
            dynamicMultiSelectList)
            .sdoAllocateOrReserveJudgeName(JudicialUser.builder().idamId("").build()).build();

        StandardDirectionOrder cymru = StandardDirectionOrder.builder().sdoNewPartnerPartiesCafcassCymru(
            dynamicMultiSelectList)
            .sdoAllocateOrReserveJudgeName(JudicialUser.builder().idamId("").build()).build();

        DynamicMultiselectListElement dynamicMultiselectListElement1 = DynamicMultiselectListElement.builder().label(
            "aa")
            .build();
        DynamicMultiselectListElement dynamicMultiselectListElement2 = DynamicMultiselectListElement.builder().label(
            "bb")
            .build();

        List<DynamicMultiselectListElement> dynamicMultiselectListElementList = new ArrayList<>();
        dynamicMultiselectListElementList.add(dynamicMultiselectListElement1);
        dynamicMultiselectListElementList.add(dynamicMultiselectListElement2);
        StandardDirectionOrder factFindingHearing = StandardDirectionOrder.builder().sdoHearingsAndNextStepsList(List.of(
            SdoHearingsAndNextStepsEnum.factFindingHearing))
            .sdoWhoMadeAllegationsList(DynamicMultiSelectList.builder()
                                           .value(dynamicMultiselectListElementList).build())
            .sdoWhoNeedsToRespondAllegationsList(DynamicMultiSelectList.builder()
                                                     .value(List.of(DynamicMultiselectListElement.builder().label("bb")
                                                                        .build())).build())
            .build();
        StandardDirectionOrder[] sdoNewPartnerParties = {cafcass, cymru, factFindingHearing};

        for (StandardDirectionOrder sdo : sdoNewPartnerParties) {

            CaseData caseData = CaseData.builder()
                .id(12345L)
                .doYouWantToEditTheOrder(YesOrNo.No)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .manageOrders(manageOrders)
                .standardDirectionOrder(sdo)
                .caseManagementLocation(CaseManagementLocation.builder().regionId("7").build())
                .build();

            doCallRealMethod().when(dynamicMultiSelectListService).getStringFromDynamicMultiSelectList(any());
            when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
                .thenReturn(generatedDocumentInfo);
            Map<String, Object> caseDataUpdated = manageOrderService.generateOrderDocumentFromDocmosis(
                "testauth",
                caseData,
                CreateSelectOrderOptionsEnum.standardDirectionsOrder
            );

            assertEquals("Yes", caseDataUpdated.get("isEngDocGen"));
            assertNotNull(caseDataUpdated.get("ordersHearingDetails"));
        }
    }

    @Test
    public void testIsOrderApprovedSolicitorCreated() {

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata));
        manageOrders.setOrdersHearingDetails(hearingDataList);
        manageOrders.setWhatToDoWithOrderSolicitor(OrderApprovalDecisionsForSolicitorOrderEnum.editTheOrderAndServe);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .doYouWantToEditTheOrder(YesOrNo.Yes)
            .manageOrders(manageOrders).build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageOrderService.isOrderApproved(caseData, caseDataUpdated, "JUDGE");
        assertEquals("Yes", caseDataUpdated.get(WA_IS_ORDER_APPROVED));
        assertEquals("JUDGE", caseDataUpdated.get(WA_WHO_APPROVED_THE_ORDER));
    }

    @Test
    public void setHearingSelectedInfoForTaskWithMultipleHearings() {

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata1 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        HearingData hearingdata2 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata1));
        hearingDataList.add(element(hearingdata2));
        manageOrders.setOrdersHearingDetails(hearingDataList);
        manageOrders.setWhatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageOrderService.setHearingSelectedInfoForTask(hearingDataList, caseDataUpdated);

        assertEquals("multipleOptionSelected", caseDataUpdated.get("hearingOptionSelected"));
        assertEquals("Yes", caseDataUpdated.get("isMultipleHearingSelected"));
    }

    @Test
    public void setHearingSelectedInfoForTaskWitNoHearings() {

        Map<String, Object> caseDataUpdated = new HashMap<>();

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        manageOrders.setOrdersHearingDetails(hearingDataList);
        manageOrders.setWhatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe);
        manageOrderService.setHearingSelectedInfoForTask(hearingDataList, caseDataUpdated);

        assertNull(caseDataUpdated.get("hearingOptionSelected"));
        assertNull(caseDataUpdated.get("isMultipleHearingSelected"));
    }

    @Test
    public void setHearingSelectedInfoForTaskWitHearingsButNoHearingDateConfirmOptionEnum() {

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata1 = HearingData.builder()
            .hearingDateConfirmOptionEnum(null)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata1));
        manageOrders.setOrdersHearingDetails(hearingDataList);
        manageOrders.setWhatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageOrderService.setHearingSelectedInfoForTask(hearingDataList, caseDataUpdated);

        assertNull(caseDataUpdated.get("hearingOptionSelected"));
        assertNull(caseDataUpdated.get("isMultipleHearingSelected"));
    }

    @Test
    public void setHearingSelectedInfoForTaskWitMultipleHearingsButOnlyOneHearingDateConfirmOptionEnum() {

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata1 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        HearingData hearingdata2 = HearingData.builder()
            .hearingDateConfirmOptionEnum(null)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        HearingData hearingdata3 = HearingData.builder()
            .hearingDateConfirmOptionEnum(null)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata1));
        hearingDataList.add(element(hearingdata2));
        hearingDataList.add(element(hearingdata3));
        manageOrders.setOrdersHearingDetails(hearingDataList);
        manageOrders.setWhatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe);

        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageOrderService.setHearingSelectedInfoForTask(hearingDataList, caseDataUpdated);

        assertEquals(
            HearingDateConfirmOptionEnum.dateToBeFixed.toString(),
            caseDataUpdated.get("hearingOptionSelected")
        );
        assertEquals("No", caseDataUpdated.get("isMultipleHearingSelected"));
    }

    @Test
    public void setHearingSelectedInfoForTaskWitMultipleHearingsButMoreThnOneHearingDateConfirmOptionEnum() {

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata1 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        HearingData hearingdata2 = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        HearingData hearingdata3 = HearingData.builder()
            .hearingDateConfirmOptionEnum(null)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata1));
        hearingDataList.add(element(hearingdata2));
        hearingDataList.add(element(hearingdata3));
        manageOrders.setOrdersHearingDetails(hearingDataList);
        manageOrders.setWhatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageOrderService.setHearingSelectedInfoForTask(hearingDataList, caseDataUpdated);

        assertEquals(
            "multipleOptionSelected",
            caseDataUpdated.get("hearingOptionSelected")
        );
        assertEquals("Yes", caseDataUpdated.get("isMultipleHearingSelected"));
    }

    @Test
    public void testHandlePreviewOrderScenario1() throws Exception {
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .isSdoSelected(YesOrNo.Yes)
            .applicantCaseName("Test Case 45678")
            .respondents(partyDetails)
            .applicants(partyDetails)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            .applicants(List.of(element(PartyDetails.builder().doTheyHaveLegalRepresentation(YesNoDontKnow.no).build())))
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId("createOrders")
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        Map<String, Object> caseDataUpdated = manageOrderService.handlePreviewOrder(callbackRequest, "testAuth");
        assertNull(caseDataUpdated.get(SDO_FACT_FINDING_FLAG));

    }

    @Test
    public void testHandlePreviewOrderScenario2() throws Exception {
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        PartyDetails details1 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement1 = element(details1);
        partyDetails.add(partyDetailsElement1);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .isSdoSelected(YesOrNo.Yes)
            .applicantCaseName("Test Case 45678")
            .respondents(partyDetails)
            .applicants(partyDetails)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            .applicants(List.of(element(PartyDetails.builder().doTheyHaveLegalRepresentation(YesNoDontKnow.no).build())))
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId("createOrders")
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        Map<String, Object> caseDataUpdated = manageOrderService.handlePreviewOrder(callbackRequest, "testAuth");
        assertNotNull(caseDataUpdated.get(SDO_FACT_FINDING_FLAG));
        assertEquals("<div class=\"govuk-inset-text\"> "
                         + "If you need to include directions for a fact-finding hearing, you need to upload the"
                         + " order in manage orders instead.</div>", caseDataUpdated.get(SDO_FACT_FINDING_FLAG));

    }

    @Test
    public void testHandlePreviewOrderScenario3() throws Exception {
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        PartyDetails details1 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement1 = element(details1);
        partyDetails.add(partyDetailsElement1);
        manageOrders.toBuilder().fl404CustomFields(FL404.builder().build()).build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .isSdoSelected(YesOrNo.Yes)
            .applicantCaseName("Test Case 45678")
            .respondents(partyDetails)
            .applicants(partyDetails)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .fl401FamilymanCaseNumber("familyman12345")
            .applicants(List.of(element(PartyDetails.builder().doTheyHaveLegalRepresentation(YesNoDontKnow.no).build())))
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId("createOrders")
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        Map<String, Object> caseDataUpdated = manageOrderService.handlePreviewOrder(callbackRequest, "testAuth");
        assertNull(caseDataUpdated.get(SDO_FACT_FINDING_FLAG));
    }

    @Test
    public void testSetHearingDataForSdo() {

        UUID uuid = UUID.randomUUID();
        HearingDataFromTabToDocmosis hearingDataFromTabToDocmosis = HearingDataFromTabToDocmosis.builder().hearingType(
            "ABA5-FHR").build();

        List<Element<HearingDataFromTabToDocmosis>> elementList = new ArrayList<>();
        elementList.add(element(uuid, hearingDataFromTabToDocmosis));

        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("12345:").label("test")
                                                                  .build()).build();
        HearingData hearingDataInitial = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .build();

        HearingData hearingDataRevised = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .hearingdataFromHearingTab(elementList).build();

        StandardDirectionOrder standardDirectionOrder = StandardDirectionOrder.builder()
            .sdoUrgentHearingDetails(hearingDataInitial)
            .sdoPermissionHearingDetails(hearingDataInitial)
            .sdoSecondHearingDetails(hearingDataInitial)
            .sdoFhdraHearingDetails(hearingDataInitial)
            .sdoDraHearingDetails(hearingDataInitial)
            .sdoSettlementHearingDetails(hearingDataInitial)
            .sdoDirectionsForFactFindingHearingDetails(hearingDataInitial)
            .sdoHearingsAndNextStepsList(List.of(SdoHearingsAndNextStepsEnum.factFindingHearing))
            .sdoAllocateOrReserveJudgeName(JudicialUser.builder().idamId("").build()).build();


        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Test")
            .manageOrders(ManageOrders.builder().hearingsType(dynamicList).build())
            .standardDirectionOrder(standardDirectionOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .build();
        CaseHearing caseHearing = CaseHearing.caseHearingWith().hmcStatus("CANCELLED").hearingID(123456L).build();
        Hearings hearings = Hearings.hearingsWith()
            .caseRef("123")
            .hmctsServiceCode("ABA5")
            .caseHearings(Collections.singletonList(caseHearing))
            .build();
        when(hearingDataService.getHearingDataForSdo(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(HearingData.builder().build());
        when(hearingDataService.populateHearingDynamicLists(Mockito.anyString(),
                                                            Mockito.anyString(),
                                                            Mockito.any(),
                                                            Mockito.any()))
            .thenReturn(HearingDataPrePopulatedDynamicLists.builder().build());
        when(hearingDataService.getHearingDataForSelectedHearingForSdo(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(hearingDataRevised);

        CaseData caseDataResp = manageOrderService.setHearingDataForSdo(caseData, hearings, "auth");
        Assert.assertNull(caseData.getStandardDirectionOrder().getSdoDirectionsForFactFindingHearingDetails().getHearingdataFromHearingTab());
        Assert.assertEquals(uuid, caseDataResp.getStandardDirectionOrder()
            .getSdoDirectionsForFactFindingHearingDetails().getHearingdataFromHearingTab().get(0).getId());
    }

    @Test
    public void testServeOrderC100WithAmendedParties() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();


        List<DynamicMultiselectListElement> elements = new ArrayList<>();
        DynamicMultiselectListElement element = DynamicMultiselectListElement.builder()
            .code(uuid.toString())
            .label("test label").build();
        elements.add(element);

        List<Element<ServedParties>> servedParties = new ArrayList<>();
        Element<ServedParties> servedPartiesElement = ElementUtils.element(uuid, ServedParties.builder()
            .partyName("test")
            .partyId(uuid.toString())
            .build());
        servedParties.add(servedPartiesElement);
        Element<OrderDetails> orders = Element.<OrderDetails>builder().id(uuid).value(OrderDetails
                                                                                          .builder()
                                                                                          .orderDocument(Document
                                                                                                             .builder()
                                                                                                             .build())
                                                                                          .dateCreated(now)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .serveOrderDetails(
                                                                                              ServeOrderDetails.builder()
                                                                                                  .servedParties(
                                                                                                      servedParties)
                                                                                                  .build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder().firstName("first").lastName("lastname")
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);

        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .childArrangementsOrdersToIssue(List.of(childArrangementsOrder, prohibitedStepsOrder))
            .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.liveWithOrder)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .recipientsOptions(DynamicMultiSelectList.builder()
                                   .value(elements)
                                   .listItems(elements)
                                   .build())
            .childOption(DynamicMultiSelectList.builder()
                             .listItems(elements)
                             .build())
            .otherParties(DynamicMultiSelectList.builder()
                              .listItems(elements)
                              .build())
            .serveToRespondentOptions(YesOrNo.Yes)
            .servingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .deliveryByOptionsCA(DeliveryByEnum.post)
            .emailInformationCA(List.of(Element.<EmailInformation>builder()
                                            .value(EmailInformation.builder().emailAddress("test").build()).build()))
            .postalInformationCA(List.of(Element.<PostalInformation>builder()
                                             .value(PostalInformation.builder().postalAddress(
                                                 Address.builder().postCode("NE65LA").build()).build()).build()))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(partyDetails)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .dateOrderMade(LocalDate.now())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();


        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());
        List<Element<OrderDetails>> orderDetails = manageOrderService.serveOrder(caseData, orderList);
        assertNotNull(orderDetails);
        assertNotNull(orderDetails.get(0));
        assertNotNull(orderDetails.get(0).getValue().getServeOrderDetails());
        assertNotNull(orderDetails.get(0).getValue().getServeOrderDetails().getServedParties());
        assertNotNull(orderDetails.get(0).getValue().getServeOrderDetails().getServedParties().get(0));
        assertEquals(
            orderDetails.get(0).getValue().getServeOrderDetails().getServedParties().get(0).getValue()
                .getPartyId(),
            (orders.getValue().getServeOrderDetails().getServedParties().get(0).getValue().getPartyId())
        );
    }

    @Test
    public void testWaSetHearingOptionDetailsForTask_whenDoYouWantToEditOrderYesForSdo() {

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateToBeFixed)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata));
        manageOrders.setOrdersHearingDetails(hearingDataList);
        manageOrders.setWhatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.editTheOrderAndServe);

        DraftOrder draftOrder = DraftOrder.builder()
            .orderType(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .sdoDetails(SdoDetails.builder().sdoUrgentHearingDetails(hearingdata).build())
            .otherDetails(OtherDraftOrderDetails.builder().approvedBy("test").dateCreated(LocalDateTime.now()).build())
            .manageOrderHearingDetails(hearingDataList).build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();

        Element<DraftOrder> draftOrderElement = element(uuid, draftOrder);
        draftOrderCollection.add(draftOrderElement);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .doYouWantToEditTheOrder(YesOrNo.Yes)
            .draftOrderCollection(draftOrderCollection)
            .draftOrdersDynamicList(ElementUtils.asDynamicList(
                draftOrderCollection,
                null,
                DraftOrder::getLabelForOrdersDynamicList
            ))
            .manageOrders(manageOrders).build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageOrderService.setHearingOptionDetailsForTask(
            caseData,
            caseDataUpdated,
            Event.EDIT_AND_APPROVE_ORDER.getId(),
            "JUDGE",
            uuid.toString()
        );
        assertEquals(
            HearingDateConfirmOptionEnum.dateToBeFixed.toString(),
            caseDataUpdated.get("hearingOptionSelected")
        );
        assertEquals("No", caseDataUpdated.get("isMultipleHearingSelected"));
        assertEquals("Yes", caseDataUpdated.get("isHearingTaskNeeded"));
        assertEquals("Yes", caseDataUpdated.get(WA_IS_ORDER_APPROVED));
        assertEquals("JUDGE", caseDataUpdated.get(WA_WHO_APPROVED_THE_ORDER));
    }


    @Test
    public void testWaSetHearingOptionDetailsForTask_whenDoYouWantToEditOrderNoForSdo() {

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata));

        manageOrders.setOrdersHearingDetails(hearingDataList);
        manageOrders.setWhatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.sendToAdminToServe);

        DraftOrder draftOrder = DraftOrder.builder()
            .orderType(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .sdoDetails(SdoDetails.builder()
                            .sdoSecondHearingDetails(hearingdata)
                            .sdoUrgentHearingDetails(hearingdata)
                            .sdoFhdraHearingDetails(hearingdata)
                            .sdoPermissionHearingDetails(hearingdata)
                            .sdoDraHearingDetails(hearingdata)
                            .sdoSettlementHearingDetails(hearingdata)
                            .sdoDirectionsForFactFindingHearingDetails(hearingdata)
                            .build())
            .otherDetails(OtherDraftOrderDetails.builder().approvedBy("test").dateCreated(LocalDateTime.now()).build())
            .manageOrderHearingDetails(hearingDataList).build();
        List<Element<DraftOrder>> draftOrderCollection = new ArrayList<>();

        Element<DraftOrder> draftOrderElement = element(uuid, draftOrder);
        draftOrderCollection.add(draftOrderElement);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .doYouWantToEditTheOrder(YesOrNo.No)
            .draftOrderCollection(draftOrderCollection)
            .manageOrders(manageOrders).build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageOrderService.setHearingOptionDetailsForTask(caseData,
                                                          caseDataUpdated,
                                                          Event.EDIT_AND_APPROVE_ORDER.getId(),
                                                          "JUDGE", uuid.toString());
        assertEquals(
            "multipleOptionSelected",
            caseDataUpdated.get("hearingOptionSelected")
        );
        assertEquals("Yes", caseDataUpdated.get("isMultipleHearingSelected"));
        assertEquals("Yes", caseDataUpdated.get("isHearingTaskNeeded"));
        assertEquals("Yes", caseDataUpdated.get(WA_IS_ORDER_APPROVED));
        assertEquals("JUDGE", caseDataUpdated.get(WA_WHO_APPROVED_THE_ORDER));
    }

    @Test
    public void testWaSetHearingOptionDetailsForTask_whenManagerOrdersJourneyForSdo() {

        List<Element<HearingData>> hearingDataList = new ArrayList<>();
        HearingData hearingdata = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .hearingTypes(DynamicList.builder()
                              .value(null).build())
            .hearingChannelsEnum(null).build();
        hearingDataList.add(element(hearingdata));

        manageOrders.setOrdersHearingDetails(hearingDataList);
        manageOrders.setWhatToDoWithOrderCourtAdmin(OrderApprovalDecisionsForCourtAdminOrderEnum.sendToAdminToServe);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .doYouWantToEditTheOrder(YesOrNo.No)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                                        .sdoSecondHearingDetails(hearingdata)
                                        .sdoUrgentHearingDetails(hearingdata)
                                        .sdoFhdraHearingDetails(hearingdata)
                                        .sdoPermissionHearingDetails(hearingdata)
                                        .sdoDraHearingDetails(hearingdata)
                                        .sdoSettlementHearingDetails(hearingdata)
                                        .sdoDirectionsForFactFindingHearingDetails(hearingdata)
                                        .build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .manageOrders(manageOrders).build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageOrderService.setHearingOptionDetailsForTask(caseData,
                                                          caseDataUpdated,
                                                          Event.MANAGE_ORDERS.getId(),
                                                          "JUDGE", null);
        assertEquals(
            "multipleOptionSelected",
            caseDataUpdated.get("hearingOptionSelected")
        );
        assertEquals("Yes", caseDataUpdated.get("isMultipleHearingSelected"));
        assertEquals("Yes", caseDataUpdated.get("isHearingTaskNeeded"));
        assertNull(caseDataUpdated.get(WA_IS_ORDER_APPROVED));
        assertNull(caseDataUpdated.get(WA_WHO_APPROVED_THE_ORDER));
    }

    @Test
    public void testGetHearingData() {
        when(hearingService.getHearings(Mockito.anyString(),Mockito.anyString())).thenReturn(Hearings.hearingsWith().build());
        when(hearingDataService.populateHearingDynamicLists(Mockito.anyString(),Mockito.anyString(),Mockito.any(),Mockito.any()))
            .thenReturn(HearingDataPrePopulatedDynamicLists.builder().build());
        when(hearingDataService.generateHearingData(Mockito.any(),Mockito.any()))
            .thenReturn(HearingData.builder().build());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .build();
        assertNotNull(manageOrderService.getHearingData("test", caseData));
    }

    @Test
    public void testHandlePreviewOrderScenario4() throws Exception {
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        PartyDetails details1 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement1 = element(details1);
        partyDetails.add(partyDetailsElement1);
        manageOrders.toBuilder().fl404CustomFields(FL404.builder().build()).build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .isSdoSelected(YesOrNo.Yes)
            .applicantCaseName("Test Case 45678")
            .respondents(partyDetails)
            .applicants(partyDetails)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .fl401FamilymanCaseNumber("familyman12345")
            .applicants(List.of(element(PartyDetails.builder().doTheyHaveLegalRepresentation(YesNoDontKnow.no).build())))
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.uploadAnOrder)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(MANAGE_ORDERS.getId())
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        Map<String, Object> caseDataUpdated = manageOrderService.handlePreviewOrder(callbackRequest, "testAuth");
        assertNull(caseDataUpdated.get(SDO_FACT_FINDING_FLAG));
    }

    @Test
    public void testPopulateFinalOrderFromCaseDataWithNoCheck1() throws Exception {

        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder().forename("test")
                                                                     .roles(List.of(Roles.COURT_ADMIN.getValue())).build());


        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<OrderRecipientsEnum> recipientList = new ArrayList<>();
        List<Element<PartyDetails>> partyDetails = new ArrayList<>();
        PartyDetails details = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test Org").build())
            .build();
        Element<PartyDetails> partyDetailsElement = element(details);
        partyDetails.add(partyDetailsElement);
        recipientList.add(OrderRecipientsEnum.applicantOrApplicantSolicitor);
        recipientList.add(OrderRecipientsEnum.respondentOrRespondentSolicitor);

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        ReflectionTestUtils.setField(manageOrderService, "c21Template", "c21-template");
        manageOrders = ManageOrders.builder()
            .withdrawnOrRefusedOrder(WithDrawTypeOfOrderEnum.withdrawnApplication)
            .isCaseWithdrawn(YesOrNo.No)
            .isTheOrderAboutAllChildren(YesOrNo.Yes)
            .amendOrderSelectCheckOptions(AmendOrderCheckEnum.noCheck)
            .childArrangementsOrdersToIssue(List.of(OrderTypeEnum.childArrangementsOrder))
            .selectChildArrangementsOrder(ChildArrangementOrderTypeEnum.spendTimeWithOrder)
            .childOption(
                dynamicMultiSelectList
            )
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(StandardDirectionOrder.builder().build())
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            .dateOrderMade(LocalDate.now())
            .orderRecipients(recipientList)
            .applicants(partyDetails)
            .serveOrderData(ServeOrderData.builder().doYouWantToServeOrder(YesOrNo.Yes).build())
            .respondents(partyDetails)
            .selectTypeOfOrder(SelectTypeOfOrderEnum.finl)
            .doesOrderClosesCase(YesOrNo.Yes)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .manageOrders(manageOrders)
            .build();

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));
    }

    @Test
    public void testHandleFetchOrderDetails1() {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .isSdoSelected(YesOrNo.Yes)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.directionOnIssue)
            .fl401FamilymanCaseNumber("familyman12345")
            .applicants(List.of(element(PartyDetails.builder().doTheyHaveLegalRepresentation(YesNoDontKnow.no).build())))
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        Map<String, Object> caseDataUpdated = manageOrderService.handleFetchOrderDetails("testAuth", callbackRequest);
        assertEquals(YesOrNo.Yes, caseDataUpdated.get("isSdoSelected"));
    }

    @Test
    public void testHandleFetchOrderDetails2() {
        manageOrders = manageOrders.toBuilder()
            .c21OrderOptions(C21OrderOptionsEnum.c21other).build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .isSdoSelected(YesOrNo.Yes)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .applicants(List.of(element(PartyDetails.builder().doTheyHaveLegalRepresentation(YesNoDontKnow.no).build())))
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        Map<String, Object> caseDataUpdated = manageOrderService.handleFetchOrderDetails("testAuth", callbackRequest);
        assertEquals(YesOrNo.Yes, caseDataUpdated.get("isSdoSelected"));

    }

    @Test
    public void testHandleFetchOrderDetails3() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .isSdoSelected(YesOrNo.Yes)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            .applicants(List.of(element(PartyDetails.builder().doTheyHaveLegalRepresentation(YesNoDontKnow.no).build())))
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        Map<String, Object> caseDataUpdated = manageOrderService.handleFetchOrderDetails("testAuth", callbackRequest);
        assertEquals(YesOrNo.Yes, caseDataUpdated.get("isSdoSelected"));

    }

    @Test
    public void testHandleFetchOrderDetails4() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .isSdoSelected(YesOrNo.Yes)
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedings)
            .fl401FamilymanCaseNumber("familyman12345")
            .applicants(List.of(element(PartyDetails.builder().doTheyHaveLegalRepresentation(YesNoDontKnow.no).build())))
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        Map<String, Object> caseDataUpdated = manageOrderService.handleFetchOrderDetails("testAuth", callbackRequest);
        assertEquals(YesOrNo.No, caseDataUpdated.get("isSdoSelected"));

    }

}
