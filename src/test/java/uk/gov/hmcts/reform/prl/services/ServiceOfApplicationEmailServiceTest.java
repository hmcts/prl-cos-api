package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenCaseSubmissionEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.time.Time;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_DASHBOARD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_CAFCASS_CYMRU;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceOfApplicationEmailServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    @Mock
    private SendgridService sendgridService;

    @InjectMocks
    private ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Mock
    private DgsService dgsService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private Time dateTime;

    @Mock
    private ObjectMapper objectMapper;

    @Value("${citizen.url}")
    private String citizenUrl;

    public static final String TEST_AUTH = "test auth";

    @Test
    public void testC100ApplicantsEmailNotification() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder()
                                            .solicitorEmail("test@gmail.com")
                                            .representativeLastName("LastName")
                                            .representativeFirstName("FirstName")
                                            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                                            .canYouProvideEmailAddress(YesOrNo.Yes)
                                            .email("test@applicant.com")
                                            .build())))
            .respondents(List.of(element(PartyDetails.builder()
                                             .solicitorEmail("test@gmail.com")
                                             .representativeLastName("LastName")
                                             .representativeFirstName("FirstName")
                                             .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                             .build())))

            .build();
        String applicantName = "FirstName LastName";

        EmailTemplateVars emailTemplateVars = CitizenCaseSubmissionEmail.builder()
            .caseNumber(String.valueOf(caseData.getId()))
            .applicantName(applicantName)
            .caseName(caseData.getApplicantCaseName())
            .caseLink(citizenUrl + CITIZEN_DASHBOARD)
            .build();
        doNothing().when(emailService).sendSoa("test@applicant.com", EmailTemplateNames.CA_APPLICANT_SERVICE_APPLICATION,
                                               emailTemplateVars, LanguagePreference.english);
        serviceOfApplicationEmailService.sendEmailToC100Applicants(caseData);

        verify(emailService, times(1)).sendSoa(Mockito.anyString(),
                                            Mockito.any(),
                                            Mockito.any(), Mockito.any()
        );
    }


    @Test
    public void testC100EmailNotificationForMultipleApplicants() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicants(List.of(
                element(PartyDetails.builder()
                            .solicitorEmail("test1@gmail.com")
                            .representativeLastName("LastName")
                            .representativeFirstName("FirstName")
                            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
                            .canYouProvideEmailAddress(YesOrNo.Yes)
                            .email("test1@applicant.com")
                            .build()),
                element(PartyDetails.builder()
                            .solicitorEmail("test2@gmail.com")
                            .representativeLastName("LastName1")
                            .representativeFirstName("FirstName1")
                            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                            .canYouProvideEmailAddress(YesOrNo.Yes)
                            .email("test2@applicant.com")
                            .build())
            ))
            .respondents(List.of(
                element(PartyDetails.builder()
                            .solicitorEmail("test@gmail.com")
                            .representativeLastName("LastName")
                            .representativeFirstName("FirstName")
                            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                            .build()),
                element(PartyDetails.builder()
                            .solicitorEmail("test@gmail.com")
                            .representativeLastName("LastName1")
                            .representativeFirstName("FirstName1")
                            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                            .build())
            ))
            .build();
        String applicantName = "FirstName LastName";

        EmailTemplateVars emailTemplateVars = CitizenCaseSubmissionEmail.builder()
            .caseNumber(String.valueOf(caseData.getId()))
            .applicantName(applicantName)
            .caseName(caseData.getApplicantCaseName())
            .caseLink(citizenUrl + CITIZEN_DASHBOARD)
            .build();
        doNothing().when(emailService).sendSoa("test@applicant.com", EmailTemplateNames.CA_APPLICANT_SERVICE_APPLICATION,
                                               emailTemplateVars, LanguagePreference.english);
        serviceOfApplicationEmailService.sendEmailToC100Applicants(caseData);

        verify(emailService, times(2)).sendSoa(Mockito.anyString(),
                                            Mockito.any(),
                                            Mockito.any(), Mockito.any()
        );
    }

    @Test
    public void testC100ApplicantSolicitorEmailNotification() throws Exception {

        PartyDetails applicant = PartyDetails.builder()
            .solicitorEmail("test@gmail.com")
            .representativeLastName("LastName")
            .representativeFirstName("FirstName")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@applicant.com")
            .build();

        Document finalDoc = Document.builder()
            .documentUrl("finalDoc")
            .documentBinaryUrl("finalDoc")
            .documentHash("finalDoc")
            .build();

        Document coverSheet = Document.builder()
            .documentUrl("coverSheet")
            .documentBinaryUrl("coverSheet")
            .documentHash("coverSheet")
            .build();

        final List<Document> documentList = List.of(coverSheet, finalDoc);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("test")
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(applicant)))
            .respondents(List.of(element(PartyDetails.builder()
                                  .solicitorEmail("test@gmail.com")
                                  .representativeLastName("LastName")
                                  .representativeFirstName("FirstName")
                                  .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                  .build())))
            .build();
        String applicantName = "FirstName LastName";

        final EmailTemplateVars emailTemplateVars = CitizenCaseSubmissionEmail.builder()
            .caseNumber(String.valueOf(caseData.getId()))
            .applicantName(applicantName)
            .caseName(caseData.getApplicantCaseName())
            .caseLink(citizenUrl + CITIZEN_DASHBOARD)
            .build();

        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", caseData.getApplicantCaseName());
        combinedMap.put("caseNumber", String.valueOf(caseData.getId()));
        combinedMap.put("solicitorName", applicant.getRepresentativeFullName());
        combinedMap.put("subject", "Case documents for : ");
        combinedMap.put("content", "Case details");
        combinedMap.put("attachmentType", "pdf");
        combinedMap.put("disposition", "attachment");

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String currentDate = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);

        EmailNotificationDetails emailNotificationDetails = EmailNotificationDetails.builder()
            .emailAddress("test@email.com")
            .servedParty(SERVED_PARTY_APPLICANT_SOLICITOR)
            .docs(documentList.stream().map(s -> element(s)).collect(Collectors.toList()))
            .attachedDocs(String.join(",", documentList.stream().map(a -> a.getDocumentFileName()).collect(
                Collectors.toList())))
            .timeStamp(currentDate).build();
        doNothing().when(emailService).sendSoa("test@applicant.com", EmailTemplateNames.APPLICANT_SOLICITOR_CA,
                                               emailTemplateVars, LanguagePreference.english);
        when(sendgridService.sendEmailWithAttachments(TEST_AUTH, combinedMap, applicant.getSolicitorEmail(),
                                                      documentList, SERVED_PARTY_APPLICANT_SOLICITOR))
            .thenReturn(emailNotificationDetails);

        assertEquals(emailNotificationDetails, serviceOfApplicationEmailService
            .sendEmailNotificationToApplicantSolicitor(TEST_AUTH, caseData,applicant,
                                                       EmailTemplateNames.APPLICANT_SOLICITOR_CA,
                                                       documentList, SERVED_PARTY_APPLICANT_SOLICITOR));


    }

    @Test
    public void testFL401ApplicantSolicitorEmailNotification() throws Exception {

        PartyDetails applicant = PartyDetails.builder()
            .solicitorEmail("test@gmail.com")
            .representativeLastName("LastName")
            .representativeFirstName("FirstName")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@applicant.com")
            .build();

        Document finalDoc = Document.builder()
            .documentUrl("finalDoc")
            .documentBinaryUrl("finalDoc")
            .documentHash("finalDoc")
            .build();

        Document coverSheet = Document.builder()
            .documentUrl("coverSheet")
            .documentBinaryUrl("coverSheet")
            .documentHash("coverSheet")
            .build();

        final List<Document> documentList = List.of(coverSheet, finalDoc);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("test")
            .caseTypeOfApplication("FL401")
            .applicantsFL401(applicant)
            .respondentsFL401(PartyDetails.builder()
                                  .solicitorEmail("test@gmail.com")
                                  .representativeLastName("LastName")
                                  .representativeFirstName("FirstName")
                                  .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                  .build())
            .build();
        String applicantName = "FirstName LastName";

        final EmailTemplateVars emailTemplateVars = CitizenCaseSubmissionEmail.builder()
            .caseNumber(String.valueOf(caseData.getId()))
            .applicantName(applicantName)
            .caseName(caseData.getApplicantCaseName())
            .caseLink(citizenUrl + CITIZEN_DASHBOARD)
            .build();

        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", caseData.getApplicantCaseName());
        combinedMap.put("caseNumber", String.valueOf(caseData.getId()));
        combinedMap.put("solicitorName", applicant.getRepresentativeFullName());
        combinedMap.put("subject", "Case documents for : ");
        combinedMap.put("content", "Case details");
        combinedMap.put("attachmentType", "pdf");
        combinedMap.put("disposition", "attachment");

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String currentDate = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);

        EmailNotificationDetails emailNotificationDetails = EmailNotificationDetails.builder()
            .emailAddress("test@email.com")
            .servedParty(SERVED_PARTY_APPLICANT_SOLICITOR)
            .docs(documentList.stream().map(s -> element(s)).collect(Collectors.toList()))
            .attachedDocs(String.join(",", documentList.stream().map(a -> a.getDocumentFileName()).collect(
                Collectors.toList())))
            .timeStamp(currentDate).build();
        doNothing().when(emailService).sendSoa("test@applicant.com", EmailTemplateNames.APPLICANT_SOLICITOR_DA,
                                               emailTemplateVars, LanguagePreference.english);
        when(sendgridService.sendEmailWithAttachments(TEST_AUTH, combinedMap, applicant.getSolicitorEmail(),
                                                      documentList, SERVED_PARTY_APPLICANT_SOLICITOR))
            .thenReturn(emailNotificationDetails);

        assertEquals(emailNotificationDetails, serviceOfApplicationEmailService
            .sendEmailNotificationToApplicantSolicitor(TEST_AUTH, caseData,applicant,
                                                       EmailTemplateNames.APPLICANT_SOLICITOR_DA,
                                                       documentList, SERVED_PARTY_APPLICANT_SOLICITOR));


    }

    @Test
    public void testC100FirstApplicantSolicitorEmailNotification() throws Exception {

        PartyDetails applicant = PartyDetails.builder()
            .solicitorEmail("test@gmail.com")
            .representativeLastName("LastName")
            .representativeFirstName("FirstName")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@applicant.com")
            .build();

        Document finalDoc = Document.builder()
            .documentUrl("finalDoc")
            .documentBinaryUrl("finalDoc")
            .documentHash("finalDoc")
            .build();

        Document coverSheet = Document.builder()
            .documentUrl("coverSheet")
            .documentBinaryUrl("coverSheet")
            .documentHash("coverSheet")
            .build();

        final List<Document> documentList = List.of(coverSheet, finalDoc);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("test")
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(applicant)))
            .respondents(List.of(element(PartyDetails.builder()
                                             .solicitorEmail("test@gmail.com")
                                             .representativeLastName("LastName")
                                             .representativeFirstName("FirstName")
                                             .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                             .build())))
            .build();
        String applicantName = "FirstName LastName";

        final EmailTemplateVars emailTemplateVars = CitizenCaseSubmissionEmail.builder()
            .caseNumber(String.valueOf(caseData.getId()))
            .applicantName(applicantName)
            .caseName(caseData.getApplicantCaseName())
            .caseLink(citizenUrl + CITIZEN_DASHBOARD)
            .build();

        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", caseData.getApplicantCaseName());
        combinedMap.put("caseNumber", String.valueOf(caseData.getId()));
        combinedMap.put("solicitorName", applicant.getRepresentativeFullName());
        combinedMap.put("subject", "Case documents for : ");
        combinedMap.put("content", "Case details");
        combinedMap.put("attachmentType", "pdf");
        combinedMap.put("disposition", "attachment");
        combinedMap.put("specialNote", "Yes");

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String currentDate = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);

        EmailNotificationDetails emailNotificationDetails = EmailNotificationDetails.builder()
            .emailAddress("test@email.com")
            .servedParty(SERVED_PARTY_APPLICANT_SOLICITOR)
            .docs(documentList.stream().map(s -> element(s)).collect(Collectors.toList()))
            .attachedDocs(String.join(",", documentList.stream().map(a -> a.getDocumentFileName()).collect(
                Collectors.toList())))
            .timeStamp(currentDate).build();
        doNothing().when(emailService).sendSoa("test@applicant.com", EmailTemplateNames.APPLICANT_SOLICITOR_CA,
                                               emailTemplateVars, LanguagePreference.english);
        when(sendgridService.sendEmailWithAttachments(TEST_AUTH, combinedMap, applicant.getSolicitorEmail(),
                                                      documentList, SERVED_PARTY_APPLICANT_SOLICITOR))
            .thenReturn(emailNotificationDetails);

        assertEquals(emailNotificationDetails, serviceOfApplicationEmailService
            .sendEmailNotificationToFirstApplicantSolicitor(TEST_AUTH, caseData,applicant,
                                                       EmailTemplateNames.APPLICANT_SOLICITOR_CA,
                                                       documentList, SERVED_PARTY_APPLICANT_SOLICITOR));


    }

    @Test
    public void testC100RespondentSolicitorEmailNotification() throws Exception {

        PartyDetails respondent = PartyDetails.builder()
            .solicitorEmail("test@gmail.com")
            .representativeLastName("LastName")
            .representativeFirstName("FirstName")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@applicant.com")
            .build();

        Document finalDoc = Document.builder()
            .documentUrl("finalDoc")
            .documentBinaryUrl("finalDoc")
            .documentHash("finalDoc")
            .build();

        Document coverSheet = Document.builder()
            .documentUrl("coverSheet")
            .documentBinaryUrl("coverSheet")
            .documentHash("coverSheet")
            .build();

        final List<Document> documentList = List.of(coverSheet, finalDoc);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("test")
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder()
                                            .solicitorEmail("test@gmail.com")
                                            .representativeLastName("LastName")
                                            .representativeFirstName("FirstName")
                                            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                            .build())))
            .respondents(List.of(element(respondent)))
            .build();
        String applicantName = "FirstName LastName";

        final EmailTemplateVars emailTemplateVars = CitizenCaseSubmissionEmail.builder()
            .caseNumber(String.valueOf(caseData.getId()))
            .applicantName(applicantName)
            .caseName(caseData.getApplicantCaseName())
            .caseLink(citizenUrl + CITIZEN_DASHBOARD)
            .build();

        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", caseData.getApplicantCaseName());
        combinedMap.put("caseNumber", String.valueOf(caseData.getId()));
        combinedMap.put("solicitorName", respondent.getRepresentativeFullName());
        combinedMap.put("subject", "Case documents for : ");
        combinedMap.put("content", "Case details");
        combinedMap.put("attachmentType", "pdf");
        combinedMap.put("disposition", "attachment");

        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        String currentDate = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss").format(zonedDateTime);

        EmailNotificationDetails emailNotificationDetails = EmailNotificationDetails.builder()
            .emailAddress("test@email.com")
            .servedParty(SERVED_PARTY_RESPONDENT_SOLICITOR)
            .docs(documentList.stream().map(s -> element(s)).collect(Collectors.toList()))
            .attachedDocs(String.join(",", documentList.stream().map(a -> a.getDocumentFileName()).collect(
                Collectors.toList())))
            .timeStamp(currentDate).build();
        doNothing().when(emailService).sendSoa("test@applicant.com", EmailTemplateNames.RESPONDENT_SOLICITOR,
                                               emailTemplateVars, LanguagePreference.english);
        when(sendgridService.sendEmailWithAttachments(TEST_AUTH, combinedMap, respondent.getSolicitorEmail(),
                                                      documentList, SERVED_PARTY_RESPONDENT_SOLICITOR))
            .thenReturn(emailNotificationDetails);

        assertEquals(emailNotificationDetails, serviceOfApplicationEmailService
            .sendEmailNotificationToRespondentSolicitor(TEST_AUTH, caseData,respondent,
                                                       documentList, SERVED_PARTY_RESPONDENT_SOLICITOR));

    }

    @Test
    public void testCafcassEmailNotification() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder()
                                            .solicitorEmail("test@gmail.com")
                                            .representativeLastName("LastName")
                                            .representativeFirstName("FirstName")
                                            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                                            .canYouProvideEmailAddress(YesOrNo.Yes)
                                            .email("test@applicant.com")
                                            .build())))
            .respondents(List.of(element(PartyDetails.builder()
                                             .solicitorEmail("test@gmail.com")
                                             .representativeLastName("LastName")
                                             .representativeFirstName("FirstName")
                                             .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                                             .build())))

            .build();
        String applicantName = "FirstName LastName";

        EmailTemplateVars emailTemplateVars = CitizenCaseSubmissionEmail.builder()
            .caseNumber(String.valueOf(caseData.getId()))
            .applicantName(applicantName)
            .caseName(caseData.getApplicantCaseName())
            .caseLink(citizenUrl + CITIZEN_DASHBOARD)
            .build();
        doNothing().when(emailService).sendSoa("test@applicant.com", EmailTemplateNames.CA_APPLICANT_SERVICE_APPLICATION,
                                               emailTemplateVars, LanguagePreference.english);
        serviceOfApplicationEmailService.sendEmailNotificationToCafcass(caseData, "test@applicant.com", SERVED_PARTY_CAFCASS_CYMRU);

        verify(emailService, times(1)).sendSoa(Mockito.anyString(),
                                               Mockito.any(),
                                               Mockito.any(), Mockito.any()
        );
    }

}
