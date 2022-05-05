package uk.gov.hmcts.reform.prl.services;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404b;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.father;
import static uk.gov.hmcts.reform.prl.enums.RelationshipsEnum.specialGuardian;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ManageOrderServiceTest {


    @InjectMocks
    private ManageOrderService manageOrderService;

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
    public void testPupulateHeader() {
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
    public void whenFl404bOrder_thenPopulateCustomFields() {
        CaseData caseData = CaseData.builder()
            .id(12345674L)
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.blank)
            .courtName("Court name")
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

        FL404b expectedDetails = FL404b.builder()
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

        assertEquals(updatedCaseData.getManageOrders().getFl404bCustomFields(), expectedDetails);


    }

    @Test
    public void whenNoOrder_thenReturnUnalteredCaseData() {
    }
}
