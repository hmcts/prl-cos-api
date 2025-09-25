package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.judicial.FinalisationDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
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
    public void shouldReturnNullWhenJudgeOrMagistrateTitleIsNull() {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build())
            .build();

        FinalisationDetails result = finalisationDetailsService.buildFinalisationDetails(caseData);

        assertNotNull(result);
        assertNull(result.getJudgeOrMagistrateTitle());
    }

    @Test
    public void shouldThrowNullPointerWhenManageOrdersIsNull() {
        CaseData caseData = CaseData.builder().build();

        assertThrows(NullPointerException.class, () ->
            finalisationDetailsService.buildFinalisationDetails(caseData)
        );
    }
}
