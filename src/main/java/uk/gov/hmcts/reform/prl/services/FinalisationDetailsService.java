package uk.gov.hmcts.reform.prl.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.judicial.FinalisationDetails;

@Service
public class FinalisationDetailsService {

    public FinalisationDetails buildFinalisationDetails(CaseData caseData) {
        return FinalisationDetails.builder()
            .judgeOrMagistrateTitle(caseData.getManageOrders().getJudgeOrMagistrateTitle().name())
            .build();
    }
}
