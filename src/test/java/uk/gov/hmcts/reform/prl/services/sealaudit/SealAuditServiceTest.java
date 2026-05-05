package uk.gov.hmcts.reform.prl.services.sealaudit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.service.notify.NotificationClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    private CaseDetails createCaseDetailsWithServedOrder() {
        Map<String, Object> orderDocument = new HashMap<>();
        orderDocument.put("document_url", "http://dm-store/documents/123");
        orderDocument.put("document_binary_url", "http://dm-store/documents/123/binary");
        orderDocument.put("document_filename", "order.pdf");
        orderDocument.put("upload_timestamp", "2025-01-15T10:00:00.000000");

        Map<String, Object> servedParty = new HashMap<>();
        servedParty.put("partyId", UUID.randomUUID().toString());
        servedParty.put("partyName", "Test Party");
        servedParty.put("servedDateTime", "2025-01-15T10:30:00.000000");

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
            .id(1234567890123456L)
            .data(caseData)
            .build();
    }
}
