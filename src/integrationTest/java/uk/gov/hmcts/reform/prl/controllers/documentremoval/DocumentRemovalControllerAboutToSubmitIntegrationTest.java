package uk.gov.hmcts.reform.prl.controllers.documentremoval;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;

import java.io.UnsupportedEncodingException;

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
public class DocumentRemovalControllerAboutToSubmitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AuthorisationService authorisationService;

    private static final String DOCUMENT_REMOVAL_DOCUMENT_TO_REMOVE = "$.data.documentRemovalDocumentToRemove";

    @BeforeEach
    public void setUp() {
        objectMapper.findAndRegisterModules();
    }

    @Test
    public void testC100FinalDocument() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        String basePath = "$.data.finalServedApplicationDetailsList[0].value.";

        MvcResult result = mockMvc.perform(buildRequest("c100-final-document.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath(DOCUMENT_REMOVAL_DOCUMENT_TO_REMOVE).doesNotExist())
            .andExpect(jsonPath("$.data.finalDocument").doesNotExist())
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

        verifyResponseDeserialises(result);
    }

    @Test
    public void testOrder() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        MvcResult result = mockMvc.perform(buildRequest("order.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath(DOCUMENT_REMOVAL_DOCUMENT_TO_REMOVE).doesNotExist())
            .andExpect(jsonPath("$.data.orderCollection", hasSize(1)))
            .andExpect(jsonPath("$.data.orderCollection[0].value.orderType").exists())
            .andExpect(jsonPath("$.data.orderCollection[0].value.orderDocument").doesNotExist())
            .andReturn();

        verifyResponseDeserialises(result);
    }

    @Test
    public void testServiceOfApplication() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        MvcResult result = mockMvc.perform(buildRequest("service-of-application.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath(DOCUMENT_REMOVAL_DOCUMENT_TO_REMOVE).doesNotExist())
            .andExpect(jsonPath("$.data.orderCollection", hasSize(1)))
            .andExpect(jsonPath("$.data.orderCollection[0].value.orderType").exists())
            .andExpect(jsonPath("$.data.orderCollection[0].value.orderDocument").doesNotExist())
            .andExpect(jsonPath("$.data.finalServedApplicationDetailsList[0].value.emailNotificationDetails[0].value.docs", hasSize(10)))
            .andReturn();

        verifyResponseDeserialises(result);
    }

    @Test
    public void testSolicitorUploadedCaseDocument() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        MvcResult result = mockMvc.perform(buildRequest("case-document-solicitor-uploaded.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath(DOCUMENT_REMOVAL_DOCUMENT_TO_REMOVE).doesNotExist())
            .andExpect(jsonPath("$.data.legalProfUploadDocListDocTab", hasSize(0)))
            .andReturn();

        verifyResponseDeserialises(result);
    }

    @Test
    public void testSoASupportingDocument() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        MvcResult result = mockMvc.perform(buildRequest("service-of-application-supporting-document.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath(DOCUMENT_REMOVAL_DOCUMENT_TO_REMOVE).doesNotExist())
            .andExpect(jsonPath("$.data.finalServedApplicationDetailsList[0].value.emailNotificationDetails[0].value.docs", hasSize(10)))
            .andReturn();

        verifyResponseDeserialises(result);
    }

    @Test
    public void testApplicationWithinProceedingsC2() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        MvcResult result = mockMvc.perform(buildRequest("application-within-proceedings-c2.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath(DOCUMENT_REMOVAL_DOCUMENT_TO_REMOVE).doesNotExist())
            .andExpect(jsonPath("$.data.additionalApplicationsBundle[0].value.c2DocumentBundle.finalDocument").isEmpty())
            .andExpect(jsonPath("$.data.additionalApplicationsBundle[0].value.c2DocumentBundle.supportingEvidenceBundle", hasSize(1)))
            .andReturn();

        verifyResponseDeserialises(result);
    }

    @Test
    public void testDraftOrderAndMessage() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        MvcResult result = mockMvc.perform(buildRequest("draft-order-and-message.json"))
            .andExpect(status().isOk())
            .andExpect(jsonPath(DOCUMENT_REMOVAL_DOCUMENT_TO_REMOVE).doesNotExist())
            .andExpect(jsonPath("$.data.draftOrderCollection").isEmpty())
            .andExpect(jsonPath("$.data.messages[0].value.internalMessageAttachDocs").isEmpty())
            .andExpect(jsonPath("$.data.messages[0].value.messageIdentifier").value("79a658dd-b0ad-40e2-af7a-267a329a793c"))
            .andReturn();

        verifyResponseDeserialises(result);
    }

    private MockHttpServletRequestBuilder buildRequest(String jsonFile) throws Exception {
        String jsonRequest = ResourceLoader.loadJson("document-removal/about-to-submit/" + jsonFile);
        return post("/document-removal/about-to-submit")
            .header("Authorization", "Bearer test-token")
            .header("ServiceAuthorization", "test-s2s-token")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(jsonRequest);
    }

    private void verifyResponseDeserialises(MvcResult result) throws UnsupportedEncodingException, JsonProcessingException {
        String responseBody = result.getResponse().getContentAsString();
        CaseData updatedCaseData = objectMapper.readValue(responseBody, CaseData.class);
        assertThat(updatedCaseData).isNotNull();
    }
}
