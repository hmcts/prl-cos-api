package uk.gov.hmcts.reform.prl.services;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.ProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.Silent.class)
public class OtherProceedingsServiceTest {

    @InjectMocks
    OtherProceedingsService otherProceedingsService;

    ArrayList<TypeOfOrderEnum> typeOfOrder;

    @Before
    public void setUp() {

        typeOfOrder = new ArrayList<>();
        typeOfOrder.add(TypeOfOrderEnum.emergencyProtectionOrder);
        typeOfOrder.add(TypeOfOrderEnum.childArrangementsOrder);


    }


    @Test
    public void testExistingProceedingsNotEmpty() {

        ProceedingDetails  proceedingDetails = ProceedingDetails.builder().dateEnded(LocalDate.of(1990, 8, 1))
                .caseNumber("2344").dateStarted(LocalDate.of(2020, 8, 1))
                .nameOfCourt("Court Name").nameOfJudge("Judge Name").typeOfOrder(typeOfOrder)
                .nameAndOffice("Office Name").nameOfGuardian("Guardian Name").otherTypeOfOrder("Other Type of Order")
                .nameOfChildrenInvolved("Name Of Children").previousOrOngoingProceedings(ProceedingsEnum.ongoing)
                .uploadRelevantOrder(Document.builder().build())
                .build();
        Element<ProceedingDetails> proceedingDetailsElement = Element.<ProceedingDetails>builder()
                .value(proceedingDetails).build();
        List<Element<ProceedingDetails>> existingProceedings = Collections.singletonList(proceedingDetailsElement);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseData.builder().existingProceedings(existingProceedings).build();
        otherProceedingsService.populateCaseDocumentsData(caseData, caseDataUpdated);
        Assert.assertTrue(caseDataUpdated.containsKey("existingProceedingsWithDoc"));

    }

    @Test
    public void testExistingProceedingsNotEmptyWithoutDoc() {

        ProceedingDetails  proceedingDetails = ProceedingDetails.builder().dateEnded(LocalDate.of(1990, 8, 1))
                .caseNumber("2344").dateStarted(LocalDate.of(2020, 8, 1))
                .nameOfCourt("Court Name").nameOfJudge("Judge Name").typeOfOrder(typeOfOrder)
                .nameAndOffice("Office Name").nameOfGuardian("Guardian Name").otherTypeOfOrder("Other Type of Order")
                .nameOfChildrenInvolved("Name Of Children").previousOrOngoingProceedings(ProceedingsEnum.ongoing)
                .build();
        Element<ProceedingDetails> proceedingDetailsElement = Element.<ProceedingDetails>builder()
                .value(proceedingDetails).build();
        List<Element<ProceedingDetails>> existingProceedings = Collections.singletonList(proceedingDetailsElement);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseData.builder().existingProceedings(existingProceedings).build();
        otherProceedingsService.populateCaseDocumentsData(caseData, caseDataUpdated);
        Assert.assertFalse(caseDataUpdated.containsKey("existingProceedingsWithDoc"));

    }

    @Test
    public void testExistingProceedingsEmpty() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseData.builder().build();
        otherProceedingsService.populateCaseDocumentsData(caseData, caseDataUpdated);
        Assert.assertTrue(caseDataUpdated.isEmpty());

    }

    @Test
    public void testExistingProceedingsNotEmptyWithOnlyOneDoc() {
        ProceedingDetails  proceedingDetails = ProceedingDetails.builder().dateEnded(LocalDate.of(1990, 8, 1))
                .caseNumber("2344").dateStarted(LocalDate.of(2020, 8, 1))
                .nameOfCourt("Court Name").nameOfJudge("Judge Name").typeOfOrder(typeOfOrder)
                .nameAndOffice("Office Name").nameOfGuardian("Guardian Name").otherTypeOfOrder("Other Type of Order")
                .nameOfChildrenInvolved("Name Of Children").previousOrOngoingProceedings(ProceedingsEnum.ongoing)
                .build();
        ProceedingDetails  proceedingDetailsWithDoc = ProceedingDetails.builder().dateEnded(LocalDate.of(1990, 8, 1))
                .caseNumber("2344").dateStarted(LocalDate.of(2020, 8, 1))
                .nameOfCourt("Court Name").nameOfJudge("Judge Name").typeOfOrder(typeOfOrder)
                .nameAndOffice("Office Name").nameOfGuardian("Guardian Name").otherTypeOfOrder("Other Type of Order")
                .nameOfChildrenInvolved("Name Of Children").previousOrOngoingProceedings(ProceedingsEnum.ongoing)
                .uploadRelevantOrder(Document.builder().build())
                .build();
        Element<ProceedingDetails> proceedingDetailsElement = Element.<ProceedingDetails>builder()
                .value(proceedingDetails).build();
        Element<ProceedingDetails> proceedingDetailsElementDoc = Element.<ProceedingDetails>builder()
                .value(proceedingDetailsWithDoc).build();
        List<Element<ProceedingDetails>> existingProceedings = List.of(proceedingDetailsElement,proceedingDetailsElementDoc);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseData.builder().existingProceedings(existingProceedings).build();
        otherProceedingsService.populateCaseDocumentsData(caseData, caseDataUpdated);
        List<Element<ProceedingDetails>> results = (List<Element<ProceedingDetails>>) caseDataUpdated.get("existingProceedingsWithDoc");
        Assert.assertEquals(1, results.size());
    }


}

