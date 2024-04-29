package uk.gov.hmcts.reform.prl.controllers.noticeofchnage;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class NoticeOfChangeControllerFunctionalTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    public static final String AUTHORIZATION = "Authorization";

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    public static final String CASE_ID = "caseId";

    public static final String EVENT_ID = "eventId";

    public static final String updatePartyDetailsEndPoint = "/citizen/{caseId}/{eventId}/update-party-details";

    public static final String saveDraftCitizenApplicationEndPoint = "/citizen/{caseId}/save-c100-draft-application";

    public static final String deleteApplicationCitizenEndPoint = "/citizen/{caseId}/delete-application";

    public static final String withDrawCaseCitizenEndPoint = "/citizen/{caseId}/withdraw";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private static final String CREATE_CASE_WITH_ACCESS_CODE_REQUEST_BODY = "requests/update-case-event-citizen-request.json";

    private static CaseDetails caseDetails1;

    private final RequestSpecification request1 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private final RequestSpecification request2 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        RestAssured.registerParser("text/html", Parser.JSON);
    }


    private static final String VALID_REQUEST_BODY = "requests/call-back-controller.json";

    @Test
    public void givenRequestBody_whenAboutToSubmitNoCRequest_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/noc/aboutToSubmitNoCRequest")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenSubmittedNoCRequest_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/noc/submittedNoCRequest")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

}
