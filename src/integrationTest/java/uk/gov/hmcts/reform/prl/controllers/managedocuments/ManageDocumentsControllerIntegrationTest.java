package uk.gov.hmcts.reform.prl.controllers.managedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;

import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ManageDocumentsControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    ManageDocumentsService manageDocumentsService;

    @MockBean
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    @Test
    public void testAboutToStartManageDocuments() throws Exception {
        String url = "/manage-documents/about-to-start";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(manageDocumentsService.copyDocument(any(), any())).thenReturn(new HashMap<>());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testMidEventManageDocuments() throws Exception {
        String url = "/manage-documents/mid-event";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(manageDocumentsService.validateRestrictedReason(any(), any())).thenReturn(new ArrayList<>());
        Mockito.when(manageDocumentsService.validateCourtUser(any(), any())).thenReturn(new ArrayList<>());
        Mockito.when(userService.getUserDetails(any())).thenReturn(UserDetails.builder().build());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testCopyManageDocs() throws Exception {
        String url = "/manage-documents/copy-manage-docs";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(manageDocumentsService.copyDocument(any(), any())).thenReturn(new HashMap<>());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testSubmittedManageDocuments() throws Exception {
        String url = "/manage-documents/submitted";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.doNothing().when(manageDocumentsService).appendConfidentialDocumentNameForCourtAdminAndUpdate(any(), any());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}
