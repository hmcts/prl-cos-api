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
import uk.gov.hmcts.reform.prl.clients.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.exception.CoreCaseDataStoreException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

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

    private static final String LINK_CASE_TO_CITIZEN_SUMMARY = "Link case to Citizen account";
    private static final String LINK_CASE_TO_CITIZEN_DESCRIPTION = "Link case to Citizen account with access code";

    private static final String CCD_UPDATE_FAILURE_MESSAGE
        = "Failed linking case in CCD store for case id %s on event %s";

    private final SystemUserService systemUserService;

    private final CoreCaseDataService coreCaseDataService;

    @Autowired
    IdamClient idamClient;
    @Autowired
    CoreCaseDataApi coreCaseDataApi;
    @Autowired
    AuthTokenGenerator authTokenGenerator;
    @Autowired
    ObjectMapper objectMapper;

    public CaseDetails linkDefendant(
        String anonymousUserToken,
        Long caseId,
        CaseData caseData,
        CaseEvent caseEvent
    ) {
        try {
            String userToken = systemUserService.getSysUserToken();
            String systemUpdateUserId = systemUserService.getUserId(userToken);

            EventRequestData eventRequestData = coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId);

            //TODO CONFIRM IF THIS IS CITIZEN
            StartEventResponse startEventResponse =
                coreCaseDataService.startUpdate(
                    userToken,
                    eventRequestData,
                    String.valueOf(caseId),
                    true
                );

            CaseData caseDataUpdated = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);
            CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContent(
                startEventResponse,
                caseDataUpdated
            );

            return coreCaseDataService.submitUpdate(
                userToken,
                eventRequestData,
                caseDataContent,
                String.valueOf(caseId),
                true
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
            String userToken = systemUserService.getSysUserToken();
            String systemUpdateUserId = systemUserService.getUserId(userToken);

            EventRequestData eventRequestData = coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId);

            //TODO CONFIRM IF THIS IS CITIZEN
            StartEventResponse startEventResponse =
                coreCaseDataService.startUpdate(
                    userToken,
                    eventRequestData,
                    String.valueOf(caseId),
                    true
                );

            CaseData caseDataUpdated = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);
            CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContent(
                startEventResponse,
                caseDataUpdated
            );

            return coreCaseDataService.submitUpdate(
                userToken,
                eventRequestData,
                caseDataContent,
                String.valueOf(caseId),
                true
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
        EventRequestData eventRequestData = eventRequest(CITIZEN_CASE_CREATE,
            userDetails.getId()
        );

        if (userDetails.getRoles().contains(CITIZEN_ROLE)) {
            caseData.setCaseCreatedBy(CITIZEN);
        }
        StartEventResponse startEventResponse = startSubmitCreate(
            authorisation,
            cosApis2sToken,
            userDetails.getId(),
            eventRequestData,
            !userDetails.getRoles().contains(CITIZEN_ROLE)
        );

        Map<String, Object> caseDataMap = caseData.toMap(objectMapper);
        Iterables.removeIf(caseDataMap.values(), Objects::isNull);
        CaseDataContent caseDataContent = caseDataContent(startEventResponse, caseDataMap);

        return submitCreate(
            authorisation,
            cosApis2sToken,
            userDetails.getId(),
            caseDataContent,
            !userDetails.getRoles().contains(CITIZEN_ROLE)
        );
    }

    private StartEventResponse startSubmitCreate(
        String authorisation,
        String s2sToken,
        String userId,
        EventRequestData eventRequestData,
        boolean isRepresented
    ) {
        if (isRepresented) {
            return coreCaseDataApi.startForCaseworker(
                authorisation,
                s2sToken,
                userId,
                JURISDICTION,
                CASE_TYPE,
                eventRequestData.getEventId()
            );
        } else {
            return coreCaseDataApi.startForCitizen(
                authorisation,
                s2sToken,
                userId,
                JURISDICTION,
                CASE_TYPE,
                eventRequestData.getEventId()
            );
        }
    }

    private CaseDetails submitCreate(
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

    public CaseDetails getCase(String authorisation, String caseId) {
        String cosApis2sToken = authTokenGenerator.generate();
        return coreCaseDataApi.getCase(authorisation, cosApis2sToken, caseId);
    }
}
