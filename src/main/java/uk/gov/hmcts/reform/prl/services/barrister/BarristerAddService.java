package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.UUID;

import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BarristerAddService extends AbstractBarristerService {
    public AllocatedBarrister getAllocatedBarrister(CaseData caseData) {
        return AllocatedBarrister.builder()
            .partyList(getSolicitorPartyDynamicList(caseData))
            .barristerOrg(Organisation.builder().build())
            .build();
    }

    @Override
    protected String getLabelForAction(boolean applicantOrRespondent, PartyDetails partyDetails) {
        return String.format("%s %s (%s), %s, %s", partyDetails.getFirstName(),
                                     partyDetails.getLastName(),
                                     applicantOrRespondent ? APPLICANT : RESPONDENT,
                                     partyDetails.getRepresentativeFullName(),
                                     partyDetails.getSolicitorOrg().getOrganisationName()
        );
    }

    protected UUID getCodeForAction(PartyDetails partyDetails) {
        return partyDetails.getPartyId();
    }

    @Override
    protected boolean isPartyApplicable(boolean applicantOrRespondent, PartyDetails partyDetails) {
        return (partyDetails.getBarrister() == null) && ((applicantOrRespondent && partyDetails.getSolicitorPartyId() != null)
            || (!applicantOrRespondent && yes.equals(partyDetails.getDoTheyHaveLegalRepresentation())));
    }

}
