package uk.gov.hmcts.reform.prl.controllers.documentremoval;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.hamcrest.Matchers.hasSize;
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

    @Test
    public void testAboutToSubmitC100FinalDocument() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        String jsonRequest = ResourceLoader.loadJson("document-removal/about-to-submit/c100-final-document.json");

        String basePath = "$.data.finalServedApplicationDetailsList[0].value.";

        MvcResult result = mockMvc.perform(post("/document-removal/about-to-submit")
                                               .header("Authorization", "Bearer test-token")
                                               .header("ServiceAuthorization", "test-s2s-token")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON)
                                               .content(jsonRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.finalDocument").doesNotExist())
            .andExpect(jsonPath("$.data.documentRemovalDocumentToRemove").doesNotExist())
            .andExpect(jsonPath(basePath + "bulkPrintDetails[0].value.printDocs[?(@.id == '79e52430-f987-4669-931e-a48da8bf5c75')]")
                           .doesNotExist())
            .andExpect(jsonPath(basePath + "bulkPrintDetails[0].value.printDocs[?(@.value.document_filename == 'C100FinalDocument.pdf')]")
                           .doesNotExist())
            .andExpect(jsonPath(basePath + "bulkPrintDetails[0].value.printDocs", hasSize(16)))
            .andExpect(jsonPath(basePath + "emailNotificationDetails[0].value.docs[?(@.id == 'de918464-9af1-4daf-81ba-00dcc9aefb3f')]")
                           .doesNotExist())
            .andExpect(jsonPath(basePath + "emailNotificationDetails[0].value.docs[?(@.value.document_filename == 'C100FinalDocument.pdf')]")
                           .doesNotExist())
            .andExpect(jsonPath(basePath + "emailNotificationDetails[0].value.docs", hasSize(11)))
            .andReturn();

        // Verify data can be deserialized into CaseData
        String responseBody = result.getResponse().getContentAsString();
        CaseData updatedCaseData = objectMapper.readValue(responseBody, CaseData.class);
        assertThat(updatedCaseData).isNotNull();
    }

    @Test
    public void testAboutToSubmitOrder() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        String jsonRequest = ResourceLoader.loadJson("document-removal/about-to-submit/order.json");

        MvcResult result = mockMvc.perform(post("/document-removal/about-to-submit")
                                               .header("Authorization", "Bearer test-token")
                                               .header("ServiceAuthorization", "test-s2s-token")
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .accept(MediaType.APPLICATION_JSON)
                                               .content(jsonRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.documentRemovalDocumentToRemove").doesNotExist())
            .andExpect(jsonPath("$.data.orderCollection", hasSize(1)))
            .andExpect(jsonPath("$.data.orderCollection[0].value.orderType").exists())
            .andExpect(jsonPath("$.data.orderCollection[0].value.orderDocument").doesNotExist())
            .andReturn();

        // Verify data can be deserialized into CaseData
        String responseBody = result.getResponse().getContentAsString();
        CaseData updatedCaseData = objectMapper.readValue(responseBody, CaseData.class);
        assertThat(updatedCaseData).isNotNull();
    }

}
