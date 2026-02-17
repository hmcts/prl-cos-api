package uk.gov.hmcts.reform.prl.services.localauthority;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.localauthority.TypeOfSocialWorkerEventEnum;
import uk.gov.hmcts.reform.prl.events.SocialWorkerChangeEvent;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;

import java.util.Optional;


@AllArgsConstructor
public abstract class AbstractSocialWorkerService {

    protected final OrganisationService organisationService;
    protected final EventService eventPublisher;

    protected void prepareAndPublishSocialWorkerChangeEvent(CaseData caseData,
                                                            TypeOfSocialWorkerEventEnum typeOfEvent) {
        if (caseData.getLocalAuthoritySocialWorker() != null) {
            SocialWorkerChangeEvent socialWorkerChangeEvent = SocialWorkerChangeEvent.builder()
                .caseData(caseData)
                .typeOfEvent(typeOfEvent)
                .build();
            eventPublisher.publishEvent(socialWorkerChangeEvent);
        }
    }

    private String getUserOrgId(String usersAuthorisation) {

        Optional<Organisations> usersOrganisation = organisationService.findUserOrganisation(usersAuthorisation);
        if (usersOrganisation.isPresent()) {
            return usersOrganisation.get().getOrganisationIdentifier();
        }
        return null;
    }

    public abstract void notifySocialWorker(CaseData caseData);
}
