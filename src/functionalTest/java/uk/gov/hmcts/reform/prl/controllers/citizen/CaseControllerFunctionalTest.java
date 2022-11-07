package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;

import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseControllerFunctionalTest {


    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    CoreCaseDataApi coreCaseDataApi;

    @MockBean
    ObjectMapper objectMapper;

    @MockBean
    CaseService caseService;

    @MockBean
    AuthorisationService authorisationService;

    @MockBean
    protected IdamTokenGenerator idamTokenGenerator;

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    private static final String VALID_REQUEST_BODY = "requests/case-controller-update-case-request.json";

    @Test
    public void givenCaseIdInGetCaseRequest_then200Response() throws Exception {
        String token = "Beare token";

        CaseData caseData = CaseData.builder()
            .build();

        CaseDetails caseDetails =  CaseDetails.builder()
                .id(1645102524592014L)
            .data(new HashMap<>())
            .build();
        Mockito.when(caseService.getCase(token,"1645102524592014")).thenReturn(caseDetails);
        Mockito.when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        mockMvc.perform(get("/1645102524592014")
                            .header("Authorization", token)
                            .header("ServiceAuthorization", "s2sToken"))
            .andExpect(status().isOk())
            .andReturn();
    }



    @Test
    public void givenCitizenUidRetrieveCasesForCitizenUser_then200Response() throws Exception {
        String token = "Beare token";

        CaseData caseData = CaseData.builder()
            .build();

        CaseDetails caseDetails =  CaseDetails.builder()
            .id(1645102524592014L)
            .data(new HashMap<>())
            .build();
        Mockito.when(authTokenGenerator.generate()).thenReturn("s2sToken");
        Mockito.when(caseService
                         .retrieveCases(Mockito.anyString(),Mockito.anyString(),
                                        Mockito.anyString(),Mockito.anyString())).thenReturn(List.of(caseData));


        mockMvc.perform(get("/citizen/citizen-user/retrieve-cases/citizenUserId")
                            .header("Authorization", token)
                            .header("ServiceAuthorization", "s2sToken")
                            .contentType("application/json"))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenSystemUserRetrieveCases_then200Response() throws Exception {
        String token = "Beare token";

        CaseData caseData = CaseData.builder()
            .build();

        CaseDetails caseDetails =  CaseDetails.builder()
            .id(1645102524592014L)
            .data(new HashMap<>())
            .build();

        Mockito.when(authTokenGenerator.generate()).thenReturn("s2sToken");
        Mockito.when(caseService.retrieveCases(Mockito.anyString(),Mockito.anyString())).thenReturn(List.of(caseData));

        mockMvc.perform(get("/cases")
                            .header("Authorization", token)
                            .header("ServiceAuthorization", "s2sToken")
                            .contentType("application/json"))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenAccessCodeThenLinkCase_then200Response() throws Exception {
        String token = "Beare token";

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);

        CaseData caseData = CaseData.builder()
            .build();

        CaseDetails caseDetails =  CaseDetails.builder()
            .id(1645102524592014L)
            .data(new HashMap<>())
            .build();


        mockMvc.perform(post("/citizen/link")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", token)
                            .header("ServiceAuthorization", "s2sToken")
                            .header("accessCode","ABCD")
                            .header("caseId","1645102524592014L")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenAccessCodeValidateAccessCode_then200Response() throws Exception {
        String token = "Beare token";

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);

        CaseData caseData = CaseData.builder()
            .build();

        CaseDetails caseDetails =  CaseDetails.builder()
            .id(1645102524592014L)
            .data(new HashMap<>())
            .build();

        Mockito.when(authTokenGenerator.generate()).thenReturn("s2sToken");
        Mockito.when(caseService.validateAccessCode(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn("access-code-matched");
        Mockito.when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);


        mockMvc.perform(get("/validate-access-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", token)
                            .header("ServiceAuthorization", "s2sToken")
                            .header("accessCode","ABCD")
                            .header("caseId","1645102524592014L"))
            .andExpect(status().isOk())
            .andReturn();
    }

}
