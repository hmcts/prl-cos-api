package uk.gov.hmcts.reform.prl.tasks.emails;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;
import uk.gov.hmcts.reform.prl.utils.TaskContextProvider;
import uk.gov.hmcts.reform.prl.utils.TestConstants;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ExampleEmailTaskTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ExampleEmailTask task;

    @Test
    public void getTemplateShouldReturnValidValue() {
        assertThat(task.getTemplate(), is(EmailTemplateNames.EXAMPLE));
    }

    @Test
    public void getPersonalisationShouldReturnModel() {
        assertThat(
            task.getPersonalisation(TaskContextProvider.empty(), CaseDetailsProvider.empty()),
            is(expectedPersonalisation())
        );
    }

    @Test
    public void executeCallsEmailService() {
        task.execute(TaskContextProvider.empty(), CaseDetailsProvider.empty());

        verify(emailService).send(
            TestConstants.TEST_SOLICITOR_EMAIL,
            EmailTemplateNames.EXAMPLE,
            expectedPersonalisation(),
            LanguagePreference.english
        );
    }

    public static EmailTemplateVars expectedPersonalisation() {
        return CitizenEmail.builder()
            .caseReference(TestConstants.TEST_CASE_ID)
            .petitionerName(TestConstants.TEST_PETITIONER_NAME)
            .respondentName(TestConstants.TEST_RESPONDENT_NAME)
            .build();
    }
}
