package uk.gov.hmcts.reform.prl.services.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.exception.CoreCaseDataStoreException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenCoreCaseDataService {

    private static final String LINK_CASE_TO_CITIZEN_SUMMARY = "Link case to Citizen account";
    private static final String LINK_CASE_TO_CITIZEN_DESCRIPTION = "Link case to Citizen account with access code";

    private static final String CCD_UPDATE_FAILURE_MESSAGE
        = "Failed linking case in CCD store for case id %s on event %s";

    @Autowired
    IdamClient idamClient;
    @Autowired
    CoreCaseDataApi coreCaseDataApi;
    @Autowired
    UserService userService;

    public CaseDetails updateCaseData(
        String authToken,
        String s2sToken,
        Long caseId,
        CaseData caseData,
        CaseEvent caseEvent
    ) {
        log.info("Inside CitizenCoreCaseDataService::updateCaseData");
        log.info("CaseEvent "  + caseEvent);
        boolean isRepresented = true;
        try {
            UserDetails userDetails = idamClient.getUserDetails(authToken);
            EventRequestData eventRequestData = eventRequest(caseEvent, userDetails.getId());

            //Added for citizen case update
            if (userService.getUserDetails(authToken).getRoles().contains("citizen")) {
                isRepresented = false;
            }
            log.info("isRepresented " + isRepresented);
            log.info("isRepresented " + eventRequestData);
            StartEventResponse startEventResponse = startUpdate(
                authToken,
                s2sToken,
                eventRequestData,
                caseId,
                isRepresented
            );

            CaseDataContent caseDataContent = caseDataContent(startEventResponse, caseData);
            return submitUpdate(
                authToken,
                s2sToken,
                eventRequestData,
                caseDataContent,
                caseId,
                isRepresented
            );
        } catch (Exception exception) {
            throw new CoreCaseDataStoreException(
                String.format(
                    CCD_UPDATE_FAILURE_MESSAGE,
                    caseId,
                    caseEvent
                ), exception
            );
        }
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, Object content) {
        log.info("Inside CitizenCoreCaseDataService::caseDataContent");
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .summary(LINK_CASE_TO_CITIZEN_SUMMARY)
                       .description(LINK_CASE_TO_CITIZEN_DESCRIPTION)
                       .build())
            .data(content)
            .build();
    }


    private EventRequestData eventRequest(CaseEvent caseEvent, String userId) {
        return EventRequestData.builder()
            .userId(userId)
            .jurisdictionId(PrlAppsConstants.JURISDICTION)
            .caseTypeId(PrlAppsConstants.CASE_TYPE)
            .eventId(caseEvent.getValue())
            .ignoreWarning(true)
            .build();
    }

    private StartEventResponse startUpdate(
        String authorisation,
        String s2sToken,
        EventRequestData eventRequestData,
        Long caseId,
        boolean isRepresented
    ) {
        log.info("Inside CitizenCoreCaseDataService::startUpdate");
        if (isRepresented) {
            return coreCaseDataApi.startEventForCaseWorker(
                authorisation,
                s2sToken,
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                caseId.toString(),
                eventRequestData.getEventId()
            );
        } else {
            return coreCaseDataApi.startEventForCitizen(
                authorisation,
                s2sToken,
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                caseId.toString(),
                eventRequestData.getEventId()
            );
        }
    }

    private CaseDetails submitUpdate(
        String authorisation,
        String s2sToken,
        EventRequestData eventRequestData,
        CaseDataContent caseDataContent,
        Long caseId,
        boolean isRepresented
    ) {
        log.info("Inside CitizenCoreCaseDataService::submitUpdate");
        if (isRepresented) {
            return coreCaseDataApi.submitEventForCaseWorker(
                authorisation,
                s2sToken,
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                caseId.toString(),
                eventRequestData.isIgnoreWarning(),
                caseDataContent
            );
        } else {
            return coreCaseDataApi.submitEventForCitizen(
                authorisation,
                s2sToken,
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                caseId.toString(),
                eventRequestData.isIgnoreWarning(),
                caseDataContent
            );
        }
    }
}
