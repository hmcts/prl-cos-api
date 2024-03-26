package uk.gov.hmcts.reform.prl.controllers.citizen;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
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
@SpringBootTest(classes = {Application.class, CaseControllerIntegrationTest.class})
public class CaseControllerIntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String createCaseEndpoint = "/case/create";

    private final String getCaseEndpoint = "/1234567";

    private final String updateCaseEndpoint = "1234567/{eventId}/update-case";

    private final String retrieveCaseEndpoint = "/citizen/solicitor/retrieve-cases/12345";

    private final String retrieveCitizenCasesEndpoint = "/cases";

    private final String linkCitizenToCaseEndpoint = "/citizen/link";

    private final String validateAccessCodeEndpoint = "/validate-access-code";

    private final String retrieveCitizenFlagsEndpoint = "/1234567/retrieve-ra-flags/party-1";

    private final String updateCitizenFlagsEndpoint = "/1234567/c100RequestSupport/party-update-ra";

    private final String validBody = "controller/valid-casedata-input.json";

    @Autowired
    CaseService caseService;

    @Autowired
    IdamTokenGenerator idamTokenGenerator;

    @Autowired
    ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Test
    public void testCreateCaseEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(validBody);
        HttpPost httpPost = new HttpPost(serviceUrl + createCaseEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader("serviceAuthorization", serviceAuthenticationGenerator.generate());
        httpPost.addHeader(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem());
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }


    @Test
    public void testGetCaseEndpoint() throws Exception {
        HttpGet httpGet = new HttpGet(serviceUrl + getCaseEndpoint);
        httpGet.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpGet.addHeader("serviceAuthorization", serviceAuthenticationGenerator.generate());
        httpGet.addHeader(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem());
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpGet);
        assertEquals(
            HttpStatus.SC_NOT_FOUND,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testUpdateCaseEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(validBody);
        HttpPost httpPost = new HttpPost(serviceUrl + updateCaseEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader("serviceAuthorization", serviceAuthenticationGenerator.generate());
        httpPost.addHeader(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem());
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveCaseEndpoint() throws Exception {
        HttpGet httpGet = new HttpGet(serviceUrl + retrieveCaseEndpoint);
        httpGet.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpGet.addHeader("serviceAuthorization", serviceAuthenticationGenerator.generate());
        httpGet.addHeader(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem());
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpGet);
        assertEquals(
            HttpStatus.SC_NOT_FOUND,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveCitizenCasesEndpoint() throws Exception {
        HttpGet httpGet = new HttpGet(serviceUrl + retrieveCitizenCasesEndpoint);
        httpGet.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpGet.addHeader("serviceAuthorization", serviceAuthenticationGenerator.generate());
        httpGet.addHeader(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem());
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpGet);
        assertEquals(
            HttpStatus.SC_NOT_FOUND,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testLinkCitizenToCaseEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(validBody);
        HttpPost httpPost = new HttpPost(serviceUrl + linkCitizenToCaseEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader("serviceAuthorization", serviceAuthenticationGenerator.generate());
        httpPost.addHeader(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem());
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testValidateAccessCodeEndpoint() throws Exception {
        HttpGet httpGet = new HttpGet(serviceUrl + validateAccessCodeEndpoint);
        httpGet.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpGet.addHeader("serviceAuthorization", serviceAuthenticationGenerator.generate());
        httpGet.addHeader(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem());
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpGet);
        assertEquals(
            HttpStatus.SC_NOT_FOUND,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrieveCitizenFlagsEndpoint() throws Exception {
        HttpGet httpGet = new HttpGet(serviceUrl + retrieveCitizenFlagsEndpoint);
        httpGet.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpGet.addHeader("serviceAuthorization", serviceAuthenticationGenerator.generate());
        httpGet.addHeader(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem());
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpGet);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testUpdateCitizenFlagsEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson("controller/valid-ra-update-request.json");
        HttpPost httpPost = new HttpPost(serviceUrl + updateCitizenFlagsEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader("serviceAuthorization", serviceAuthenticationGenerator.generate());
        httpPost.addHeader(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem());
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

}
