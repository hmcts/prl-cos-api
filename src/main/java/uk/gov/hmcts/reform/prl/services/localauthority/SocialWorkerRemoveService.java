package uk.gov.hmcts.reform.prl.services.localauthority;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.localauthority.TypeOfSocialWorkerEventEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;

@Slf4j
@Service
public class SocialWorkerRemoveService extends AbstractSocialWorkerService {

    public SocialWorkerRemoveService(OrganisationService organisationService,
                                     EventService eventPublisher) {
        super(organisationService, eventPublisher);
    }

    @Override
    public void notifySocialWorker(CaseData caseData) {
        prepareAndPublishSocialWorkerChangeEvent(caseData, TypeOfSocialWorkerEventEnum.removeSocialWorker);
    }
}
