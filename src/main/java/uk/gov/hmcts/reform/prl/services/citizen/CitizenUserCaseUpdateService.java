package uk.gov.hmcts.reform.prl.services.citizen;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class CitizenUserCaseUpdateService {

    private final AllTabServiceImpl allTabService;

    public CaseDetails updateCaseUsingCitizenUserAuth(String authorisation,
                                                      String caseId,
                                                      CaseEvent caseEvent,
                                                      Consumer<StartAllTabsUpdateDataContent> updater) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificUserEvent(
                caseId,
                caseEvent.getValue(),
                authorisation
            );

        updater.accept(startAllTabsUpdateDataContent);

        return allTabService.submitUpdateForSpecificUserEvent(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            startAllTabsUpdateDataContent.caseDataMap(),
            startAllTabsUpdateDataContent.userDetails()
        );
    }
}
