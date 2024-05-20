package uk.gov.hmcts.reform.prl.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.FmPendingParty;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationDetails;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationType;
import uk.gov.hmcts.reform.prl.models.dto.notification.PartyType;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;

import java.util.List;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class Fm5NotificationServiceTest {

    private CaseData caseData;
    PartyDetails applicant;
    PartyDetails respondent;


    public static final String PRL_DRAFT_TEMPLATE = "FL-DIV-GOR-ENG-00062.docx";

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
    DgsApiClient dgsApiClient;

    @Before
    public void setUp() throws Exception {
        applicant = PartyDetails.builder()
            .firstName("app FN")
            .lastName("app LN")
            .email("app@test.com")
            .solicitorEmail("app.sol@test.com")
            .representativeFirstName("app LR FN")
            .representativeLastName("app LR LN")
            .address(Address.builder().addressLine1("test").build())
            .build();

        respondent = PartyDetails.builder()
            .firstName("resp FN")
            .lastName("resp LN")
            .email("resp@test.com")
            .solicitorEmail("resp.sol@test.com")
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
    public void sendFm5ReminderForApplicantSolicitors() {
        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(caseData, FmPendingParty.APPLICANT);

        //verify
        Assert.assertFalse(notifications.isEmpty());
        Assert.assertNotNull(notifications.get(0).getValue().getPartyId());
        Assert.assertEquals(PartyType.APPLICANT_SOLICITOR, notifications.get(0).getValue().getPartyType());
        Assert.assertEquals(NotificationType.SENDGRID_EMAIL, notifications.get(0).getValue().getNotificationType());
    }

    @Test
    public void sendFm5ReminderForRespondentSolicitors() {
        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(caseData, FmPendingParty.RESPONDENT);

        //verify
        Assert.assertFalse(notifications.isEmpty());
        Assert.assertNotNull(notifications.get(0).getValue().getPartyId());
        Assert.assertEquals(PartyType.RESPONDENT_SOLICITOR, notifications.get(0).getValue().getPartyType());
        Assert.assertEquals(NotificationType.SENDGRID_EMAIL, notifications.get(0).getValue().getNotificationType());
    }

    @Test
    public void sendFm5ReminderForBothApplicantRespondentSolicitors() {
        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(caseData, FmPendingParty.BOTH);

        //verify
        Assert.assertFalse(notifications.isEmpty());
        Assert.assertNotNull(notifications.get(0).getValue().getPartyId());
        Assert.assertEquals(PartyType.APPLICANT_SOLICITOR, notifications.get(0).getValue().getPartyType());
        Assert.assertEquals(PartyType.RESPONDENT_SOLICITOR, notifications.get(1).getValue().getPartyType());
        Assert.assertEquals(NotificationType.SENDGRID_EMAIL, notifications.get(0).getValue().getNotificationType());
    }

    @Test
    public void sendFm5ReminderForNoApplicantRespondentSolicitorsNotification() {
        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService
            .sendFm5ReminderNotifications(caseData, FmPendingParty.NOTIFICATION_NOT_REQUIRED);

        //verify
        Assert.assertTrue(notifications.isEmpty());
        Assert.assertEquals(0, notifications.size());
    }

    @Test
    public void sendFm5ReminderForNoApplicantEmail() {

        applicant = applicant.toBuilder().solicitorEmail("").build();
        caseData = caseData.toBuilder().applicants(List.of(element(applicant))).build();

        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(
            caseData,
            FmPendingParty.APPLICANT
        );

        //verify
        Assert.assertFalse(notifications.isEmpty());
        Assert.assertNotNull(notifications.get(0).getValue().getPartyId());
        Assert.assertEquals(PartyType.APPLICANT, notifications.get(0).getValue().getPartyType());
        Assert.assertEquals(1, notifications.size());
    }

    @Test
    public void sendFm5ReminderForNoApplicantAddress() {

        applicant = applicant.toBuilder().solicitorEmail("").address(null).build();
        caseData = caseData.toBuilder().applicants(List.of(element(applicant))).build();

        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(
            caseData,
            FmPendingParty.APPLICANT
        );

        //verify
        Assert.assertFalse(notifications.isEmpty());
        Assert.assertNotNull(notifications.get(0).getValue().getPartyId());
        Assert.assertEquals(PartyType.APPLICANT, notifications.get(0).getValue().getPartyType());
        Assert.assertEquals(1, notifications.size());
    }

    @Test
    public void sendFm5ReminderForNoRespondentAddress() {

        respondent = respondent.toBuilder().solicitorEmail("").address(Address.builder().addressLine1(null).build()).build();
        caseData = caseData.toBuilder().applicants(List.of(element(respondent))).build();

        //invoke
        List<Element<NotificationDetails>> notifications = fm5NotificationService.sendFm5ReminderNotifications(
            caseData,
            FmPendingParty.RESPONDENT
        );

        //verify
        Assert.assertFalse(notifications.isEmpty());
        Assert.assertNotNull(notifications.get(0).getValue().getPartyId());
        Assert.assertEquals(PartyType.RESPONDENT_SOLICITOR, notifications.get(0).getValue().getPartyType());
        Assert.assertEquals(1, notifications.size());
    }

}
