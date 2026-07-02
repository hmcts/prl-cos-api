package uk.gov.hmcts.reform.prl.services.taskmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.models.wa.CompletableTaskResponse;
import uk.gov.hmcts.reform.prl.models.wa.SearchEventAndCaseRequest;
import uk.gov.hmcts.reform.prl.services.WaSystemUserService;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskManagementService {

    private final WaSystemUserService waSystemUserService;
    private final TaskManagementClient taskManagementClient;
    private final AuthTokenGenerator authTokenGenerator;

    public boolean hasNoCompletableTasksForHearing(String hearingId, String caseId, String eventId) {
        CompletableTaskResponse completableTaskResponse = getCompletableTasks(caseId, eventId);
        return completableTaskResponse.getTasks().stream().noneMatch(t -> hearingId.equals(t.getAdditionalProperties().getHearingId()));
    }

    private CompletableTaskResponse getCompletableTasks(String caseId, String eventId) {

        SearchEventAndCaseRequest searchEventAndCaseRequest = SearchEventAndCaseRequest.builder()
            .caseJurisdiction(JURISDICTION)
            .caseType(CASE_TYPE)
            .caseId(caseId)
            .eventId(eventId)
            .build();
        return taskManagementClient.searchForCompletable(waSystemUserService.getWaSysUserToken(),
                                                         authTokenGenerator.generate(), searchEventAndCaseRequest);
    }
}
