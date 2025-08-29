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

import java.util.function.Function;

@Slf4j
@Service
public class BarristerRemoveService extends  AbstractBarristerService {

    public BarristerRemoveService(UserService userService, OrganisationService organisationService) {
        super(userService, organisationService);
    }

    public AllocatedBarrister getBarristerListToRemove(CaseData caseData,
                                                       String authorisation,
                                                       Function<PartyDetails, String> legalRepOrganisation) {
        return AllocatedBarrister.builder()
            .partyList(getPartiesToList(caseData, authorisation, legalRepOrganisation))
            .barristerOrg(Organisation.builder().build())
            .build();
    }


    @Override
    protected boolean isPartyApplicableForFiltering(boolean applicantOrRespondent, BarristerFilter barristerFilter, PartyDetails partyDetails) {
        boolean isApplicable = hasBarrister(partyDetails);

        return isPartyApplicableForFiltering(barristerFilter,
                                             partyDetails,
                                             isApplicable,
                                             partyId -> log.info("Barrister Remove Service - This party {} has an empty solicitor org or "
                                                                     + "the user org identifier is empty", partyId.toString()));

    }

    @Override
    protected String getLabelForAction(boolean applicantOrRespondent, BarristerFilter barristerFilter, PartyDetails partyDetails) {
        String partyDetailsInfo = partyDetails.getBarrister().getBarristerFullName();

        return getLabelForAction(applicantOrRespondent, partyDetails, partyDetailsInfo);
    }

    @Override
    protected String getCodeForAction(Element<PartyDetails> partyDetailsElement) {
        return partyDetailsElement.getId().toString();
    }
}
