package uk.gov.hmcts.reform.prl.controllers.courtnav;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.io.File;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CourtNavCaseControllerFunctionalTest {


    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String VALID_REQUEST_BODY = "requests/courtnav-request.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }


    @Test
    public void givenCourtNavCaseCreationData_then200Response() throws Exception {

        log.info("Request: {}", request);


        String randomServerPort = "4044";
        final String baseUrl = "http://localhost:" + randomServerPort + "/case";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", idamTokenGenerator.generateIdamTokenForCourtNav());
        headers.set("ServiceAuthorization", serviceAuthenticationGenerator.generateApiGwServiceAuth());
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> result = this.restTemplate.postForEntity(uri, request, String.class);

        assertEquals(201, result.getStatusCodeValue());
    }


    @Test
    public void givenNoCaseDataInRequestBody_then400Response() throws Exception {
        String randomServerPort = "4044";
        final String baseUrl = "http://localhost:" + randomServerPort + "/case";
        URI uri = new URI(baseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Auth");
        headers.set("ServiceAuthorization", "ServAuth");
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> result = this.restTemplate.postForEntity(uri, request, String.class);

        Assert.assertEquals(403, result.getStatusCodeValue());
        Assert.assertEquals(true, result.getBody().contains("Missing request header"));



    }

    @Ignore
    @Test
    public void givenValidDocumentData_then200Response() {
        request
            .header(
                "Authorization",
                idamTokenGenerator.generateIdamTokenForCourtNav(),
                "ServiceAuthorization",
                serviceAuthenticationGenerator.generateApiGwServiceAuth()
            )
            .multiPart("file",new File("courtnav/Dummy_pdf_file.pdf"))
            .pathParam("caseId","1647520545879276")
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .post("/{caseId}/document")
            .then().assertThat().statusCode(200);


    }
}
