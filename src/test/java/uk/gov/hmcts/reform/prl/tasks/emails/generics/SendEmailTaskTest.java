package uk.gov.hmcts.reform.prl.tasks.emails.generics;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.framework.context.TaskContext;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.utils.CaseDataProvider;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsProvider;
import uk.gov.hmcts.reform.prl.utils.EmailTemplateVarsProvider;
import uk.gov.hmcts.reform.prl.utils.TaskContextProvider;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_EMAIL;

@RunWith(MockitoJUnitRunner.class)
public class SendEmailTaskTest {

    public static final EmailTemplateNames EMAIL_TEMPLATE_ID = EmailTemplateNames.EXAMPLE;

    @Mock
    private EmailService emailService;

    private SendEmailTask task;

    @Before
    public void getSendEmailTaskInstance() {
        task = new SendEmailTask(emailService) {
            @Override
            protected EmailTemplateVars getPersonalisation(TaskContext context, CaseDetails caseDetails) {
                return new EmailTemplateVars();
            }

            @Override
            protected EmailTemplateNames getTemplate() {
                return EMAIL_TEMPLATE_ID;
            }

            @Override
            protected String getRecipientEmail(CaseDetails caseDetails) {
                return TEST_EMAIL;
            }

            @Override
            protected boolean canEmailBeSent(CaseDetails caseDetails) {
                return caseDetails.getCaseData() != null;
            }
        };
    }

    @Test
    public void getLanguageShouldReturnEnglishWhenNotSpecified() {
        assertThat(task.getLanguage(CaseDetailsProvider.empty()), is(LanguagePreference.english));
    }

    @Test
    public void getLanguageShouldReturnEnglishWhenSetEnglishOrEmpty() {
        asList(CaseDataProvider.english(), CaseDataProvider.empty()).forEach(caseData -> {
            assertThat(
                task.getLanguage(CaseDetailsProvider.of(caseData)),
                is(LanguagePreference.english)
            );
        });
    }

    @Test
    public void getLanguageShouldReturnWelshWhenSetWelsh() {
        CaseData caseData = CaseDataProvider.welsh();

        assertThat(
            task.getLanguage(CaseDetailsProvider.of(caseData)),
            is(LanguagePreference.welsh)
        );
    }

    @Test
    public void canEmailBeSentShouldReturnTrue() {
        assertThat(task.canEmailBeSent(CaseDetailsProvider.empty()), is(true));
    }

    @Test
    public void getTemplateShouldReturnValidValue() {
        assertThat(task.getTemplate(), is(EmailTemplateNames.EXAMPLE));
    }

    @Test
    public void getPersonalisationShouldReturnModel() {
        assertThat(
            task.getPersonalisation(TaskContextProvider.empty(), CaseDetailsProvider.empty()),
            is(EmailTemplateVarsProvider.empty())
        );
    }

    /**
     * This will be improved once we define email fields in CaseData model. For now it's fake.
     */
    @Test
    public void getRecipientEmailShouldReturnEmail() {
        assertThat(
            task.getRecipientEmail(CaseDetailsProvider.empty()),
            is(TEST_EMAIL)
        );
    }

    @Test
    public void executeCallsEmailService() {
        task.execute(TaskContextProvider.empty(), CaseDetailsProvider.empty());

        verify(emailService).send(
            TEST_EMAIL,
            EMAIL_TEMPLATE_ID,
            EmailTemplateVarsProvider.empty(),
            LanguagePreference.english
        );
    }

    @Test
    public void executeDoesNotCallsEmailServiceWhenCaseDataIsNull() {
        task.execute(TaskContextProvider.empty(), CaseDetailsProvider.of(null));

        verifyNoInteractions(emailService);
    }
}
