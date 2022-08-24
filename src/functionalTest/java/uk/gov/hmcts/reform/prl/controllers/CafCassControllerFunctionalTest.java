package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;

import static io.restassured.RestAssured.given;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CafCassControllerFunctionalTest {

    private final String userToken = "Bearer testToken";

    private final String targetInstance =
            StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:4044"
            );

    private final RequestSpecification request = given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenDatetimeWindow_whenGetRequestToSearchCasesByCafCassController_then200Response() throws Exception {
        request
                .header("Authorization", userToken)
                .contentType("application/json")
                .given()
                .pathParams("start_date",
                        "22-02-2022 14:00",
                        "end_date",
                        "22-02-2022 14:15")
                .when()
                .get("/searchCases")
                .then().assertThat().statusCode(200);

    }

    @Test
    public void givenNullDateWindow_whenGetRequestToSearchCasesByCafCassController_then400Response() throws Exception {
        request
                .header("Authorization", userToken)
                .contentType("application/json")
                .given()
                .pathParams("start_date",
                        null,
                        "end_date",
                        "22-02-2022 14:15")
                .when()
                .get("/searchCases")
                .then().assertThat().statusCode(400);

    }
}
