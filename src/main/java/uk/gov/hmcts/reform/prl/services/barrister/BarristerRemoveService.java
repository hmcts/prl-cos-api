package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.OrganisationService;

import static uk.gov.hmcts.reform.prl.enums.PartyEnum.applicant;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.respondent;

@Slf4j
@Service
public class BarristerRemoveService extends  AbstractBarristerService {
    protected BarristerRemoveService(OrganisationService organisationService) {
        super(organisationService);
    }

    public DynamicList getBarristerListToRemove(CaseData caseData, UserDetails userDetails, String authorisation) {
        return getSolicitorPartyDynamicList(caseData, userDetails, authorisation);
    }

    @Override
    protected boolean isPartyApplicable(boolean isApplicant, PartyDetails partyDetails) {
        return hasBarrister(partyDetails) && partyDetails.getBarrister().getBarristerId() != null;
    }

    @Override
    protected String getLabelForAction(boolean isApplicant, PartyDetails partyDetails) {
        return String.format("%s (%s), %s, %s", partyDetails.getLabelForDynamicList(),
                             isApplicant ? applicant.getDisplayedValue() : respondent.getDisplayedValue(),
                             partyDetails.getRepresentativeFullName(),
                             partyDetails.getBarrister().getBarristerFullName()
        );
    }

    @Override
    protected String getCodeForAction(Element<PartyDetails> partyDetails) {
        return partyDetails.getValue().getBarrister().getBarristerId();
    }
}
