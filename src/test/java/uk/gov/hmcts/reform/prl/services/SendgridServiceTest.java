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
import uk.gov.hmcts.reform.prl.exception.SendGridNotificationException;
import uk.gov.hmcts.reform.prl.models.documents.Document;
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
    private static final String CASE_REFERENCE = "1234123412341234";

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
        List<Document> documentList = createDocumentList();
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
            .toEmailAddress("test@gmail.com")
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
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody("test body");

        byte[] biteData = "test bytes".getBytes();
        List<Document> documentList = createDocumentList();
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
        dynamicTemplateData.put("caseName", "test");
        dynamicTemplateData.put("caseReference", CASE_REFERENCE);
        when(sendgridEmailTemplatesConfig.getTemplates())
            .thenReturn(
                Map.of(
                    LanguagePreference.english, Map.of(SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION, "111"),
                    LanguagePreference.welsh, Map.of(SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION, "222")
                )
            );
        SendgridEmailConfig sendgridEmailConfig = SendgridEmailConfig.builder()
            .caseReference(CASE_REFERENCE)
            .listOfAttachments(documentList)
            .toEmailAddress("test@gmail.com")
            .languagePreference(LanguagePreference.english)
            .dynamicTemplateData(dynamicTemplateData).build();

        sendgridService
            .sendEmailUsingTemplateWithAttachments(
                SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION,
                TEST_AUTH,
                sendgridEmailConfig);

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid).api(requestCaptor.capture());
        verifyRequestContainsCustomArgsCaseReference(requestCaptor.getValue());
    }

    @Test
    void testSendEmailUsingTemplatesWithAttachments_scenario2() throws Exception {
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody("test body");

        byte[] biteData = "test bytes".getBytes();
        List<Document> documentList = createDocumentList();
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
        dynamicTemplateData.put("caseName", "test");
        dynamicTemplateData.put("caseReference", CASE_REFERENCE);
        when(sendgridEmailTemplatesConfig.getTemplates())
            .thenReturn(
                ImmutableMap.of(
                    LanguagePreference.english, ImmutableMap.of(SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION, "111"),
                    LanguagePreference.welsh, ImmutableMap.of(SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION, "222")
                )
            );
        SendgridEmailConfig sendgridEmailConfig = SendgridEmailConfig.builder()
            .caseReference(CASE_REFERENCE)
            .listOfAttachments(documentList)
            .toEmailAddress("test@gmail.com")
            .languagePreference(LanguagePreference.english)
            .dynamicTemplateData(dynamicTemplateData).build();

        sendgridService
            .sendEmailUsingTemplateWithAttachments(
                SendgridEmailTemplateNames.SERVE_ORDER_ANOTHER_ORGANISATION,
                TEST_AUTH,
                sendgridEmailConfig);

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid).api(requestCaptor.capture());
        verifyRequestContainsCustomArgsCaseReference(requestCaptor.getValue());
    }

    @Test
    void testTransferCourtEmailWithAttachmentsThrowsException() throws Exception {

        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", "test");
        combinedMap.put("caseNumber", CASE_REFERENCE);
        combinedMap.put("solicitorName", "FirstName LastName");
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
        List<Document> documentList = createDocumentList();
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
                .sendTransferCourtEmailWithAttachments(TEST_AUTH, combinedMap, "test@gmail.com", documentList));
    }

    @Test
    void testTransferCourtEmailWithAttachmentsReturnError() throws Exception {
        ch.qos.logback.classic.Logger logger = (Logger) LoggerFactory.getLogger(SendgridService.class);
        TestLogAppender appender = new TestLogAppender();
        appender.start();
        logger.addAppender(appender);

        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", "test");
        combinedMap.put("caseNumber", CASE_REFERENCE);
        combinedMap.put("solicitorName", "FirstName LastName");
        combinedMap.put("subject", "Case documents for : ");
        combinedMap.put("content", "Case details");
        combinedMap.put("attachmentType", "pdf");
        combinedMap.put("disposition", "attachment");
        combinedMap.put("specialNote", "Yes");

        when(launchDarklyClient.isFeatureEnabled("transfer-case-sendgrid")).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);

        byte[] biteData = "test bytes".getBytes();
        List<Document> documentList = createDocumentList();
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

        sendgridService.sendTransferCourtEmailWithAttachments(TEST_AUTH, combinedMap, "test@gmail.com", documentList);

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

        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", "test");
        combinedMap.put("caseNumber", CASE_REFERENCE);
        combinedMap.put("solicitorName", "FirstName LastName");
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

        sendgridService.sendTransferCourtEmailWithAttachments(TEST_AUTH, combinedMap, "test@gmail.com", createDocumentList());

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(sendGrid).api(requestCaptor.capture());
        Request capturedRequest = requestCaptor.getValue();
        verifyRequestContainsCustomArgsCaseReference(capturedRequest);

        assertTrue(appender.getEvents().stream().anyMatch(
                       e -> e.getFormattedMessage().contains("Notification to party sent successfully")
                   )
        );

        logger.detachAppender(appender);
    }

    private void verifyRequestContainsCustomArgsCaseReference(Request request) {
        String body = request.getBody();
        assertTrue(body.contains("custom_args\":{\"caseReference\":\"1234123412341234\"}"));
    }

    private List<Document> createDocumentList() {
        Document finalDoc = createDocument("finalDoc");
        Document coverSheet = createDocument("coverSheet");
        return List.of(coverSheet, finalDoc);
    }

    private Document createDocument(String url) {
        return Document.builder()
            .documentUrl(url)
            .documentBinaryUrl(url)
            .documentHash(url)
            .build();
    }
}
