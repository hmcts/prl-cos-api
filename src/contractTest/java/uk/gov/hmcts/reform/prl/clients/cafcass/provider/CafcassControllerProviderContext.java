package uk.gov.hmcts.reform.prl.clients.cafcass.provider;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.cafcass.CaseDataService;

@Configuration
public class CafcassControllerProviderContext {

    @MockBean
    CaseDataService caseDataService;

    @MockBean
    AuthorisationService authService;

    @MockBean
    EventService eventService;
}
