package uk.gov.hmcts.reform.prl.clients.ccd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CcdCoreCaseDataService {
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;

    public StartEventResponse startUpdate(
        String authorisation,
        EventRequestData eventRequestData,
        String caseId,
        boolean isRepresented
    ) {
        if (isRepresented) {
            return coreCaseDataApi.startEventForCaseWorker(
                authorisation,
                authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                caseId,
                eventRequestData.getEventId()
            );
        } else {
            return coreCaseDataApi.startEventForCitizen(
                authorisation,
                authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                caseId,
                eventRequestData.getEventId()
            );
        }
    }

    public CaseDetails submitUpdate(
        String authorisation,
        EventRequestData eventRequestData,
        CaseDataContent caseDataContent,
        String caseId,
        boolean isRepresented
    ) {
        if (isRepresented) {
            return coreCaseDataApi.submitEventForCaseWorker(
                authorisation,
                authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                caseId,
                eventRequestData.isIgnoreWarning(),
                caseDataContent
            );
        } else {
            return coreCaseDataApi.submitEventForCitizen(
                authorisation,
                authTokenGenerator.generate(),
                eventRequestData.getUserId(),
                eventRequestData.getJurisdictionId(),
                eventRequestData.getCaseTypeId(),
                caseId,
                eventRequestData.isIgnoreWarning(),
                caseDataContent
            );
        }
    }

    public EventRequestData eventRequest(CaseEvent caseEvent, String userId) {
        return EventRequestData.builder()
            .userId(userId)
            .jurisdictionId(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .eventId(caseEvent.getValue())
            .ignoreWarning(true)
            .build();
    }

    public CaseDataContent createCaseDataContent(StartEventResponse startEventResponse, Object data) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(data)
            .build();
    }

    public StartEventResponse startSubmitCreate(
        String authorisation,
        String s2sToken,
        EventRequestData eventRequestData,
        boolean isRepresented
    ) {
        if (isRepresented) {
            return coreCaseDataApi.startForCaseworker(
                authorisation,
                s2sToken,
                eventRequestData.getUserId(),
                JURISDICTION,
                CASE_TYPE,
                eventRequestData.getEventId()
            );
        } else {
            return coreCaseDataApi.startForCitizen(
                authorisation,
                s2sToken,
                eventRequestData.getUserId(),
                JURISDICTION,
                CASE_TYPE,
                eventRequestData.getEventId()
            );
        }
    }

    public CaseDetails submitCreate(
        String authorisation,
        String s2sToken,
        String userId,
        CaseDataContent caseDataContent,
        boolean isRepresented
    ) {
        if (isRepresented) {
            return coreCaseDataApi.submitForCaseworker(
                authorisation,
                s2sToken,
                userId,
                JURISDICTION,
                CASE_TYPE,
                isRepresented,
                caseDataContent
            );
        } else {
            return coreCaseDataApi.submitForCitizen(
                authorisation,
                s2sToken,
                userId,
                JURISDICTION,
                CASE_TYPE,
                isRepresented,
                caseDataContent
            );
        }
    }

    public CaseDetails findCaseById(String authorisation, String caseId) {
        String cosApis2sToken = authTokenGenerator.generate();
        return coreCaseDataApi.getCase(authorisation, cosApis2sToken, caseId);
    }

    public CaseDataContent createCaseDataContentOnlyWithSecurityClassification(StartEventResponse startEventResponse) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(startEventResponse.getCaseDetails().getData())
            .securityClassification(Classification.RESTRICTED)
            .build();
    }
}
