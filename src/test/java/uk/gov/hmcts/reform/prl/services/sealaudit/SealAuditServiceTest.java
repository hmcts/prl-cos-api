package uk.gov.hmcts.reform.prl.services.sealaudit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.sealaudit.SealDetectionService.SealStatus;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SealAuditServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private SealDetectionService sealDetectionService;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private SealAuditService sealAuditService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sealAuditService, "searchCaseTypeId", "PRLAPPS");
        ReflectionTestUtils.setField(sealAuditService, "batchSize", 100);
        ReflectionTestUtils.setField(sealAuditService, "batchDelaySeconds", 1);
        ReflectionTestUtils.setField(sealAuditService, "resultSize", "10000");
        ReflectionTestUtils.setField(sealAuditService, "fromDateStr", "2024-01-01");
        ReflectionTestUtils.setField(sealAuditService, "toDateStr", "2026-12-31");
        ReflectionTestUtils.setField(sealAuditService, "emailEnabled", false);
        ReflectionTestUtils.setField(sealAuditService, "toEmailAddress", "");
        ReflectionTestUtils.setField(sealAuditService, "emailTemplateId", "");

        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.registerModule(new ParameterNamesModule());
        ReflectionTestUtils.setField(sealAuditService, "objectMapper", objectMapper);
    }

    @Test
    void shouldHandleEmptySearchResult() {
        when(systemUserService.getSysUserToken()).thenReturn("test-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");

        SearchResult searchResult = SearchResult.builder()
            .cases(List.of())
            .build();

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(searchResult);

        sealAuditService.runAudit();

        verify(coreCaseDataApi).searchCases(anyString(), anyString(), anyString(), anyString());
        verifyNoInteractions(caseDocumentClient, sealDetectionService, notificationClient);
    }

    @Test
    void shouldHandleNullSearchResult() {
        when(systemUserService.getSysUserToken()).thenReturn("test-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(null);

        sealAuditService.runAudit();

        verify(coreCaseDataApi).searchCases(anyString(), anyString(), anyString(), anyString());
        verifyNoInteractions(caseDocumentClient, sealDetectionService, notificationClient);
    }

    @Test
    void shouldSkipCaseWithNoOrders() {
        when(systemUserService.getSysUserToken()).thenReturn("test-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");

        SearchResult searchResult = SearchResult.builder()
            .cases(List.of(createCaseDetailsWithNoOrders()))
            .build();
        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(searchResult);

        sealAuditService.runAudit();
        verifyNoInteractions(caseDocumentClient, sealDetectionService, notificationClient);
    }

    @Test
    void shouldSkipOrdersThatAreNotServedPdfOrders() {
        when(systemUserService.getSysUserToken()).thenReturn("test-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");

        SearchResult searchResult = SearchResult.builder()
            .cases(List.of(
                createCaseDetailsWithServedOrder(
                    1111111111111111L,
                    "order.docx",
                    "2025-01-15T10:30:00.000000"
                ),
                createCaseDetailsWithServedOrder(
                    2222222222222222L,
                    "order.pdf",
                    "2023-12-31T10:30:00.000000"
                ),
                createCaseDetailsWithoutServedParties()
            ))
            .build();

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(searchResult);

        sealAuditService.runAudit();

        verifyNoInteractions(caseDocumentClient, sealDetectionService, notificationClient);
    }

    @ParameterizedTest
    @MethodSource("provideSealStatusesForEmailTest")
    void shouldSendEmailForSealStatus(SealStatus sealStatus, String sealsPresent, String sealsMissing, String errors)
        throws NotificationClientException, IOException {
        ReflectionTestUtils.setField(sealAuditService, "emailEnabled", true);
        ReflectionTestUtils.setField(sealAuditService, "toEmailAddress", "test@hmcts.net");
        ReflectionTestUtils.setField(sealAuditService, "emailTemplateId", "12345");

        when(systemUserService.getSysUserToken()).thenReturn("test-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");

        SearchResult searchResult = SearchResult.builder()
            .cases(List.of(
                createCaseDetailsWithServedOrder()
            ))
            .build();

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(searchResult);
        mockDocumentSealStatus(sealStatus);

        sealAuditService.runAudit();

        ArgumentCaptor<Map<String, Object>> templateVarsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(notificationClient).sendEmail(anyString(), anyString(), templateVarsCaptor.capture(), anyString());

        Map<String, Object> templateVars = templateVarsCaptor.getValue();
        assertEquals("1", templateVars.get("total_orders"));
        assertEquals(sealsPresent, templateVars.get("seals_present"));
        assertEquals(sealsMissing, templateVars.get("seals_missing"));
        assertEquals(errors, templateVars.get("errors"));
    }

    private static Stream<Arguments> provideSealStatusesForEmailTest() {
        return Stream.of(
            Arguments.of(SealStatus.PRESENT, "1", "0", "0"),
            Arguments.of(SealStatus.MISSING, "0", "1", "0"),
            Arguments.of(SealStatus.ERROR, "0", "0", "1")
        );
    }

    @Test
    void shouldCheckSealStatusWhenDocumentDownloadsSuccessfully() {
        Document document = Document.builder()
            .documentBinaryUrl("http://dm-store/documents/123/binary")
            .build();

        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), anyString()))
            .thenReturn(ResponseEntity.ok(new ByteArrayResource("pdf-bytes".getBytes())));

        when(sealDetectionService.detectSeal(any()))
            .thenReturn(SealStatus.PRESENT);

        SealStatus result = ReflectionTestUtils.invokeMethod(
            sealAuditService,
            "checkSealStatus",
            document,
            "user-token",
            "s2s-token",
            "1234567890123456"
        );

        assertEquals(SealStatus.PRESENT, result);
        verify(caseDocumentClient).getDocumentBinary(
            "user-token",
            "s2s-token",
            "http://dm-store/documents/123/binary"
        );
        verify(sealDetectionService).detectSeal(any());
    }

    @Test
    void shouldReturnErrorWhenDocumentDownloadBodyIsNull() {
        Document document = Document.builder()
            .documentBinaryUrl("http://dm-store/documents/123/binary")
            .build();

        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), anyString()))
            .thenReturn(ResponseEntity.ok(null));

        SealStatus result = ReflectionTestUtils.invokeMethod(
            sealAuditService,
            "checkSealStatus",
            document,
            "user-token",
            "s2s-token",
            "1234567890123456"
        );

        assertEquals(SealStatus.ERROR, result);
        verify(caseDocumentClient).getDocumentBinary(anyString(), anyString(), anyString());
        verify(sealDetectionService, never()).detectSeal(any());
    }

    @Test
    void shouldReturnErrorWhenDocumentDownloadThrowsException() {
        Document document = Document.builder()
            .documentBinaryUrl("http://dm-store/documents/123/binary")
            .build();

        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("DM Store unavailable"));

        SealStatus result = ReflectionTestUtils.invokeMethod(
            sealAuditService,
            "checkSealStatus",
            document,
            "user-token",
            "s2s-token",
            "1234567890123456"
        );

        assertEquals(SealStatus.ERROR, result);
        verify(caseDocumentClient).getDocumentBinary(anyString(), anyString(), anyString());
        verify(sealDetectionService, never()).detectSeal(any());
    }

    @Test
    void shouldSendSummaryEmailWhenEnabled() throws Exception {
        ReflectionTestUtils.setField(sealAuditService, "emailEnabled", true);
        ReflectionTestUtils.setField(sealAuditService, "toEmailAddress", "test@example.com");
        ReflectionTestUtils.setField(sealAuditService, "emailTemplateId", "template-id");

        when(systemUserService.getSysUserToken()).thenReturn("test-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");

        SearchResult searchResult = SearchResult.builder()
            .cases(List.of())
            .build();

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(searchResult);

        sealAuditService.runAudit();

        verify(notificationClient).sendEmail(
            eq("template-id"),
            eq("test@example.com"),
            any(),
            anyString()
        );
    }

    @Test
    void shouldNotSendEmailWhenTemplateIdIsBlank() throws Exception {
        ReflectionTestUtils.setField(sealAuditService, "emailEnabled", true);
        ReflectionTestUtils.setField(sealAuditService, "toEmailAddress", "test@example.com");
        ReflectionTestUtils.setField(sealAuditService, "emailTemplateId", "");

        when(systemUserService.getSysUserToken()).thenReturn("test-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");

        SearchResult searchResult = SearchResult.builder()
            .cases(List.of())
            .build();

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(searchResult);

        sealAuditService.runAudit();

        verify(notificationClient, never()).sendEmail(anyString(), anyString(), any(), anyString());
    }

    @Test
    void shouldHandleSearchException() {
        when(systemUserService.getSysUserToken()).thenReturn("test-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("CCD unavailable"));

        sealAuditService.runAudit();

        verify(coreCaseDataApi).searchCases(anyString(), anyString(), anyString(), anyString());
        verify(sealDetectionService, never()).detectSeal(any());
    }

    @Test
    void shouldParseValidDateUsingReflection() {
        Optional<LocalDate> result = ReflectionTestUtils.invokeMethod(
            sealAuditService,
            "parseDate",
            "2025-01-15"
        );

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(LocalDate.of(2025, 1, 15), result.get());
    }

    @Test
    void shouldReturnEmptyOptionalForInvalidDateUsingReflection() {
        Optional<LocalDate> result = ReflectionTestUtils.invokeMethod(
            sealAuditService,
            "parseDate",
            "not-a-date"
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldBuildCsvRowAndEscapeCommasAndQuotes() {
        String result = ReflectionTestUtils.invokeMethod(
            sealAuditService,
            "buildCsvRow",
            "123",
            "Court, with comma",
            "order type",
            "2025-01-15T10:00:00",
            "order \"quoted\".pdf",
            "15 Jan 2025",
            SealStatus.MISSING
        );

        assertEquals(
            "123,\"Court, with comma\",order type,"
                + "\"order \"\"quoted\"\".pdf\",15 Jan 2025,MISSING,2025-01-15T10:00:00",
            result
        );
    }

    @Test
    void shouldTreatNullDocumentDownloadBodyAsError() {
        Document document = Document.builder()
            .documentBinaryUrl("http://dm-store/documents/123/binary")
            .build();

        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), anyString()))
            .thenReturn(ResponseEntity.ok(null));

        SealStatus result = ReflectionTestUtils.invokeMethod(
            sealAuditService,
            "checkSealStatus",
            document,
            "user-token",
            "s2s-token",
            "1234567890123456"
        );

        assertEquals(SealStatus.ERROR, result);
        verify(caseDocumentClient).getDocumentBinary(anyString(), anyString(), anyString());
        verify(sealDetectionService, never()).detectSeal(any());
    }

    private CaseDetails createCaseDetailsWithServedOrder() {
        return createCaseDetailsWithServedOrder(
            1234567890123456L,
            "order.pdf",
            "2025-01-15T10:30:00"
        );
    }

    private CaseDetails createCaseDetailsWithServedOrder(
        Long caseId,
        String fileName,
        String servedDateTime
    ) {
        Map<String, Object> orderDocument = new HashMap<>();
        orderDocument.put("document_url", "http://dm-store/documents/" + caseId);
        orderDocument.put("document_binary_url", "http://dm-store/documents/" + caseId + "/binary");
        orderDocument.put("document_filename", fileName);
        orderDocument.put("upload_timestamp", "2025-01-15T10:00:00");

        Map<String, Object> servedParty = new HashMap<>();
        servedParty.put("partyId", UUID.randomUUID().toString());
        servedParty.put("partyName", "Test Party");
        servedParty.put("servedDateTime", servedDateTime);

        Map<String, Object> servedPartyElement = new HashMap<>();
        servedPartyElement.put("id", UUID.randomUUID().toString());
        servedPartyElement.put("value", servedParty);

        Map<String, Object> serveOrderDetails = new HashMap<>();
        serveOrderDetails.put("servedParties", List.of(servedPartyElement));

        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("orderMadeDate", "15 Jan 2025");

        Map<String, Object> orderValue = new HashMap<>();
        orderValue.put("orderDocument", orderDocument);
        orderValue.put("serveOrderDetails", serveOrderDetails);
        orderValue.put("otherDetails", otherDetails);
        orderValue.put("orderTypeId", "Standard directions order");

        Map<String, Object> orderElement = new HashMap<>();
        orderElement.put("id", UUID.randomUUID().toString());
        orderElement.put("value", orderValue);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("orderCollection", List.of(orderElement));
        caseData.put("courtName", "Test Court");

        return CaseDetails.builder()
            .id(caseId)
            .data(caseData)
            .build();
    }

    private CaseDetails createCaseDetailsWithNoOrders() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("courtName", "Test Court");

        return CaseDetails.builder()
            .id(1111111111111111L)
            .data(caseData)
            .build();
    }

    private CaseDetails createCaseDetailsWithoutServedParties() {
        Map<String, Object> orderDocument = new HashMap<>();
        orderDocument.put("document_url", "http://dm-store/documents/no-served-parties");
        orderDocument.put("document_binary_url", "http://dm-store/documents/no-served-parties/binary");
        orderDocument.put("document_filename", "order.pdf");
        orderDocument.put("upload_timestamp", "2025-01-15T10:00:00.000000");

        Map<String, Object> serveOrderDetails = new HashMap<>();
        serveOrderDetails.put("servedParties", List.of());

        Map<String, Object> orderValue = new HashMap<>();
        orderValue.put("orderDocument", orderDocument);
        orderValue.put("serveOrderDetails", serveOrderDetails);

        Map<String, Object> orderElement = new HashMap<>();
        orderElement.put("id", UUID.randomUUID().toString());
        orderElement.put("value", orderValue);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("orderCollection", List.of(orderElement));
        caseData.put("courtName", "Test Court");

        return CaseDetails.builder()
            .id(9999999999999999L)
            .data(caseData)
            .build();
    }

    private void mockDocumentSealStatus(SealStatus sealStatus) throws IOException {
        byte[] mockPdfBytes = "mock-pdf-content".getBytes();
        Resource resource = mock(Resource.class);
        when(resource.getInputStream()).thenReturn(new ByteArrayResource(mockPdfBytes).getInputStream());

        when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), anyString()))
            .thenReturn(ResponseEntity.ok(resource));

        when(sealDetectionService.detectSeal(mockPdfBytes)).thenReturn(sealStatus);
    }
}
