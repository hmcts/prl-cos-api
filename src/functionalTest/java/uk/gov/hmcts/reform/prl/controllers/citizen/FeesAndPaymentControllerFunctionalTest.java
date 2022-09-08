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
public class FeesAndPaymentControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String CREATE_PAYMENT_INPUT = "requests/create-payment-input.json";


    private final String targetInstance =
            StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:4044"
            );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void createPaymentTest() throws Exception {
        String requestBody = ResourceLoader.loadJson(CREATE_PAYMENT_INPUT);
        request
                .header("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJaNEJjal"
                        + "ZnZnZ1NVpleEt6QkVFbE1TbTQzTHM9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJjaXRpemVuX3"
                        + "Rlc3RAbWFpbGluYXRvci5jb20iLCJjdHMiOiJPQVVUSDJfU1RBVEVMRVNTX0dSQU5UIiwiYXV"
                        + "0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiYzU5MjA1NjQtMGViNy00MmUwLThkNzAt"
                        + "NTVlOWQyODgxNjAyLTE3OTA0NDYyIiwiaXNzIjoiaHR0cHM6Ly9mb3JnZXJvY2stYW0uc2V"
                        + "ydmljZS5jb3JlLWNvbXB1dGUtaWRhbS1kZW1vLmludGVybmFsOjg0NDMvb3BlbmFtL29h"
                        + "dXRoMi9yZWFsbXMvcm9vdC9yZWFsbXMvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZ"
                        + "W4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiJwdUdGNkJ1a2tJZlFRbWt"
                        + "0Zk1DOUJsQ2hZQTgiLCJhdWQiOiJjbWNfY2l0aXplbiIsIm5iZiI6MTY2MjM3NDU2OSwiZ3Jhb"
                        + "nRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUi"
                        + "LCJyb2xlcyJdLCJhdXRoX3RpbWUiOjE2NjIzNzQ1NjksInJlYWxtIjoiL2htY3RzIiwiZXhwIjo"
                        + "xNjYyNDAzMzY5LCJpYXQiOjE2NjIzNzQ1NjksImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiJEbndk"
                        + "VnlTaE5iRl9qdXE3QURwRVFvdERfWU0ifQ.SjpYqMqIhtI9Qg8wQdbY2gT9AMzUj5XhRX8Bio_6xQ"
                        + "efRN7Bcskl393nwZdwHOQOiT4inWhdwb3CXtfixjX2Qr-1QrkvSVH3Pj6ckbt-4DWxlKo9jIxNolE"
                        + "UvwjY06YRuKJ80hia3O1E7sFt3pl1UWRPM7iI15wGP8bzslB8z2COkLtUtiyXK0ouYGERZ5HGm2Gw"
                        + "xYrfXzlYCTPgoWXHbMCIEj9l90LyJ8enKwV7H2v8cNQeQvxPsoZZmBoIDSeRbgS6qKoUoFODRzd8G"
                        + "uZqq9nCvS-O31ZkAYF4PlH0S8cXBLh-TtX7eII2R8qhuziqLdPpPjwBVvPhkvmD5tm9wA")
                .header("ServiceAuthorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWI"
                        + "iOiJwcmxfY2l0aXplbl9mcm9udGVuZCIsImV4cCI6MTY2MjE0OTc4MH0.cFT1lU4b3V49JUK6ZD12V8"
                        + "2L-PuzUpSrKtoP6pCDWfrqPaVDJ6C_Limov_4jZ0YVajzFUw8R-Seim9MvDL4ZaQ")
                .body(requestBody)
                .when()
                .contentType("application/json")
                .post("/fees-and-payment-apis/")
                .then()
                .assertThat().statusCode(200);
    }

    @Test
    public void retrievePaymentStatustest() throws Exception {
        request
            .header("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJaNEJjal"
                + "ZnZnZ1NVpleEt6QkVFbE1TbTQzTHM9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJjaXRpemVuX3"
                + "Rlc3RAbWFpbGluYXRvci5jb20iLCJjdHMiOiJPQVVUSDJfU1RBVEVMRVNTX0dSQU5UIiwiYXV"
                + "0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiYzU5MjA1NjQtMGViNy00MmUwLThkNzAt"
                + "NTVlOWQyODgxNjAyLTE3OTA0NDYyIiwiaXNzIjoiaHR0cHM6Ly9mb3JnZXJvY2stYW0uc2V"
                + "ydmljZS5jb3JlLWNvbXB1dGUtaWRhbS1kZW1vLmludGVybmFsOjg0NDMvb3BlbmFtL29h"
                + "dXRoMi9yZWFsbXMvcm9vdC9yZWFsbXMvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZ"
                + "W4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiJwdUdGNkJ1a2tJZlFRbWt"
                + "0Zk1DOUJsQ2hZQTgiLCJhdWQiOiJjbWNfY2l0aXplbiIsIm5iZiI6MTY2MjM3NDU2OSwiZ3Jhb"
                + "nRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUi"
                + "LCJyb2xlcyJdLCJhdXRoX3RpbWUiOjE2NjIzNzQ1NjksInJlYWxtIjoiL2htY3RzIiwiZXhwIjo"
                + "xNjYyNDAzMzY5LCJpYXQiOjE2NjIzNzQ1NjksImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiJEbndk"
                + "VnlTaE5iRl9qdXE3QURwRVFvdERfWU0ifQ.SjpYqMqIhtI9Qg8wQdbY2gT9AMzUj5XhRX8Bio_6xQ"
                + "efRN7Bcskl393nwZdwHOQOiT4inWhdwb3CXtfixjX2Qr-1QrkvSVH3Pj6ckbt-4DWxlKo9jIxNolE"
                + "UvwjY06YRuKJ80hia3O1E7sFt3pl1UWRPM7iI15wGP8bzslB8z2COkLtUtiyXK0ouYGERZ5HGm2Gw"
                + "xYrfXzlYCTPgoWXHbMCIEj9l90LyJ8enKwV7H2v8cNQeQvxPsoZZmBoIDSeRbgS6qKoUoFODRzd8G"
                + "uZqq9nCvS-O31ZkAYF4PlH0S8cXBLh-TtX7eII2R8qhuziqLdPpPjwBVvPhkvmD5tm9wA")
            .header("ServiceAuthorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWI"
                + "iOiJwcmxfY2l0aXplbl9mcm9udGVuZCIsImV4cCI6MTY2MjE0OTc4MH0.cFT1lU4b3V49JUK6ZD12V8"
                + "2L-PuzUpSrKtoP6pCDWfrqPaVDJ6C_Limov_4jZ0YVajzFUw8R-Seim9MvDL4ZaQ")
            .when()
            .contentType("application/json")
            .post("/fees-and-payment-apis/retrievePaymentStatus/RC-1599-4778-4711-5958/1656350492135029")
            .then()
            .assertThat().statusCode(200);
    }
}
