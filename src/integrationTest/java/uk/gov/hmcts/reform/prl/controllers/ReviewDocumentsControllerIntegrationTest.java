package uk.gov.hmcts.reform.prl.controllers;

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
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.reviewdocument.ReviewDocumentService;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ReviewDocumentsControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;


    @MockBean
    ReviewDocumentService reviewDocumentService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testHandleAboutToStart() throws Exception {
        String url = "/review-documents/about-to-start";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(reviewDocumentService.fetchDocumentDynamicListElements(any(), any())).thenReturn(new ArrayList<>());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "testAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testHandleMidEvent() throws Exception {
        String url = "/review-documents/mid-event";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        mockMvc.perform(
                post(url)
                    .header("Authorization", "testAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testHandleAboutToSubmit() throws Exception {
        String url = "/review-documents/about-to-submit";
        String jsonRequest = ResourceLoader.loadJson("requests/review-doc-body.json");

        mockMvc.perform(
                post(url)
                    .header("Authorization", "testAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testHandleSubmitted() throws Exception {
        String url = "/review-documents/submitted";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        mockMvc.perform(
                post(url)
                    .header("Authorization", "testAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}
