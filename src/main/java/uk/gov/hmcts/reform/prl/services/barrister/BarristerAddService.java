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
        boolean isApplicable = (!hasBarrister(partyDetails)) && (partyHasSolicitorOrg(partyDetails));

        return isPartyApplicableForFiltering(barristerFilter,
                                             partyDetails,
                                             isApplicable,
                                             partyId -> log.info("Barrister Add Service - This party {} has an empty solicitor org or "
                                                                     + "the user org identifier is empty", partyId.toString()));

    }


    @Override
    protected String getLabelForAction(boolean applicantOrRespondent, BarristerFilter barristerFilter, PartyDetails partyDetails) {
        String partyDetailsInfo = partyDetails.getSolicitorOrg().getOrganisationName();

        return getLabelForAction(applicantOrRespondent, partyDetails, partyDetailsInfo);
    }

    @Override
    protected String getCodeForAction(Element<PartyDetails> partyDetailsElement) {
        return partyDetailsElement.getId().toString();
    }

}
