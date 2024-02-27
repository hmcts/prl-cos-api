package uk.gov.hmcts.reform.prl.controllers;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.junit.Assert.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(classes = {Application.class, ListOnNoticeControllerIntegrationTest.class})
public class ListOnNoticeControllerIntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String listOnNoticeMidEventEndpoint = "/listOnNotice/reasonUpdation/mid-event";

    private final String listOnNoticePrepopulateEndpoint = "/pre-populate-list-on-notice";

    private final String listOnNoticeSendNotificationEndpoint = "/send-listOnNotice-notification";

    private static final String LIST_ON_NOTICE_VALID_REQUEST_BODY = "requests/ListOnNoticeRequest.json";


    @Test
    public void testListOnNoticeMidEventEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_VALID_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + listOnNoticeMidEventEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testListOnNoticePrepopulateEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_VALID_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + listOnNoticePrepopulateEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testListOnNoticeSendNotificationEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(LIST_ON_NOTICE_VALID_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + listOnNoticeSendNotificationEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

}
