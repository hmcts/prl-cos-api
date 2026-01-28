package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.PoiTlDocxRenderer;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


public class CustomOrderServiceTest {
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private HearingDataService hearingDataService;
    @Mock
    private PoiTlDocxRenderer poiTlDocxRenderer;
    @Mock
    private UploadDocumentService uploadService;
    @Mock
    private CaseDocumentClient caseDocumentClient;

    @InjectMocks
    private CustomOrderService customOrderService;
    private CustomOrderService spyCustomOrderService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        spyCustomOrderService = spy(customOrderService);
    }

    @Test
    public void testCustomOrderDocRemovedAndTransformedDocPersisted() throws Exception {
        // Arrange

        Map<String, Object> caseDataUpdated = new HashMap<>();
        uk.gov.hmcts.reform.prl.models.documents.Document customOrderDoc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentBinaryUrl("http://binary-url")
            .documentUrl("http://doc-url")
            .documentFileName("template.docx")
            .build();
        caseDataUpdated.put("customOrderDoc", customOrderDoc);

        // Mock download, render, and upload
        byte[] templateBytes = new byte[]{1,2,3};
        byte[] filledBytes = new byte[]{4,5,6};
        when(objectMapper.convertValue(any(), eq(uk.gov.hmcts.reform.prl.models.documents.Document.class))).thenReturn(customOrderDoc);
        doNothing().when(hearingDataService).populatePartiesAndSolicitorsNames(any(), any());
        when(poiTlDocxRenderer.render(eq(templateBytes), any())).thenReturn(filledBytes);
        // Mock Document.Links and Document.Link
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Links links = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link selfLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link binaryLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        selfLink.href = "http://self";

        binaryLink.href = "http://binary";
        links.self = selfLink;
        links.binary = binaryLink;
        uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDoc = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();
        uploadedDoc.links = links;
        uploadedDoc.originalDocumentName = "filled.docx";
        String authorisation = "auth";
        when(uploadService.uploadDocument(eq(filledBytes), any(), any(), eq(authorisation))).thenReturn(uploadedDoc);
        doReturn(templateBytes).when(spyCustomOrderService).downloadFromBinaryUrl(any(), any(), any());
        CaseData caseData = CaseData.builder().build();
        Long caseId = 123L;
        // Act
        Map<String, Object> result = spyCustomOrderService.renderUploadedCustomOrderAndStoreOnManageOrders(
            authorisation,
            caseId,
            caseData,
            caseDataUpdated,
            c -> c,
            c -> c
        );

        // Assert
        assertFalse(result.containsKey("customOrderDoc"));
        assertNotNull(result.get("customOrderTransformedDoc"));
        uk.gov.hmcts.reform.prl.models.documents.Document transformed =
            (uk.gov.hmcts.reform.prl.models.documents.Document) result.get("customOrderTransformedDoc");
        assertEquals("http://self", transformed.getDocumentUrl());
        assertEquals("http://binary", transformed.getDocumentBinaryUrl());
        assertEquals("filled.docx", transformed.getDocumentFileName());
    }
}
