package uk.gov.hmcts.reform.prl.controllers;

import jakarta.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class SendAndReplyControllerIT {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    CaseDocumentClient caseDocumentClient;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String taskListControllerEndPoint = "/update-task-list/submitted";

    private final String validBody = "requests/send-and-reply-case-data.json";

    @Test
    public void givenWac_whenServletContext_thenItProvidesSendAndReplyController() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(webApplicationContext.getBean("SendAndReplyController"));
    }

    @Test
    public void whenSendAndReplyMidEventValidRequest_Return200() throws Exception {

        HttpPost httpPost = new HttpPost(serviceUrl + "/send-and-reply-to-messages/mid-event");
        String requestBody = ResourceLoader.loadJson(validBody);
        httpPost.addHeader("Authorization", "TestAuth");
        httpPost.addHeader("serviceAuthorization", "s2sToken");
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        Map<String, Object> httpResponse = (Map<String, Object>) HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(httpResponse.get("http_status"),"200 OK");
        assertNotNull(httpResponse);
        assertNull(httpResponse);

    }
}

