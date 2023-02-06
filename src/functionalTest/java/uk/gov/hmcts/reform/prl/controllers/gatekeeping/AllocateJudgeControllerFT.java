package uk.gov.hmcts.reform.prl.controllers.gatekeeping;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
public class AllocateJudgeControllerFT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private CaseSummaryTabService caseSummaryTabService;

    @MockBean
    RefDataUserService refDataUserService;


    @MockBean
    private AllocatedJudgeService allocatedJudgeService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    private static final String LEGAL_ADVISER_PREPOPULATE_VALID_REQUEST_BODY = "controller/valid-request-body.json";

    private static final String ALLOCATE_JUDGE_VALID_REQUEST_BODY = "requests/gatekeeping/AllocateJudgeDetailsRequest1.json";

    private static final String ALLOCATE_LEGAL_ADVISER_VALID_REQUEST_BODY = "requests/gatekeeping/LegalAdvisorApiRequest.json";

    private static final String ALLOCATE_TIER_OF_JUDICIARY_VALID_REQUEST_BODY = "requests/gatekeeping/AllocateJudgeDetailsRequest2.json";

    private final String prePopulateLegalAdvisersEndpoint = "/allocateJudge/pre-populate-legalAdvisor-details";

    private final String allocateJudgeEndpoint = "/allocateJudge/allocatedJudgeDetails";

    @Test
    public void testPrepopulateLegalAdviserDetails_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(LEGAL_ADVISER_PREPOPULATE_VALID_REQUEST_BODY);

        mockMvc.perform(post(prePopulateLegalAdvisersEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "auth")
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("errors").isEmpty())
            .andReturn();

    }

    @Test
    public void testAllocateJudgeWhenJudgeDetailsOptionSelected_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(ALLOCATE_JUDGE_VALID_REQUEST_BODY);

        mockMvc.perform(post(allocateJudgeEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "auth")
                .header("ServiceAuthorization", "serviceAuth")
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("errors").isEmpty())
            .andReturn();

    }

    @Test
    public void testAllocateJudgeWhenLegalAdvisorOptionSelected_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(ALLOCATE_LEGAL_ADVISER_VALID_REQUEST_BODY);

        mockMvc.perform(post(allocateJudgeEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "auth")
                .header("ServiceAuthorization", "serviceAuth")
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("errors").isEmpty())
            .andReturn();

    }

    @Test
    public void testAllocateJudgeWhenTierOfJudiciaryOptionSelected_200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(ALLOCATE_TIER_OF_JUDICIARY_VALID_REQUEST_BODY);

        mockMvc.perform(post(allocateJudgeEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "auth")
                .header("ServiceAuthorization", "serviceAuth")
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("errors").isEmpty())
            .andReturn();

    }
}
