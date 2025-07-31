package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.OrganisationService;

import java.util.UUID;

import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;

@Slf4j
@Service
public class BarristerAddService extends AbstractBarristerService {
    public BarristerAddService(OrganisationService organisationService) {
        super(organisationService);
    }

    public AllocatedBarrister getAllocatedBarrister(CaseData caseData, UserDetails userDetails, String authorisation) {
        return AllocatedBarrister.builder()
            .partyList(getSolicitorPartyDynamicList(caseData, userDetails, authorisation))
            .barristerName(null)
            .barristerEmail(null)
            .barristerOrg(Organisation.builder().build())
            .roleItem(null)
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
        return (applicantOrRespondent && partyDetails.getSolicitorPartyId() != null)
            || (!applicantOrRespondent && yes.equals(partyDetails.getDoTheyHaveLegalRepresentation()));
    }

}
