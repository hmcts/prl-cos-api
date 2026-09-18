package uk.gov.hmcts.reform.prl.services.citizen;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.function.Consumer;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class CitizenUserCaseUpdateService {

    private final AllTabServiceImpl allTabService;
    private final CitizenCoreCaseDataService citizenCoreCaseDataService;

    public void validateCitizenCaseAccess(String authorisation,
                                          String caseId) {
        if (!citizenCoreCaseDataService.hasCitizenAccess(authorisation, caseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public CaseDetails updateCaseUsingCitizenUserAuth(String authorisation,
                                                      String caseId,
                                                      CaseEvent caseEvent,
                                                      Consumer<StartAllTabsUpdateDataContent> updater) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = startCitizenUserEvent(
            authorisation,
            caseId,
            caseEvent
        );

        updater.accept(startAllTabsUpdateDataContent);

        return submitCitizenUserEvent(caseId, startAllTabsUpdateDataContent);
    }

    public <T> T updateCaseUsingCitizenUserAuthAndReturn(String authorisation,
                                                         String caseId,
                                                         CaseEvent caseEvent,
                                                         Function<StartAllTabsUpdateDataContent, T> updater) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = startCitizenUserEvent(
            authorisation,
            caseId,
            caseEvent
        );

        T result = updater.apply(startAllTabsUpdateDataContent);

        submitCitizenUserEvent(caseId, startAllTabsUpdateDataContent);

        return result;
    }

    private StartAllTabsUpdateDataContent startCitizenUserEvent(String authorisation,
                                                               String caseId,
                                                               CaseEvent caseEvent) {

        return allTabService.getStartUpdateForSpecificUserEvent(
            caseId,
            caseEvent.getValue(),
            authorisation
        );
    }

    private CaseDetails submitCitizenUserEvent(String caseId,
                                               StartAllTabsUpdateDataContent startAllTabsUpdateDataContent) {
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
