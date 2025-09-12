package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.judicial.FinalisationDetails;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FinalisationDetailsServiceTest {
    @InjectMocks
    private FinalisationDetailsService finalisationDetailsService;

    @Test
    public void shouldBuildFinalisationDetailsWithJudgeOrMagistrateTitle() {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.circuitJudge)
                              .build())
            .build();

        FinalisationDetails result = finalisationDetailsService.buildFinalisationDetails(caseData);

        assertNotNull(result);
        assertEquals(JudgeOrMagistrateTitleEnum.circuitJudge.name(), result.getJudgeOrMagistrateTitle());
    }

    @Test
    public void shouldThrowNullPointerWhenJudgeOrMagistrateTitleIsNull() {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .build();

        assertThrows(NullPointerException.class, () ->
            finalisationDetailsService.buildFinalisationDetails(caseData)
        );
    }

    @Test
    public void shouldThrowNullPointerWhenManageOrdersIsNull() {
        CaseData caseData = CaseData.builder().build();

        assertThrows(NullPointerException.class, () ->
            finalisationDetailsService.buildFinalisationDetails(caseData)
        );
    }
}
