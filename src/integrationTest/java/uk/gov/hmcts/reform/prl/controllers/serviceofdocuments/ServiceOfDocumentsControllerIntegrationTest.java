package uk.gov.hmcts.reform.prl.controllers.serviceofdocuments;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.controllers.ControllerTestSupport;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.serviceofdocuments.ServiceOfDocumentsService;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(ServiceOfDocumentsController.class)
@ExtendWith(SpringExtension.class)
@Import(ControllerTestSupport.class)
public class ServiceOfDocumentsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthorisationService authorisationService;

    @MockBean
    ServiceOfDocumentsService serviceOfDocumentsService;

    @Test
    public void testServiceOfDocumentsAboutToStart() throws Exception {
        String url = "/service-of-documents/about-to-start";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(serviceOfDocumentsService.validateDocuments(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testServiceOfDocumentsValidate() throws Exception {
        String url = "/service-of-documents/validate";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testServiceOfDocumentsAboutToSubmit() throws Exception {
        String url = "/service-of-documents/about-to-submit";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(serviceOfDocumentsService.handleAboutToSubmit(any(), any())).thenReturn(new HashMap<>());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testServiceOfDocumentsSubmitted() throws Exception {
        String url = "/service-of-documents/submitted";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(serviceOfDocumentsService.handleSubmitted(any(), any()))
            .thenReturn(ResponseEntity.ok(SubmittedCallbackResponse.builder().build()));

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}
