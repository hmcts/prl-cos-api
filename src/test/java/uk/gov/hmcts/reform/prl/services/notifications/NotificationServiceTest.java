package uk.gov.hmcts.reform.prl.services.notifications;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.io.IOException;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_CAFCASS_CYMRU;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NotificationServiceTest {

    @InjectMocks
    NotificationService notificationService;

    @Mock
    private DocumentLanguageService documentLanguageService;

    @Mock
    private EmailService emailService;

    @Mock
    private SendgridService sendgridService;

    @Mock
    private SystemUserService systemUserService;

    private CaseData caseData;
    private QuarantineLegalDoc quarantineLegalDoc;

    @Before
    public void init() {
        when(systemUserService.getSysUserToken()).thenReturn("auth");

        quarantineLegalDoc = QuarantineLegalDoc.builder()
            .citizenQuarantineDocument(Document.builder().build())
            .solicitorRepresentedPartyName("name")
            .uploadedBy("test")
            .uploaderRole(CITIZEN)
            .build();

        PartyDetails applicant1 = PartyDetails.builder() //dashboard access
            .firstName("af1").lastName("al1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("afl11@test.com")
            .contactPreferences(ContactPreferences.email)
            .user(User.builder().idamId("1234").build())
            .build();
        PartyDetails applicant2 = PartyDetails.builder() //has solicitor
            .firstName("af2").lastName("al2")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("asf2").representativeLastName("asl2")
            .solicitorEmail("asl22@test.com")
            .build();
        PartyDetails applicant3 = PartyDetails.builder()
            .firstName("af3").lastName("al3")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .representativeFirstName("asf3").representativeLastName("asl3")
            .contactPreferences(ContactPreferences.email)
            .email("afl31@test.com")
            .build();
        PartyDetails applicant4 = PartyDetails.builder()
            .firstName("af4").lastName("al4")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .representativeFirstName("asf4").representativeLastName("asl4")
            .email("afl41@test.com")
            .build();
        caseData = CaseData.builder()
            .id(123)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("test_case")
            .applicants(List.of(element(applicant1), element(applicant2), element(applicant3), element(applicant4)))
            .build();
    }

    @Test
    public void testNotificationsWhenRespondentSubmitsResponse_C7Application() throws IOException {
        testNotificationsWhenRespondentSubmitsResponse(
            "respondentApplication",
            EmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT_SOLICITOR
        );
    }

    @Test
    public void testNotificationsWhenRespondentSubmitsResponse_C1AApplication() throws IOException {
        testNotificationsWhenRespondentSubmitsResponse(
            "respondentC1AApplication",
            EmailTemplateNames.C1A_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C1A_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C1A_NOTIFICATION_APPLICANT_SOLICITOR
        );
    }

    @Test
    public void testNotificationsWhenRespondentSubmitsResponse_C1AResponse() throws IOException {
        testNotificationsWhenRespondentSubmitsResponse(
            "respondentC1AResponse",
            EmailTemplateNames.C1A_RESPONSE_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C1A_RESPONSE_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C1A_RESPONSE_NOTIFICATION_APPLICANT_SOLICITOR
        );
    }

    public void testNotificationsWhenRespondentSubmitsResponse(String category,
                                                               EmailTemplateNames emailTemplate,
                                                               SendgridEmailTemplateNames sendGridEmailTemplate,
                                                               SendgridEmailTemplateNames solicitorEmailTemplate) throws IOException {
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId(category)
            .uploaderRole(CITIZEN)
            .build();

        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(DocumentLanguage.builder().isGenEng(
            true).build());


        notificationService.sendNotifications(caseData,
                                              quarantineLegalDoc,
                                              CITIZEN);

        verify(emailService, times(1)).send(eq("afl11@test.com"),
                                            eq(emailTemplate), any(),
                                            eq(LanguagePreference.english)
        );

        verify(sendgridService, times(1)).sendEmailUsingTemplateWithAttachments(
            eq(sendGridEmailTemplate),
            anyString(),
            any(SendgridEmailConfig.class)
        );

        verify(sendgridService, times(1)).sendEmailUsingTemplateWithAttachments(
            eq(solicitorEmailTemplate),
            anyString(),
            any(SendgridEmailConfig.class)
        );
    }


    @Test
    public void testCafcassCymruNotifications_C7Application() {
        testCafcassCymruNotifications(
            "respondentApplication",
            EmailTemplateNames.RESPONDENT_RESPONDED_CAFCASS
        );
    }

    @Test
    public void testCafcassCymruNotifications_C1AApplication() {
        testCafcassCymruNotifications(
            "respondentC1AApplication",
            EmailTemplateNames.RESPONDENT_ALLEGATIONS_OF_HARM_CAFCASS
        );
    }

    @Test
    public void testCafcassCymruNotifications_C1AResponse() {
        testCafcassCymruNotifications(
            "respondentC1AResponse",
            EmailTemplateNames.RESPONDENT_RESPONDED_ALLEGATIONS_OF_HARM_CAFCASS
        );
    }

    public void testCafcassCymruNotifications(String category,
                                              EmailTemplateNames emailTemplate) {

        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId(category)
            .citizenQuarantineDocument(Document.builder().build())
            .solicitorRepresentedPartyName("name")
            .uploaderRole(CITIZEN)
            .build();

        caseData = caseData.toBuilder()
            .finalServedApplicationDetailsList(List.of(element(
                ServedApplicationDetails.builder()
                    .emailNotificationDetails(List.of(element(
                        EmailNotificationDetails.builder()
                            .servedParty(SERVED_PARTY_CAFCASS_CYMRU)
                            .emailAddress("test@email.com")
                            .build()
                    )))
                    .build())))
            .build();

        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(DocumentLanguage.builder().isGenEng(
            true).build());

        notificationService.sendNotifications(caseData,
                                              quarantineLegalDoc,
                                              CITIZEN);

        //cafcass
        verify(emailService, times(1)).send(eq("test@email.com"),
                                            eq(emailTemplate), any(),
                                            eq(LanguagePreference.english)
        );
    }

    @Test
    public void testSendNotificationsAsync() throws IOException, InterruptedException {
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId("respondentApplication")
            .document(Document.builder().build())
            .uploaderRole(SOLICITOR)
            .build();

        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(DocumentLanguage.builder().isGenEng(
            true).build());

        notificationService.sendNotificationsAsync(caseData,
                                              quarantineLegalDoc,
                                              SOLICITOR);

        await().untilAsserted(() -> {
            verify(emailService, times(1)).send(eq("afl11@test.com"),
                                                eq(EmailTemplateNames.C7_NOTIFICATION_APPLICANT), any(),
                                                eq(LanguagePreference.english)
            );
            verify(sendgridService, times(1)).sendEmailUsingTemplateWithAttachments(
                eq(SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT),
                anyString(),
                any(SendgridEmailConfig.class)
            );
            verify(sendgridService, times(1)).sendEmailUsingTemplateWithAttachments(
                eq(SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT_SOLICITOR),
                anyString(),
                any(SendgridEmailConfig.class)
            );
        });
    }
}
