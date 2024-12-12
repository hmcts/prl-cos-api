package uk.gov.hmcts.reform.prl.controllers.citizen;


import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.UploadDocumentService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenDocumentService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseDocumentControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    DocumentGenService documentGenService;

    @MockBean
    UploadDocumentService uploadService;

    @MockBean
    AuthorisationService authorisationService;

    @MockBean
    CoreCaseDataApi coreCaseDataApi;
    @MockBean
    IdamClient idamClient;

    @MockBean
    CaseService caseService;

    @MockBean
    EmailService emailService;

    @MockBean
    CitizenDocumentService citizenDocumentService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testDeleteCitizenStatementDocument() throws Exception {


        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        Map<String, Object> map = new HashMap<>();
        map.put("caseId", "123");
        map.put("citizenUploadedDocumentList", List.of(element(UploadedDocuments.builder().build())));


        when(coreCaseDataApi.getCase(anyString(), anyString(), anyString())).thenReturn(CaseDetails.builder()
                                                                                            .id(123L)
                                                                                            .data(map)
                                                                                            .build());

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        String url = "/delete-citizen-statement-document";
        String jsonRequest = "{\"values\": {\"caseId\": \"123\", \"documentId\": \"456\"}}";

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}
