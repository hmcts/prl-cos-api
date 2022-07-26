package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_PRL_CREATE_EVENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;


@Slf4j
@Service
public class CaseService {

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    IdamClient idamClient;

    @Autowired
    ObjectMapper objectMapper;

    public CaseDetails updateCase(CaseData caseData, String authToken, String s2sToken, String caseId, String eventId) {

        UserDetails userDetails = idamClient.getUserDetails(authToken);

        return updateCaseDetails(caseData, authToken, s2sToken, caseId, eventId, userDetails);

    }

    private CaseDetails updateCaseDetails(CaseData caseData, String authToken, String s2sToken, String caseId,
                                          String eventId, UserDetails userDetails) {
        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            authToken,
            s2sToken,
            userDetails.getId(),
            JURISDICTION,
            CASE_TYPE,
            caseId,
            eventId
        );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(caseData)
            .build();

        return coreCaseDataApi.submitEventForCaseWorker(
            authToken,
            s2sToken,
            userDetails.getId(),
            JURISDICTION,
            CASE_TYPE,
            caseId,
            true,
            caseDataContent
        );
    }

    public CaseDetails createCase(CaseData caseData, String authToken, String s2sToken) {
        UserDetails userDetails = idamClient.getUserDetails(authToken);

        return createCase(caseData, authToken, s2sToken, userDetails);
    }

    private CaseDetails createCase(CaseData caseData, String authToken, String s2sToken, UserDetails userDetails) {
        return coreCaseDataApi.submitForCitizen(
                authToken,
                s2sToken,
                userDetails.getId(),
                JURISDICTION,
                CASE_TYPE,
                true,
                getCaseDataContent(authToken, s2sToken, caseData, userDetails.getId())
        );
    }

    private CaseDataContent getCaseDataContent(String authorization, String s2sToken, CaseData caseData, String userId) {
        Map<String, Object> caseDataMap = caseData.toMap(objectMapper);
        Iterables.removeIf(caseDataMap.values(), Objects::isNull);
        return CaseDataContent.builder()
                .data(caseDataMap)
                .event(Event.builder().id(CITIZEN_PRL_CREATE_EVENT).build())
                .eventToken(getEventToken(authorization, s2sToken, userId, CITIZEN_PRL_CREATE_EVENT))
                .build();
    }

    public String getEventToken(String authorization, String s2sToken, String userId, String eventId) {
        StartEventResponse res = coreCaseDataApi.startForCitizen(authorization,
                s2sToken,
                userId,
                JURISDICTION,
                CASE_TYPE,
                eventId);

        return nonNull(res) ? res.getToken() : null;
    }
}
