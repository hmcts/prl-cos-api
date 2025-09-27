package uk.gov.hmcts.reform.prl.controllers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.config.FeesConfig;
import uk.gov.hmcts.reform.prl.tasks.ScheduledTaskRunner;

@TestConfiguration
public class ControllerTestSupport {

    @MockBean
    private Application application;

    @MockBean
    private ScheduledTaskRunner scheduledTaskRunner;

    @MockBean
    private CaseDocumentClient caseDocumentClient;

    @MockBean
    private FeesConfig feesConfig;
}
