package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.UploadAdditionalApplicationService;
import uk.gov.hmcts.reform.prl.utils.ApplicantsListGenerator;

@Slf4j
@SpringBootTest
@ContextConfiguration
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UploadAdditionalApplicationControllerFunctionalTest {

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    @Autowired
    private WebApplicationContext webApplicationContext;

    private static final String VALID_REQUEST_BODY = "requests/call-back-controller.json";

    @Autowired
    private ApplicantsListGenerator applicantsListGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UploadAdditionalApplicationService uploadAdditionalApplicationService;


    @Test
    public void testPre_populate_applicants_with_invliad_request_thenResponse415() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", "auth")
            .header("ServiceAuthorization", "s2sToken")
            .body(requestBody)
            .when()
            .post("/pre-populate-applicants")
            .then()
            .assertThat().statusCode(415)
            .extract()
            .response();
    }
}
