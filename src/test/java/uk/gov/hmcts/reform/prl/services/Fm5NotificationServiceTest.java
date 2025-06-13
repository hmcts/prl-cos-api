package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.Fm5PendingParty;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationDetails;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationType;
import uk.gov.hmcts.reform.prl.models.dto.notification.PartyType;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ENG_STATIC_DOCS_PATH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MULTIPART_FILE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;
import static uk.gov.hmcts.reform.prl.services.Fm5NotificationService.BLANK_FM5_FILE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class Fm5NotificationServiceTest {

    private CaseData caseData;
    PartyDetails applicant;
    PartyDetails respondent;

    @InjectMocks
    private Fm5NotificationService fm5NotificationService;

    @Mock
    private ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Mock
    private ServiceOfApplicationPostService serviceOfApplicationPostService;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private SendgridService sendgridService;

    @Mock
    private DocumentLanguageService documentLanguageService;

    @Mock
    private DocumentLanguage documentLanguage;

    @Mock
    private DgsService dgsService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private Document document;

    @Mock
    private DocumentUtils documentUtils;

    @Mock
    DgsApiClient dgsApiClient;

    @BeforeEach
    void setUp() throws Exception {
        applicant = PartyDetails.builder()
            .firstName("app FN")
            .lastName("app LN")
            .email("app@test.com")
            .solicitorEmail("app.sol@test.com")
            .contactPreferences(ContactPreferences.post)
            .representativeFirstName("app LR FN")
            .representativeLastName("app LR LN")
            .address(Address.builder().addressLine1("test").build())
            .build();

        respondent = PartyDetails.builder()
            .firstName("resp FN")
            .lastName("resp LN")
            .email("resp@test.com")
            .solicitorEmail("resp.sol@test.com")
            .contactPreferences(ContactPreferences.post)
            .representativeFirstName("resp LR FN")
            .representativeLastName("resp LR LN")
            .address(Address.builder().addressLine1("test").build())
            .build();

        caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("Test case")
            .applicants(List.of(element(applicant)))
            .respondents(List.of(element(respondent)))
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder().url("test")
            .build();

        when(systemUserService.getSysUserToken()).thenReturn("authToken");

        documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.anyString(),Mockito.anyString(),
                                         Mockito.anyMap()
        ))
            .thenReturn(generatedDocumentInfo);

    }

    @Test
    void sendFm5ReminderForApplicantSolicitors() {
        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(caseData, Fm5PendingParty.APPLICANT);

        //verify
        assertFalse(notifications.isEmpty());
        assertNotNull(notifications.getFirst().getValue().getPartyId());
        assertEquals(PartyType.APPLICANT_SOLICITOR, notifications.getFirst().getValue().getPartyType());
        assertEquals(NotificationType.SENDGRID_EMAIL, notifications.getFirst().getValue().getNotificationType());
    }

    @Test
    void sendFm5ReminderForRespondentSolicitors() {
        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(caseData, Fm5PendingParty.RESPONDENT);

        //verify
        assertFalse(notifications.isEmpty());
        assertNotNull(notifications.getFirst().getValue().getPartyId());
        assertEquals(PartyType.RESPONDENT_SOLICITOR, notifications.getFirst().getValue().getPartyType());
        assertEquals(NotificationType.SENDGRID_EMAIL, notifications.getFirst().getValue().getNotificationType());
    }

    @Test
    void sendFm5ReminderForBothApplicantRespondentSolicitors() {
        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(caseData, Fm5PendingParty.BOTH);

        //verify
        assertFalse(notifications.isEmpty());
        assertNotNull(notifications.getFirst().getValue().getPartyId());
        assertEquals(PartyType.APPLICANT_SOLICITOR, notifications.getFirst().getValue().getPartyType());
        assertEquals(PartyType.RESPONDENT_SOLICITOR, notifications.get(1).getValue().getPartyType());
        assertEquals(NotificationType.SENDGRID_EMAIL, notifications.getFirst().getValue().getNotificationType());
    }

    @Test
    void sendFm5ReminderForNoApplicantRespondentSolicitorsNotification() {
        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService
            .sendFm5ReminderNotifications(caseData, Fm5PendingParty.NOTIFICATION_NOT_REQUIRED);

        //verify
        assertTrue(notifications.isEmpty());
        assertEquals(0, notifications.size());
    }

    @Test
    void sendFm5ReminderForNoApplicantEmail() {

        applicant = applicant.toBuilder().solicitorEmail("").build();
        caseData = caseData.toBuilder().applicants(List.of(element(applicant))).build();

        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(
            caseData,
            Fm5PendingParty.APPLICANT
        );

        //verify
        assertFalse(notifications.isEmpty());
        assertNotNull(notifications.getFirst().getValue().getPartyId());
        assertEquals(PartyType.APPLICANT, notifications.getFirst().getValue().getPartyType());
        assertEquals(1, notifications.size());
    }

    @Test
    void sendFm5ReminderForNoRespondentEmail() {

        respondent = respondent.toBuilder().contactPreferences(ContactPreferences.email).solicitorEmail("").build();
        caseData = caseData.toBuilder().respondents(List.of(element(respondent))).build();

        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(
            caseData,
            Fm5PendingParty.RESPONDENT
        );

        //verify
        assertFalse(notifications.isEmpty());
        assertNotNull(notifications.getFirst().getValue().getPartyId());
        assertEquals(PartyType.RESPONDENT, notifications.getFirst().getValue().getPartyType());
        assertEquals(1, notifications.size());
    }

    @Test
    void sendFm5ReminderForNoApplicantAddress() {

        applicant = applicant.toBuilder().solicitorEmail("").address(null).build();
        caseData = caseData.toBuilder().applicants(List.of(element(applicant))).build();

        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(
            caseData,
            Fm5PendingParty.APPLICANT
        );

        //verify
        assertFalse(notifications.isEmpty());
        assertNotNull(notifications.getFirst().getValue().getPartyId());
        assertEquals(PartyType.APPLICANT, notifications.getFirst().getValue().getPartyType());
        assertEquals(1, notifications.size());
    }

    @Test
    void sendFm5ReminderForNoRespondentAddress() {

        respondent = respondent.toBuilder().solicitorEmail("").address(Address.builder().addressLine1(null).build()).build();
        caseData = caseData.toBuilder().respondents(List.of(element(respondent))).build();

        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(
            caseData,
            Fm5PendingParty.RESPONDENT
        );

        //verify
        assertFalse(notifications.isEmpty());
        assertNotNull(notifications.getFirst().getValue().getPartyId());
        assertEquals(PartyType.RESPONDENT, notifications.getFirst().getValue().getPartyType());
        assertEquals(1, notifications.size());
    }



    @Test
    void sendFm5ReminderForUploadResponse() {

        final MultipartFile file = new InMemoryMultipartFile(SOA_MULTIPART_FILE,
                                                             BLANK_FM5_FILE,
                                                             APPLICATION_PDF_VALUE,
                                                             DocumentUtils.readBytes(URL_STRING
                                                                                         + ENG_STATIC_DOCS_PATH + BLANK_FM5_FILE));


        UploadResponse uploadResponse = new UploadResponse(List.of(AmendOrderServiceTest.testDocument()));
        when(caseDocumentClient.uploadDocuments("authToken", authTokenGenerator.generate(),
                                                PrlAppsConstants.CASE_TYPE,
                                                PrlAppsConstants.JURISDICTION,
                                                List.of(file)))
            .thenReturn(uploadResponse);
        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(caseData, Fm5PendingParty.RESPONDENT);

        //verify
        assertFalse(notifications.isEmpty());
        assertNotNull(notifications.getFirst().getValue().getPartyId());
        assertEquals(PartyType.RESPONDENT_SOLICITOR, notifications.getFirst().getValue().getPartyType());
        assertEquals(NotificationType.SENDGRID_EMAIL, notifications.getFirst().getValue().getNotificationType());
    }

    @Test
    void sendFm5ReminderForApplicantException() throws Exception {

        applicant = applicant.toBuilder().solicitorEmail("").build();
        caseData = caseData.toBuilder().applicants(List.of(element(applicant))).build();

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.anyString(),Mockito.anyString(),
                                         Mockito.anyMap()
        )).thenThrow(new RuntimeException());

        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(
            caseData,
            Fm5PendingParty.APPLICANT
        );

        //verify
        assertFalse(notifications.isEmpty());
        assertNotNull(notifications.getFirst().getValue().getPartyId());
        assertEquals(PartyType.APPLICANT, notifications.getFirst().getValue().getPartyType());
        assertEquals(1, notifications.size());
    }


    @Test
    void sendFm5ReminderForRespondentException() {

        respondent = respondent.toBuilder().solicitorEmail("").build();
        caseData = caseData.toBuilder().respondents(List.of(element(respondent))).build();
        when(serviceOfApplicationPostService.getCoverSheets(any(),
                                                            any(),
                                                            any(),
                                                            any(),
                                                            any()
        )).thenThrow(new RuntimeException());

        NullPointerException ex = assertThrows(NullPointerException.class, () -> {
            fm5NotificationService.sendFm5ReminderNotifications(caseData, Fm5PendingParty.RESPONDENT);
        });

        assertEquals("Cannot invoke \"java.util.Collection.toArray()\" because \"c\" is null", ex.getMessage());
    }

    @Test
    void sendFm5ReminderForNoRespondentEmailNotification() {

        respondent = respondent.toBuilder().solicitorEmail("")
            .user(User.builder().idamId("123").build())
            .contactPreferences(ContactPreferences.email)
            .address(Address.builder().addressLine1(null).build()).build();
        caseData = caseData.toBuilder().respondents(List.of(element(respondent))).build();

        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(
            caseData,
            Fm5PendingParty.RESPONDENT
        );

        //verify
        assertFalse(notifications.isEmpty());
        assertNotNull(notifications.getFirst().getValue().getPartyId());
        assertEquals(PartyType.RESPONDENT, notifications.getFirst().getValue().getPartyType());
        assertEquals(1, notifications.size());
    }


}
