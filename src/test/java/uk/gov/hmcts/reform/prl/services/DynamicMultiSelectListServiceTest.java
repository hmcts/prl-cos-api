package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class DynamicMultiSelectListServiceTest {

    @InjectMocks
    private DynamicMultiSelectListService dynamicMultiSelectListService;

    @Mock
    private ObjectMapper objectMapper;

    private CaseData caseData;

    private CaseData caseDataC100;
    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";
    private List<Element<PartyDetails>> partyDetails;

    @BeforeEach
    void setUp() {
        List<Element<Child>> children = List.of(Element.<Child>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(Child.builder().build()).build());
        partyDetails = List.of(Element.<PartyDetails>builder().id(UUID.fromString(TEST_UUID))
                                                             .value(PartyDetails.builder()
                                                                        .partyId(UUID.fromString(TEST_UUID))
                                                                        .doTheyHaveLegalRepresentation(
                                                                 YesNoDontKnow.yes)
                                                                        .representativeFirstName("test")
                                                                        .representativeLastName("test")
                                                                        .user(User.builder().solicitorRepresented(Yes)
                                                                                  .build())
                                                                        .partyId(UUID.fromString(TEST_UUID))
                                                                        .build()).build());
        List<Element<OrderDetails>> orders = List.of(Element.<OrderDetails>builder().id(UUID.fromString(TEST_UUID))
                                                         .value(OrderDetails.builder()
                                                                    .orderTypeId("test")
                                                                    .otherDetails(OtherOrderDetails.builder()
                                                                                      .orderCreatedDate("today").build())
                                                                    .build())
                                                         .build());

        PartyDetails partyDetails1 = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .isRemoveLegalRepresentativeRequested(Yes)
            .user(User.builder().solicitorRepresented(Yes).build())
            .representativeFirstName("test")
            .representativeLastName("test")
            .build();

        caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .children(children)
            .applicantsFL401(partyDetails1)
            .respondentsFL401(partyDetails1)
            .applicants(partyDetails)
            .applicantChildDetails(List.of(Element.<ApplicantChild>builder().id(UUID.fromString(TEST_UUID))
                                               .value(ApplicantChild.builder().fullName("test").build())
                                               .build()))
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutAllChildren(YesOrNo.No)
                              .isTheOrderAboutChildren((Yes))
                              .childOption(DynamicMultiSelectList.builder()
                                               .value(List.of(DynamicMultiselectListElement.builder().code(TEST_UUID)
                                                                  .label("")
                                                                  .build()))
                                               .build()).build())
            .respondents(partyDetails)
            .orderCollection(orders)
            .othersToNotify(partyDetails)
            .build();

        caseDataC100 = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .children(children)
            .applicantChildDetails(List.of(Element.<ApplicantChild>builder().id(UUID.fromString(TEST_UUID))
                                               .value(ApplicantChild.builder().fullName("test").build())
                                               .build()))
            .applicants(partyDetails)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutAllChildren(YesOrNo.No)
                              .isTheOrderAboutChildren((Yes))
                              .childOption(DynamicMultiSelectList.builder()
                                               .value(List.of(DynamicMultiselectListElement.builder().code(TEST_UUID)
                                                                  .label("")
                                                                  .build()))
                                               .build()).build())
            .respondents(partyDetails)
            .orderCollection(orders)
            .othersToNotify(partyDetails)
            .build();
    }

    @Test
    void testChildDetails() {
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseData);
        assertNotNull(listItems);
    }

    @Test
    void testChildDetailsScenario2() {
        List<Element<ChildDetailsRevised>> children = List.of(Element.<ChildDetailsRevised>builder().id(UUID.fromString(TEST_UUID))
                                                    .value(ChildDetailsRevised.builder().isFinalOrderIssued(YesOrNo.No).build()).build());
        caseDataC100 = caseDataC100.toBuilder().taskListVersion("v2").newChildDetails(children).build();
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getChildrenMultiSelectList(caseDataC100);
        assertNotNull(listItems);
    }

    @Test
    void testOrderDetails() throws Exception {
        DynamicMultiSelectList dynamicMultiSelectList = dynamicMultiSelectListService
            .getOrdersAsDynamicMultiSelectList(caseData);
        assertNotNull(dynamicMultiSelectList);
    }

    @Test
    void testOrderDetailsAsNull() throws Exception {
        caseData = caseData.toBuilder().orderCollection(List.of(Element.<OrderDetails>builder().value(OrderDetails.builder()
                                                            .otherDetails(OtherOrderDetails.builder().build())
                                                            .build()).build())).build();
        DynamicMultiSelectList dynamicMultiSelectList = dynamicMultiSelectListService
            .getOrdersAsDynamicMultiSelectList(caseData);
        assertNotNull(dynamicMultiSelectList);
    }

    @Test
    void testOrderDetailsOtherDetailsAsNull() throws Exception {
        caseData = caseData.toBuilder().orderCollection(List.of(Element.<OrderDetails>builder()
                                                                    .value(OrderDetails.builder()
                                                                               .otherDetails(OtherOrderDetails.builder()
                                                                                                 .orderServedDate("")
                                                                                                 .build())
                                                                               .build()).build())).build();
        DynamicMultiSelectList dynamicMultiSelectList = dynamicMultiSelectListService
            .getOrdersAsDynamicMultiSelectList(caseData);
        assertNotNull(dynamicMultiSelectList);
    }

    @Test
    void testApplicantDetails() throws Exception {
        List<DynamicMultiselectListElement> applicants = dynamicMultiSelectListService
            .getApplicantsMultiSelectList(caseData).get("applicants");
        List<DynamicMultiselectListElement> solicitors = dynamicMultiSelectListService
            .getApplicantsMultiSelectList(caseData).get("applicantSolicitors");
        assertNotNull(applicants);
        assertNotNull(solicitors);
    }

    @Test
    void testRespondantDetails() throws Exception {
        List<DynamicMultiselectListElement> respondents = dynamicMultiSelectListService
            .getRespondentsMultiSelectList(caseData).get("respondents");
        List<DynamicMultiselectListElement> solicitors = dynamicMultiSelectListService
            .getRespondentsMultiSelectList(caseData).get("respondentSolicitors");

        assertNotNull(respondents);
        assertNotNull(solicitors);
    }

    @Test
    void testOtherPeopleDetails() {
        List<DynamicMultiselectListElement> listItems = dynamicMultiSelectListService
            .getOtherPeopleMultiSelectList(caseData);
        assertNotNull(listItems);
    }

    @Test
    void testApplicantDetailsFl401() throws Exception {
        caseData = caseData.toBuilder()
            .applicantsFL401(partyDetails.getFirst().getValue())
            .respondentsFL401(partyDetails.getFirst().getValue())
            .applicants(null)
            .respondents(null)
            .caseTypeOfApplication("FL401")
            .build();
        List<DynamicMultiselectListElement> applicants = dynamicMultiSelectListService
            .getApplicantsMultiSelectList(caseData).get("applicants");
        assertNotNull(applicants);
        List<DynamicMultiselectListElement> solicitors = dynamicMultiSelectListService
            .getApplicantsMultiSelectList(caseData).get("applicantSolicitors");
        assertNotNull(solicitors);
        List<DynamicMultiselectListElement> respondents = dynamicMultiSelectListService
            .getRespondentsMultiSelectList(caseData).get("respondents");
        assertNotNull(respondents);
        List<DynamicMultiselectListElement> rsolicitors = dynamicMultiSelectListService
            .getRespondentsMultiSelectList(caseData).get("respondentSolicitors");
        assertNotNull(rsolicitors);
    }

    @Test
    void testChildDetailsFl401() throws Exception {
        caseData = caseData.toBuilder().children(null)
            .applicantChildDetails(List.of(Element.<ApplicantChild>builder().id(UUID.fromString(TEST_UUID))
                                               .value(ApplicantChild.builder().fullName("test").build())
                                               .build())).build();
        List<DynamicMultiselectListElement> children = dynamicMultiSelectListService.getChildrenMultiSelectList(caseData);
        assertNotNull(children);
    }

    @Test
    void testGetStringFromDynMulSelectList() {
        DynamicMultiselectListElement listElement = DynamicMultiselectListElement.builder()
            .label("Child (Child 1)")
            .build();
        String str = dynamicMultiSelectListService
            .getStringFromDynamicMultiSelectList(DynamicMultiSelectList
                                                     .builder()
                                                     .value(List.of(listElement, listElement))
                                                     .build());
        assertEquals("Child, Child", str);
    }

    @Test
    void testGetServedPartiesFromDynMulSelectList() {
        DynamicMultiselectListElement listElement = DynamicMultiselectListElement.builder()
            .code("2323WDWDw2322")
            .label("Child (Child 1)")
            .build();
        List<Element<ServedParties>> servedParties = dynamicMultiSelectListService
            .getServedPartyDetailsFromDynamicSelectList(DynamicMultiSelectList
                                                     .builder()
                                                     .value(List.of(listElement, listElement))
                                                     .build());
        assertEquals(listElement.getCode(), servedParties.getFirst().getValue().getPartyId());
        assertEquals(listElement.getLabel(), servedParties.getFirst().getValue().getPartyName());
    }

    @Test
    void testGetStringFromDynMultiSelectListFromListItems() {
        DynamicMultiselectListElement listElement = DynamicMultiselectListElement.builder()
            .label("Child (Child 1)")
            .build();
        String str = dynamicMultiSelectListService
            .getStringFromDynamicMultiSelectListFromListItems(DynamicMultiSelectList
                                                     .builder()
                                                     .listItems(List.of(listElement, listElement))
                                                     .build());
        assertEquals("Child, Child", str);
    }

    @Test
    void testGetStringFromDynMultiSelectListFromListItemsForEmptyList() {
        String str = dynamicMultiSelectListService
            .getStringFromDynamicMultiSelectListFromListItems(DynamicMultiSelectList
                                                                  .builder()
                                                                  .listItems(List.of(DynamicMultiselectListElement.EMPTY))
                                                                  .build());
        assertEquals("", str);
    }

    @Test
    void testDynamicMultiSelectForDocmosis() {

        caseData = caseData.toBuilder()
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutChildren((Yes))
                              .childOption(DynamicMultiSelectList.builder()
                                               .value(List.of(DynamicMultiselectListElement.builder().code(TEST_UUID)
                                                                  .label("")
                                                                  .build()))
                                               .build()).build())
            .build();
        List<Element<Child>> str = dynamicMultiSelectListService
            .getChildrenForDocmosis(caseData);
        assertNotNull(str);
    }

    @Test
    void testGetChildrenForDocmosisC100() {
        List<Element<Child>> str = dynamicMultiSelectListService
            .getChildrenForDocmosis(caseDataC100);
        assertNotNull(str);
    }


    @Test
    void testGetApplicantChildDetailsForDocmosis() {
        List<Element<ApplicantChild>> str = dynamicMultiSelectListService
            .getApplicantChildDetailsForDocmosis(caseData);
        assertNotNull(str);
    }

    @Test
    void testGetRemoveLegalRepAndPartiesListFL401() {
        DynamicMultiSelectList listItems = dynamicMultiSelectListService
            .getRemoveLegalRepAndPartiesList(caseData);
        assertNotNull(listItems);
    }

    @Test
    void testGetRemoveLegalRepAndPartiesListFL401SolicitorOrg() {
        PartyDetails partyDetails1 = PartyDetails.builder()
            .isRemoveLegalRepresentativeRequested(Yes)
            .solicitorOrg(Organisation.builder().organisationID("test").build())
            .user(User.builder().build())
            .representativeFirstName("test")
            .representativeLastName("test")
            .build();

        CaseData caseDataOrg = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .applicantsFL401(partyDetails1)
            .respondentsFL401(partyDetails1)
            .applicants(partyDetails)
            .applicantChildDetails(List.of(Element.<ApplicantChild>builder().id(UUID.fromString(TEST_UUID))
                                               .value(ApplicantChild.builder().fullName("test").build())
                                               .build()))
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutAllChildren(YesOrNo.No)
                              .isTheOrderAboutChildren((Yes))
                              .childOption(DynamicMultiSelectList.builder()
                                               .value(List.of(DynamicMultiselectListElement.builder().code(TEST_UUID)
                                                                  .label("")
                                                                  .build()))
                                               .build()).build())
            .respondents(partyDetails)
            .othersToNotify(partyDetails)
            .build();
        DynamicMultiSelectList listItems = dynamicMultiSelectListService
            .getRemoveLegalRepAndPartiesList(caseDataOrg);
        assertNotNull(listItems);
    }

    @Test
    void testGetRemoveLegalRepAndPartiesListFL401LegalRep() {
        PartyDetails partyDetails1 = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .isRemoveLegalRepresentativeRequested(Yes)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .user(User.builder().build())
            .representativeFirstName("test")
            .representativeLastName("test")
            .build();

        CaseData caseDataOrg = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .applicantsFL401(partyDetails1)
            .respondentsFL401(partyDetails1)
            .applicants(partyDetails)
            .applicantChildDetails(List.of(Element.<ApplicantChild>builder().id(UUID.fromString(TEST_UUID))
                                               .value(ApplicantChild.builder().fullName("test").build())
                                               .build()))
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutAllChildren(YesOrNo.No)
                              .isTheOrderAboutChildren((Yes))
                              .childOption(DynamicMultiSelectList.builder()
                                               .value(List.of(DynamicMultiselectListElement.builder().code(TEST_UUID)
                                                                  .label("")
                                                                  .build()))
                                               .build()).build())
            .respondents(partyDetails)
            .othersToNotify(partyDetails)
            .build();
        DynamicMultiSelectList listItems = dynamicMultiSelectListService
            .getRemoveLegalRepAndPartiesList(caseDataOrg);
        assertNotNull(listItems);
    }

    @Test
    void testGetRemoveLegalRepAndPartiesListC100() {
        DynamicMultiSelectList listItems = dynamicMultiSelectListService
            .getRemoveLegalRepAndPartiesList(caseDataC100);
        assertNotNull(listItems);
    }

    @Test
    void testGetRemoveLegalRepAndPartiesListC100LegalRep() {
        partyDetails = List.of(Element.<PartyDetails>builder().id(UUID.fromString(TEST_UUID))
                                   .value(PartyDetails.builder()
                                              .user(User.builder().build())
                                              .doTheyHaveLegalRepresentation(
                                                  YesNoDontKnow.yes)
                                              .representativeFirstName("test")
                                              .representativeLastName("test")
                                              .build()).build());

        caseDataC100 = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .applicantChildDetails(List.of(Element.<ApplicantChild>builder().id(UUID.fromString(TEST_UUID))
                                               .value(ApplicantChild.builder().fullName("test").build())
                                               .build()))
            .applicants(partyDetails)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutAllChildren(YesOrNo.No)
                              .isTheOrderAboutChildren((Yes))
                              .childOption(DynamicMultiSelectList.builder()
                                               .value(List.of(DynamicMultiselectListElement.builder().code(TEST_UUID)
                                                                  .label("")
                                                                  .build()))
                                               .build()).build())
            .respondents(partyDetails)
            .othersToNotify(partyDetails)
            .build();
        DynamicMultiSelectList listItems = dynamicMultiSelectListService
            .getRemoveLegalRepAndPartiesList(caseDataC100);
        assertNotNull(listItems);
    }

    @Test
    void testGetRemoveLegalRepAndPartiesListC100SolOrg() {
        partyDetails = List.of(Element.<PartyDetails>builder().id(UUID.fromString(TEST_UUID))
                                   .value(PartyDetails.builder()
                                              .user(User.builder().build())
                                              .solicitorOrg(Organisation.builder().organisationID("test").build())
                                              .representativeFirstName("test")
                                              .representativeLastName("test")
                                              .build()).build());

        caseDataC100 = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .applicantChildDetails(List.of(Element.<ApplicantChild>builder().id(UUID.fromString(TEST_UUID))
                                               .value(ApplicantChild.builder().fullName("test").build())
                                               .build()))
            .applicants(partyDetails)
            .manageOrders(ManageOrders.builder()
                              .isTheOrderAboutAllChildren(YesOrNo.No)
                              .isTheOrderAboutChildren((Yes))
                              .childOption(DynamicMultiSelectList.builder()
                                               .value(List.of(DynamicMultiselectListElement.builder().code(TEST_UUID)
                                                                  .label("")
                                                                  .build()))
                                               .build()).build())
            .respondents(partyDetails)
            .othersToNotify(partyDetails)
            .build();
        DynamicMultiSelectList listItems = dynamicMultiSelectListService
            .getRemoveLegalRepAndPartiesList(caseDataC100);
        assertNotNull(listItems);
    }

    @Test
    void testGetSolicitorRepresentedParties() {
        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .build();

        PartyDetails applicant2 = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .partyId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
            .build();

        Element<PartyDetails> wrappedApplicant1 = Element.<PartyDetails>builder()
            .id(UUID.fromString("00000000-0000-0000-0000-000000000000")).value(applicant1).build();
        Element<PartyDetails> wrappedApplicant2 = Element.<PartyDetails>builder().value(applicant2).build();
        List<Element<PartyDetails>> listOfApplicants = new ArrayList<>();
        listOfApplicants.add(wrappedApplicant1);
        listOfApplicants.add(wrappedApplicant2);


        DynamicMultiSelectList listItems = dynamicMultiSelectListService
            .getSolicitorRepresentedParties(listOfApplicants);
        assertNotNull(listItems);
    }

    @Test
    void testGetDynamicMultiSellectEMptyList() {
        assertEquals(1, dynamicMultiSelectListService.getEmptyDynMultiSelectList().getListItems().size());
    }
}
