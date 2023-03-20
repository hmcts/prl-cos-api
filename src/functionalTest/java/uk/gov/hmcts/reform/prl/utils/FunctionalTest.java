package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@Slf4j
@TestPropertySource("classpath:application.yaml")
public abstract class FunctionalTest {

    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 04, 28, 1, 0);
    private static final String SOLICITOR_CREATE = "solicitorCreate";

    @Value("${test-url}")
    protected String testUrl = "http://localhost:4044";

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected CoreCaseDataApi coreCaseDataApi;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    protected ObjectMapper objectMapper;


    protected CaseDetails createCaseInCcd() {
        String solicitorToken = idamTokenGenerator.generateIdamTokenForSolicitor();
        String s2sTokenForCaseApi = serviceAuthenticationGenerator.generate("nfdiv_case_api");
        String solicitorUserId = idamTokenGenerator.getUserDetailsFor(solicitorToken).getId();
        StartEventResponse startEventResponse = startEventForCreateCase(
            solicitorToken,
            s2sTokenForCaseApi,
            solicitorUserId
        );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(SOLICITOR_CREATE)
                       .summary("Create draft case")
                       .description("Create draft case for functional tests")
                       .build())
            .data(Map.of(
                "applicant1SolicitorName", "functional test",
                "applicant1LanguagePreferenceWelsh", "NO"
            ))
            .build();

        return submitNewCase(caseDataContent, solicitorToken, s2sTokenForCaseApi, solicitorUserId);
    }

    private StartEventResponse startEventForCreateCase(
        String solicitorToken,
        String s2sToken,
        String solicitorUserId
    ) {
        // not including in try catch to fail fast the method
        return coreCaseDataApi.startForCaseworker(
            solicitorToken,
            s2sToken,
            solicitorUserId,
            JURISDICTION,
            CASE_TYPE,
            SOLICITOR_CREATE
        );
    }

    private CaseDetails submitNewCase(
        CaseDataContent caseDataContent,
        String solicitorToken,
        String s2sToken,
        String solicitorUserId
    ) {
        // not including in try catch to fast fail the method
        return coreCaseDataApi.submitForCaseworker(
            solicitorToken,
            s2sToken,
            solicitorUserId,
            JURISDICTION,
            CASE_TYPE,
            true,
            caseDataContent
        );
    }

    protected Response triggerCallback(Map<String, Object> caseData, String eventId, String url) throws IOException {
        CallbackRequest request = CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetailsBefore(
                CaseDetails
                    .builder()
                    .id(1234567890123456L)
                    .data(caseData)
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(CASE_TYPE)
                    .build())
            .caseDetails(
                CaseDetails
                    .builder()
                    .id(1234567890123456L)
                    .data(caseData)
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(CASE_TYPE)
                    .build()
            )
            .build();

        return triggerCallback(request, url);
    }


    protected Response triggerCallback(CallbackRequest request, String url) throws IOException {
        return RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(testUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
            .header(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(request)
            .when()
            .post(url);
    }


    protected CaseData getCaseData(Map<String, Object> data) {
        return objectMapper.convertValue(data, CaseData.class);
    }
}
