package uk.gov.hmcts.reform.prl.controllers;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.util.ServiceAuthenticationGenerator;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(classes = {Application.class, C100IssueCaseControllerIntegrationTest.class})
public class C100IssueCaseControllerIntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String issueAndSendToLocalCourtEndpoint = "/issue-and-send-to-local-court";

    private final String issueAndSendToLocalCourtEndpointNotify = "/issue-and-send-to-local-court-notification";

    private static final String VALID_REQUEST_BODY = "requests/call-back-controller.json";

    public static final String VALID_REQUEST_BODY_ISSUE_AND_SEND_TO_LOCAL_COURT_NOTIFICATION
        = "requests/issue-and-send-email-request.json";

    @Autowired
    IdamTokenGenerator idamTokenGenerator;

    @Autowired
    ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Test
    public void testIssueAndSendToLocalCourtEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + issueAndSendToLocalCourtEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, idamTokenGenerator.getSysUserToken());
        httpPost.addHeader(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd());
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testIssueAndSendToLocalCourtEndpointNotification() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_ISSUE_AND_SEND_TO_LOCAL_COURT_NOTIFICATION);
        HttpPost httpPost = new HttpPost(serviceUrl + issueAndSendToLocalCourtEndpointNotify);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, idamTokenGenerator.getSysUserToken());
        httpPost.addHeader(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd());
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }
}
