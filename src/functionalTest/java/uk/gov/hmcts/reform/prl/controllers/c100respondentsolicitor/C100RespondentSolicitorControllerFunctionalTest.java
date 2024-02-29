package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class C100RespondentSolicitorControllerFunctionalTest {

    private final String userToken = "Bearer testToken";
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private C100RespondentSolicitorService respondentSolicitorService;
    @MockBean
    private DocumentGenService documentGenService;
    @MockBean
    private ConfidentialDetailsMapper confidentialDetailsMapper;
    private static final String VALID_REQUEST_BODY = "requests/c100-respondent-solicitor-call-back-controller.json";

    private static final String VALID_REQUEST_BODY_FOR_C1A_DRAFT = "requests/c100-respondent-solicitor-c1adraft-generate.json";

    private static final String VALID_REQUEST_BODY_FOR_C1A_FINAL = "requests/c100-respondent-solicitor-c1afinal-generate.json";

    private static final String VALID_REQUEST_BODY_C1A = "requests/c100-respondent-solicitor-C1A.json";

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenRequestBody_whenRespondent_solicitor_about_to_start_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/respondent-solicitor/about-to-start")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("errors").isEmpty())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenRespondent_solicitor_about_to_submit_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/respondent-solicitor/about-to-submit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("errors").isEmpty())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenAbout_to_start_response_validation_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/respondent-solicitor/about-to-start-response-validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("errors").isEmpty())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenSubmit_c7_response_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/respondent-solicitor/submit-c7-response")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("errors").isEmpty())
            .andReturn();
    }

    @Test
    @Ignore
    public void givenRequestBody_whenKeep_details_private_list_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/respondent-solicitor/keep-details-private-list")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("errors").isEmpty())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenGenerate_c7response_draft_document_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        mockMvc.perform(post("/respondent-solicitor/generate-c7response-document")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("errors").isEmpty())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenAbout_to_start_response_validation_then200Response_C1A() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_C1A);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/respondent-solicitor/about-to-start-response-validation")
            .then()
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    /**
     * Respondent solicitor ViewResponseDraftDocument journey - C1A both English and Welsh Draft document should be generated.
     */
    @Test
    public void givenRequestBody_whenGenerate_c7c1a_draft_welshAndEnglish_document() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_FOR_C1A_DRAFT);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/respondent-solicitor/generate-c7response-document")
            .then()
            .body("data.draftC1ADocWelsh.document_filename", equalTo("Draft_C1A_allegation_of_harm_Welsh.pdf"),
                  "data.draftC1ADoc.document_filename", equalTo("Draft_C1A_allegation_of_harm.pdf"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
    }

    /**
     * Respondent solicitor submit journey - C1A both English and Welsh Final document should be generated
     * and moved to quarantine (Documents To be Reviewed).
     */
    @Test
    public void givenRequestBody_whenSubmit_c7C1A_final_welshAndEnglish_document() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_FOR_C1A_FINAL);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/respondent-solicitor/submit-c7-response")
            .then()
            .body("data.legalProfQuarantineDocsList[2].value.document.document_filename", equalTo("C1A_allegation_of_harm.pdf"),
                  "data.legalProfQuarantineDocsList[2].value.categoryId", equalTo("respondentC1AApplication"),
                  "data.legalProfQuarantineDocsList[2].value.categoryName", equalTo("Respondent C1A Application"),
                  "data.legalProfQuarantineDocsList[3].value.document.document_filename", equalTo("Final_C1A_allegation_of_harm_Welsh.pdf"),
                  "data.legalProfQuarantineDocsList[3].value.categoryId", equalTo("respondentC1AApplication"),
                  "data.legalProfQuarantineDocsList[3].value.categoryName", equalTo("Respondent C1A Application"))
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

}
