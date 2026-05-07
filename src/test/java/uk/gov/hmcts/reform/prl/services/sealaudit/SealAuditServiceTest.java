package uk.gov.hmcts.reform.prl.services.sealaudit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.sealaudit.SealDetectionService.SealStatus;
import uk.gov.service.notify.NotificationClient;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    }

    @Test
    void shouldSearchForCasesWithOrderCollection() {
        when(systemUserService.getSysUserToken()).thenReturn("test-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");

        SearchResult searchResult = SearchResult.builder()
            .cases(List.of())
            .build();

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(searchResult);

        sealAuditService.runAudit();

        verify(coreCaseDataApi).searchCases(anyString(), anyString(), anyString(), anyString());
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
        verify(sealDetectionService, times(0)).detectSeal(any());
    }

    @Test
    void shouldHandleNullSearchResult() {
        when(systemUserService.getSysUserToken()).thenReturn("test-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(null);

        sealAuditService.runAudit();

        verify(coreCaseDataApi).searchCases(anyString(), anyString(), anyString(), anyString());
        verify(sealDetectionService, times(0)).detectSeal(any());
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

        verify(caseDocumentClient, never()).getDocumentBinary(anyString(), anyString(), anyString());
        verify(sealDetectionService, never()).detectSeal(any());
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

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldBuildCsvRowAndEscapeCommasAndQuotes() {
        String result = ReflectionTestUtils.invokeMethod(
            sealAuditService,
            "buildCsvRow",
            "123",
            "Court, with comma",
            "order-id",
            "2025-01-15T10:00:00",
            "order \"quoted\".pdf",
            "15 Jan 2025",
            "2025-01-15 10:30:00",
            SealStatus.MISSING
        );

        assertEquals(
            "123,\"Court, with comma\",order-id,2025-01-15T10:00:00,"
                + "\"order \"\"quoted\"\".pdf\",15 Jan 2025,2025-01-15 10:30:00,MISSING",
            result
        );
    }

    private CaseDetails createCaseDetailsWithServedOrder() {
        return createCaseDetailsWithServedOrder(
            1234567890123456L,
            "order.pdf",
            "2025-01-15T10:30:00.000000"
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
        orderDocument.put("upload_timestamp", "2025-01-15T10:00:00.000000");

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
}
