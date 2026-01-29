package uk.gov.hmcts.reform.prl.services;

import ch.qos.logback.classic.Logger;
import com.google.common.collect.ImmutableMap;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.config.SendgridEmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.controllers.testingsupport.TestLogAppender;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.exception.SendGridNotificationException;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.json.JsonObject;

import static org.bouncycastle.cert.ocsp.OCSPResp.SUCCESSFUL;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SendgridServiceTest {

    @InjectMocks
    private SendgridService sendgridService;

    @Mock
    private SendGrid sendGrid;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private DocumentGenService documentGenService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private SendgridEmailTemplatesConfig sendgridEmailTemplatesConfig;

    @Value("${citizen.url}")
    private String citizenUrl;
    public static final String TEST_AUTH = "test auth";
    public static final String s2sToken = "s2s token";


    @Test
    public void testSendEmailInvokingSendGridApi() throws IOException {
        Response response = new Response();
        response.setStatusCode(200);
        JsonObject jsonObject = new NullAwareJsonObjectBuilder()
            .add("applicantCaseName","hello")
            .build();
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        sendgridService.sendEmail(jsonObject);
        verify(sendGrid,times(1)).api(any(Request.class));
    }


    @Test
    public void testSendEmailUsingTemplatesWithAttachments() throws Exception {

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
        when(launchDarklyClient.isFeatureEnabled("soa-sendgrid")).thenReturn(true);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody("test body");
        when(sendgridEmailTemplatesConfig.getTemplates())
            .thenReturn(
                ImmutableMap.of(
                    LanguagePreference.english, ImmutableMap.of(SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION, "111"),
                    LanguagePreference.welsh, ImmutableMap.of(SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION, "222")
                )
            );

        byte[] biteData = "test bytes".getBytes();
        for (Document d : documentList) {
            when(documentGenService.getDocumentBytes(d.getDocumentUrl(),
                                                     TEST_AUTH,
                                                     s2sToken)).thenReturn(biteData);
        }
        final Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "test auth");
        headers.put("Content-Type", "mail/send");
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(sendGrid.api(any(Request.class))).thenThrow(new IOException("expected exception"));
        Map<String, Object> dynamicTemplateData = new HashMap<>();
        SendgridEmailConfig sendgridEmailConfig = SendgridEmailConfig.builder().listOfAttachments(documentList)
            .toEmailAddress(applicant.getSolicitorEmail())
            .languagePreference(LanguagePreference.english)
            .dynamicTemplateData(dynamicTemplateData).build();
        assertThrows(
            SendGridNotificationException.class,
            () -> sendgridService
                .sendEmailUsingTemplateWithAttachments(
                    SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION,
                    TEST_AUTH,
                    sendgridEmailConfig
                )
        );
    }

    @Test
    public void testSendEmailUsingTemplatesWithAttachments_scenario3() throws Exception {

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
        when(launchDarklyClient.isFeatureEnabled("soa-sendgrid")).thenReturn(true);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody("test body");

        byte[] biteData = "test bytes".getBytes();
        for (Document d : documentList) {
            when(documentGenService.getDocumentBytes(
                d.getDocumentUrl(),
                TEST_AUTH,
                s2sToken
            )).thenReturn(biteData);
        }
        final Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "test auth");
        headers.put("Content-Type", "mail/send");
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(sendGrid.api(any(Request.class))).thenReturn(new Response(
            400,
            "response body",
            Map.of()
        ));
        Map<String, Object> dynamicTemplateData = new HashMap<>();
        dynamicTemplateData.put("caseName", caseData.getApplicantCaseName());
        dynamicTemplateData.put("caseReference", String.valueOf(caseData.getId()));
        when(sendgridEmailTemplatesConfig.getTemplates())
            .thenReturn(
                Map.of(
                    LanguagePreference.english, Map.of(SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION, "111"),
                    LanguagePreference.welsh, Map.of(SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION, "222")
                )
            );
        SendgridEmailConfig sendgridEmailConfig = SendgridEmailConfig.builder().listOfAttachments(documentList).toEmailAddress(
            applicant.getSolicitorEmail())
            .languagePreference(LanguagePreference.english)
            .dynamicTemplateData(dynamicTemplateData).build();
        sendgridService
            .sendEmailUsingTemplateWithAttachments(
                SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION,
                TEST_AUTH,
                sendgridEmailConfig);
        verify(sendGrid, times(1)).api(any(Request.class));
    }

    @Test
    public void testSendEmailUsingTemplatesWithAttachments_scenario2() throws Exception {

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
        when(launchDarklyClient.isFeatureEnabled("soa-sendgrid")).thenReturn(true);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody("test body");

        byte[] biteData = "test bytes".getBytes();
        for (Document d : documentList) {
            when(documentGenService.getDocumentBytes(
                d.getDocumentUrl(),
                TEST_AUTH,
                s2sToken
            )).thenReturn(biteData);
        }
        final Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "test auth");
        headers.put("Content-Type", "mail/send");
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(sendGrid.api(any(Request.class))).thenReturn(new Response(
            202,
            "response body",
            Map.of()
        ));
        Map<String, Object> dynamicTemplateData = new HashMap<>();
        dynamicTemplateData.put("caseName", caseData.getApplicantCaseName());
        dynamicTemplateData.put("caseReference", String.valueOf(caseData.getId()));
        when(sendgridEmailTemplatesConfig.getTemplates())
            .thenReturn(
                ImmutableMap.of(
                    LanguagePreference.english, ImmutableMap.of(SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION, "111"),
                    LanguagePreference.welsh, ImmutableMap.of(SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION, "222")
                )
            );
        SendgridEmailConfig sendgridEmailConfig = SendgridEmailConfig.builder().listOfAttachments(documentList).toEmailAddress(
            applicant.getSolicitorEmail())
            .languagePreference(LanguagePreference.english)
            .dynamicTemplateData(dynamicTemplateData).build();
        sendgridService
            .sendEmailUsingTemplateWithAttachments(
                SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION,
                TEST_AUTH,
                sendgridEmailConfig);
        verify(sendGrid, times(1)).api(any(Request.class));
    }

    @Test
    public void testTransferCourtEmailWithAttachmentsThrowsException() throws Exception {

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
        when(launchDarklyClient.isFeatureEnabled("transfer-case-sendgrid")).thenReturn(true);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody("test body");

        byte[] biteData = "test bytes".getBytes();
        for (Document d : documentList) {
            when(documentGenService.getDocumentBytes(d.getDocumentUrl(),
                                                     TEST_AUTH,
                                                     s2sToken)).thenReturn(biteData);
        }
        final Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "test auth");
        headers.put("Content-Type", "mail/send");
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        Response response = new Response();
        response.setBody("test response");
        response.setHeaders(headers);
        response.setStatusCode(SUCCESSFUL);
        when(sendGrid.api(any(Request.class))).thenThrow(new IOException("expected exception"));
        assertThrows(
            IOException.class,
            () -> sendgridService
                .sendTransferCourtEmailWithAttachments(TEST_AUTH, combinedMap, applicant.getSolicitorEmail(),
                                                       documentList));


    }

    @Test
    public void testTransferCourtEmailWithAttachmentsReturnError() throws Exception {
        ch.qos.logback.classic.Logger logger = (Logger) LoggerFactory.getLogger(SendgridService.class);
        TestLogAppender appender = new TestLogAppender();
        appender.start();
        logger.addAppender(appender);

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

        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", caseData.getApplicantCaseName());
        combinedMap.put("caseNumber", String.valueOf(caseData.getId()));
        combinedMap.put("solicitorName", applicant.getRepresentativeFullName());
        combinedMap.put("subject", "Case documents for : ");
        combinedMap.put("content", "Case details");
        combinedMap.put("attachmentType", "pdf");
        combinedMap.put("disposition", "attachment");
        combinedMap.put("specialNote", "Yes");

        when(launchDarklyClient.isFeatureEnabled("transfer-case-sendgrid")).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);

        byte[] biteData = "test bytes".getBytes();
        for (Document d : documentList) {
            when(documentGenService.getDocumentBytes(
                d.getDocumentUrl(),
                TEST_AUTH,
                s2sToken
            )).thenReturn(biteData);
        }

        Response mockResponse = new Response();
        mockResponse.setStatusCode(500);
        mockResponse.setBody("Internal Server Error");
        mockResponse.setHeaders(new HashMap<>());
        when(sendGrid.api(any(Request.class))).thenReturn(mockResponse);

        sendgridService
            .sendTransferCourtEmailWithAttachments(TEST_AUTH, combinedMap, applicant.getSolicitorEmail(),
                                                   documentList);

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid, times(1)).api(captor.capture());

        Request sentRequest = captor.getValue();
        assertEquals(Method.POST, sentRequest.getMethod());
        assertEquals("mail/send", sentRequest.getEndpoint());
        assertNotNull(sentRequest.getBody());

        assertTrue(appender.getEvents().stream().anyMatch(
            e -> e.getFormattedMessage().contains("Notification to parties failed for CASE ID: "
                                                      + combinedMap.get("caseNumber"))
            )
        );

        logger.detachAppender(appender);
    }

    @Test
    public void testTransferCourtEmailWithAttachmentsReturnSuccess() throws Exception {
        ch.qos.logback.classic.Logger logger = (Logger) LoggerFactory.getLogger(SendgridService.class);
        TestLogAppender appender = new TestLogAppender();
        appender.start();
        logger.addAppender(appender);

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

        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", caseData.getApplicantCaseName());
        combinedMap.put("caseNumber", String.valueOf(caseData.getId()));
        combinedMap.put("solicitorName", applicant.getRepresentativeFullName());
        combinedMap.put("subject", "Case documents for : ");
        combinedMap.put("content", "Case details");
        combinedMap.put("attachmentType", "pdf");
        combinedMap.put("disposition", "attachment");
        combinedMap.put("specialNote", "Yes");

        when(launchDarklyClient.isFeatureEnabled("transfer-case-sendgrid")).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(documentGenService.getDocumentBytes(any(), any(), any())).thenReturn("bytes".getBytes());

        Response mockSendGridResponse = new Response();
        mockSendGridResponse.setStatusCode(200);
        when(sendGrid.api(any(Request.class))).thenReturn(mockSendGridResponse);

        sendgridService.sendTransferCourtEmailWithAttachments(TEST_AUTH, combinedMap, applicant.getSolicitorEmail(), documentList);

        verify(sendGrid, times(1)).api(any(Request.class));
        assertTrue(appender.getEvents().stream().anyMatch(
                       e -> e.getFormattedMessage().contains("Notification to party sent successfully")
                   )
        );

        logger.detachAppender(appender);
    }
}
