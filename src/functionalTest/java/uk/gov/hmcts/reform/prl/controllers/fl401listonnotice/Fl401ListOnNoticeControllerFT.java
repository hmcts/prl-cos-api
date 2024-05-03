package uk.gov.hmcts.reform.prl.controllers.fl401listonnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.services.fl401listonnotice.Fl401ListOnNoticeService.CONFIRMATION_HEADER;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class Fl401ListOnNoticeControllerFT {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    ObjectMapper objectMapper;

    private static final String LIST_ON_NOTICE_VALID_REQUEST_BODY = "requests/call-back-controller-fl401-gatekeeping-case-data.json";

    private final String aboutToStartEndpoint = "/pre-populate-screen-and-hearing-data";

    private final String aboutToSubmitEndpoint = "/fl401-list-on-notice/about-to-submit";

    private final String submittedEndpoint = "/fl401-send-listOnNotice-notification";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }


    /* commented below test case since it was failing in the master pipeline
    * will be fixed as part of FPET-959 */
    @Test
    public void testSubmittedEvent() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_VALID_REQUEST_BODY);
        MvcResult res = mockMvc.perform(post(submittedEndpoint)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForJudge())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(requestBody)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String json = res.getResponse().getContentAsString();
        assertTrue(json.contains("confirmation_header"));
        assertTrue(json.contains(CONFIRMATION_HEADER));
    }

    @Test
    public void testAboutToSubmitEvent() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_VALID_REQUEST_BODY);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(aboutToSubmitEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res);
        Assert.assertNotNull(res.getData().get(CASE_NOTES));
    }

    @Test
    public void testAboutToStartEvent() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_VALID_REQUEST_BODY);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(aboutToStartEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res);
        Assert.assertNotNull(res.getData().get(FL401_CASE_WITHOUT_NOTICE));
        Assert.assertEquals("No", res.getData().get(FL401_CASE_WITHOUT_NOTICE));
    }
}
