package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;

import static uk.gov.hmcts.reform.prl.enums.PartyEnum.applicant;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.respondent;

@Slf4j
@Service
public class BarristerRemoveService extends  AbstractBarristerService {

    public BarristerRemoveService(UserService userService, OrganisationService organisationService) {
        super(userService, organisationService);
    }

    public DynamicList getBarristerListToRemove(CaseData caseData, String authorisation) {
        return getPartiesToList(caseData, authorisation);
    }

    @Override
    protected boolean isPartyApplicableForFiltering(boolean applicantOrRespondent, BarristerFilter barristerFilter, PartyDetails partyDetails) {
        return hasBarrister(partyDetails) && partyDetails.getBarrister().getBarristerId() != null;
    }

    @Override
    protected String getLabelForAction(boolean applicantOrRespondent, BarristerFilter barristerFilter, PartyDetails partyDetails) {
        return String.format("%s (%s), %s, %s", partyDetails.getLabelForDynamicList(),
                             applicantOrRespondent ? applicant.getDisplayedValue() : respondent.getDisplayedValue(),
                             partyDetails.getRepresentativeFullName(),
                             partyDetails.getBarrister().getBarristerFullName()
        );
    }

    @Override
    protected String getCodeForAction(Element<PartyDetails> partyDetails) {
        return partyDetails.getValue().getBarrister().getBarristerId();
    }
}
