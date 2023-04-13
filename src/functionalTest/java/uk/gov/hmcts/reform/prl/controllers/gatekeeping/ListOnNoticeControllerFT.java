package uk.gov.hmcts.reform.prl.controllers.gatekeeping;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.ListOnNoticeReasonsEnum;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SELECTED_AND_ADDITIONAL_REASONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBJECT;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ListOnNoticeControllerFT {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    ObjectMapper objectMapper;

    private static final String LIST_ON_NOTICE_VALID_REQUEST_BODY = "requests/gatekeeping/ListOnNoticeRequest.json";

    private static final String LIST_ON_NOTICE_REQUEST_BODY_WITHOUT_ANY_REASONS_SELECTED =
        "requests/gatekeeping/ListOnNoticeRequestWithoutReasons.json";

    private final String userToken = "Bearer testToken";

    private final String listOnNoticeMidEventEndpoint = "/listOnNotice/reasonUpdation/mid-event";

    private final String listOnNoticeSubmissionEndpoint = "/listOnNotice";

    private final String listOnNoticePrepopulateEndpoint = "/pre-populate-list-on-notice";


    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void testListOnNoticeWhenReasonsSelected_200ResponseAndNoErrorsFromMidEvent() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_VALID_REQUEST_BODY);

        Response response = request
            .header(HttpHeaders.AUTHORIZATION,userToken)
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(listOnNoticeMidEventEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res);
        String reasonsSelectedString = ListOnNoticeReasonsEnum.getDisplayedValue("childrenResideWithApplicantAndBothProtectedByNonMolestationOrder")
            + "\n" + ListOnNoticeReasonsEnum.getDisplayedValue("noEvidenceOnRespondentSeekToFrustrateTheProcessIfTheyWereGivenNotice") + "\n";
        Assert.assertEquals(reasonsSelectedString,res.getData().get(SELECTED_AND_ADDITIONAL_REASONS));
    }

    @Test
    public void testListOnNoticeWhenNoReasonsSelected_200ResponseAndNoErrorsFromMidEvent() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_REQUEST_BODY_WITHOUT_ANY_REASONS_SELECTED);

        Response response = request
            .header(HttpHeaders.AUTHORIZATION,userToken)
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(listOnNoticeMidEventEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res);
        Assert.assertEquals(null,res.getData().get(SELECTED_AND_ADDITIONAL_REASONS));
    }

    @Test
    public void testListOnNoticeSubmissionWhenAdditionalReasonsSelected() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_VALID_REQUEST_BODY);

        Response response = request
            .header(HttpHeaders.AUTHORIZATION,idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(listOnNoticeSubmissionEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res);
        String reasonsSelectedString = ListOnNoticeReasonsEnum.getDisplayedValue("childrenResideWithApplicantAndBothProtectedByNonMolestationOrder")
            + "\n" + ListOnNoticeReasonsEnum.getDisplayedValue("noEvidenceOnRespondentSeekToFrustrateTheProcessIfTheyWereGivenNotice") + "\n";
        Assert.assertEquals(reasonsSelectedString,res.getData().get(SELECTED_AND_ADDITIONAL_REASONS));
        Assert.assertNull(CASE_NOTE);
        Assert.assertNull(SUBJECT);
        Assert.assertNotNull(res.getData().get(CASE_NOTES));
    }

    @Test
    public void testListOnNoticeSubmissionWhenNoReasonsSelected() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_REQUEST_BODY_WITHOUT_ANY_REASONS_SELECTED);

        Response response = request
            .header(HttpHeaders.AUTHORIZATION,idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(listOnNoticeSubmissionEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res);
        Assert.assertNull(res.getData().get(SELECTED_AND_ADDITIONAL_REASONS));
        Assert.assertNull(CASE_NOTE);
        Assert.assertNull(SUBJECT);
        Assert.assertNull(res.getData().get(CASE_NOTES));
    }

    @Test
    public void testListOnNoticePrepopulate() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_REQUEST_BODY_WITHOUT_ANY_REASONS_SELECTED);

        Response response = request
            .header(HttpHeaders.AUTHORIZATION,userToken)
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(listOnNoticePrepopulateEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res);
        //TODO: Need to add more assertions as per the stories
    }
}
