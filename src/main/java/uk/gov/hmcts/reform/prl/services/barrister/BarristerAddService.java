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

import static uk.gov.hmcts.reform.prl.enums.PartyEnum.applicant;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.respondent;

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
        boolean isApplicable = (!hasBarrister(partyDetails)) && (partyHasSolicitorOrg(partyDetails));

        if (barristerFilter.isCaseworkerOrSolicitor()) {
            return isApplicable;
        } else {
            if (partyDetails.getSolicitorOrg() == null || barristerFilter.getUserOrgIdentifier() == null) {
                log.info("This party {} has an empty solicitor org or the user org identifier is empty", partyDetails.getPartyId());
                return false;
            }

            return isApplicable
                && barristerFilter.getUserOrgIdentifier().equals(partyDetails.getSolicitorOrg().getOrganisationID());
        }
    }

    @Override
    protected String getLabelForAction(boolean applicantOrRespondent, BarristerFilter barristerFilter, PartyDetails partyDetails) {
        return String.format("%s %s (%s), %s, %s", partyDetails.getFirstName(),
                                     partyDetails.getLastName(),
                                     applicantOrRespondent ? applicant.getDisplayedValue() : respondent.getDisplayedValue(),
                                     partyDetails.getRepresentativeFullName(),
                                     partyDetails.getSolicitorOrg().getOrganisationName()
        );
    }

    @Override
    protected String getCodeForAction(Element<PartyDetails> partyDetailsElement) {
        return partyDetailsElement.getId().toString();
    }

}
