package uk.gov.hmcts.reform.prl.controllers.gatekeeping;

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
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AllocateJudgeControllerIntegrationTest.class, Application.class})
public class AllocateJudgeControllerIntegrationTest extends IntegrationTest {
    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;


    private final String legalAdvisorEndpoint = "/allocateJudge/pre-populate-legalAdvisor-details";

    private final String allocateJudgeEndpoint = "/allocateJudge/allocatedJudgeDetails";


    private static final String VALID_REQUEST_BODY = "requests/C100-case-data.json";

    private static final String VALID_REQUEST_BODY_JUDGE = "requests/AllocateJudgeDetailsRequest1.json";

    private static final String VALID_REQUEST_BODY_LEGALADVISOR = "requests/LegalAdvisorApiRequest.json";

    private static final String VALID_REQUEST_BODY_TIER_OF_JUDICIARY = "requests/AllocateJudgeDetailsRequest2.json";

    @Test
    public void testPrePoulateLegalAdvisorDetails_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + legalAdvisorEndpoint);
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        httpPost.addHeader("Authorization", "Bearer testauthtoken");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testAllocateJudgeWhenJudgeDetailsProvided_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + allocateJudgeEndpoint);
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_JUDGE);
        httpPost.addHeader("Authorization", "Bearer testauthtoken");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testAllocateJudgeWhenLegalAdvisorDetailsProvided_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + allocateJudgeEndpoint);
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_LEGALADVISOR);
        httpPost.addHeader("Authorization", "Bearer testauthtoken");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testAllocateJudgeWhenTierOfJudiciaryProvided_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + allocateJudgeEndpoint);
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_LEGALADVISOR);
        httpPost.addHeader("Authorization", "Bearer testauthtoken");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(
            HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }
}
