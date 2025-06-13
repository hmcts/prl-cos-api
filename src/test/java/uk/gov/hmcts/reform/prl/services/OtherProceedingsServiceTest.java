package uk.gov.hmcts.reform.prl.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class OtherProceedingsServiceTest {

    @InjectMocks
    OtherProceedingsService otherProceedingsService;

    ArrayList<TypeOfOrderEnum> typeOfOrder;

    @BeforeEach
    void setUp() {

        typeOfOrder = new ArrayList<>();
        typeOfOrder.add(TypeOfOrderEnum.emergencyProtectionOrder);
        typeOfOrder.add(TypeOfOrderEnum.childArrangementsOrder);
    }

    @Test
    void testExistingProceedingsNotEmpty() {

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
        assertTrue(caseDataUpdated.containsKey("existingProceedingsWithDoc"));
    }

    @Test
    void testExistingProceedingsNotEmptyWithoutDoc() {

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
        assertFalse(caseDataUpdated.containsKey("existingProceedingsWithDoc"));
    }

    @Test
    void testExistingProceedingsEmpty() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseData.builder().build();
        otherProceedingsService.populateCaseDocumentsData(caseData, caseDataUpdated);
        assertTrue(caseDataUpdated.isEmpty());
    }

    @Test
    void testExistingProceedingsNotEmptyWithOnlyOneDoc() {
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
        assertEquals(1, results.size());
    }
}

