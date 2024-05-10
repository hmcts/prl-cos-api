package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenResponseNotificationEmailService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.controllers.ManageOrdersControllerFunctionalTest.VALID_CAFCASS_REQUEST_JSON;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseApplicationResponseControllerFunctionalTest {

    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private CaseService caseService;
    @MockBean
    private CitizenResponseNotificationEmailService citizenResponseNotificationEmailService;

    @MockBean
    private DocumentGenService documentGenService;

    private static final String SLASH = "/";

    private static final String PATH_CORE = SLASH.concat("cases").concat(SLASH);


    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    private  IdamTokenGenerator idamTokenGenerator;

    @Value("${TEST_URL}")
    protected String cosApiUrl;

    private static CaseDetails caseDetails;

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
    public void givenRequestBody_whenGenerate_c7document_then200Response() throws Exception {
        //Element<PartyDetails> partyDetailsElement = element(PartyDetails.builder().firstName("test").build());
        String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);
        caseDetails =  request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/testing-support/create-ccd-case-data")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(CaseDetails.class);

        Long id = caseDetails.getId();
        List<Map> respondents = (List) caseDetails.getData().get("respondents");

        Document response1 = RestAssured.given().relaxedHTTPSValidation().baseUri(cosApiUrl)
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body("")
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/" + id + "/"+respondents.stream().findFirst().get().get("id")+"/generate-c7document")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(Document.class);

    }
    public RequestSpecification getMultipleAuthHeaders() {
        return SerenityRest.with()
            .relaxedHTTPSValidation()
            .baseUri(cosApiUrl)
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd());
    }
}
