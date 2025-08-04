package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.OrganisationService;

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
            .barristerFirstName(null)
            .barristerLastName(null)
            .barristerEmail(null)
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

    protected String getCodeForAction(Element<PartyDetails> partyDetailsElement) {
        return partyDetailsElement.getId().toString();
    }

    @Override
    protected boolean isPartyApplicable(boolean applicantOrRespondent, PartyDetails partyDetails) {
        return (!hasBarrister(partyDetails)) && ((applicantOrRespondent && partyDetails.getSolicitorPartyId() != null)
            || (!applicantOrRespondent && yes.equals(partyDetails.getDoTheyHaveLegalRepresentation())));
    }

}
