package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
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
                .header("Authorization", "eyJ0eXAiOiJKV1QiLCJraWQiOiIxZXIwV1J3Z0lPVEFGb2p"
                        + "FNHJDL2ZiZUt1M0k9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJyYWh1bC5zYW5qYXZhQGhtY3RzLm5ldCIsIm"
                        + "N0cyI6Ik9BVVRIMl9TVEFURUxFU1NfR1JBTlQiLCJhdXRoX2xldmVsIjowLCJhdWRpdFRyYWNraW5nSWQiOiIyN"
                        + "GJmODNhOS02MDVkLTQzZGYtYjY2YS1hMWY3NTY4ODUwNjAtMzg5Njc2MTQiLCJpc3MiOiJodHRwczovL2Zvcmdl"
                        + "cm9jay1hbS5zZXJ2aWNlLmNvcmUtY29tcHV0ZS1pZGFtLWFhdDIuaW50ZXJuYWw6ODQ0My9vcGVuYW0vb2F1dGg"
                        + "yL3JlYWxtcy9yb290L3JlYWxtcy9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUi"
                        + "OiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6IjgxY1VhaW9PbVhQQm5MeWgyR2xCY1BTX1dhQSIsImF1ZCI6ImNtY19ja"
                        + "XRpemVuIiwibmJmIjoxNjYyMTM3NDU1LCJncmFudF90eXBlIjoiYXV0aG9yaXphdGlvbl9jb2RlIiwic2NvcGUiOls"
                        + "ib3BlbmlkIiwicHJvZmlsZSIsInJvbGVzIl0sImF1dGhfdGltZSI6MTY2MjEzNzQ1NSwicmVhbG0iOiIvaG1jdHMiL"
                        + "CJleHAiOjE2NjIxNjYyNTUsImlhdCI6MTY2MjEzNzQ1NSwiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6IkRqbFlJX01"
                        + "vSURXVGxGYVhYZnNKcXJKa05wYyJ9.x9nmsqV5Np2fYXRfKV1h9hvB303qzFyT8d9Tx6RGYMo_G7wlsEo7YMrec3W8n"
                        + "rOR7ksvf4AoTJxSCYXGMQ1Ezi7-SX5Vct9L6rGIAOeH595SrM4AkponlRP5jWD43DP76z62GpfmBcm_0tHDyWDiQ23"
                        + "ZExl1SKJ9OUkDcunSV0jh_BV2DWyWU-SxN8L8cy5UBa5CnEJ4_tzuI4epAJq7QnV35dNyNiq-fseCMAH4GOQAzuoFL"
                        + "18nBprrmqREK3auzQLAYXqQRdaOf33663oABZg7Hxi29AJ2SBtJbMHpqGhhk0WQ7Kns3djASt0UJUL2VWv6jbllGv"
                        + "a9y3wgL-OXbg")
                .header("serviceAuthorization", serviceAuthenticationGenerator.generate())
                .body(requestBody)
                .when()
                .contentType("application/json")
                .post("/case/create")
                .then()
                .assertThat().statusCode(200);
    }
}