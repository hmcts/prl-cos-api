package uk.gov.hmcts.reform.prl.controllers.gatekeeping;

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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.ListOnNoticeReasonsEnum;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SELECTED_AND_ADDITIONAL_REASONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TIER_OF_JUDICIARY;

@Slf4j
@SpringBootTest
@ContextConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ListOnNoticeControllerFT {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    ObjectMapper objectMapper;

    private static final String LIST_ON_NOTICE_VALID_REQUEST_BODY = "requests/gatekeeping/ListOnNoticeRequest.json";

    private static final String VALID_CAFCASS_REQUEST_JSON = "requests/cafcass-cymru-send-email-request.json";

    private static final String LIST_ON_NOTICE_REQUEST_BODY_WITHOUT_ANY_REASONS_SELECTED =
        "requests/gatekeeping/ListOnNoticeRequestWithoutReasons.json";

    private final String userToken = "Bearer testToken";

    private final String listOnNoticeMidEventEndpoint = "/listOnNotice/reasonUpdation/mid-event";

    private final String listOnNoticeSubmissionEndpoint = "/listOnNotice";

    private final String listOnNoticePrepopulateEndpoint = "/pre-populate-list-on-notice";

    private final String listOnNoticeSendNotificationEndpoint = "/send-listOnNotice-notification";

    private static CaseDetails caseDetails;
    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


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

    @Test
    public void testListOnNoticeWhenReasonsSelected_200ResponseAndNoErrorsFromMidEvent() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_VALID_REQUEST_BODY);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(listOnNoticeMidEventEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res);
        String reasonsSelectedString = ListOnNoticeReasonsEnum.getDisplayedValue("childrenResideWithApplicantAndBothProtectedByNonMolestationOrder")
            + "\n\n" + ListOnNoticeReasonsEnum.getDisplayedValue("noEvidenceOnRespondentSeekToFrustrateTheProcessIfTheyWereGivenNotice") + "\n\n";
        Assert.assertEquals(reasonsSelectedString,res.getData().get(SELECTED_AND_ADDITIONAL_REASONS));
    }

    @Test
    public void testListOnNoticeWhenNoReasonsSelected_200ResponseAndNoErrorsFromMidEvent() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_REQUEST_BODY_WITHOUT_ANY_REASONS_SELECTED);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
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
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(listOnNoticeSubmissionEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res);
        String reasonsSelectedString = "The child[ren] reside with applicant and both are protected by a Non-Molestation Order"
            + "\nThere is no evidence to suggest that the respondent seeks to remove the child[ren] from the applicant's care and therefore "
            + "there is no genuine emergency"
            + "\ntestAdditionalReason";
        Assert.assertNotNull(res.getData().get(SELECTED_AND_ADDITIONAL_REASONS));
        Assert.assertNotNull(res.getData().get(CASE_NOTES));
        Assert.assertNotNull(res.getData().get(TIER_OF_JUDICIARY));
    }

    @Test
    public void testListOnNoticeSubmissionWhenNoReasonsSelected() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_REQUEST_BODY_WITHOUT_ANY_REASONS_SELECTED);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(listOnNoticeSubmissionEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res);
        Assert.assertNull(res.getData().get(SELECTED_AND_ADDITIONAL_REASONS));
    }

    @Test
    public void testListOnNoticePrepopulate() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_REQUEST_BODY_WITHOUT_ANY_REASONS_SELECTED);

        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post(listOnNoticePrepopulateEndpoint);
        response.then().assertThat().statusCode(200);
        AboutToStartOrSubmitCallbackResponse res = objectMapper.readValue(response.getBody().asString(), AboutToStartOrSubmitCallbackResponse.class);
        Assert.assertNotNull(res);
        Assert.assertTrue(res.getData().containsKey("legalAdviserList"));
    }

    @Test
    public void testSendListOnNoticeNotification() throws Exception {

        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_REQUEST_BODY_WITHOUT_ANY_REASONS_SELECTED);
        String requestBodyRevised = requestBody
            .replace("1648728532100631", caseDetails.getId().toString());
        Response response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post(listOnNoticeSendNotificationEndpoint);
        response.then().assertThat().statusCode(200);
    }
}
