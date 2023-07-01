package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RestrictedCaseAccessService {
    private final AuthorisationService authorisationService;
    private final IdamClient idamClient;
    private final CcdCoreCaseDataService coreCaseDataService;

    public void markAsRestricted(CallbackRequest callbackRequest, String authorisation) {
        if (isAuthorized(authorisation)) {
            processRestrictedCaseAccessCallback(callbackRequest.getCaseDetails().getId(), authorisation);
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private boolean isAuthorized(String authorisation) {
        return Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation));
    }


    public void processRestrictedCaseAccessCallback(long caseId, String authorisation) {
        EventRequestData eventRequestData = coreCaseDataService.eventRequest(
            CaseEvent.MARK_CASE_AS_RESTRICTED,
            idamClient.getUserInfo(authorisation).getUid()
        );
        StartEventResponse startEventResponse =
            coreCaseDataService.startUpdate(
                authorisation,
                eventRequestData,
                String.valueOf(caseId),
                true
            );

        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
            startEventResponse);

        coreCaseDataService.submitUpdate(
            authorisation,
            eventRequestData,
            caseDataContent,
            String.valueOf(caseId),
            true
        );
    }
}
