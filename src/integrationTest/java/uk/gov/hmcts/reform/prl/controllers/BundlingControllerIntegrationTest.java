package uk.gov.hmcts.reform.prl.controllers;

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
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.util.ServiceAuthenticationGenerator;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BundlingControllerIntegrationTest.class, Application.class})
public class BundlingControllerIntegrationTest extends IntegrationTest {
    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    @Autowired
    ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    IdamTokenGenerator idamTokenGenerator;

    private final String bundleControllerEndpoint = "/bundle/createBundle";

    private static final String VALID_REQUEST_BODY = "requests/C100-case-data.json";

    @Test
    public void whenValidRequestFormat_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + bundleControllerEndpoint);
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        httpPost.addHeader(AUTHORIZATION, idamTokenGenerator.getSysUserToken());
        httpPost.addHeader(SERVICE_AUTHORIZATION_HEADER, serviceAuthenticationGenerator.generateTokenForCcd());
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

}
