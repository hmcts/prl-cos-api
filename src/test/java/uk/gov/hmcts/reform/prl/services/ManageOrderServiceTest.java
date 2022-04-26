package uk.gov.hmcts.reform.prl.services;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;

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
    private  DgsService dgsService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

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
    public void testPopulatePreviewOrderFromCaseData() throws Exception{

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

        manageOrderService.getCaseData("test token",caseData,caseDataUpdated);

        assertNotNull(caseDataUpdated.get("previewOrderDoc"));

    }
}
