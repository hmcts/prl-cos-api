package uk.gov.hmcts.reform.prl.controllers.listwithoutnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static uk.gov.hmcts.reform.prl.controllers.listwithoutnotice.ListWithoutNoticeController.CONFIRMATION_BODY_PREFIX_CA;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ListWithoutNoticeControllerFT {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String LIST_WITHOUT_NOTICE_VALID_REQUEST_BODY = "requests/listwithoutnotice/ListWithoutNotice1.json";

    private static final String dateConfirmedInHearingsTab = "requests/listwithoutnotice/ListWithoutNoticeWithdateConfirmedInHearingsTab.json";

    private static final String LIST_WITHOUT_NOTICE_VALID_REQUEST_BODY2 = "requests/listwithoutnotice/PrePopulateHearingWithExstingRequest.json";
    private static final String dateReservedWithListAssit = "requests/listwithoutnotice/dateReservedWithListAssit.json";

    private static final String dateConfirmedByListingTeam = "requests/listwithoutnotice/dateConfirmedByListingTeam.json";
    private static final String dateToBeFixed = "requests/listwithoutnotice/dateToBeFixed.json";

    private final String prePopulateHearingPageEndpoint = "/pre-populate-hearingPage-Data";

    private static final String LIST_WITHOUT_NOTICE_CA_VALID_REQUEST_BODY = "requests/listwithoutnotice/ListWithoutNoticeCa.json";

    private final String listWithoutNoticeEndpoint = "/listWithoutNotice";

    private final String listWithoutNoticeConfirmationEndpoint = "/listWithoutNotice-confirmation";

    private final String c100ListWithoutNoticeEndpoint = "/ca-listWithoutNotice";

    private final String c100ListWithoutNoticeConfirmationEndpoint = "/ca-listWithoutNotice-confirmation";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    @Ignore
    public void testListWithoutNotice_200ResponseAndNoErrors() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_WITHOUT_NOTICE_VALID_REQUEST_BODY);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(listWithoutNoticeEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res.getData());
        Assert.assertTrue(res.getData().containsKey("listWithoutNoticeHearingDetails"));
    }

    @Test
    @Ignore
    public void testDateConfirmedInHearingsTab_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(dateConfirmedInHearingsTab);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(listWithoutNoticeEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertTrue(res.getData().containsKey("listWithoutNoticeHearingDetails"));
    }

    @Test
    public void testPrePopulateHearingPageWithExstingData1_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(LIST_WITHOUT_NOTICE_VALID_REQUEST_BODY2);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(prePopulateHearingPageEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(
            response.getBody().asString(),
            AboutToStartOrSubmitCallbackResponse.class
        );
        Assert.assertNotNull(res.getData());
        Assert.assertTrue(res.getData().containsKey("listWithoutNoticeHearingDetails"));
    }

    @Test
    @Ignore
    public void testDateReservedWithListAssit_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(dateReservedWithListAssit);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(prePopulateHearingPageEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(
            response.getBody().asString(),
            AboutToStartOrSubmitCallbackResponse.class
        );
        Assert.assertNotNull(res.getData());
        Assert.assertTrue(res.getData().containsKey("listWithoutNoticeHearingDetails"));
    }


    @Test
    public void testDateConfirmedByListingTeam_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(dateConfirmedByListingTeam);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(prePopulateHearingPageEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(
            response.getBody().asString(),
            AboutToStartOrSubmitCallbackResponse.class
        );
        Assert.assertNotNull(res.getData());
        Assert.assertTrue(res.getData().containsKey("listWithoutNoticeHearingDetails"));
    }

    @Test
    @Ignore
    public void testdateToBeFixed_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(dateToBeFixed);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(prePopulateHearingPageEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(
            response.getBody().asString(),
            AboutToStartOrSubmitCallbackResponse.class
        );
        Assert.assertNotNull(res.getData());
        Assert.assertTrue(res.getData().containsKey("listWithoutNoticeHearingDetails"));
    }

    @Test
    public void testListWithoutNotice_200ResponseAndNoErrors_CA() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_WITHOUT_NOTICE_CA_VALID_REQUEST_BODY);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(c100ListWithoutNoticeEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res.getData());
        Assert.assertTrue(res.getData().containsKey("caseNotes"));
    }

    @Test
    public void testListWithoutNoticeConfirmationEndpoint_200ResponseAndNoErrors() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_WITHOUT_NOTICE_CA_VALID_REQUEST_BODY);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(c100ListWithoutNoticeConfirmationEndpoint);
        response.then().assertThat().statusCode(200);
        SubmittedCallbackResponse res = objectMapper.readValue(
            response.getBody().asString(),
            SubmittedCallbackResponse.class
        );
        Assert.assertEquals(res.getConfirmationBody(),CONFIRMATION_BODY_PREFIX_CA);
    }
}
