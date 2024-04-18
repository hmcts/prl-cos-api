package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.exception.CoreCaseDataStoreException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.enums.CaseCreatedBy.CITIZEN;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_CREATE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenCoreCaseDataService {
    private static final String CCD_UPDATE_FAILURE_MESSAGE
        = "Failed linking case in CCD store for case id %s on event %s";
    private final IdamClient idamClient;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final CcdCoreCaseDataService ccdCoreCaseDataService;

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, Object content) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(content)
            .build();
    }


    private EventRequestData eventRequest(CaseEvent caseEvent, String userId) {
        return EventRequestData.builder()
            .userId(userId)
            .jurisdictionId(JURISDICTION)
            .caseTypeId(CASE_TYPE)
            .eventId(caseEvent.getValue())
            .ignoreWarning(true)
            .build();
    }

    public CaseDetails updateCase(
        String authorisation,
        Long caseId,
        CaseData caseData,
        CaseEvent caseEvent
    ) {
        try {
            UserDetails userDetails = idamClient.getUserDetails(authorisation);
            EventRequestData eventRequestData = eventRequest(caseEvent, userDetails.getId());
            StartEventResponse startEventResponse = ccdCoreCaseDataService.startUpdate(
                authorisation,
                eventRequestData,
                caseId.toString(),
                !userDetails.getRoles().contains(CITIZEN_ROLE)
            );
            Map<String, Object> caseDataMap = caseData.toMap(objectMapper);
            Iterables.removeIf(caseDataMap.values(), Objects::isNull);
            CaseDataContent caseDataContent = caseDataContent(startEventResponse, caseDataMap);
            return ccdCoreCaseDataService.submitUpdate(
                authorisation,
                eventRequestData,
                caseDataContent,
                String.valueOf(caseId),
                !userDetails.getRoles().contains(CITIZEN_ROLE)
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

    public CaseDetails createCase(String authorisation, CaseData caseData) {
        String cosApis2sToken = authTokenGenerator.generate();
        UserDetails userDetails = idamClient.getUserDetails(authorisation);

        // We can Add a Caseworker Event as well in future depending on the Role from userdetails
        EventRequestData eventRequestData = eventRequest(
            CITIZEN_CASE_CREATE,
            userDetails.getId()
        );

        if (userDetails.getRoles().contains(CITIZEN_ROLE)) {
            caseData.setCaseCreatedBy(CITIZEN);
        }
        StartEventResponse startEventResponse = ccdCoreCaseDataService.startSubmitCreate(
            authorisation,
            cosApis2sToken,
            eventRequestData,
            !userDetails.getRoles().contains(CITIZEN_ROLE)
        );

        Map<String, Object> caseDataMap = caseData.toMap(objectMapper);
        Iterables.removeIf(caseDataMap.values(), Objects::isNull);
        CaseDataContent caseDataContent = caseDataContent(startEventResponse, caseDataMap);

        return ccdCoreCaseDataService.submitCreate(
            authorisation,
            cosApis2sToken,
            userDetails.getId(),
            caseDataContent,
            !userDetails.getRoles().contains(CITIZEN_ROLE)
        );
    }

    public CaseDetails getCase(String authorisation, String caseId) {
        String cosApis2sToken = authTokenGenerator.generate();
        return coreCaseDataApi.getCase(authorisation, cosApis2sToken, caseId);
    }
}
