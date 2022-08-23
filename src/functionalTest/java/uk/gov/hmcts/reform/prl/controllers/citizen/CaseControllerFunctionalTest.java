package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String CASE_DATA_INPUT = "requests/create-case-valid-casedata-input.json";

    private final String targetInstance =
            StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:4044"
            );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void createCaseInCcd() throws Exception {
        String requestBody = ResourceLoader.loadJson(CASE_DATA_INPUT);
        request
                .header("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiIxZXIwV1J3"
                        + "Z0lPVEFGb2pFNHJDL2ZiZUt1M0k9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJyYWh1bC5zYW5qY"
                        + "XZhQGhtY3RzLm5ldCIsImN0cyI6Ik9BVVRIMl9TVEFURUxFU1NfR1JBTlQiLCJhdXRoX2xldmVsIjow"
                        + "LCJhdWRpdFRyYWNraW5nSWQiOiI5MTlmOTdkOS0zZDgyLTQzOGMtOGI1YS1hN2ViNjcwYjFiYWQtMTc2N"
                        + "DA0NjcwIiwiaXNzIjoiaHR0cHM6Ly9mb3JnZXJvY2stYW0uc2VydmljZS5jb3JlLWNvbXB1dGUtaWRhbS1h"
                        + "YXQyLmludGVybmFsOjg0NDMvb3BlbmFtL29hdXRoMi9yZWFsbXMvcm9vdC9yZWFsbXMvaG1jdHMiLCJ0b2tlb"
                        + "k5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiJ1ZnFvMTVXe"
                        + "VpvZmkwbXUwdFhPUEtTanRkS0UiLCJhdWQiOiJjbWNfY2l0aXplbiIsIm5iZiI6MTY1ODg0ODM0MSwiZ3JhbnRfdHl"
                        + "wZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyJdLCJhdXRoX3"
                        + "RpbWUiOjE2NTg4NDgzNDEsInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNjU4ODc3MTQxLCJpYXQiOjE2NTg4NDgzNDEsIm"
                        + "V4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiJZT215NHQ3NWhCXzlpaC12Vkg5b3ZkLXhaUEEifQ.xQL_vGcoxUMPy88nzm"
                        + "piY5Hbjp7QmHXSLt-Oxi-yFbQ-d3rUw7MlQtp8NrCetr-OgipgJguL7Sx_E1aiOcOEEjuU_axvS8eVdb143gnwO_vy7e"
                        + "MBfewrT9PDR7ehU_7gVzCqLlTegPp4SMaW6ffANvQXaVBUOrABC8xFpE_RD1kcyo-nOslKH-MEW5rnXhQZjeRhCA2CUwfj"
                        + "DP5Qkc7oT5ZO3wIq2EngnDPl9WxhJiKltzavVA64AcAsdy5zdjPRJ-kMPvpZtkC1oUonXslddxeHSQQHaQYxlmiUr1M3f_DL"
                        + "6c4JC8DjQGWfitWXcIXBGG1kHRJuLOgUKz0NODzF4w")
                .header("serviceAuthorization", serviceAuthenticationGenerator.generate())
                .body(requestBody)
                .when()
                .contentType("application/json")
                .post("/case/create")
                .then()
                .assertThat().statusCode(200);
    }
}