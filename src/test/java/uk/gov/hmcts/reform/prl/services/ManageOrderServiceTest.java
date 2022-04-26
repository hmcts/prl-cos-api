package uk.gov.hmcts.reform.prl.services;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.OrderDetails;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.time.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.applicantOrApplicantSolicitor;
import static uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum.respondentOrRespondentSolicitor;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ManageOrderServiceTest {


    @InjectMocks
    private ManageOrderService manageOrderService;

    @Mock
    Time time;

    @Test
    public void getUpdatedCaseData() {

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
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .familymanCaseNumber("familyman12345")
            .children(listOfChildren)
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        CaseData caseData1 = manageOrderService.getUpdatedCaseData(caseData);

        assertEquals("Child 1: TestName\n", caseData1.getChildrenList());
        assertNotNull(caseData1.getSelectedOrder());

    }

    @Test
    public void testPopulateHeader() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .childArrangementOrders(ChildArrangementOrdersEnum.financialCompensationC82)
            .build();

        Map<String, Object> responseMap = manageOrderService.populateHeader(caseData);

        assertEquals("Case Name: Test Case 45678\n\n"
                         + "Family Man ID: familyman12345\n\n", responseMap.get("manageOrderHeader1"));

    }

    @Test
    public void givenEmptyOrderList_thenNewOrderShouldBeAddedAndListReturned() {
        PartyDetails applicant = PartyDetails.builder()
            .email("app@email.com")
            .solicitorOrg(Organisation.builder().organisationName("Test App Org Name").build())
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .email("rep@email.com")
            .solicitorOrg(Organisation.builder().organisationName("Test Res Org Name").build())
            .build();

        CaseData caseData = CaseData.builder()
            .selectedOrder("Selected order")
            .judgeOrMagistratesLastName("Test last name")
            .dateOrderMade(LocalDate.of(2022, 01, 01))
            .applicants(List.of(element(applicant)))
            .respondents(List.of(element(respondent)))
            .orderRecipients(List.of(applicantOrApplicantSolicitor, respondentOrRespondentSolicitor))
            .build();

        when(time.now()).thenReturn(LocalDateTime.now());

        List<Element<OrderDetails>> actual = manageOrderService.addOrderDetailsAndReturnReverseSortedList(caseData);

        assertEquals(1, actual.size());
        assertTrue(actual.stream().map(Element::getValue).allMatch(order -> order.getOrderType().equals("Selected order")));

    }

    @Test
    public void givenOrderListPresent_thenNewOrderShouldBeAddedAndListReturnedInCorrectOrder() {
        PartyDetails applicant = PartyDetails.builder()
            .email("app@email.com")
            .solicitorOrg(Organisation.builder().organisationName("Test App Org Name").build())
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .email("rep@email.com")
            .solicitorOrg(Organisation.builder().organisationName("Test Res Org Name").build())
            .build();

        OrderDetails orderDetails = OrderDetails.builder()
            .dateCreated(LocalDateTime.of(LocalDate.of(2021, 1, 10), LocalTime.of(22, 10)))
            .orderType("This is a test order")
            .build();

        Element<OrderDetails> order = element(orderDetails);
        List<Element<OrderDetails>> orderList = new ArrayList<>();
        orderList.add(order);

        CaseData caseData = CaseData.builder()
            .selectedOrder("Selected order")
            .judgeOrMagistratesLastName("Test last name")
            .dateOrderMade(LocalDate.of(2022, 01, 01))
            .applicants(List.of(element(applicant)))
            .respondents(List.of(element(respondent)))
            .orderRecipients(List.of(applicantOrApplicantSolicitor, respondentOrRespondentSolicitor))
            .orderCollection(orderList)
            .build();

        LocalDateTime fixedDateTime = LocalDateTime.of(LocalDate.of(2022, 1, 10), LocalTime.of(22, 10));

        when(time.now()).thenReturn(fixedDateTime);

        List<Element<OrderDetails>> actual = manageOrderService.addOrderDetailsAndReturnReverseSortedList(caseData);

        //check that item added to list
        assertEquals(2, actual.size());
        //check that first item in the list is the most recently created order
        assertEquals(actual.get(0).getValue().getDateCreated(), fixedDateTime);

    }

}
