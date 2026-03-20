package uk.gov.hmcts.reform.prl.controllers.documentremoval;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DocumentRemovalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AuthorisationService authorisationService;

    @BeforeEach
    public void setUp() {
        objectMapper.findAndRegisterModules();
    }

    //Test
    public void testAboutToSubmit() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        String jsonRequest = ResourceLoader.loadJson("document-removal/about-to-submit.json");

        MvcResult result = mockMvc.perform(post("/document-removal/about-to-submit")
                            .header("Authorization", "Bearer test-token")
                            .header("ServiceAuthorization", "test-s2s-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.courtStaffUploadDocListDocTab").exists())
            .andExpect(jsonPath("$.data.courtStaffUploadDocListDocTab.lettersOfComplaintDocument").doesNotExist())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        CaseData updatedCaseData = objectMapper.readValue(responseBody, CaseData.class);
        assertThat(updatedCaseData).isNotNull();
    }

    //@Test
    public void testAboutToSubmitV2() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        String jsonRequest = ResourceLoader.loadJson("document-removal/about-to-submit-message.json");

        MvcResult result = mockMvc.perform(post("/document-removal/2/about-to-submit")
                                               .header("Authorization", "Bearer test-token")
                                               .header("ServiceAuthorization", "test-s2s-token")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON)
                                               .content(jsonRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.courtStaffUploadDocListDocTab").exists())
            .andExpect(jsonPath("$.data.courtStaffUploadDocListDocTab.lettersOfComplaintDocument").doesNotExist())
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        CaseData updatedCaseData = objectMapper.readValue(responseBody, CaseData.class);
        assertThat(updatedCaseData).isNotNull();
    }
}
