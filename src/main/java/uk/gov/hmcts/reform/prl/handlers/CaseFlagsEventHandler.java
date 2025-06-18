package uk.gov.hmcts.reform.prl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.events.CaseFlagsEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsWaService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseFlagsEventHandler {
    private final ObjectMapper objectMapper;
    private final AllTabServiceImpl allTabService;
    private final CaseFlagsWaService caseFlagsWaService;

    @Async
    @EventListener
    public void triggerDummyEventForCaseFlags(final CaseFlagsEvent event) {

        CaseData caseData = CaseUtils.getCaseData(event.callbackRequest().getCaseDetails(), objectMapper);
        if (caseData.getReviewRaRequestWrapper() != null
            && YesOrNo.No.equals(caseData.getReviewRaRequestWrapper().getIsCaseFlagsTaskCreated())
            && !caseFlagsWaService.isCaseHasNoRequestedFlags(caseData)) {
            String caseId = String.valueOf(caseData.getId());
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificEvent(
                caseId,
                CaseEvent.CREATE_WA_TASK_FOR_CTSC_CASE_FLAGS.getValue()
            );
            startAllTabsUpdateDataContent.caseDataMap().put("isCaseFlagsTaskCreated", YesOrNo.Yes);
            allTabService.submitAllTabsUpdate(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                startAllTabsUpdateDataContent.caseDataMap()
            );
        }
    }
}
