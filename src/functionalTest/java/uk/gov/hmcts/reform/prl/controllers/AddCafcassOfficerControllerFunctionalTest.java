package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;


@Slf4j
@SpringBootTest
@ContextConfiguration
public class AddCafcassOfficerControllerFunctionalTest {

    private static final String VALID_REQUEST_BODY = "requests/add-cafcass-officer.json";

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String C100_SEND_TO_GATEKEEPERJUDGE = "requests/call-back-controller-send-to-gatekeeperForJudge.json";


    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenRequestBody_whenAdd_cafcass_officer_about_to_submit_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);

        AboutToStartOrSubmitCallbackResponse response =  request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/add-cafcass-officer/about-to-submit")
            .then()
            .assertThat().statusCode(200)
            .body("data.caseStatus.state", equalTo("Submitted"),
                  "data.caseTypeOfApplication", equalTo("C100"),
                  "data.newChildDetails.list_items", hasSize(1),
                  "data.newChildDetails[0].value.cafcassOfficerName", notNullValue(),
                  "data.newChildDetails[0].value.cafcassOfficerPhoneNo", notNullValue(),
                  "data.newChildDetails[0].value.cafcassOfficerEmailAddress", notNullValue()
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

        Assert.assertNotNull(response.getData().get("newChildDetails"));
    }
}
