package uk.gov.hmcts.reform.prl.springfunctional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;
import uk.gov.hmcts.reform.prl.utils.TestConstants;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.prl.tasks.emails.ExampleEmailTaskTest.expectedPersonalisation;

@ContextConfiguration(classes = Application.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yaml")
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@Ignore
public class ExampleSendEmailTest {

    private static final String API_URL = "/send-email";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private EmailService emailService;

    @Autowired
    private MockMvc webClient;

    @Test
    public void emailShouldBeSent() throws Exception {
        callEndpointWithData();

        verify(emailService).send(
            TestConstants.TEST_SOLICITOR_EMAIL,
            EmailTemplateNames.EXAMPLE,
            expectedPersonalisation(),
            LanguagePreference.english
        );
    }

    private void callEndpointWithData() throws Exception {
        CallbackRequest input = CallbackRequest.builder()
            .caseDetails(CaseDetailsProvider.full())
            .build();

        webClient.perform(
            post(API_URL)
                .content(convertObjectToJsonString(input))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    static String convertObjectToJsonString(final Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }
}
