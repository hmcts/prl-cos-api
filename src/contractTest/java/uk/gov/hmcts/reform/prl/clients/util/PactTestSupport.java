package uk.gov.hmcts.reform.prl.clients.util;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.config.FeesConfig;
import uk.gov.hmcts.reform.prl.tasks.ScheduledTaskRunner;

@TestConfiguration
public class PactTestSupport {

    @MockBean
    private Application application;

    @MockBean
    private ScheduledTaskRunner scheduledTaskRunner;

    @MockBean
    private FeesConfig feesConfig;

    @MockBean
    private OAuth2Configuration authConfig;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private CaseDocumentClient caseDocumentClient;
}
