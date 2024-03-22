package uk.gov.hmcts.reform.prl.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@TestPropertySource("classpath:application.yaml")
@Service
public class ServiceAuthenticationGenerator {

    @Value("${idam.s2s-auth.microservice}")
    private String microservice;

    @Value("${idam.s2s-auth.url}")
    private String s2sUrl;

    public String generate() {
        return generate(this.microservice);
    }

    public String generate(final String s2sName) {
        final Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(s2sUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(Map.of("microservice", s2sName))
            .when()
            .post("/testing-support/lease")
            .andReturn();

        assertThat(response.getStatusCode()).isEqualTo(200);

        return "Bearer " + response.getBody().asString();
    }

    public String generateTokenForCcd() {
        final Response response = RestAssured
            .given()
            .relaxedHTTPSValidation()
            .baseUri(s2sUrl)
            .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .body(Map.of("microservice", "ccd_data"))
            .when()
            .post("/testing-support/lease")
            .andReturn();

        assertThat(response.getStatusCode()).isEqualTo(200);

        return "Bearer " + response.getBody().asString();
    }
}
