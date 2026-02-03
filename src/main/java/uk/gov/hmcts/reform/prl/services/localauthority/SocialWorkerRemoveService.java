package uk.gov.hmcts.reform.prl.services.localauthority;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.localauthority.TypeOfSocialWorkerEventEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.BarristerHelper;

@Slf4j
@Service
public class SocialWorkerRemoveService extends AbstractSocialWorkerService {

    public SocialWorkerRemoveService(UserService userService,
                                     OrganisationService organisationService,
                                     EventService eventPublisher,
                                     BarristerHelper barristerHelper) {
        super(userService, organisationService, eventPublisher, barristerHelper);
    }

    @Override
    public void notifySocialWorker(CaseData caseData) {
        prepareAndPublishSocialWorkerChangeEvent(caseData, TypeOfSocialWorkerEventEnum.removeSocialWorker);
    }
}
