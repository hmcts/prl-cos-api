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
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.util.ServiceAuthenticationGenerator;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.junit.Assert.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(classes = {Application.class, EditReturnOrderControllerIntegrationTest.class})
public class EditReturnOrderControllerIntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String editReturnedOrderAboutToStartEndpoint = "/edit-returned-order/about-to-start";
    private final String editReturnedOrderMidEndpoint = "/edit-returned-order/mid-event/populate-instructions";
    private final String editReturnedOrderSubmittedEndpoint = "/edit-returned-order/submitted";

    private static final String VALID_EDIT_RETURN_ORDER_REQUEST_BODY = "requests/call-back-controller.json";

    @Autowired
    CaseService caseService;

    @Autowired
    IdamTokenGenerator idamTokenGenerator;

    @Autowired
    ServiceAuthenticationGenerator serviceAuthenticationGenerator;


    @Test
    public void testEditReturnedOrderAboutToStartEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_EDIT_RETURN_ORDER_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + editReturnedOrderAboutToStartEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testEditReturnedOrderMidEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_EDIT_RETURN_ORDER_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + editReturnedOrderMidEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testEditReturnedOrderSubmittedEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_EDIT_RETURN_ORDER_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + editReturnedOrderSubmittedEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, httpResponse.getStatusLine().getStatusCode());
    }
}
