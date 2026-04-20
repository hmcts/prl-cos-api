package uk.gov.hmcts.reform.prl.controllers.testingsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestingSupportCreateCaseDataController {

    private final ObjectMapper objectMapper;

    private final IdamClient idamClient;

    private final CcdCoreCaseDataService coreCaseDataService;

    private final AuthTokenGenerator authTokenGenerator;

    @PostMapping(path = "/testing-support/create-ccd-case-data",
        consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to create test ccd case")
    public CaseDetails createCcdTestCase(@RequestHeader(HttpHeaders.AUTHORIZATION)
                                                                      @Parameter(hidden = true) String authorisation,
                                                                  @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
                                                                  @RequestBody CallbackRequest callbackRequest) {
        String userId = null;
        String caseType = null;
        String eventId = null;

        try {
            log.info("createCcdTestCase: Starting test case creation");

            // Get user info
            userId = idamClient.getUserInfo(authorisation).getUid();
            log.info("createCcdTestCase: userId={}", userId);

            // Build case data
            caseType = PrlAppsConstants.C100_CASE_TYPE;
            CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails().toBuilder()
                                                          .id(0L)
                                                          .caseTypeId(caseType)
                                                          .state(State.JUDICIAL_REVIEW.getValue())
                                                          .createdDate(LocalDateTime.now())
                                                          .jurisdiction("PRIVATELAW")
                                                          .lastModified(LocalDateTime.now())
                                                          .build(), objectMapper);

            log.info("createCcdTestCase: caseType={}, applicantCaseName={}",
                caseType, caseData.getApplicantCaseName());

            Map<String, Object> caseDataMap = caseData.toMap(CcdObjectMapper.getObjectMapper());

            // Start event
            EventRequestData eventRequestData = coreCaseDataService.eventRequest(
                CaseEvent.TS_ADMIN_APPLICATION_NOC,
                userId
            );
            eventId = eventRequestData.getEventId();
            log.info("createCcdTestCase: Starting event eventId={}", eventId);

            StartEventResponse startEventResponse = coreCaseDataService.startSubmitCreate(
                authorisation,
                authTokenGenerator.generate(),
                eventRequestData,
                true
            );
            log.info("createCcdTestCase: Event started, token received");

            // Submit
            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                           .id(startEventResponse.getEventId())
                           .build())
                .data(caseDataMap).build();

            log.info("createCcdTestCase: Submitting case creation");
            CaseDetails result = coreCaseDataService.submitCreate(
                authorisation,
                authTokenGenerator.generate(),
                userId,
                caseDataContent,
                true
            );

            log.info("createCcdTestCase: SUCCESS - caseId={}", result.getId());
            return result;

        } catch (Exception e) {
            log.error("createCcdTestCase: FAILED - userId={}, caseType={}, eventId={}, error={}",
                userId, caseType, eventId, e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                String.format("Failed to create test case: %s (userId=%s, eventId=%s)",
                    e.getMessage(), userId, eventId),
                e
            );
        }
    }

}
