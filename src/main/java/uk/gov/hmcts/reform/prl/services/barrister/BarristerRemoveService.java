package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BarristerRemoveService extends  AbstractBarristerService {

    public DynamicList getBarristerListToRemove(CaseData caseData) {
        return getSolicitorPartyDynamicList(caseData);
    }

    @Override
    protected boolean isPartyApplicable(boolean applicantOrRespondent, PartyDetails partyDetails) {
        return partyDetails.getBarristerPartyId() != null;
    }

    @Override
    protected String getLabelForAction(boolean applicantOrRespondent, PartyDetails partyDetails) {
        return String.format("%s (%s), %s, %s", partyDetails.getLabelForDynamicList(),
                             applicantOrRespondent ? APPLICANT : RESPONDENT,
                             partyDetails.getRepresentativeFullName(),
                             partyDetails.getBarristerFullName()
        );
    }

    @Override
    protected UUID getCodeForAction(PartyDetails partyDetails) {
        return partyDetails.getBarristerPartyId();
    }
}
