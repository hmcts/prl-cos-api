package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.ProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class OtherProceedingsMapperTest {

    @InjectMocks
    OtherProceedingsMapper otherProceedingsMapper;
    List<Element<ProceedingDetails>> existingProceedings;
    ProceedingDetails proceedingDetails;

    @Before
    public void setUp() {

        ArrayList<TypeOfOrderEnum> typeOfOrder = new ArrayList<>();
        typeOfOrder.add(TypeOfOrderEnum.emergencyProtectionOrder);
        typeOfOrder.add(TypeOfOrderEnum.childArrangementsOrder);

        proceedingDetails = ProceedingDetails.builder().dateEnded(LocalDate.of(1990, 8, 1))
            .caseNumber("2344").dateStarted(LocalDate.of(2020, 8, 1))
            .nameOfCourt("Court Name").nameOfJudge("Judge Name").typeOfOrder(typeOfOrder)
            .nameAndOffice("Office Name").nameOfGuardian("Guardian Name").otherTypeOfOrder("Other Type of Order")
            .nameOfChildrenInvolved("Name Of Children").previousOrOngoingProceedings(ProceedingsEnum.ongoing)
            .build();
        Element<ProceedingDetails> proceedingDetailsElement = Element.<ProceedingDetails>builder()
            .value(proceedingDetails).build();
        existingProceedings = Collections.singletonList(proceedingDetailsElement);

    }


    @Test
    public void testOtherProceedingsMapperWithAllFields() {
        CaseData caseData = CaseData.builder().previousOrOngoingProceedingsForChildren(YesNoDontKnow.yes)
            .existingProceedings(existingProceedings).build();
        assertNotNull(otherProceedingsMapper.map(caseData));

    }

    @Test
    public void testOtherProceedingsMapperWithSomeFields() {
        CaseData caseData = CaseData.builder().previousOrOngoingProceedingsForChildren(YesNoDontKnow.yes)
            .build();
        assertNotNull(otherProceedingsMapper.map(caseData));

    }

    @Test
    public void testEmptyExistingProceedings() {
        existingProceedings = Collections.emptyList();;
        assertTrue(otherProceedingsMapper.mapExistingProceedings(existingProceedings).isEmpty());

    }
}
