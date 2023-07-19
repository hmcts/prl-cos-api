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

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.junit.Assert.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(classes = {Application.class, CaseDeletionControllerIntegrationTest.class})
public class CaseDeletionControllerIntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String caseDeletionEndpoint = "/callback/case-deletion/about-to-submit";

    private static final String VALID_REQUEST_BODY = "requests/call-back-controller-delete-application-request.json";

    @Autowired
    CaseService caseService;

    @Autowired
    IdamTokenGenerator idamTokenGenerator;

    @Test
    public void testCaseDeletionEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        HttpPost httpPost = new HttpPost(serviceUrl + caseDeletionEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem());
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

}
