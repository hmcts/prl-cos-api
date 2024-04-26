package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Assert;
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
@SpringBootTest(classes = {Application.class, ManageOrderControllerIntegrationTest.class})
@Slf4j
public class ManageOrderControllerIntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String populatePreviewOrderEndpoint = "/populate-preview-order";
    private final String fetchChildDetailsEndpoint = "/fetch-child-details";
    private final String populateHeaderEndpoint = "/populate-header";
    private final String caseOrderEmailNotificationEndpoint = "/case-order-email-notification";
    private final String editAndApproveOrderSubmittedEndpoint = "/edit-and-approve/submitted";
    private final String manageOrdersEndpoint = "/manage-orders/about-to-submit";
    private final String addressValidationEndpoint = "/manage-orders/recipients-validations";

    private static final String VALID_REQUEST_BODY = "requests/call-back-controller.json";
    private static final String VALID_MANAGE_ORDER_REQUEST_BODY = "requests/manage-order-fetch-children-request.json";

    private static final String VALID_MANAGE_ORDER_AUTOMATED_HEARING_REQUEST_BODY = "requests/auto-hearing-case-data-request.json";

    private static final String VALID_MANAGE_ORDER_REQUEST_BODY_REVISED = "requests/manage-order-fetch-children-request-integration.json";

    private static final String VALID_REQUEST_RESPONDENT_LIP_WITH_ADDRESS = "requests/manage-order-fetch-children-request-integration.json";

    @Autowired
    CaseService caseService;

    @Autowired
    IdamTokenGenerator idamTokenGenerator;

    @Autowired
    ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void testPopulatePreviewOrderEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + populatePreviewOrderEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testFetchChildDetailsEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_MANAGE_ORDER_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + fetchChildDetailsEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testPopulateHeaderEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_MANAGE_ORDER_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + populateHeaderEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testCaseOrderEmailNotificationEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_MANAGE_ORDER_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + caseOrderEmailNotificationEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testCaseOrderEmailNotificationEndpointForAutoHearing() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_MANAGE_ORDER_AUTOMATED_HEARING_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + caseOrderEmailNotificationEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testOrdersCreateUploadEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_MANAGE_ORDER_REQUEST_BODY_REVISED);
        HttpPost httpPost = new HttpPost(serviceUrl + manageOrdersEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testCaseOrderSubmittedEndPoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_MANAGE_ORDER_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + editAndApproveOrderSubmittedEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testCaseOrderSubmittedEndPointForAutoHearing() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_MANAGE_ORDER_AUTOMATED_HEARING_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + editAndApproveOrderSubmittedEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testValidateAddressErrorMessageEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_RESPONDENT_LIP_WITH_ADDRESS);
        HttpPost httpPost = new HttpPost(serviceUrl + addressValidationEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, "Bearer testauth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        String content = EntityUtils.toString(httpResponse.getEntity());
        JSONObject jsonObj = new JSONObject(content);
        Assert.assertNotNull(jsonObj.getJSONObject("data"));
        Assert.assertNotNull("1647373355918192", jsonObj.getJSONObject("data").get("id"));
        Assert.assertNotNull("John Smith", jsonObj.getJSONObject("data").get("applicantCaseName"));
    }
}
