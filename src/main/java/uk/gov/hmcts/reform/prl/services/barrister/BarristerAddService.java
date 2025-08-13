package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;

@Slf4j
@Service
public class BarristerAddService extends AbstractBarristerService {

    public BarristerAddService(UserService userService, OrganisationService organisationService) {
        super(userService, organisationService);
    }

    public AllocatedBarrister getAllocatedBarrister(CaseData caseData, String authorisation) {
        return AllocatedBarrister.builder()
            .partyList(getPartiesToList(caseData, authorisation))
            .barristerOrg(Organisation.builder().build())
            .build();
    }

    @Override
    protected boolean isPartyApplicableForFiltering(boolean applicantOrRespondent, BarristerFilter barristerFilter, PartyDetails partyDetails) {
        if (barristerFilter.isCaseworkerOrSolicitor()) {
            return (!hasBarrister(partyDetails)) && (partyHasSolicitorOrg(partyDetails));
        } else {
            return false;
        }
    }

    @Override
    protected String getLabelForAction(boolean applicantOrRespondent, BarristerFilter barristerFilter, PartyDetails partyDetails) {
        return String.format("%s %s (%s), %s, %s", partyDetails.getFirstName(),
                                     partyDetails.getLastName(),
                                     applicantOrRespondent ? APPLICANT : RESPONDENT,
                                     partyDetails.getRepresentativeFullName(),
                                     partyDetails.getSolicitorOrg().getOrganisationName()
        );
    }

    @Override
    protected String getCodeForAction(Element<PartyDetails> partyDetailsElement) {
        return partyDetailsElement.getId().toString();
    }

}
