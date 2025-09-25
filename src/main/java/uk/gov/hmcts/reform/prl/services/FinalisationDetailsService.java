package uk.gov.hmcts.reform.prl.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.judicial.FinalisationDetails;

@Service
public class FinalisationDetailsService {

    public FinalisationDetails buildFinalisationDetails(CaseData caseData) {
        FinalisationDetails.FinalisationDetailsBuilder builder = FinalisationDetails.builder();

        if (caseData.getManageOrders().getJudgeOrMagistrateTitle() != null) {
            builder.judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle().name());
        }

        return builder.build();
    }
}
