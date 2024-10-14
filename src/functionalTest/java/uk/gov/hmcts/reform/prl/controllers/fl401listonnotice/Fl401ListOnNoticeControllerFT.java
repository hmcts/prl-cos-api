package uk.gov.hmcts.reform.prl.controllers.fl401listonnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.controllers.ManageOrdersControllerFunctionalTest.VALID_CAFCASS_REQUEST_JSON;
import static uk.gov.hmcts.reform.prl.services.fl401listonnotice.Fl401ListOnNoticeService.CONFIRMATION_HEADER;

@Slf4j
@SpringBootTest
@ContextConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

    private static CaseDetails caseDetails;

    @Test
    @Order(1)
    public void createCcdTestCase() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);
        caseDetails =  request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/testing-support/create-ccd-case-data")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(CaseDetails.class);

        Assert.assertNotNull(caseDetails);
        Assert.assertNotNull(caseDetails.getId());
    }


    /* commented below test case since it was failing in the master pipeline
    * will be fixed as part of FPET-959 */
    /*@Test*/
    @Test
    @Order(2)
    public void testSubmittedEvent() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_VALID_REQUEST_BODY);
        String requestBodyRevised = requestBody
            .replace("1713544377136711", caseDetails.getId().toString());

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post(submittedEndpoint);

        String json = response.getBody().asString();
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
