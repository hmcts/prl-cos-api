package uk.gov.hmcts.reform.prl.controllers.hearingmanagement;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.util.ServiceAuthenticationGenerator;

import static org.junit.Assert.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(classes = {Application.class, HearingManagementControllerIntegrationTest.class})
public class HearingManagementControllerIntegrationTest extends IntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String hearingManagementStateEndpoint = "/hearing-management-state-update";

    private static final String VALID_HEARING_MANAGEMENT_REQUEST_BODY = "requests/hearing-management-controller.json";

    @Autowired
    CaseService caseService;

    @Autowired
    ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Test
    public void testHearingManagementStateEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_HEARING_MANAGEMENT_REQUEST_BODY);
        HttpPut httpPut = new HttpPut(serviceUrl + hearingManagementStateEndpoint);
        httpPut.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPut.addHeader("serviceAuthorization", serviceAuthenticationGenerator.generate());
        StringEntity body = new StringEntity(requestBody);
        httpPut.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPut);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

}
