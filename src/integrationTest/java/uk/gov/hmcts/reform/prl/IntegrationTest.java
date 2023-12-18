package uk.gov.hmcts.reform.prl;

import io.restassured.response.Response;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import net.serenitybdd.rest.SerenityRest;
import org.assertj.core.util.Strings;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.prl.models.CreateUserRequest;
import uk.gov.hmcts.reform.prl.models.UserCode;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;


@Slf4j
@RunWith(SerenityRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
public abstract class IntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    @Value("${case.orchestration.prepopulate.uri}")
    protected String prePopulateUri;

    @Value("${case.orchestration.documentgenerate.uri}")
    protected String documentGenerateUri;

    @Value("${http.proxy:#{null}}")
    protected String httpProxy;

    @Value("${idam.user.genericpassword}")
    protected String aatPassword;

    @Value("${idam.api.url}")
    private String idamUserBaseUrl;

    @Value("${idam.client.redirect_uri}")
    private String idamRedirectUri;

    @Value("${idam.client.authorize.context-path}")
    private String idamAuthorizeContextPath;

    @Value("${idam.client.token.context-path}")
    private String idamTokenContextPath;

    @Value("${idam.client.clientId}")
    private String idamAuthClientID;

    @Value("${idam.client.secret}")
    private String idamSecret;

    @Value("${idam.s2s-auth.url}")
    private String idamS2sAuthUrl;

    @Value("${payments.api.url}")
    private String paymentUrl;

    @Value("${payments.api.callback-url}")
    private String paymentCallBackUrl;


    private String idamUsername;

    private int responseCode;

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration;

    private static String userToken = null;
    private String username;

    public IntegrationTest() {
        this.springMethodIntegration = new SpringIntegrationMethodRule();
    }

    @PostConstruct
    public void init() {
        if (!Strings.isNullOrEmpty(httpProxy)) {
            configProxyHost();
        }
    }

    public Response callPrePopulateFeeAndSolicitorName(String requestBody) {
        return PrePopulateFeeAndSolicitorUtil
            .prePopulateFeeAndSolicitorName(
                requestBody,
                prePopulateUri,
                getUserToken()
            );
    }

    public Response callDocGenerateAndSave(String requestBody) {
        return DocumentGenerateUtil
                .documentGenerate(
                requestBody,
                documentGenerateUri,
                "Bearer TestAuthToken"
            );
    }

    private synchronized String getUserToken() {
        username =  "simulate-delivered" + UUID.randomUUID() + "@mailinator.com";

        if (userToken == null) {
            createCaseworkerUserInIdam(username, aatPassword);
            userToken = generateUserTokenWithNoRoles(username, aatPassword);
        }
        return userToken;
    }

    public String getAuthorizationToken() {
        String authToken = getUserToken();
        return authToken;
    }

    public void createCaseworkerUserInIdam(String username, String password) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
            .email(username)
            .password(password)
            .forename("Henry")
            .surname("Harper")
            .roles(new UserCode[]{UserCode.builder().code("caseworker-privatelaw-solicitor").build()})
            .build();

        Response userResponse = null;

        userResponse = SerenityRest.given()
            .header("Content-Type", "application/json")
            .body(ResourceLoader.objectToJson(userRequest))
            .post(idamCreateUrl());
    }

    public Response callInvalidPrePopulateFeeAndSolicitorName(String requestBody) {
        return PrePopulateFeeAndSolicitorUtil
            .prePopulateFeeAndSolicitorName(
                requestBody,
                prePopulateUri,
                "Bearer TestAuthToken"
            );
    }

    public String generateUserTokenWithNoRoles(String username, String password) {
        String userLoginDetails = String.join(":", username, password);
        final String authHeader = "Basic " + new String(Base64.getEncoder().encode(userLoginDetails.getBytes()));
        Response response = null;

        response = SerenityRest.given()
            .header("Authorization", authHeader)
            .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .relaxedHTTPSValidation()
            .post(idamCodeUrl());

        responseCode = response.getStatusCode();

        if (response.getStatusCode() >= 300) {
            throw new IllegalStateException("Token generation failed with code: " + response.getStatusCode()
                                                + " body: " + response.getBody().prettyPrint());
        }

        response = SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .relaxedHTTPSValidation()
            .post(idamTokenUrl(response.getBody().path("code")));

        assert response.getStatusCode() == 200 : "Error generating code from IDAM: " + response.getStatusCode();

        String token = response.getBody().path("access_token");
        return "Bearer " + token;
    }

    private String idamCreateUrl() {
        return idamUserBaseUrl + "/testing-support/accounts";
    }

    private String idamCodeUrl() {

        return idamUserBaseUrl + idamAuthorizeContextPath
            + "?response_type=code"
            + "&client_id=" + idamAuthClientID
            + "&redirect_uri=" + idamRedirectUri;
    }

    private String idamTokenUrl(String code) {

        return idamUserBaseUrl + idamTokenContextPath
            + "?code=" + code
            + "&client_id=" + idamAuthClientID
            + "&client_secret=" + idamSecret
            + "&redirect_uri=" + idamRedirectUri
            + "&grant_type=authorization_code";

    }

    private void configProxyHost() {
        try {
            URL proxy = new URL(httpProxy);
            if (InetAddress.getByName(proxy.getHost()).isReachable(2000)) {
                System.setProperty("http.proxyHost", proxy.getHost());
                System.setProperty("http.proxyPort", Integer.toString(proxy.getPort()));
                System.setProperty("https.proxyHost", proxy.getHost());
                System.setProperty("https.proxyPort", Integer.toString(proxy.getPort()));
            } else {
                throw new IOException();
            }
        } catch (IOException e) {
            log.error("Error setting up proxy - are you connected to the VPN?", e);
            throw new RuntimeException(e);
        }
    }

}
