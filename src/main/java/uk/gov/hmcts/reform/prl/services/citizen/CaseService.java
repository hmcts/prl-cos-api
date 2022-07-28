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

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
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
        log.info("Input casedata, applicantcaseName :::: {}", caseData.getApplicantCaseName());
        Map<String, Object> caseDataMap = caseData.toMap(objectMapper);
        Iterables.removeIf(caseDataMap.values(), Objects::isNull);
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
            .data(caseDataMap)
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
}
