package uk.gov.hmcts.reform.prl.controllers.cafcass;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.CAFCASS_DOCUMENT_DOWNLOAD_ENDPOINT;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CafcassDocumentManagementControllerFunctionalTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    CaseDocumentClient caseDocumentClient;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenValidUuidDownloadFileWith200Response() throws Exception {
        final UUID documentId = UUID.randomUUID();
        Mockito.when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any(UUID.class)))
                .thenReturn(new ResponseEntity<Resource>(HttpStatus.OK));

        MvcResult mvcResult = mockMvc.perform(get(CAFCASS_DOCUMENT_DOWNLOAD_ENDPOINT, documentId)
                        .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                        .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

    }

    @Test
    public void givenInvalidUuidDownloadFileWith400Response() throws Exception {
        final UUID documentId = UUID.randomUUID();
        Mockito.when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any(UUID.class)))
                .thenReturn(new ResponseEntity<Resource>(HttpStatus.NOT_FOUND));

        MvcResult mvcResult = mockMvc.perform(get("/cases/documents/{documentId}/binary", documentId)
                        .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                        .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();

    }
}
