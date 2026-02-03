package uk.gov.hmcts.reform.prl.services.localauthority;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.localauthority.TypeOfSocialWorkerEventEnum;
import uk.gov.hmcts.reform.prl.events.SocialWorkerChangeEvent;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.BarristerHelper;

import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;


@AllArgsConstructor
public abstract class AbstractSocialWorkerService {
    protected static final String APPLICANT = "Applicant";
    protected static final String RESPONDENT = "Respondent";
    protected final UserService userService;
    protected final OrganisationService organisationService;
    protected final EventService eventPublisher;
    protected final BarristerHelper barristerHelper;

    protected void prepareAndPublishSocialWorkerChangeEvent(CaseData caseData,
                                                            TypeOfSocialWorkerEventEnum typeOfEvent) {
        if (caseData.getAllocatedBarrister() != null) {
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

    private boolean isC100CaseType(CaseData caseData) {
        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            return false;
        } else if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return true;
        } else {
            throw new RuntimeException("Invalid case type detected for case " + caseData.getId());
        }
    }

    public abstract void notifySocialWorker(CaseData caseData);
}
