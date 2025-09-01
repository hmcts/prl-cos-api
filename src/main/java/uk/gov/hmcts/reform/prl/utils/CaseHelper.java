package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.UUID;

@Slf4j
@Component
public class CaseHelper {

    public void setAllocatedBarrister(PartyDetails partyDetails,
                                      CaseData caseData,
                                      UUID partyId) {
        if (partyDetails != null) {
            caseData.setAllocatedBarrister(caseData.getAllocatedBarrister().toBuilder()
                                               .partyList(
                                                   DynamicList.builder()
                                                       .value(DynamicListElement.builder()
                                                                  .code(partyId)
                                                                  .build())
                                                       .build())
                                               .barristerOrg(partyDetails.getBarrister().getBarristerOrg())
                                               .barristerEmail(partyDetails.getBarrister().getBarristerEmail())
                                               .barristerFirstName(partyDetails.getBarrister().getBarristerFirstName())
                                               .barristerLastName(partyDetails.getBarrister().getBarristerLastName())
                                               .build());
        } else {
            log.error("For case id {} : Party details not present. So,Failed to set allocated barrister for party id {}",
                      caseData.getId(),
                      partyId);
        }
    }
}

