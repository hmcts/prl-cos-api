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
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
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
import uk.gov.hmcts.reform.prl.enums.manageorders.ServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.WithDrawTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
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
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServeOrderData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.StandardDirectionOrder;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;
import static uk.gov.hmcts.reform.prl.services.ManageOrderService.CHILD_OPTION;
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

    private LocalDateTime now;
    @Mock
    private HearingService hearingService;

    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setup() {
        manageOrders = ManageOrders.builder()
            .withdrawnOrRefusedOrder(WithDrawTypeOfOrderEnum.withdrawnApplication)
            .isCaseWithdrawn(YesOrNo.No)
            .childOption(
                DynamicMultiSelectList.builder()
                    .value(List.of(DynamicMultiselectListElement.builder().label("John (Child 1)").build())).build()
            )
            .build();

        now = dateTime.now();
        DynamicListElement dynamicListElement = DynamicListElement.builder().code(TEST_UUID).label(" ").build();
        dynamicList = DynamicList.builder()
            .listItems(List.of(dynamicListElement))
            .value(dynamicListElement)
            .build();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code(TEST_UUID + "-" + now)
            .label("test")
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();
        manageOrders = ManageOrders.builder()
            .withdrawnOrRefusedOrder(WithDrawTypeOfOrderEnum.withdrawnApplication)
            .isCaseWithdrawn(YesOrNo.No)
            .childOption(dynamicMultiSelectList)
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        uuid = UUID.fromString(TEST_UUID);
        when(elementUtils.getDynamicListSelectedValue(Mockito.any(), Mockito.any())).thenReturn(uuid);
        when(dynamicMultiSelectListService.getOrdersAsDynamicMultiSelectList(
            Mockito.any(CaseData.class),
            Mockito.anyString()
        ))
            .thenReturn(dynamicMultiSelectList);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .roles(List.of(Roles.JUDGE.getValue())).build());
        when(dynamicMultiSelectListService.getStringFromDynamicMultiSelectList(Mockito.any(DynamicMultiSelectList.class)))
            .thenReturn("testChild");
        when(userService.getUserByUserId(Mockito.anyString(), Mockito.anyString())).thenReturn(UserDetails.builder()
                                                                                                   .forename("")
                                                                                                   .surname("")
                                                                                                   .build());
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
            .caseTypeOfApplication("C100")
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

        assertNotNull(responseMap.get("caseTypeOfApplication"));

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
                                 .representativeLastName("test")
                                 .representativeFirstName("test")
                                 .build())
            .manageOrders(ManageOrders.builder()
                              .recitalsOrPreamble("test")
                              .isCaseWithdrawn(YesOrNo.Yes)
                              .isTheOrderByConsent(YesOrNo.Yes)
                              .fl404CustomFields(FL404.builder().build())
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
                              .orderDirections("test")
                              .furtherDirectionsIfRequired("test")
                              .build())
            .respondentsFL401(PartyDetails.builder()
                                  .firstName("resp")
                                  .lastName("testLast")
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

        CaseData updatedCaseData = manageOrderService.populateCustomOrderFields(caseData);

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
            .isTheOrderByConsent(YesOrNo.Yes)
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
        builder.caseTypeOfApplication("C100");
        builder.applicantCaseName("Test Case 45678");
        builder.orderCollection(List.of(element(OrderDetails.builder()
                                                    .orderType("other")
                                                    .otherDetails(OtherOrderDetails.builder()
                                                                      .orderCreatedDate("10-Feb-2023").build()).build())));
        builder.childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82);
        CaseData caseData = builder
            .build();

        Map<String, Object> responseMap = manageOrderService.populateHeader(caseData);

        assertEquals("C100", responseMap.get("caseTypeOfApplication"));

    }

    @Test
    public void testPopulateHeaderC100TestWithCafcass() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .isCafcass(YesOrNo.Yes)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(element(OrderDetails.builder()
                                                 .orderType("other")
                                                 .otherDetails(OtherOrderDetails.builder()
                                                                   .orderCreatedDate("10-Feb-2023").build()).build())))
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        Map<String, Object> responseMap = manageOrderService.populateHeader(caseData);

        assertEquals("C100", responseMap.get("caseTypeOfApplication"));

    }

    @Test
    public void testPopulateHeaderC100TestWithRegionId() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .caseManagementLocation(CaseManagementLocation.builder().regionId("1").build())
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(element(OrderDetails.builder()
                                                 .orderType("other")
                                                 .otherDetails(OtherOrderDetails.builder()
                                                                   .orderCreatedDate("10-Feb-2023").build()).build())))
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        Map<String, Object> responseMap = manageOrderService.populateHeader(caseData);

        assertEquals("C100", responseMap.get("caseTypeOfApplication"));
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

        assertEquals("FL401", responseMap.get("caseTypeOfApplication"));

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
            .caseTypeOfApplication("C100")
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
            .caseTypeOfApplication("C100")
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
            .caseTypeOfApplication("C100")
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

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
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
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .manageOrders(manageOrders)
            .standardDirectionOrder(StandardDirectionOrder.builder()
                                        .sdoAllocateOrReserveJudgeName(JudicialUser.builder().idamId("").build()).build())
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.standardDirectionsOrder)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
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
            .caseTypeOfApplication("C100")
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
            .caseTypeOfApplication("C100")
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
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .dateOrderMade(LocalDate.now())
            .orderRecipients(recipientList)
            .applicants(partyDetails)
            .respondents(partyDetails)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        assertNotNull(manageOrderService.populateHeader(caseData).get("caseTypeOfApplication"));
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
            .caseTypeOfApplication("C100")
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

        assertNotNull(manageOrderService.populateHeader(caseData).get("caseTypeOfApplication"));
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
            .caseTypeOfApplication("C100")
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
            .firstName("")
            .lastName("")
            .dateOfBirth(LocalDate.now())
            .address(Address.builder().build())
            .build();
        CaseData caseData = CaseData.builder()
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .manageOrders(ManageOrders.builder().isTheOrderByConsent(YesOrNo.Yes).build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.generalForm).build();
        assertNotNull(manageOrderService.populateCustomOrderFields(caseData));
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
            .isTheOrderByConsent(YesOrNo.Yes)
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
        assertNotNull(manageOrderService.populateCustomOrderFields(caseData));
    }

    @Test
    public void testServeOrderCA() throws Exception {
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();


        ManageOrders manageOrders = ManageOrders.builder()
            .cafcassCymruServedOptions(YesOrNo.No)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveToRespondentOptions(YesOrNo.Yes)
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
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

        CaseData caseData = CaseData.builder()
            .id(12345L)
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
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .deliveryByOptionsCA(DeliveryByEnum.post)
            .emailInformationCA(List.of(Element.<EmailInformation>builder()
                                            .value(EmailInformation.builder().emailAddress("test").build()).build()))
            .postalInformationCA(List.of(Element.<PostalInformation>builder()
                                             .value(PostalInformation.builder().postalAddress(
                                                 Address.builder().postCode("NE65LA").build()).build()).build()))
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
        doNothing().when(dynamicMultiSelectListService).updateChildrenWithCaseCloseStatus(caseData, orders);
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
            .otherPartiesOnlyC47a(dynamicMultiSelectList)
            .cafcassServedOptions(YesOrNo.Yes)
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .serveToRespondentOptions(YesOrNo.Yes)
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .deliveryByOptionsCA(DeliveryByEnum.post)
            .emailInformationCA(List.of(Element.<EmailInformation>builder()
                                            .value(EmailInformation.builder().emailAddress("test").build()).build()))
            .postalInformationCA(List.of(Element.<PostalInformation>builder()
                                             .value(PostalInformation.builder().postalAddress(
                                                 Address.builder().postCode("NE65LA").build()).build()).build()))
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
        doNothing().when(dynamicMultiSelectListService).updateChildrenWithCaseCloseStatus(caseData, orders);
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

        ManageOrders manageOrders = ManageOrders.builder()
            .serveOrderDynamicList(dynamicMultiSelectList)
            .serveOrderAdditionalDocuments(List.of(Element.<Document>builder()
                                                       .value(Document.builder().documentFileName(
                                                           "abc.pdf").build())
                                                       .build()))
            .servingRespondentsOptionsDA(ServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesDA(List.of(ServeOtherPartiesOptions.other))
            .deliveryByOptionsDA(DeliveryByEnum.post)
            .emailInformationCA(List.of(Element.<EmailInformation>builder()
                                            .value(EmailInformation.builder().emailAddress("test").build()).build()))
            .postalInformationCA(List.of(Element.<PostalInformation>builder()
                                             .value(PostalInformation.builder().postalAddress(
                                                 Address.builder().postCode("NE65LA").build()).build()).build()))
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
            .build();


        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());
        doNothing().when(dynamicMultiSelectListService).updateChildrenWithCaseCloseStatus(caseData, orders);
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
            .isTheOrderByConsent(YesOrNo.Yes)
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
        assertNotNull(manageOrderService.populateCustomOrderFields(caseData));
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
            .caseTypeOfApplication("C100")
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
            .caseTypeOfApplication("C100")
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
            .build();
        assertEquals("Other", manageOrderService.getSelectedOrderInfoForUpload(caseData));
    }

    @Test
    public void testGetSelectedOrderInForUpload() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .build();
        assertEquals("", manageOrderService.getSelectedOrderInfoForUpload(caseData));
    }

    @Test
    public void testPopulateDraftOrderForJudge() throws Exception {
        when(userService.getUserDetails(Mockito.anyString()))
            .thenReturn(UserDetails.builder().roles(List.of(Roles.JUDGE.getValue())).build());

        when(dateTime.now()).thenReturn(LocalDateTime.now());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
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
                              .isTheOrderByConsent(YesOrNo.Yes)
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
            "COURT_ADMIN",
            "editAndApproveAnOrder",
            "Created by Admin"
        );
        assertEquals(OrderStatusEnum.reviewedByJudge.getDisplayedValue(), status);
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
            .caseTypeOfApplication("C100")
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
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .deliveryByOptionsCA(DeliveryByEnum.post)
            .recipientsOptions(dynamicMultiSelectList)
            .otherParties(dynamicMultiSelectList)
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

        CaseData caseData = CaseData.builder()
            .id(12345L)
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
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .deliveryByOptionsCA(DeliveryByEnum.post)
            .recipientsOptionsOnlyC47a(dummyDynamicMultiSelectList)
            .otherPartiesOnlyC47a(dummyDynamicMultiSelectList)
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

        CaseData caseData = CaseData.builder()
            .id(12345L)
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
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
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

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
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
        Map<String, Object> stringObjectMap = manageOrderService.checkOnlyC47aOrderSelectedToServe(callbackRequest);
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
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
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

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
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

        String courtEmail = "test1@test.com";
        when(welshCourtEmail.populateCafcassCymruEmailInManageOrders(Mockito.any())).thenReturn(courtEmail);

        Map<String, Object> stringObjectMap = manageOrderService.checkOnlyC47aOrderSelectedToServe(callbackRequest);
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
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
            .serveOtherPartiesCA(List.of(OtherOrganisationOptions.anotherOrganisation))
            .cafcassCymruEmail("test")
            .deliveryByOptionsCA(DeliveryByEnum.post)
            .emailInformationCA(List.of(Element.<EmailInformation>builder()
                                            .value(EmailInformation.builder().emailAddress("test").build()).build()))
            .postalInformationCA(List.of(Element.<PostalInformation>builder()
                                             .value(PostalInformation.builder().postalAddress(
                                                 Address.builder().postCode("NE65LA").build()).build()).build()))
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
            .manageOrders(manageOrders.toBuilder()
                              .amendOrderSelectCheckOptions(AmendOrderCheckEnum.noCheck)
                              .build())
            .manageOrdersOptions(ManageOrdersOptionsEnum.createAnOrder)
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
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
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
                                                                                          .isTheOrderAboutChildren(YesOrNo.Yes)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
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
        manageOrders = manageOrders.toBuilder().amendOrderSelectCheckOptions(AmendOrderCheckEnum.noCheck).build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .standardDirectionOrder(StandardDirectionOrder.builder().build())
            .caseTypeOfApplication("C100")
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
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
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
                                                                                          .isTheOrderAboutChildren(YesOrNo.Yes)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
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
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
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

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
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
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
            .emailInformationCA(List.of(Element.<EmailInformation>builder()
                                            .value(EmailInformation.builder().emailAddress("test").build()).build()))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
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
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
            .emailInformationCA(List.of(Element.<EmailInformation>builder()
                                            .value(EmailInformation.builder().emailAddress("test").build()).build()))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
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
        Assert.assertEquals(YesOrNo.No,caseDataUpdated.get("markedToServeEmailNotification"));

    }


    @Test
    public void testCleanUpSelectedManageOrderOptions() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("manageOrdersOptions","manageOrdersOptions");
        manageOrderService.cleanUpSelectedManageOrderOptions(caseDataUpdated);
        Assert.assertNull(caseDataUpdated.get("manageOrdersOptions"));

    }

    @Test
    public void testGetAllChildrenFinalOrderIssuedStatusForC100() {

        Child child = Child.builder()
            .firstName("Test")
            .lastName("Name")
            .gender(female)
            .orderAppliedFor(Collections.singletonList(childArrangementsOrder))
            .applicantsRelationshipToChild(specialGuardian)
            .respondentsRelationshipToChild(father)
            .parentalResponsibilityDetails("test")
            .isFinalOrderIssued(YesOrNo.Yes)
            .build();

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);
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
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
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
                                                                                          .isTheOrderAboutChildren(YesOrNo.Yes)
                                                                                          .orderTypeId(TEST_UUID)
                                                                                          .otherDetails(
                                                                                              OtherOrderDetails.builder().build())
                                                                                          .build()).build();
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(orders);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirections)
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(orderList)
            .children(listOfChildren)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        Assert.assertEquals(YesOrNo.Yes,manageOrderService.getAllChildrenFinalOrderIssuedStatus(caseData));
    }

    @Test
    public void testGetAllChildrenFinalOrderIssuedStatusForFL401() {

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("Applicant401")
            .lastName("last name")
            .build();

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
            .servingRespondentsOptionsCA(ServingRespondentsEnum.courtAdmin)
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
                                                                                          .isTheOrderAboutChildren(YesOrNo.Yes)
                                                                                          .orderTypeId(TEST_UUID)
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
            .applicantsFL401(partyDetails)
            .doesOrderClosesCase(YesOrNo.Yes)
            .orderCollection(orderList)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .manageOrdersOptions(ManageOrdersOptionsEnum.servedSavedOrders)
            .manageOrders(manageOrders)
            .build();

        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        Assert.assertEquals(YesOrNo.Yes,manageOrderService.getAllChildrenFinalOrderIssuedStatus(caseData));
    }
}
