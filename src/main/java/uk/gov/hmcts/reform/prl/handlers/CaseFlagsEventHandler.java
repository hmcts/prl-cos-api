package uk.gov.hmcts.reform.prl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.events.CaseFlagsEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsWaService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseFlagsEventHandler {
    private final ObjectMapper objectMapper;
    private final CaseFlagsWaService caseFlagsWaService;

    @Async
    @EventListener
    public void triggerDummyEventForCaseFlags(final CaseFlagsEvent event) {
        CaseData caseDataBefore = CaseUtils.getCaseData(event.callbackRequest().getCaseDetailsBefore(), objectMapper);
        CaseData caseData = CaseUtils.getCaseData(event.callbackRequest().getCaseDetails(), objectMapper);
        caseFlagsWaService.checkCaseFlagsToCreateTask(caseData, caseDataBefore);
    }
}
