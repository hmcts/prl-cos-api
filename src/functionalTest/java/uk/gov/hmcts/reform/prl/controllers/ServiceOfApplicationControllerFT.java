package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ServiceOfApplicationControllerFT {

    private static final String VALID_REQUEST_BODY = "requests/service-of-application.json";

    private static final String VALID_REQUEST_BODY_WITHOUT_OTHER_PEOPLE = "requests/soa-with-out-other-people.json";

    private static final String VALID_REQUEST_BODY_WITH_OTHER_PEOPLE = "requests/soa-with-other-people.json";

    private static final String VALID_REQUEST_BODY_WITH_OUT_C6A_ORDERS = "requests/soa-with-out-c6a-orders.json";

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    @Ignore
    public void givenRequestWithCaseData_ResponseContainsHeaderAndCollapsable() throws Exception {

        final String userToken = "Bearer testToken";

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/about-to-start")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void givenRequestWithCaseData_Response_AboutToSubmit() throws Exception {

        final String userToken = "Bearer testToken";

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/about-to-submit")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    @Ignore
    public void givenRequestWithCaseData_Response_Submitted() throws Exception {

        final String userToken = "Bearer testToken";

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/submitted")
            .then()
            .assertThat().statusCode(200);
    }

    /**
     * When Other people not selected.
     * then error should not appear at all during the service of application submission.
     *
     */
    @Test
    public void givenRequestWithCaseData_MidEvent_whenOtherpeopleNotSelected_then_c6A_isNotRequired() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WITHOUT_OTHER_PEOPLE);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/soa-validation")
            .then()
            .body("errors", equalTo(null))
            .assertThat().statusCode(200);
    }


    /**
     * When Other people selected, but C6a Order not selected.
     * then error should appear during Service of application submission.
     *
     */
    @Test
    public void givenCaseData_whenOtherpeopleSelectedButC6A_NotSelected_then_ValidationError() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WITH_OTHER_PEOPLE);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/soa-validation")
            .then()
            .body("errors[0]", equalTo(OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR))
            .assertThat().statusCode(200);
    }


    /**
     * When Other people selected, but C6a Order not event present in the order collection.
     * then error should appear during Service of application submission.
     *
     */
    @Test
    public void givenCaseData_whenOtherpeopleSelectedButC6A_NotEvenPresent_then_ValidationError() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WITH_OUT_C6A_ORDERS);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/soa-validation")
            .then()
            .body("errors[0]", equalTo(OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR))
            .assertThat().statusCode(200);
    }

}
