package uk.gov.hmcts.reform.prl.controllers.citizen;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.UploadDocumentEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CaseDocumentControllerTest {

    @InjectMocks
    private CaseDocumentController caseDocumentController;

    @Mock
    private DocumentGenService documentGenService;

    private GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest;

    @Mock
    private EmailService emailService;

    @Before
    public void setUp() {

        generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(Map.of("fileName", "test.docx"))
            .build();
    }

    @Test
    public void testNotifyOtherPartiesRespondedentCA() throws Exception {
        User user = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fb").build();
        PartyDetails applicant = PartyDetails.builder().user(user).email("test@hmcts.net").firstName("test").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        User user1 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fa").build();
        PartyDetails respondent = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondent);

        CaseData casedata = CaseData.builder().id(123456).caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).applicants(
            applicantList).respondents(respondentList).build();


        Method method = CaseDocumentController.class.getDeclaredMethod(
            "notifyOtherParties",
            String.class,
            CaseData.class
        );
        method.setAccessible(true);
        method.invoke(caseDocumentController, "577346ec-5c58-491d-938a-112c4bff06fb", casedata);
        UploadDocumentEmail emailTemplateVars = UploadDocumentEmail.builder().caseReference("123456").name("test").build();
        verify(emailService).send(
            "test@hmcts.net",
            EmailTemplateNames.DOCUMENT_UPLOADED,
            emailTemplateVars,
            LanguagePreference.english
        );
    }

    @Test
    public void testNotifyOtherPartiesRespondedentsCA() throws Exception {
        User user = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fb").build();
        PartyDetails applicant = PartyDetails.builder().user(user).email("test@hmcts.net").firstName("test").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        User user1 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fa").build();
        PartyDetails respondent = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        User user2 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fc").build();
        PartyDetails respondent1 = PartyDetails.builder().user(user2).email("test1@hmcts.net").firstName("test").build();
        Element<PartyDetails> wrappedRespondent1 = Element.<PartyDetails>builder().value(respondent1).build();
        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondent);
        respondentList.add(wrappedRespondent1);

        CaseData casedata = CaseData.builder().id(123456).caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).applicants(
            applicantList).respondents(respondentList).build();


        Method method = CaseDocumentController.class.getDeclaredMethod(
            "notifyOtherParties",
            String.class,
            CaseData.class
        );
        method.setAccessible(true);
        method.invoke(caseDocumentController, "577346ec-5c58-491d-938a-112c4bff06fb", casedata);
        UploadDocumentEmail emailTemplateVars = UploadDocumentEmail.builder().caseReference("123456").name("test").build();
        verify(emailService).send(
            "test@hmcts.net",
            EmailTemplateNames.DOCUMENT_UPLOADED,
            emailTemplateVars,
            LanguagePreference.english
        );
        verify(emailService).send(
            "test1@hmcts.net",
            EmailTemplateNames.DOCUMENT_UPLOADED,
            emailTemplateVars,
            LanguagePreference.english
        );
    }

    @Test
    public void testNotifyOtherPartiesApplicantCA() throws Exception {
        User user = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fb").build();
        PartyDetails applicant = PartyDetails.builder().user(user).email("test@hmcts.net").firstName("test").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        User user1 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fa").build();
        PartyDetails respondent = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondent);

        CaseData casedata = CaseData.builder().id(123456).caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).applicants(
            applicantList).respondents(respondentList).build();


        Method method = CaseDocumentController.class.getDeclaredMethod(
            "notifyOtherParties",
            String.class,
            CaseData.class
        );
        method.setAccessible(true);
        method.invoke(caseDocumentController, "577346ec-5c58-491d-938a-112c4bff06fa", casedata);
        UploadDocumentEmail emailTemplateVars = UploadDocumentEmail.builder().caseReference("123456").name("test").build();
        verify(emailService).send(
            "test@hmcts.net",
            EmailTemplateNames.DOCUMENT_UPLOADED,
            emailTemplateVars,
            LanguagePreference.english
        );
    }

    @Test
    public void testNotifyOtherPartiesApplicantDA() throws Exception {
        User user = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fb").build();
        PartyDetails applicant = PartyDetails.builder().user(user).email("test@hmcts.net").firstName("test").build();

        User user1 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fa").build();
        PartyDetails respondent = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();

        CaseData casedata = CaseData.builder().id(123456).caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE).applicantsFL401(
            applicant).respondentsFL401(respondent).build();


        Method method = CaseDocumentController.class.getDeclaredMethod(
            "notifyOtherParties",
            String.class,
            CaseData.class
        );
        method.setAccessible(true);
        method.invoke(caseDocumentController, "577346ec-5c58-491d-938a-112c4bff06fa", casedata);
        UploadDocumentEmail emailTemplateVars = UploadDocumentEmail.builder().caseReference("123456").name("test").build();
        verify(emailService).send(
            "test@hmcts.net",
            EmailTemplateNames.DOCUMENT_UPLOADED,
            emailTemplateVars,
            LanguagePreference.english
        );
    }

    @Test
    public void testNotifyOtherPartiesRespondentDA() throws Exception {
        User user = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fb").build();
        PartyDetails applicant = PartyDetails.builder().user(user).email("test@hmcts.net").firstName("test").build();

        User user1 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fa").build();
        PartyDetails respondent = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();

        CaseData casedata = CaseData.builder().id(123456).caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE).applicantsFL401(
            applicant).respondentsFL401(respondent).build();


        Method method = CaseDocumentController.class.getDeclaredMethod(
            "notifyOtherParties",
            String.class,
            CaseData.class
        );
        method.setAccessible(true);
        method.invoke(caseDocumentController, "577346ec-5c58-491d-938a-112c4bff06fb", casedata);
        UploadDocumentEmail emailTemplateVars = UploadDocumentEmail.builder().caseReference("123456").name("test").build();
        verify(emailService).send(
            "test@hmcts.net",
            EmailTemplateNames.DOCUMENT_UPLOADED,
            emailTemplateVars,
            LanguagePreference.english
        );
    }
}
