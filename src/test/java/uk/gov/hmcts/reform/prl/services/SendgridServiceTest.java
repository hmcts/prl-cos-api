package uk.gov.hmcts.reform.prl.services;

import ch.qos.logback.classic.Logger;
import com.google.common.collect.ImmutableMap;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
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
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.JsonObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class SendgridServiceTest {

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

    private static final String TEST_AUTH = "test auth";
    private static final String S2S_TOKEN = "s2s token";

    @Test
    void testSendEmailInvokingSendGridApi() throws IOException {
        Response response = new Response();
        response.setStatusCode(200);
        JsonObject jsonObject = new NullAwareJsonObjectBuilder()
            .add("applicantCaseName","hello")
            .build();
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        sendgridService.sendEmail(jsonObject);
        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void testSendEmailUsingTemplatesWithAttachments() throws Exception {
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
                                                     S2S_TOKEN
            )).thenReturn(biteData);
        }
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
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
    void testSendEmailUsingTemplatesWithAttachments_scenario3() throws Exception {
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

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody("test body");

        byte[] biteData = "test bytes".getBytes();
        for (Document d : documentList) {
            when(documentGenService.getDocumentBytes(
                d.getDocumentUrl(),
                TEST_AUTH,
                S2S_TOKEN
            )).thenReturn(biteData);
        }
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
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
        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void testSendEmailUsingTemplatesWithAttachments_scenario2() throws Exception {
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

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody("test body");

        byte[] biteData = "test bytes".getBytes();
        for (Document d : documentList) {
            when(documentGenService.getDocumentBytes(
                d.getDocumentUrl(),
                TEST_AUTH,
                S2S_TOKEN
            )).thenReturn(biteData);
        }
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
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
        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void testTransferCourtEmailWithAttachmentsThrowsException() throws Exception {
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

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody("test body");

        byte[] biteData = "test bytes".getBytes();
        for (Document d : documentList) {
            when(documentGenService.getDocumentBytes(d.getDocumentUrl(),
                                                     TEST_AUTH,
                                                     S2S_TOKEN
            )).thenReturn(biteData);
        }
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(sendGrid.api(any(Request.class))).thenThrow(new IOException("expected exception"));

        assertThrows(
            IOException.class,
            () -> sendgridService
                .sendTransferCourtEmailWithAttachments(TEST_AUTH, combinedMap, applicant.getSolicitorEmail(),
                                                       documentList));
    }

    @Test
    void testTransferCourtEmailWithAttachmentsReturnError() throws Exception {
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
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);

        byte[] biteData = "test bytes".getBytes();
        for (Document d : documentList) {
            when(documentGenService.getDocumentBytes(
                d.getDocumentUrl(),
                TEST_AUTH,
                S2S_TOKEN
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
        verify(sendGrid).api(captor.capture());

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
    void testTransferCourtEmailWithAttachmentsReturnSuccess() throws Exception {
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
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(documentGenService.getDocumentBytes(any(), any(), any())).thenReturn("bytes".getBytes());

        Response mockSendGridResponse = new Response();
        mockSendGridResponse.setStatusCode(200);
        when(sendGrid.api(any(Request.class))).thenReturn(mockSendGridResponse);

        sendgridService.sendTransferCourtEmailWithAttachments(TEST_AUTH, combinedMap, applicant.getSolicitorEmail(), documentList);

        verify(sendGrid).api(any(Request.class));
        assertTrue(appender.getEvents().stream().anyMatch(
                       e -> e.getFormattedMessage().contains("Notification to party sent successfully")
                   )
        );

        logger.detachAppender(appender);
    }
}
