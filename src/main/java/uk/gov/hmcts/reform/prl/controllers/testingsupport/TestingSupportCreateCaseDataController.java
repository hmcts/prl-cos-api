package uk.gov.hmcts.reform.prl.controllers.testingsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
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
import uk.gov.hmcts.reform.prl.services.courtnav.CourtNavCaseService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestingSupportCreateCaseDataController {

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final IdamClient idamClient;

    @Autowired
    private final CcdCoreCaseDataService coreCaseDataService;

    @Autowired
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    private final CourtNavCaseService courtNavCaseService;

    @PostMapping(path = "/testing-support/create-ccd-case-data", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to create test ccd case")
    public AboutToStartOrSubmitCallbackResponse createCcdTestCase(@RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
                                                                  @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
                                                                  @RequestBody CallbackRequest callbackRequest) throws  Exception{

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails().toBuilder()
                                                      .id(0L)
                                                      .caseTypeId(PrlAppsConstants.C100_CASE_TYPE)
                                                      .state(State.JUDICIAL_REVIEW.getValue())
                                                      .createdDate(LocalDateTime.now())
                                                      .jurisdiction("PRIVATELAW")
                                                      .lastModified(LocalDateTime.now())
                                                      .build(), objectMapper);
        Map<String, Object> caseDataMap = caseData.toMap(CcdObjectMapper.getObjectMapper());
        EventRequestData eventRequestData = coreCaseDataService.eventRequest(
            CaseEvent.COURTNAV_CASE_CREATION,
            idamClient.getUserInfo(authorisation).getUid()
        );
        StartEventResponse startEventResponse = coreCaseDataService.startSubmitCreate(
            authorisation,
            authTokenGenerator.generate(),
            eventRequestData,
            true
        );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(caseDataMap).build();

        CaseDetails caseDetails =  coreCaseDataService.submitCreate(
            authorisation,
            authTokenGenerator.generate(),
            idamClient.getUserInfo(authorisation).getUid(),
            caseDataContent,
            true
        );
        courtNavCaseService.refreshTabs(authorisation,caseDetails.getData(),caseDetails.getId());
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDetails.getData()).build();
    }

}
