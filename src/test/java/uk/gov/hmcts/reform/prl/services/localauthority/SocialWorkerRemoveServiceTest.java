package uk.gov.hmcts.reform.prl.services.localauthority;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.events.SocialWorkerChangeEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.localauthority.LocalAuthoritySocialWorker;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(SpringExtension.class)
class SocialWorkerRemoveServiceTest {
    @InjectMocks
    SocialWorkerRemoveService socialWorkerRemoveService;
    @Mock
    protected UserService userService;
    @Mock
    protected OrganisationService organisationService;
    @Mock
    protected EventService eventPublisher;

    @Test
    void shouldNotifySocialWorkerSuccessfully() {
        CaseData caseData = CaseData.builder()
            .localAuthoritySocialWorker(LocalAuthoritySocialWorker.builder().build())
            .build();

        socialWorkerRemoveService.notifySocialWorker(caseData);

        verify(eventPublisher).publishEvent(isA(SocialWorkerChangeEvent.class));

    }

    @Test
    void shouldNotNotifySocialWorkerWhenSocialWorkerIsNull() {
        CaseData caseData = CaseData.builder().build();

        socialWorkerRemoveService.notifySocialWorker(caseData);

        verifyNoInteractions(eventPublisher);

    }
}
