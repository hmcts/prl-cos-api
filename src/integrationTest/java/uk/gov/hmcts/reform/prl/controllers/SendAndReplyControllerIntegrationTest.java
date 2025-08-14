package uk.gov.hmcts.reform.prl.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;
import uk.gov.hmcts.reform.prl.services.UploadAdditionalApplicationService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

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
public class SendAndReplyControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    SendAndReplyService sendAndReplyService;

    @MockBean
    ElementUtils elementUtils;

    @MockBean
    AllTabServiceImpl allTabService;

    @MockBean
    UploadAdditionalApplicationService uploadAdditionalApplicationService;

    @Autowired
    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    @Test
    public void testAboutToStart() throws Exception {
        String url = "/send-and-reply-to-messages/about-to-start";
        String jsonRequest = ResourceLoader.loadJson("requests/send-and-reply-case-data.json");

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
    public void testMidEvent() throws Exception {
        String url = "/send-and-reply-to-messages/mid-event";
        String jsonRequest = ResourceLoader.loadJson("requests/send-and-reply-case-data.json");

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
    public void testAboutToSubmit() throws Exception {
        String url = "/send-and-reply-to-messages/about-to-submit";
        String jsonRequest = ResourceLoader.loadJson("requests/send-and-reply-case-data.json");

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
    public void testSubmitted() throws Exception {
        String url = "/send-and-reply-to-messages/submitted";
        String jsonRequest = ResourceLoader.loadJson("requests/send-and-reply-case-data.json");

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
    public void testSendOrReplyToMessagesAboutToStart() throws Exception {
        String url = "/send-and-reply-to-messages/send-or-reply-to-messages/about-to-start";
        String jsonRequest = ResourceLoader.loadJson("requests/send-and-reply-case-data.json");

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
    public void testSendOrReplyToMessagesMidEvent() throws Exception {
        String url = "/send-and-reply-to-messages/send-or-reply-to-messages/mid-event";
        String jsonRequest = ResourceLoader.loadJson("requests/send-and-reply-case-data.json");

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
    public void testSendOrReplyToMessagesAboutToSubmit() throws Exception {
        String url = "/send-and-reply-to-messages/send-or-reply-to-messages/about-to-submit";
        String jsonRequest = ResourceLoader.loadJson("requests/send-and-reply-case-data.json");

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
    public void testSendOrReplyToMessagesSubmitted() throws Exception {
        String url = "/send-and-reply-to-messages/send-or-reply-to-messages/submitted";
        String jsonRequest = ResourceLoader.loadJson("requests/send-and-reply-case-data.json");

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
    public void testClearDynamicLists() throws Exception {
        String url = "/send-and-reply-to-messages/send-or-reply-to-messages/clear-dynamic-lists";
        String jsonRequest = ResourceLoader.loadJson("requests/send-and-reply-case-data.json");

        when(sendAndReplyService.resetSendAndReplyDynamicLists(any())).thenReturn(CaseData.builder().build());

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
