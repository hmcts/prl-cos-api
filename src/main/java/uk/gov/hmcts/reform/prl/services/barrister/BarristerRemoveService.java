package uk.gov.hmcts.reform.prl.services.barrister;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.barrister.TypeOfBarristerEventEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;

@Slf4j
@Service
public class BarristerRemoveService extends AbstractBarristerService {

    public BarristerRemoveService(UserService userService,
                                  OrganisationService organisationService,
                                  EventService eventPublisher) {
        super(userService, organisationService, eventPublisher);
    }

    public AllocatedBarrister getBarristerListToRemove(CaseData caseData, String authorisation) {
        return AllocatedBarrister.builder()
            .partyList(getPartiesToList(caseData, authorisation))
            .barristerOrg(Organisation.builder().build())
            .build();
    }

    @Override
    protected boolean isPartyApplicableForFiltering(boolean applicantOrRespondent, BarristerFilter barristerFilter, PartyDetails partyDetails) {
        return hasBarrister(partyDetails) && partyDetails.getBarrister().getBarristerId() != null;
    }

    @Override
    protected String getLabelForAction(boolean applicantOrRespondent, BarristerFilter barristerFilter, PartyDetails partyDetails) {
        return String.format("%s (%s), %s, %s", partyDetails.getLabelForDynamicList(),
                             applicantOrRespondent ? APPLICANT : RESPONDENT,
                             partyDetails.getRepresentativeFullName(),
                             partyDetails.getBarrister().getBarristerFullName()
        );
    }

    @Override
    protected String getCodeForAction(Element<PartyDetails> partyDetailsElement) {
        return partyDetailsElement.getId().toString();
    }

    @Override
    protected void notifyBarrister(AllocatedBarrister allocatedBarrister, CaseData caseData) {
        prepareAndPublishBarristerChangeEvent(allocatedBarrister, caseData, TypeOfBarristerEventEnum.removeBarrister);
    }
}
