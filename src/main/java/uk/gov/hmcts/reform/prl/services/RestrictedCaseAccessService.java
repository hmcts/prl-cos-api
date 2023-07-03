package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RestrictedCaseAccessService {
    private final AuthorisationService authorisationService;
    private final SystemUserService systemUserService;
    private final CcdCoreCaseDataService coreCaseDataService;

    public void markAsRestricted(CallbackRequest callbackRequest, String authorisation) {
        if (isAuthorized(authorisation)) {
            processRestrictedCaseAccessCallback(callbackRequest.getCaseDetails().getId());
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    public void markAsRestricted1(CallbackRequest callbackRequest, String authorisation) {
        if (isAuthorized(authorisation)) {
            log.info("authorised");
            processRestrictedCaseAccessCallback1(callbackRequest.getCaseDetails().getId());
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    private boolean isAuthorized(String authorisation) {
        return Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation));
    }

    public void processRestrictedCaseAccessCallback(long caseId) {
        String sysAuthorisation = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(sysAuthorisation);
        log.info("processing callback");
        EventRequestData eventRequestData = coreCaseDataService.eventRequest(
            CaseEvent.UPDATE_ALL_TABS,
            systemUpdateUserId
        );
        StartEventResponse startEventResponse =
            coreCaseDataService.startUpdate(
                sysAuthorisation,
                eventRequestData,
                String.valueOf(caseId),
                true
            );
        log.info("StartEventResponse");
        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
            startEventResponse);
        log.info("caseDataContent");
        coreCaseDataService.submitUpdate(
            sysAuthorisation,
            eventRequestData,
            caseDataContent,
            String.valueOf(caseId),
            true
        );
        log.info("submitUpdate");
    }

    public void processRestrictedCaseAccessCallback1(long caseId) {
        String sysAuthorisation = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(sysAuthorisation);
        log.info("processing callback");
        EventRequestData eventRequestData = coreCaseDataService.eventRequest(
            CaseEvent.UPDATE_ALL_TABS,
            systemUpdateUserId
        );
        StartEventResponse startEventResponse =
            coreCaseDataService.startUpdate(
                sysAuthorisation,
                eventRequestData,
                String.valueOf(caseId),
                true
            );
        log.info("StartEventResponse");
        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification1(
            startEventResponse);
        log.info("caseDataContent");
        coreCaseDataService.submitUpdate(
            sysAuthorisation,
            eventRequestData,
            caseDataContent,
            String.valueOf(caseId),
            true
        );
        log.info("submitUpdate");
    }
}
