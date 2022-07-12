package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ManageOrderServiceTest {


    @InjectMocks
    private ManageOrderService manageOrderService;

    @Mock
    private DgsService dgsService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private Time dateTime;

    @Mock
    private ObjectMapper objectMapper;

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
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        CaseData caseData1 = manageOrderService.getUpdatedCaseData(caseData);
        assertEquals("Child 1: Test Name\n\n", caseData1.getChildrenList());
        assertNotNull(caseData1.getSelectedOrder());
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
            .home(Home.builder()
                      .children(listOfHomeChildren)
                      .build())
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        CaseData caseData1 = manageOrderService.getUpdatedCaseData(caseData);

        assertEquals("Child 1: TestName\n", caseData1.getChildrenList());
        assertNotNull(caseData1.getSelectedOrder());
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

        FL404 expectedDetails = FL404.builder()
            .fl404bApplicantName("app testLast")
            .fl404bCaseNumber("12345674")
            .fl404bCourtName("Court name")
            .fl404bRespondentName("resp testLast")
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
        CaseData caseData = CaseData.builder()
            .id(12345674L)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blank)
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

        ManageOrders expectedDetails = ManageOrders.builder()
            .manageOrdersFl402CaseNo("12345674")
            .manageOrdersFl402CourtName("Court name")
            .manageOrdersFl402Applicant("app testLast")
            .manageOrdersFl402ApplicantRef("test test1")
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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirectionsWithdraw)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.appointmentOfGuardian)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.parentalResponsibility)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.transferOfCaseToAnotherCourt)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.powerOfArrest)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));
    }


    public void testPupulateHeaderC100Test() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        Map<String, Object> responseMap = manageOrderService.populateHeader(caseData);

        assertEquals("Case Name: Test Case 45678\n\n"
                         + "Family Man ID: \n\n", responseMap.get("manageOrderHeader1"));

    }


    public void testPupulateHeaderNoFl401FamilyManTest() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        Map<String, Object> responseMap = manageOrderService.populateHeader(caseData);

        assertEquals("Case Name: Test Case 45678\n\n"
                         + "Family Man ID: familyman12345\n\n", responseMap.get("manageOrderHeader1"));

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
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.childArrangementsSpecificProhibitedOrder)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.specialGuardianShip)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blankOrderOrDirectionsWithdraw)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
        Element<PartyDetails> partyDetailsElement = ElementUtils.element(details);
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

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

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
        assertEquals(caseDataNameList.get(0).getValue().getGuardianFullName(), "Full Name");
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
        Element<PartyDetails> partyDetailsElement = ElementUtils.element(details);
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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.amendDischargedVaried)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blank)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.nonMolestation)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.generalForm)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.noticeOfProceedings)
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        caseDataUpdated = manageOrderService.getCaseData("test token", caseData);

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
        Element<PartyDetails> partyDetailsElement = ElementUtils.element(details);
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
        Element<PartyDetails> partyDetailsElement = ElementUtils.element(details);
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

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(dateTime.now()).thenReturn(LocalDateTime.now());

        assertNotNull(manageOrderService.addOrderDetailsAndReturnReverseSortedList("test token", caseData));

    }
}
