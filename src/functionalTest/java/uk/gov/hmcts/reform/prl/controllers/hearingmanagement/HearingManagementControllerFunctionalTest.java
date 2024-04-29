package uk.gov.hmcts.reform.prl.controllers.hearingmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.hearingmanagement.HearingManagementService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HearingManagementControllerFunctionalTest {

    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String VALID_HEARING_MANAGEMENT_REQUEST_BODY = "requests/hearing-management-controller.json";

    private static final String VALID_NEXT_HEARING_DETAILS_REQUEST_BODY = "requests/hearing-mgmnt-controller-next-hearing-details.json";

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @MockBean
    private HearingManagementService hearingManagementService;

    @MockBean
    private AuthorisationService authorisationService;

    private static final String VALID_CAFCASS_REQUEST_JSON = "requests/cafcass-cymru-send-email-request.json";

    private static CaseDetails caseDetails;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request1 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void createCcdTestCase() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);

        MvcResult res = mockMvc.perform(post("/testing-support/create-ccd-case-data")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
                                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                                            .content(requestBody)
                                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
        String json = res.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        caseDetails = mapper.readValue(json, CaseDetails.class);

        Assert.assertNotNull(caseDetails);
        Assert.assertNotNull(caseDetails.getId());
    }

    @Test
    public void givenRequestBody_whenHearing_management_state_update_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_HEARING_MANAGEMENT_REQUEST_BODY);
        String requestBodyRevised = requestBody
            .replace("123456789", caseDetails.getId().toString());
        request1
            .header("authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("serviceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .pathParam("caseState","DECISION_OUTCOME")
            .when().contentType(String.valueOf(MediaType.APPLICATION_JSON))
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .put("/hearing-management-state-update/{caseState}")
            .then().assertThat().statusCode(200);
    }

    @Test
    public void givenRequestBody_whenHearing_management_next_hearing_details_update_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_NEXT_HEARING_DETAILS_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace("1710453963689559", caseDetails.getId().toString());

        request1
            .header("authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("serviceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when().contentType(String.valueOf(MediaType.APPLICATION_JSON))
            .accept(String.valueOf(MediaType.APPLICATION_JSON))
            .put("/hearing-management-next-hearing-date-update")
            .then()
            .assertThat().statusCode(200);

    }

}
