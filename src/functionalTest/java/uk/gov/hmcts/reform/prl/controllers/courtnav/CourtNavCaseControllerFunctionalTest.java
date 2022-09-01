package uk.gov.hmcts.reform.prl.controllers.courtnav;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.io.File;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
public class CourtNavCaseControllerFunctionalTest {

    private final String userToken = "Bearer testToken";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String VALID_REQUEST_BODY = "requests/fl401-submit-application-controller-validation.json";

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
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header(
                "Authorization", idamTokenGenerator.generateIdamTokenForSystem(),
                "ServiceAuthorization", serviceAuthenticationGenerator.generateApiGwServiceAuth()
            )
            .body("requestBody")
            .when()
            .contentType("application/json")
            .post("/case")
            .then().assertThat().statusCode(201);

    }


    @Test
    public void givenNoCaseDataInRequestBody_then400Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", "Bearer xyz")
            .body("requestBody")
            .when()
            .contentType("application/json")
            .post("/case")
            .then().assertThat().statusCode(400);
    }

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
