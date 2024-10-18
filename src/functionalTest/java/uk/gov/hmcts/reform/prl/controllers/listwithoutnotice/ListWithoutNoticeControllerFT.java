package uk.gov.hmcts.reform.prl.controllers.listwithoutnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static uk.gov.hmcts.reform.prl.controllers.listwithoutnotice.ListWithoutNoticeController.CONFIRMATION_BODY_PREFIX_CA;

@Slf4j
@SpringBootTest
@ContextConfiguration
public class ListWithoutNoticeControllerFT {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String LIST_WITHOUT_NOTICE_CA_VALID_REQUEST_BODY = "requests/listwithoutnotice/ListWithoutNoticeCa.json";


    private final String c100ListWithoutNoticeEndpoint = "/listWithoutNotice";

    private final String c100ListWithoutNoticeConfirmationEndpoint = "/listWithoutNotice-confirmation";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


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
