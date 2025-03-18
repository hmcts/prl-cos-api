package uk.gov.hmcts.reform.prl.services.notifications;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.exception.BulkPrintException;
import uk.gov.hmcts.reform.prl.exception.SendGridNotificationException;
import uk.gov.hmcts.reform.prl.models.Address;
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
import uk.gov.hmcts.reform.prl.services.BulkPrintService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationPostService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.util.ArrayList;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_C1A_RESPONSE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BULK_SCAN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURTNAV;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_PROFESSIONAL;
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

    @Mock
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    private ServiceOfApplicationPostService serviceOfApplicationPostService;

    @Mock
    private BulkPrintService bulkPrintService;

    private CaseData caseData;
    private CaseData fl401CaseData;
    private QuarantineLegalDoc quarantineLegalDoc;
    private boolean isCaseTypeIdC100 = true;
    private String uploaderRole;

    @Before
    public void init() throws Exception {
        when(systemUserService.getSysUserToken()).thenReturn("auth");
        uploaderRole = CITIZEN;
        Document doc1 = Document.builder().build();
        quarantineLegalDoc = QuarantineLegalDoc.builder()
            .citizenQuarantineDocument(doc1)
            .courtNavQuarantineDocument(doc1)
            .url(doc1)
            .courtStaffQuarantineDocument(doc1)
            .cafcassQuarantineDocument(doc1)
            .document(doc1)
            .solicitorRepresentedPartyName("name")
            .uploadedBy("test")
            .uploaderRole(uploaderRole)
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
            .email("afl41@test.com")
            .address(Address.builder().addressLine1("test").build())
            .build();

        PartyDetails applicant5 = PartyDetails.builder()
            .firstName("af5").lastName("al5")
            .canYouProvideEmailAddress(YesOrNo.No)
            .address(Address.builder().addressLine1("test").build())
            .build();

        PartyDetails applicant6 = PartyDetails.builder()
            .firstName("af6").lastName("al6")
            .canYouProvideEmailAddress(YesOrNo.No)
            .email("afl61@test.com")
            .build();
        PartyDetails applicant7 = PartyDetails.builder()
            .firstName("af7").lastName("al7")
            .canYouProvideEmailAddress(YesOrNo.No)
            .email("afl71@test.com")
            .address(Address.builder().build())
            .build();

        PartyDetails applicant8 = PartyDetails.builder()
            .build();

        List<Document> documents = new ArrayList<>();
        documents.add(Document.builder().documentFileName("TestFileName").build());
        caseData = CaseData.builder()
            .id(123)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("test_case")
            .applicants(List.of(element(applicant1), element(applicant2), element(applicant3), element(applicant4),
                element(applicant5),element(applicant6),element(applicant7),element(applicant8)))
            .build();

        fl401CaseData = CaseData.builder()
            .id(123)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantCaseName("test_case")
            .applicants(List.of(element(applicant1), element(applicant2), element(applicant3), element(applicant4)))
            .build();
        when(serviceOfApplicationPostService.getCoverSheets(Mockito.any(), Mockito.anyString(), Mockito.any(), Mockito.anyString(),
                                                            Mockito.anyString()))
            .thenReturn(documents);
        when(serviceOfApplicationService.fetchCoverLetter(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyBoolean(),
                                                          Mockito.anyBoolean()))
            .thenReturn(documents);
    }

    @Test
    public void testNotificationsWhenRespondentSubmitsResponse_C7Application() {
        testNotificationsWhenRespondentSubmitsResponse(
            "respondentApplication",
            EmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT_SOLICITOR
        );
    }

    @Test
    public void testNotificationsWhenRespondentSubmitsResponseAndCaseTypeIdIsFL401() {
        isCaseTypeIdC100 = false;
        testNotificationsWhenRespondentSubmitsResponse(
            "respondentApplication",
            EmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT_SOLICITOR
        );
        isCaseTypeIdC100 = true;
    }

    @Test
    public void testNotificationsWhenRespondentSubmitsResponseAndCategoryIsApplicantC1AResponse() {

        testNotificationsWhenRespondentSubmitsResponse(
            APPLICANT_C1A_RESPONSE,
            EmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT_SOLICITOR
        );
    }

    @Test
    public void testNotificationsWhenBulkPrintExceptionOccurs() {
        when(bulkPrintService.send(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any()
        )).thenThrow(BulkPrintException.class);
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId("respondentApplication")
            .uploaderRole(uploaderRole)
            .build();

        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(DocumentLanguage.builder().isGenEng(
            true).build());

        assertExpectedException(() -> {
            notificationService.sendNotifications(isCaseTypeIdC100 ? caseData : fl401CaseData,
                                                  quarantineLegalDoc,
                                                  uploaderRole);
        }, RuntimeException.class);
    }

    @Test
    public void testNotificationsWhenUserRoleIsCourtNav() {
        uploaderRole = COURTNAV;
        testNotificationsWhenRespondentSubmitsResponse(
            "respondentApplication",
            EmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT_SOLICITOR
        );
        //defaulting to CITIZEN post verification
        uploaderRole = CITIZEN;
    }

    @Test
    public void testNotificationsWhenUserRoleIsLegalProfessional() {
        uploaderRole = LEGAL_PROFESSIONAL;
        testNotificationsWhenRespondentSubmitsResponse(
            "respondentApplication",
            EmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT_SOLICITOR
        );
        //defaulting to CITIZEN post verification
        uploaderRole = CITIZEN;
    }

    @Test
    public void testNotificationsWhenUserRoleIsCafcass() {
        uploaderRole = CAFCASS;
        testNotificationsWhenRespondentSubmitsResponse(
            "respondentApplication",
            EmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT_SOLICITOR
        );
        //defaulting to CITIZEN post verification
        uploaderRole = CITIZEN;
    }

    @Test
    public void testNotificationsWhenUserRoleIsCourtStaff() {
        uploaderRole = COURT_STAFF;
        testNotificationsWhenRespondentSubmitsResponse(
            "respondentApplication",
            EmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT_SOLICITOR
        );
        //defaulting to CITIZEN post verification
        uploaderRole = CITIZEN;
    }

    @Test
    public void testNotificationsWhenUserRoleIsBulkScan() {
        uploaderRole = BULK_SCAN;
        testNotificationsWhenRespondentSubmitsResponse(
            "respondentApplication",
            EmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C7_NOTIFICATION_APPLICANT_SOLICITOR
        );
        //defaulting to CITIZEN post verification
        uploaderRole = CITIZEN;
    }



    @Test
    public void testNotificationsWhenSendGridNotificationExceptionOccurs() {

        when(sendgridService.sendEmailUsingTemplateWithAttachments(
            Mockito.any(),
            Mockito.anyString(),
            Mockito.any()
        )).thenThrow(SendGridNotificationException.class);
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId("respondentApplication")
            .uploaderRole(CITIZEN)
            .build();

        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(DocumentLanguage.builder().isGenEng(
            true).build());

        assertExpectedException(() -> {
            notificationService.sendNotifications(isCaseTypeIdC100 ? caseData : fl401CaseData,
                                                  quarantineLegalDoc,
                                                  CITIZEN);
        }, RuntimeException.class);
    }

    @Test
    public void testNotificationsWhenRespondentSubmitsResponse_C1AApplication() {
        testNotificationsWhenRespondentSubmitsResponse(
            "respondentC1AApplication",
            EmailTemplateNames.C1A_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C1A_NOTIFICATION_APPLICANT,
            SendgridEmailTemplateNames.C1A_NOTIFICATION_APPLICANT_SOLICITOR
        );
    }

    @Test
    public void testNotificationsWhenRespondentSubmitsResponse_C1AResponse() {
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
                                                               SendgridEmailTemplateNames solicitorEmailTemplate) {
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId(category)
            .uploaderRole(uploaderRole)
            .build();

        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(DocumentLanguage.builder().isGenEng(
            true).build());

        notificationService.sendNotifications(isCaseTypeIdC100 ? caseData : fl401CaseData,
                                              quarantineLegalDoc,
                                              uploaderRole);
        int numberOfExpectedInvocations = 0;
        //In source file currently there is no APPLICANT_C1A_RESPONSE. Only for testcase, below categoryId check is added
        if (isCaseTypeIdC100 && !APPLICANT_C1A_RESPONSE.equalsIgnoreCase(quarantineLegalDoc.getCategoryId())) {
            numberOfExpectedInvocations = 1;
        }
        verify(emailService, times(numberOfExpectedInvocations)).send(eq("afl11@test.com"),
                                            eq(emailTemplate), any(),
                                            eq(LanguagePreference.english)
        );

        verify(sendgridService, times(numberOfExpectedInvocations)).sendEmailUsingTemplateWithAttachments(
            eq(sendGridEmailTemplate),
            anyString(),
            any(SendgridEmailConfig.class)
        );

        verify(sendgridService, times(numberOfExpectedInvocations)).sendEmailUsingTemplateWithAttachments(
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
                    .build()))).build();

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
    public void testSendNotificationsAsync() {
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

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        Assert.assertNotNull(exception.getMessage());
    }
}
