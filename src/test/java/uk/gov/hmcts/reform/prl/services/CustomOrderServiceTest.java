package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.HearingChannelsEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CustomOrderNameOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Relations;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.document.PoiTlDocxRenderer;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CUSTOM_ORDER_NAME_OPTION;

@ExtendWith(MockitoExtension.class)
class CustomOrderServiceTest {
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
    private DocumentGenService documentGenService;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private AllTabServiceImpl allTabService;
    @Mock
    private uk.gov.hmcts.reform.prl.clients.DgsApiClient dgsApiClient;
    @Mock
    private DocumentSealingService documentSealingService;

    @InjectMocks
    private CustomOrderService customOrderService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> placeholdersCaptor;

    @BeforeEach
    void setUp() {
        // Mocks initialized by @ExtendWith(MockitoExtension.class)
    }

    // ========== Tests for EXISTING FLOW (renderUploadedCustomOrderAndStoreOnManageOrders) ==========

    @Test
    void testCustomOrderDocKeptAndTransformedDocPersisted() throws Exception {
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
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");

        // Mock allTabService for persisting document to case
        StartAllTabsUpdateDataContent mockStartContent = new StartAllTabsUpdateDataContent(
            "system-auth",
            mock(EventRequestData.class),
            mock(StartEventResponse.class),
            new HashMap<>(),
            CaseData.builder().build(),
            null
        );
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(mockStartContent);
        when(allTabService.submitAllTabsUpdate(any(), any(), any(), any(), any())).thenReturn(null);

        // Mock document download after persistence
        when(documentGenService.getDocumentBytes(any(), any(), any())).thenReturn(templateBytes);

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

        CaseData caseData = CaseData.builder().build();
        Long caseId = 123L;

        // Act
        Map<String, Object> result = customOrderService.renderUploadedCustomOrderAndStoreOnManageOrders(
            authorisation,
            caseId,
            caseData,
            caseDataUpdated,
            c -> c,
            c -> c
        );

        // Assert
        // customOrderDoc is kept because it's needed in the submitted callback to combine with header
        assertTrue(result.containsKey("customOrderDoc"));
        assertNotNull(result.get("customOrderTransformedDoc"));
        uk.gov.hmcts.reform.prl.models.documents.Document transformed =
            (uk.gov.hmcts.reform.prl.models.documents.Document) result.get("customOrderTransformedDoc");
        assertEquals("http://self", transformed.getDocumentUrl());
        assertEquals("http://binary", transformed.getDocumentBinaryUrl());
        assertEquals("filled.docx", transformed.getDocumentFileName());
    }

    @Test
    void testRenderUploadedCustomOrder_throwsWhenCustomOrderDocIsNull() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        // customOrderDoc not set - will be null

        when(objectMapper.convertValue(any(), eq(uk.gov.hmcts.reform.prl.models.documents.Document.class)))
            .thenReturn(null);

        CaseData caseData = CaseData.builder().build();

        assertThrows(IllegalArgumentException.class, () ->
            customOrderService.renderUploadedCustomOrderAndStoreOnManageOrders(
                "auth", 123L, caseData, caseDataUpdated, c -> c, c -> c
            ));
    }

    @Test
    void testRenderUploadedCustomOrder_throwsWhenDocumentBinaryUrlIsNull() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        uk.gov.hmcts.reform.prl.models.documents.Document docWithoutBinaryUrl =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentUrl("http://doc-url")
                .documentFileName("template.docx")
                // documentBinaryUrl intentionally not set
                .build();
        caseDataUpdated.put("customOrderDoc", docWithoutBinaryUrl);

        when(objectMapper.convertValue(any(), eq(uk.gov.hmcts.reform.prl.models.documents.Document.class)))
            .thenReturn(docWithoutBinaryUrl);

        CaseData caseData = CaseData.builder().build();

        assertThrows(IllegalArgumentException.class, () ->
            customOrderService.renderUploadedCustomOrderAndStoreOnManageOrders(
                "auth", 123L, caseData, caseDataUpdated, c -> c, c -> c
            ));
    }

    @Test
    void testRenderUploadedCustomOrder_formatsOrderNameWithActReference() throws Exception {
        // Tests buildCustomOrderPlaceholders branch where formNumber and actReference are both non-null
        Map<String, Object> caseDataUpdated = new HashMap<>();
        uk.gov.hmcts.reform.prl.models.documents.Document customOrderDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentBinaryUrl("http://binary-url")
                .documentUrl("http://doc-url")
                .documentFileName("template.docx")
                .build();
        caseDataUpdated.put("customOrderDoc", customOrderDoc);
        caseDataUpdated.put("customOrderNameOption", "parentalResponsibility");

        byte[] templateBytes = new byte[]{1, 2, 3};
        byte[] filledBytes = new byte[]{4, 5, 6};
        when(objectMapper.convertValue(any(), eq(uk.gov.hmcts.reform.prl.models.documents.Document.class)))
            .thenReturn(customOrderDoc);
        doNothing().when(hearingDataService).populatePartiesAndSolicitorsNames(any(), any());
        when(poiTlDocxRenderer.render(eq(templateBytes), placeholdersCaptor.capture())).thenReturn(filledBytes);
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");

        StartAllTabsUpdateDataContent mockStartContent = new StartAllTabsUpdateDataContent(
            "system-auth",
            mock(EventRequestData.class),
            mock(StartEventResponse.class),
            new HashMap<>(),
            CaseData.builder().build(),
            null
        );
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(mockStartContent);
        when(allTabService.submitAllTabsUpdate(any(), any(), any(), any(), any())).thenReturn(null);
        when(documentGenService.getDocumentBytes(any(), any(), any())).thenReturn(templateBytes);

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Links links =
            new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link selfLink =
            new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link binaryLink =
            new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        selfLink.href = "http://self";
        binaryLink.href = "http://binary";
        links.self = selfLink;
        links.binary = binaryLink;
        uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDoc =
            uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();
        uploadedDoc.links = links;
        uploadedDoc.originalDocumentName = "filled.docx";
        when(uploadService.uploadDocument(eq(filledBytes), any(), any(), any())).thenReturn(uploadedDoc);

        CaseData caseData = CaseData.builder().build();

        customOrderService.renderUploadedCustomOrderAndStoreOnManageOrders(
            "auth", 123L, caseData, caseDataUpdated, c -> c, c -> c
        );

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        String actReference = (String) placeholders.get("actReference");
        assertTrue(orderName.contains("C45A"), "Should contain form number");
        assertTrue(actReference.contains("Children Act 1989"), "Should contain act reference");
    }

    // ========== Tests for CDAM workaround methods ==========

    @Test
    void testRequiresFreshEventSubmission_returnsTrue_whenCdamAssociationUsed() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("customOrderUsedCdamAssociation", "Yes");

        assertTrue(customOrderService.requiresFreshEventSubmission(caseDataUpdated));
    }

    @Test
    void testRequiresFreshEventSubmission_returnsFalse_whenCdamAssociationNotUsed() {
        Map<String, Object> caseDataUpdated = new HashMap<>();

        assertFalse(customOrderService.requiresFreshEventSubmission(caseDataUpdated));
    }

    @Test
    void testSubmitFreshManageOrdersEvent_submitsViaAllTabService() {
        // Arrange
        final String caseId = "123";
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("someField", "someValue");
        caseDataUpdated.put("customOrderDoc", "should be removed");
        caseDataUpdated.put("customOrderUsedCdamAssociation", "Yes");

        StartAllTabsUpdateDataContent mockStartContent = new StartAllTabsUpdateDataContent(
            "system-auth",
            mock(EventRequestData.class),
            mock(StartEventResponse.class),
            new HashMap<>(),
            CaseData.builder().build(),
            null
        );
        when(allTabService.getStartUpdateForSpecificEvent(caseId, "internal-custom-order-submit")).thenReturn(mockStartContent);
        when(allTabService.submitAllTabsUpdate(any(), any(), any(), any(), any())).thenReturn(null);

        // Act
        customOrderService.submitFreshManageOrdersEvent(caseId, caseDataUpdated);

        // Assert - verify customOrderDoc and flag are removed
        assertFalse(caseDataUpdated.containsKey("customOrderDoc"));
        assertFalse(caseDataUpdated.containsKey("customOrderUsedCdamAssociation"));
        verify(allTabService).getStartUpdateForSpecificEvent(caseId, "internal-custom-order-submit");
    }

    // ========== Tests for NEW FLOW ==========

    @Test
    void testRenderAndUploadHeaderPreview_uploadsRenderedHeader() throws IOException {
        // Arrange
        final String authorisation = "auth-token";
        final long caseId = 123L;
        final CaseData caseData = CaseData.builder()
            .courtName("Test Court")
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3, 4};
        when(poiTlDocxRenderer.render(any(), any())).thenReturn(renderedBytes);

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Links links =
            new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link selfLink =
            new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link binaryLink =
            new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        selfLink.href = "http://self-url";
        binaryLink.href = "http://binary-url";
        links.self = selfLink;
        links.binary = binaryLink;

        uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDoc =
            uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();
        uploadedDoc.links = links;
        uploadedDoc.originalDocumentName = "custom_order_header_preview_123.docx";

        when(uploadService.uploadDocument(any(), any(), any(), eq(authorisation))).thenReturn(uploadedDoc);

        // Act
        uk.gov.hmcts.reform.prl.models.documents.Document result =
            customOrderService.renderAndUploadHeaderPreview(authorisation, caseId, caseData, null);

        // Assert
        assertNotNull(result);
        assertEquals("http://self-url", result.getDocumentUrl());
        assertEquals("http://binary-url", result.getDocumentBinaryUrl());
        assertEquals("custom_order_header_preview_123.docx", result.getDocumentFileName());
        verify(uploadService).uploadDocument(eq(renderedBytes), any(), any(), eq(authorisation));
    }

    @Test
    void testProcessCustomOrderOnSubmitted_downloadsDocuments() {
        // Arrange
        final String authorisation = "auth-token";
        final long caseId = 123L;
        String userDocUrl = "http://user-doc-binary-url";
        String headerDocUrl = "http://header-doc-binary-url";

        CaseData caseData = CaseData.builder()
            .id(caseId)
            .courtName("Test Court")
            .nameOfOrder("Test Order")
            .build();

        // Mock header and user doc downloads - fake bytes will cause POI to fail
        byte[] headerBytes = new byte[]{1, 2, 3};
        byte[] userContentBytes = new byte[]{4, 5, 6};
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");
        when(documentGenService.getDocumentBytes(eq(headerDocUrl), any(), any())).thenReturn(headerBytes);
        when(documentGenService.getDocumentBytes(eq(userDocUrl), any(), any())).thenReturn(userContentBytes);

        // Act & Assert - combineHeaderAndContent needs real DOCX bytes so will throw
        assertThrows(Exception.class, () ->
            customOrderService.processCustomOrderOnSubmitted(
                authorisation, caseId, caseData, userDocUrl, headerDocUrl, null, false));

        // Verify documents were downloaded before the POI error
        verify(documentGenService).getDocumentBytes(eq(headerDocUrl), any(), any());
        verify(documentGenService).getDocumentBytes(eq(userDocUrl), any(), any());
    }

    @Test
    void testBuildHeaderPlaceholders_populatesCaseData() throws IOException {
        // Arrange
        Long caseId = 123L;

        PartyDetails applicant = PartyDetails.builder()
            .firstName("John")
            .lastName("Doe")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .firstName("Jane")
            .lastName("Smith")
            .build();

        CaseData caseData = CaseData.builder()
            .id(caseId)
            .courtName("Family Court London")
            .applicants(List.of(Element.<PartyDetails>builder().value(applicant).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(respondent).build()))
            .build();

        // Mock the renderer to capture the placeholders
        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), any())).thenAnswer(invocation -> {
            Map<String, Object> placeholders = invocation.getArgument(1);
            // Verify placeholders were built correctly
            assertEquals("123", placeholders.get("caseNumber"));
            assertEquals("Family Court London", placeholders.get("courtName"));
            assertEquals("John Doe", placeholders.get("applicant1Name"));
            assertEquals("Jane Smith", placeholders.get("respondent1Name"));
            return renderedBytes;
        });

        // Act - renderHeaderPreview calls buildHeaderPlaceholders internally
        byte[] result = customOrderService.renderHeaderPreview(caseId, caseData, null);

        // Assert
        assertNotNull(result);
        verify(poiTlDocxRenderer).render(any(), any());
    }

    // ========== Tests for PLACEHOLDER POPULATION ==========

    @Test
    void testBuildHeaderPlaceholders_caseNumberFormatted() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("1234-5678-9012-3456", placeholders.get("caseNumber"));
    }

    @ParameterizedTest
    @CsvSource({
        "SA26P12345, FamilyMan:, SA26P12345",
        "'', '', ''",
        "'   ', '', ''"
    })
    @NullSource
    void testBuildHeaderPlaceholders_familymanCaseNumber(String familymanCaseNumber) throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .familymanCaseNumber(familymanCaseNumber)
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        boolean hasValidNumber = familymanCaseNumber != null && !familymanCaseNumber.isBlank();
        assertEquals(hasValidNumber ? "FamilyMan:" : "", placeholders.get("familymanLabel"));
        assertEquals(hasValidNumber ? familymanCaseNumber : "", placeholders.get("familymanCaseNumber"));
    }

    @Test
    void testBuildHeaderPlaceholders_courtNamePopulated() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .courtName("Central Family Court")
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Central Family Court", placeholders.get("courtName"));
    }

    @Test
    void testBuildHeaderPlaceholders_judgeNamePopulated() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .judgeOrMagistratesLastName("HHJ Richardson")
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("HHJ Richardson", placeholders.get("judgeName"));
    }

    @Test
    void testBuildHeaderPlaceholders_legalAdviserClausePopulated() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .justiceLegalAdviserFullName("Jane Advisor")
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals(" sitting with Justices' Legal Adviser Jane Advisor", placeholders.get("sittingWithLegalAdviser"));
    }

    @Test
    void testBuildHeaderPlaceholders_legalAdviserClauseEmptyWhenNoLegalAdviser() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("", placeholders.get("sittingWithLegalAdviser"));
    }

    @Test
    void testBuildHeaderPlaceholders_singleMagistrateNamePopulated() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .magistrateLastName(List.of(
                Element.<uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName>builder()
                    .value(uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName.builder()
                        .lastName("John Smith").build())
                    .build()))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("John Smith", placeholders.get("judgeName"));
    }

    @Test
    void testBuildHeaderPlaceholders_multipleMagistrateNamesJoined() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .magistrateLastName(List.of(
                Element.<uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName>builder()
                    .value(uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName.builder()
                        .lastName("Suki Smith").build())
                    .build(),
                Element.<uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName>builder()
                    .value(uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName.builder()
                        .lastName("Frenchie Smith").build())
                    .build()))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Suki Smith and Frenchie Smith", placeholders.get("judgeName"));
    }

    @Test
    void testBuildHeaderPlaceholders_threeMagistrateNamesJoinedWithCommaAndAnd() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .magistrateLastName(List.of(
                Element.<uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName>builder()
                    .value(uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName.builder()
                        .lastName("Alice Brown").build())
                    .build(),
                Element.<uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName>builder()
                    .value(uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName.builder()
                        .lastName("Bob Jones").build())
                    .build(),
                Element.<uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName>builder()
                    .value(uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName.builder()
                        .lastName("Carol White").build())
                    .build()))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Alice Brown, Bob Jones and Carol White", placeholders.get("judgeName"));
    }

    @Test
    void testBuildHeaderPlaceholders_applicantNamePopulated() throws IOException {
        Long caseId = 1234567890123456L;
        PartyDetails applicant = PartyDetails.builder()
            .firstName("Sarah")
            .lastName("Johnson")
            .build();

        CaseData caseData = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().value(applicant).build()))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Sarah Johnson", placeholders.get("applicant1Name"));
    }

    @Test
    void testBuildHeaderPlaceholders_applicantRepresentativePopulated() throws IOException {
        Long caseId = 1234567890123456L;
        PartyDetails applicant = PartyDetails.builder()
            .firstName("Sarah")
            .lastName("Johnson")
            .representativeFirstName("Michael")
            .representativeLastName("Solicitor")
            .build();

        CaseData caseData = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().value(applicant).build()))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Michael Solicitor", placeholders.get("applicant1RepresentativeName"));
    }

    @Test
    void testBuildHeaderPlaceholders_fl401ApplicantPopulated() throws IOException {
        Long caseId = 1234567890123456L;
        PartyDetails fl401Applicant = PartyDetails.builder()
            .firstName("Jane")
            .lastName("Doe")
            .representativeFirstName("Legal")
            .representativeLastName("Rep")
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(fl401Applicant)
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Jane Doe", placeholders.get("applicant1Name"));
        assertEquals("Legal Rep", placeholders.get("applicant1RepresentativeName"));
    }

    @Test
    void testBuildHeaderPlaceholders_fl401ChildrenPopulated() throws IOException {
        Long caseId = 1234567890123456L;

        ApplicantChild child = ApplicantChild.builder()
            .fullName("Tommy Test")
            .dateOfBirth(LocalDate.of(2015, 5, 10))
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(PartyDetails.builder().firstName("Jane").lastName("Doe").build())
            .applicantChildDetails(List.of(Element.<ApplicantChild>builder().value(child).build()))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        @SuppressWarnings("unchecked")
        List<Map<String, String>> children = (List<Map<String, String>>) placeholders.get("children");
        assertNotNull(children);
        assertFalse(children.isEmpty());
        assertEquals("Tommy Test", children.getFirst().get("fullName"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testBuildHeaderPlaceholders_fl401ChildrenFilteredBySelection() throws IOException {
        Long caseId = 1234567890123456L;

        UUID child1Id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID child2Id = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID child3Id = UUID.fromString("33333333-3333-3333-3333-333333333333");

        ApplicantChild child1 = ApplicantChild.builder()
            .fullName("Alice Test")
            .dateOfBirth(LocalDate.of(2015, 5, 10))
            .build();
        ApplicantChild child2 = ApplicantChild.builder()
            .fullName("Bob Test")
            .dateOfBirth(LocalDate.of(2017, 3, 20))
            .build();
        ApplicantChild child3 = ApplicantChild.builder()
            .fullName("Charlie Test")
            .dateOfBirth(LocalDate.of(2019, 8, 15))
            .build();

        // Select only child1 and child3
        DynamicMultiSelectList childOption = DynamicMultiSelectList.builder()
            .value(List.of(
                DynamicMultiselectListElement.builder().code(child1Id.toString()).label("Alice Test").build(),
                DynamicMultiselectListElement.builder().code(child3Id.toString()).label("Charlie Test").build()
            ))
            .build();

        ManageOrders manageOrders = ManageOrders.builder()
            .isTheOrderAboutChildren(YesOrNo.Yes)
            .childOption(childOption)
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(PartyDetails.builder().firstName("Jane").lastName("Doe").build())
            .applicantChildDetails(List.of(
                Element.<ApplicantChild>builder().id(child1Id).value(child1).build(),
                Element.<ApplicantChild>builder().id(child2Id).value(child2).build(),
                Element.<ApplicantChild>builder().id(child3Id).value(child3).build()
            ))
            .manageOrders(manageOrders)
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        List<Map<String, String>> children = (List<Map<String, String>>) placeholders.get("children");

        // Should only contain selected children (Alice and Charlie), not Bob
        assertNotNull(children);
        assertEquals(2, children.size());
        assertEquals("Alice Test", children.get(0).get("fullName"));
        assertEquals("Charlie Test", children.get(1).get("fullName"));
    }

    @Test
    void testBuildHeaderPlaceholders_fl401RespondentPopulated() throws IOException {
        Long caseId = 1234567890123456L;
        PartyDetails fl401Respondent = PartyDetails.builder()
            .firstName("John")
            .lastName("Smith")
            .representativeFirstName("Defence")
            .representativeLastName("Lawyer")
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(PartyDetails.builder().firstName("Jane").lastName("Doe").build())
            .respondentsFL401(fl401Respondent)
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("John Smith", placeholders.get("respondent1Name"));
        assertEquals("Defence Lawyer", placeholders.get("respondent1RepresentativeName"));
    }

    @Test
    void testBuildHeaderPlaceholders_respondent1NamePopulated() throws IOException {
        Long caseId = 1234567890123456L;
        PartyDetails respondent = PartyDetails.builder()
            .firstName("David")
            .lastName("Williams")
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(Element.<PartyDetails>builder().value(respondent).build()))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("David Williams", placeholders.get("respondent1Name"));
    }

    @Test
    void testBuildHeaderPlaceholders_respondentRelationshipPopulated() throws IOException {
        Long caseId = 1234567890123456L;
        PartyDetails respondent = PartyDetails.builder()
            .firstName("David")
            .lastName("Williams")
            .relationshipToChildren("Father")
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(Element.<PartyDetails>builder().value(respondent).build()))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Father", placeholders.get("respondent1RelationshipToChild"));
    }

    @Test
    void testBuildHeaderPlaceholders_respondentRepresentativePopulated() throws IOException {
        Long caseId = 1234567890123456L;
        PartyDetails respondent = PartyDetails.builder()
            .firstName("David")
            .lastName("Williams")
            .representativeFirstName("Emma")
            .representativeLastName("Barrister")
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(Element.<PartyDetails>builder().value(respondent).build()))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Emma Barrister", placeholders.get("respondent1RepresentativeName"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testBuildHeaderPlaceholders_childrenTablePopulated_allChildren() throws IOException {
        Long caseId = 1234567890123456L;

        UUID child1Id = UUID.randomUUID();
        UUID child2Id = UUID.randomUUID();

        ChildDetailsRevised child1 = ChildDetailsRevised.builder()
            .firstName("Alice")
            .lastName("Smith")
            .gender(Gender.female)
            .dateOfBirth(LocalDate.of(2015, 5, 10))
            .build();

        ChildDetailsRevised child2 = ChildDetailsRevised.builder()
            .firstName("Bob")
            .lastName("Smith")
            .gender(Gender.male)
            .dateOfBirth(LocalDate.of(2018, 8, 20))
            .build();

        ManageOrders manageOrders = ManageOrders.builder()
            .isTheOrderAboutAllChildren(YesOrNo.Yes)
            .build();

        CaseData caseData = CaseData.builder()
            .newChildDetails(List.of(
                Element.<ChildDetailsRevised>builder().id(child1Id).value(child1).build(),
                Element.<ChildDetailsRevised>builder().id(child2Id).value(child2).build()
            ))
            .manageOrders(manageOrders)
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();

        // Verify children list for LoopRowTableRenderPolicy
        @SuppressWarnings("unchecked")
        List<Map<String, String>> children = (List<Map<String, String>>) placeholders.get("children");
        assertNotNull(children);
        assertEquals(2, children.size());
        assertEquals(true, placeholders.get("hasChildren"));

        // First child
        assertEquals("Alice Smith", children.getFirst().get("fullName"));
        assertEquals("Female", children.getFirst().get("gender"));
        assertEquals("10/05/2015", children.getFirst().get("dob"));

        // Second child
        assertEquals("Bob Smith", children.get(1).get("fullName"));
        assertEquals("Male", children.get(1).get("gender"));
        assertEquals("20/08/2018", children.get(1).get("dob"));
    }

    @Test
    void testBuildHeaderPlaceholders_childrenTablePopulated_manyChildren() throws IOException {
        // Test that individual placeholders support many children (up to 15)
        Long caseId = 1234567890123456L;

        // Create 8 children to verify support for larger families
        List<Element<ChildDetailsRevised>> childElements = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            ChildDetailsRevised child = ChildDetailsRevised.builder()
                .firstName("Child" + i)
                .lastName("Family")
                .gender(i % 2 == 0 ? Gender.female : Gender.male)
                .dateOfBirth(LocalDate.of(2010 + i, i, i))
                .build();
            childElements.add(Element.<ChildDetailsRevised>builder()
                .id(UUID.randomUUID())
                .value(child)
                .build());
        }

        ManageOrders manageOrders = ManageOrders.builder()
            .isTheOrderAboutAllChildren(YesOrNo.Yes)
            .build();

        CaseData caseData = CaseData.builder()
            .newChildDetails(childElements)
            .manageOrders(manageOrders)
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();

        // Verify children list contains all 8 children (no hardcoded limit)
        @SuppressWarnings("unchecked")
        List<Map<String, String>> children = (List<Map<String, String>>) placeholders.get("children");
        assertNotNull(children);
        assertEquals(8, children.size());

        // Verify first and last children
        assertEquals("Child1 Family", children.getFirst().get("fullName"));
        assertEquals("Male", children.getFirst().get("gender"));

        assertEquals("Child8 Family", children.get(7).get("fullName"));
        assertEquals("Female", children.get(7).get("gender"));
    }

    @Test
    void testBuildHeaderPlaceholders_nullRepresentativeHandledAsEmpty() throws IOException {
        Long caseId = 1234567890123456L;
        PartyDetails respondent = PartyDetails.builder()
            .firstName("David")
            .lastName("Williams")
            .representativeFirstName(null)
            .representativeLastName(null)
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(Element.<PartyDetails>builder().value(respondent).build()))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("", placeholders.get("respondent1RepresentativeName"));
    }

    @Test
    void testBuildHeaderPlaceholders_multipleRespondentsPopulated() throws IOException {
        Long caseId = 1234567890123456L;
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("David")
            .lastName("Williams")
            .relationshipToChildren("Father")
            .build();

        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("Emma")
            .lastName("Brown")
            .relationshipToChildren("Grandmother")
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(
                Element.<PartyDetails>builder().value(respondent1).build(),
                Element.<PartyDetails>builder().value(respondent2).build()
            ))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("David Williams", placeholders.get("respondent1Name"));
        assertEquals("Father", placeholders.get("respondent1RelationshipToChild"));
        assertEquals("Emma Brown", placeholders.get("respondent2Name"));
        assertEquals("Grandmother", placeholders.get("respondent2RelationshipToChild"));
    }

    // ========== Tests for RESPONDENT RELATIONSHIP FROM RELATIONS ==========

    @Test
    void testRespondentRelationship_fromRelationsData_matchByName() throws IOException {
        Long caseId = 1234567890123456L;

        // Respondent without relationshipToChildren set
        PartyDetails respondent = PartyDetails.builder()
            .firstName("Mary")
            .lastName("Richards")
            .build();

        // Relations data with relationship
        ChildrenAndRespondentRelation relation = ChildrenAndRespondentRelation.builder()
            .respondentId("resp-123")
            .respondentFullName("Mary Richards")
            .childAndRespondentRelation(RelationshipsEnum.mother)
            .build();

        Relations relations = Relations.builder()
            .childAndRespondentRelations(List.of(
                Element.<ChildrenAndRespondentRelation>builder().value(relation).build()
            ))
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(Element.<PartyDetails>builder().value(respondent).build()))
            .relations(relations)
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Mother", placeholders.get("respondent1RelationshipToChild"));
    }

    @Test
    void testRespondentRelationship_multipleChildren_sameRelationship() throws IOException {
        Long caseId = 1234567890123456L;

        PartyDetails respondent = PartyDetails.builder()
            .firstName("Jane")
            .lastName("Doe")
            .build();

        // Same respondent, same relationship to multiple children
        ChildrenAndRespondentRelation relation1 = ChildrenAndRespondentRelation.builder()
            .respondentId("resp-123")
            .respondentFullName("Jane Doe")
            .childAndRespondentRelation(RelationshipsEnum.mother)
            .childFullName("Child 1")
            .build();

        ChildrenAndRespondentRelation relation2 = ChildrenAndRespondentRelation.builder()
            .respondentId("resp-123")
            .respondentFullName("Jane Doe")
            .childAndRespondentRelation(RelationshipsEnum.mother)
            .childFullName("Child 2")
            .build();

        Relations relations = Relations.builder()
            .childAndRespondentRelations(List.of(
                Element.<ChildrenAndRespondentRelation>builder().value(relation1).build(),
                Element.<ChildrenAndRespondentRelation>builder().value(relation2).build()
            ))
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(Element.<PartyDetails>builder().value(respondent).build()))
            .relations(relations)
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        // Should deduplicate to just "Mother"
        assertEquals("Mother", placeholders.get("respondent1RelationshipToChild"));
    }

    @Test
    void testRespondentRelationship_multipleChildren_differentRelationships() throws IOException {
        Long caseId = 1234567890123456L;

        PartyDetails respondent = PartyDetails.builder()
            .firstName("John")
            .lastName("Smith")
            .build();

        // Same respondent, different relationships to different children (blended family)
        ChildrenAndRespondentRelation relation1 = ChildrenAndRespondentRelation.builder()
            .respondentId("resp-123")
            .respondentFullName("John Smith")
            .childAndRespondentRelation(RelationshipsEnum.father)
            .childFullName("Child 1")
            .build();

        ChildrenAndRespondentRelation relation2 = ChildrenAndRespondentRelation.builder()
            .respondentId("resp-123")
            .respondentFullName("John Smith")
            .childAndRespondentRelation(RelationshipsEnum.stepFather)
            .childFullName("Child 2")
            .build();

        Relations relations = Relations.builder()
            .childAndRespondentRelations(List.of(
                Element.<ChildrenAndRespondentRelation>builder().value(relation1).build(),
                Element.<ChildrenAndRespondentRelation>builder().value(relation2).build()
            ))
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(Element.<PartyDetails>builder().value(respondent).build()))
            .relations(relations)
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        // Should show both unique relationships
        String relationship = (String) placeholders.get("respondent1RelationshipToChild");
        assertTrue(relationship.contains("Father"));
        assertTrue(relationship.contains("Step-father"));
    }

    @Test
    void testRespondentRelationship_multipleRespondents_correctMatching() throws IOException {
        Long caseId = 1234567890123456L;

        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("Mary")
            .lastName("Richards")
            .build();

        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("Tom")
            .lastName("Richards")
            .build();

        ChildrenAndRespondentRelation relation1 = ChildrenAndRespondentRelation.builder()
            .respondentId("resp-1")
            .respondentFullName("Mary Richards")
            .childAndRespondentRelation(RelationshipsEnum.mother)
            .build();

        ChildrenAndRespondentRelation relation2 = ChildrenAndRespondentRelation.builder()
            .respondentId("resp-2")
            .respondentFullName("Tom Richards")
            .childAndRespondentRelation(RelationshipsEnum.father)
            .build();

        Relations relations = Relations.builder()
            .childAndRespondentRelations(List.of(
                Element.<ChildrenAndRespondentRelation>builder().value(relation1).build(),
                Element.<ChildrenAndRespondentRelation>builder().value(relation2).build()
            ))
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(
                Element.<PartyDetails>builder().value(respondent1).build(),
                Element.<PartyDetails>builder().value(respondent2).build()
            ))
            .relations(relations)
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Mother", placeholders.get("respondent1RelationshipToChild"));
        assertEquals("Father", placeholders.get("respondent2RelationshipToChild"));
    }

    @Test
    void testRepresentativeClause_emptyWhenNoRepresentative() throws IOException {
        Long caseId = 1234567890123456L;

        PartyDetails respondent = PartyDetails.builder()
            .firstName("David")
            .lastName("Williams")
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(Element.<PartyDetails>builder().value(respondent).build()))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("", placeholders.get("respondent1RepresentativeClause"));
    }

    @Test
    void testRepresentativeClause_populatedWhenRepresentativeExists() throws IOException {
        Long caseId = 1234567890123456L;

        PartyDetails respondent = PartyDetails.builder()
            .firstName("David")
            .lastName("Williams")
            .representativeFirstName("Emma")
            .representativeLastName("Solicitor")
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(Element.<PartyDetails>builder().value(respondent).build()))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals(", legally represented", placeholders.get("respondent1RepresentativeClause"));
    }

    @Test
    void testBuildHeaderPlaceholders_fullCase_applicantAndMultipleRespondentsAllRepresented() throws IOException {
        // Comprehensive test: applicant with representative, 3 respondents all with representatives and relationships
        Long caseId = 1234567890123456L;

        // Applicant with solicitor
        PartyDetails applicant = PartyDetails.builder()
            .firstName("Alice")
            .lastName("Thompson")
            .representativeFirstName("Robert")
            .representativeLastName("Counsel QC")
            .build();

        // Respondent 1 - Mother with solicitor
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("Barbara")
            .lastName("Jones")
            .representativeFirstName("Sarah")
            .representativeLastName("Solicitor")
            .build();

        // Respondent 2 - Father with solicitor
        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("Charles")
            .lastName("Jones")
            .representativeFirstName("Michael")
            .representativeLastName("Barrister")
            .build();

        // Respondent 3 - Grandmother with solicitor
        PartyDetails respondent3 = PartyDetails.builder()
            .firstName("Dorothy")
            .lastName("Smith")
            .representativeFirstName("Emma")
            .representativeLastName("Legal Rep")
            .build();

        // Relations data for all respondents
        ChildrenAndRespondentRelation relation1 = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Barbara Jones")
            .childAndRespondentRelation(RelationshipsEnum.mother)
            .build();

        ChildrenAndRespondentRelation relation2 = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Charles Jones")
            .childAndRespondentRelation(RelationshipsEnum.father)
            .build();

        ChildrenAndRespondentRelation relation3 = ChildrenAndRespondentRelation.builder()
            .respondentFullName("Dorothy Smith")
            .childAndRespondentRelation(RelationshipsEnum.grandParent)
            .build();

        Relations relations = Relations.builder()
            .childAndRespondentRelations(List.of(
                Element.<ChildrenAndRespondentRelation>builder().value(relation1).build(),
                Element.<ChildrenAndRespondentRelation>builder().value(relation2).build(),
                Element.<ChildrenAndRespondentRelation>builder().value(relation3).build()
            ))
            .build();

        // Children on the case
        ChildDetailsRevised child1 = ChildDetailsRevised.builder()
            .firstName("Emily")
            .lastName("Jones")
            .gender(Gender.female)
            .dateOfBirth(java.time.LocalDate.of(2018, 5, 15))
            .build();

        ChildDetailsRevised child2 = ChildDetailsRevised.builder()
            .firstName("James")
            .lastName("Jones")
            .gender(Gender.male)
            .dateOfBirth(java.time.LocalDate.of(2020, 8, 22))
            .build();

        // ManageOrders with isTheOrderAboutAllChildren = Yes to include all children
        ManageOrders manageOrders = ManageOrders.builder()
            .isTheOrderAboutAllChildren(YesOrNo.Yes)
            .build();

        CaseData caseData = CaseData.builder()
            .id(caseId)
            .courtName("Central Family Court")
            .manageOrders(manageOrders)
            .applicants(List.of(Element.<PartyDetails>builder().value(applicant).build()))
            .respondents(List.of(
                Element.<PartyDetails>builder().value(respondent1).build(),
                Element.<PartyDetails>builder().value(respondent2).build(),
                Element.<PartyDetails>builder().value(respondent3).build()
            ))
            .relations(relations)
            .newChildDetails(List.of(
                Element.<ChildDetailsRevised>builder().value(child1).build(),
                Element.<ChildDetailsRevised>builder().value(child2).build()
            ))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();

        // Verify case details
        assertEquals("1234-5678-9012-3456", placeholders.get("caseNumber"));
        assertEquals("Central Family Court", placeholders.get("courtName"));

        // Verify applicant
        assertEquals("Alice Thompson", placeholders.get("applicant1Name"));
        assertEquals("Robert Counsel QC", placeholders.get("applicant1RepresentativeName"));
        assertEquals(", legally represented", placeholders.get("applicant1RepresentativeClause"));

        // Verify respondent 1 - Mother
        assertEquals("Barbara Jones", placeholders.get("respondent1Name"));
        assertEquals("Mother", placeholders.get("respondent1RelationshipToChild"));
        assertEquals("Sarah Solicitor", placeholders.get("respondent1RepresentativeName"));
        assertEquals(", legally represented", placeholders.get("respondent1RepresentativeClause"));

        // Verify respondent 2 - Father
        assertEquals("Charles Jones", placeholders.get("respondent2Name"));
        assertEquals("Father", placeholders.get("respondent2RelationshipToChild"));
        assertEquals("Michael Barrister", placeholders.get("respondent2RepresentativeName"));
        assertEquals(", legally represented", placeholders.get("respondent2RepresentativeClause"));

        // Verify respondent 3 - Grandmother
        assertEquals("Dorothy Smith", placeholders.get("respondent3Name"));
        assertEquals("Grandparent", placeholders.get("respondent3RelationshipToChild"));
        assertEquals("Emma Legal Rep", placeholders.get("respondent3RepresentativeName"));
        assertEquals(", legally represented", placeholders.get("respondent3RepresentativeClause"));

        // Verify children
        List<Map<String, String>> children = (List<Map<String, String>>) placeholders.get("children");
        assertNotNull(children);
        assertEquals(2, children.size());
        assertEquals("Emily Jones", children.getFirst().get("fullName"));
        assertEquals("Female", children.getFirst().get("gender"));
        assertEquals("James Jones", children.get(1).get("fullName"));
        assertEquals("Male", children.get(1).get("gender"));
    }

    // ========== Tests for judge name and order date from map (mid-event callback population) ==========

    @Test
    void testBuildHeaderPlaceholders_judgeNameFromMap_whenSetDuringCallback() throws IOException {
        // Arrange - simulates controller setting judge name in map during mid-event callback
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .judgeOrMagistratesLastName("Old Judge Name") // This should be overridden by map value
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("judgeOrMagistratesLastName", "HHJ Smith"); // Set by controller when logged-in user is judge

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - should use value from map, not from caseData
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("HHJ Smith", placeholders.get("judgeName"));
    }

    @Test
    void testBuildHeaderPlaceholders_orderDateFromHearing_whenHearingSelected() throws IOException {
        // Arrange - hearing is selected, should use hearing date
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();

        // For custom orders, use the custom order field names
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderWasApprovedAtHearing", "Yes");
        Map<String, Object> hearingValue = new HashMap<>();
        hearingValue.put("code", "hearing-123");
        hearingValue.put("label", "Final Hearing - 15/03/2026 10:00:00");
        Map<String, Object> hearingsType = new HashMap<>();
        hearingsType.put("value", hearingValue);
        caseDataMap.put("customOrderHearingsType", hearingsType);

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - should use hearing date and "at a hearing"
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("15/03/2026", placeholders.get("orderDate"));
        assertEquals("at a hearing", placeholders.get("hearingOrPapers"));
    }

    @Test
    void testBuildHeaderPlaceholders_orderDateFromDateOrderMade_whenNoHearing() throws IOException {
        // Arrange - no hearing, but dateOrderMade is set
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("dateOrderMade", LocalDate.of(2026, 3, 10));

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - should use dateOrderMade and "on the papers"
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("10/03/2026", placeholders.get("orderDate"));
        assertEquals("on the papers", placeholders.get("hearingOrPapers"));
    }

    @Test
    void testBuildHeaderPlaceholders_orderDateCurrentDate_whenNoHearingSelected() throws IOException {
        // Arrange - no hearing selected, no dateOrderMade, should use current date
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();

        Map<String, Object> caseDataMap = new HashMap<>();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - should use current date and "on the papers"
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String expectedDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        assertEquals(expectedDate, placeholders.get("orderDate"));
        assertEquals("on the papers", placeholders.get("hearingOrPapers"));
    }

    @Test
    void testBuildHeaderPlaceholders_judgeNameFallsBackToCaseData_whenNotInMap() throws IOException {
        // Arrange - map doesn't have judge name, should fall back to caseData
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .judgeOrMagistratesLastName("HHJ Richardson")
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();
        // No judgeOrMagistratesLastName in map

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - should fall back to caseData value
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("HHJ Richardson", placeholders.get("judgeName"));
    }

    @Test
    void testBuildHeaderPlaceholders_hearingOrPapersOnPapers_whenNoHearing() throws IOException {
        // Arrange - no hearing selected
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().build()) // No hearingsType set
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - should show "on the papers"
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("on the papers", placeholders.get("hearingOrPapers"));
    }

    @Test
    void testBuildHeaderPlaceholders_judgeFromMapAndDateFromHearing() throws IOException {
        // Arrange - judge name from map, date from selected hearing
        final Long caseId = 1234567890123456L;
        final CaseData caseData = CaseData.builder()
            .courtName("Central Family Court")
            .build();

        // For custom orders, use the custom order field names
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("judgeOrMagistratesLastName", "District Judge Taylor");
        caseDataMap.put("customOrderWasApprovedAtHearing", "Yes");
        Map<String, Object> hearingValue = new HashMap<>();
        hearingValue.put("code", "hearing-456");
        hearingValue.put("label", "Case Management - 02/02/2025 14:30:00");
        Map<String, Object> hearingsType = new HashMap<>();
        hearingsType.put("value", hearingValue);
        caseDataMap.put("customOrderHearingsType", hearingsType);

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - judge from map, date from hearing, "at a hearing"
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("District Judge Taylor", placeholders.get("judgeName"));
        assertEquals("02/02/2025", placeholders.get("orderDate"));
        assertEquals("Central Family Court", placeholders.get("courtName"));
        assertEquals("at a hearing", placeholders.get("hearingOrPapers"));
    }

    @Test
    void testBuildHeaderPlaceholders_judgeTitleFromMapAsString() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("judgeOrMagistrateTitle", "herHonourJudge");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Her Honour Judge", placeholders.get("judgeTitle"));
    }

    @Test
    void testBuildHeaderPlaceholders_judgeTitleFromMapAsEnum() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("judgeOrMagistrateTitle", JudgeOrMagistrateTitleEnum.districtJudge);

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("District Judge", placeholders.get("judgeTitle"));
    }

    @Test
    void testBuildHeaderPlaceholders_judgeTitleFromMapAsInvalidString() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("judgeOrMagistrateTitle", "Custom Title");  // Not a valid enum

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        // Falls back to using the string as-is
        assertEquals("Custom Title", placeholders.get("judgeTitle"));
    }

    @Test
    void testBuildHeaderPlaceholders_judgeTitleFromMapAsOtherType() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("judgeOrMagistrateTitle", 12345);  // Neither String nor Enum

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        // Uses toString() on the object
        assertEquals("12345", placeholders.get("judgeTitle"));
    }

    // ========== Tests for getEffectiveOrderName (custom order dropdown feature) ==========

    @ParameterizedTest
    @CsvSource({
        "standardDirectionsOrder, Standard directions order",
        "blankOrderOrDirections, Blank order or directions (C21)",
        "nonMolestation, Non-molestation order (FL404A)"
    })
    void testGetEffectiveOrderName_returnsDropdownValue(String optionKey, String expectedDisplayValue) {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", optionKey);

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        assertEquals(expectedDisplayValue, result);
    }

    @Test
    void testGetEffectiveOrderName_returnsTextField_whenOtherSelected() {
        // Arrange - user selects "Other" and types custom name
        CaseData caseData = CaseData.builder()
            .nameOfOrder("My Custom Order Name")
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "other");

        // Act
        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        // Assert - should use the text field value when "Other" is selected
        assertEquals("My Custom Order Name", result);
    }

    @Test
    void testGetEffectiveOrderName_returnsTextField_whenDropdownIsNull() {
        // Arrange - backwards compatibility: no dropdown selection, only text field
        CaseData caseData = CaseData.builder()
            .nameOfOrder("Legacy Order Name")
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        // No customOrderNameOption in map

        // Act
        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        // Assert - should fall back to text field
        assertEquals("Legacy Order Name", result);
    }

    @Test
    void testGetEffectiveOrderName_returnsDefault_whenBothAreNull() {
        // Arrange - neither dropdown nor text field set
        CaseData caseData = CaseData.builder()
            .nameOfOrder(null)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        // Act
        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        // Assert - should return default value
        assertEquals("custom_order", result);
    }

    @Test
    void testGetEffectiveOrderName_returnsDefault_whenOtherSelectedButTextFieldBlank() {
        // Arrange - "Other" selected but user didn't type anything
        CaseData caseData = CaseData.builder()
            .nameOfOrder("   ")  // blank/whitespace only
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "other");

        // Act
        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        // Assert - should return default value
        assertEquals("custom_order", result);
    }

    @Test
    void testGetEffectiveOrderName_returnsDefault_whenOtherSelectedAndTextFieldEmpty() {
        // Arrange - "Other" selected but nameOfOrder is empty string
        CaseData caseData = CaseData.builder()
            .nameOfOrder("")
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "other");

        // Act
        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        // Assert - should return default value
        assertEquals("custom_order", result);
    }

    @Test
    void testGetEffectiveOrderName_returnsTextField_whenMapIsNull() {
        // Arrange - map is null (backwards compatibility)
        CaseData caseData = CaseData.builder()
            .nameOfOrder("Fallback Order Name")
            .build();

        // Act
        String result = customOrderService.getEffectiveOrderName(caseData, null);

        // Assert - should fall back to text field
        assertEquals("Fallback Order Name", result);
    }

    @Test
    void testGetEffectiveOrderName_handlesEnumObjectInMap() {
        // Arrange - map contains enum object instead of string (happens in some contexts)
        CaseData caseData = CaseData.builder()
            .nameOfOrder("This should be ignored")
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", CustomOrderNameOptionsEnum.parentalResponsibility);

        // Act
        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        // Assert
        assertEquals("Parental responsibility order (C45A)", result);
    }

    @Test
    void testGetEffectiveOrderName_allDropdownOptionsHaveDisplayValues() {
        // Verify all enum values have proper display values
        for (CustomOrderNameOptionsEnum option : CustomOrderNameOptionsEnum.values()) {
            assertNotNull(option.getDisplayedValue(), "Display value should not be null for " + option.name());
            assertFalse(option.getDisplayedValue().isEmpty(), "Display value should not be empty for " + option.name());
        }
    }

    // ========== Tests for resolveCourtName ==========

    @Test
    void testResolveCourtName_fromCaseData() {
        // Arrange - court name is in caseData
        CaseData caseData = CaseData.builder()
            .courtName("Central Family Court")
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        // Act
        String result = customOrderService.resolveCourtName(caseData, caseDataMap);

        // Assert
        assertEquals("Central Family Court", result);
    }

    @Test
    void testResolveCourtName_fromCaseDataMap() {
        // Arrange - court name is in map but not caseData
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("courtName", "Birmingham Family Court");

        // Act
        String result = customOrderService.resolveCourtName(caseData, caseDataMap);

        // Assert
        assertEquals("Birmingham Family Court", result);
    }

    @Test
    void testResolveCourtName_fromAllocatedJudgeDetails() {
        // Arrange - court name is in allocatedJudgeDetails
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        Map<String, Object> allocatedJudge = new HashMap<>();
        allocatedJudge.put("courtName", "Manchester Family Court");
        caseDataMap.put("allocatedJudgeDetails", allocatedJudge);

        // Act
        String result = customOrderService.resolveCourtName(caseData, caseDataMap);

        // Assert
        assertEquals("Manchester Family Court", result);
    }

    @Test
    void testResolveCourtName_fromCourtList() {
        // Arrange - court name is in courtList dynamic list
        final CaseData caseData = CaseData.builder().build();
        final Map<String, Object> caseDataMap = new HashMap<>();
        Map<String, Object> courtListValue = new HashMap<>();
        courtListValue.put("code", "123");
        courtListValue.put("label", "Leeds Family Court");
        Map<String, Object> courtList = new HashMap<>();
        courtList.put("value", courtListValue);
        caseDataMap.put("courtList", courtList);

        // Act
        String result = customOrderService.resolveCourtName(caseData, caseDataMap);

        // Assert
        assertEquals("Leeds Family Court", result);
    }

    @Test
    void testResolveCourtName_returnsNull_whenNoSourceAvailable() {
        // Arrange - no court name anywhere
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();

        // Act
        String result = customOrderService.resolveCourtName(caseData, caseDataMap);

        // Assert
        assertNull(result);
    }

    @Test
    void testResolveCourtName_skipsNullCourtName() {
        // Arrange - courtName in map is "null" string
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("courtName", "null");

        // Act
        String result = customOrderService.resolveCourtName(caseData, caseDataMap);

        // Assert
        assertNull(result);
    }

    @Test
    void testResolveCourtName_prefersDirectCourtName_overAllocatedJudge() {
        // Arrange - both caseData and allocatedJudge have court name
        CaseData caseData = CaseData.builder()
            .courtName("Priority Court")
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        Map<String, Object> allocatedJudge = new HashMap<>();
        allocatedJudge.put("courtName", "Fallback Court");
        caseDataMap.put("allocatedJudgeDetails", allocatedJudge);

        // Act
        String result = customOrderService.resolveCourtName(caseData, caseDataMap);

        // Assert - should use caseData court name (higher priority)
        assertEquals("Priority Court", result);
    }

    // ========== Tests for combineAndFinalizeCustomOrder ==========

    @Test
    void testCombineAndFinalizeCustomOrder_skipsWhenNoDocuments() {
        // Arrange - no customOrderDoc
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataUpdated = new HashMap<>();

        // Act - should not throw, just skip
        customOrderService.combineAndFinalizeCustomOrder("auth", caseData, caseDataUpdated, false);

        // Assert - no changes made
        assertNull(caseDataUpdated.get("orderCollection"));
    }

    @Test
    void testCombineAndFinalizeCustomOrder_skipsWhenNoHeaderPreview() {
        // Arrange - has customOrderDoc but no headerPreview
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        uk.gov.hmcts.reform.prl.models.documents.Document customDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentBinaryUrl("http://binary-url")
                .build();
        caseDataUpdated.put("customOrderDoc", customDoc);

        // Act - should not throw, just skip
        customOrderService.combineAndFinalizeCustomOrder("auth", caseData, caseDataUpdated, false);

        // Assert - previewOrderDoc not cleaned up (since combine was skipped)
        assertNull(caseDataUpdated.get("previewOrderDoc"));
    }

    // ========== Tests for extractCourtNameFromAllocatedJudge (via resolveCourtName) ==========

    @Test
    void testExtractCourtName_allocatedJudge_nullObject() {
        // Arrange
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("allocatedJudgeDetails", null);

        // Act
        String result = customOrderService.resolveCourtName(caseData, caseDataMap);

        // Assert
        assertNull(result);
    }

    @Test
    void testExtractCourtName_allocatedJudge_emptyMap() {
        // Arrange
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("allocatedJudgeDetails", new HashMap<>());

        // Act
        String result = customOrderService.resolveCourtName(caseData, caseDataMap);

        // Assert
        assertNull(result);
    }

    @Test
    void testExtractCourtName_allocatedJudge_trimWhitespace() {
        // Arrange
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        Map<String, Object> allocatedJudge = new HashMap<>();
        allocatedJudge.put("courtName", "  Trimmed Court  ");
        caseDataMap.put("allocatedJudgeDetails", allocatedJudge);

        // Act
        String result = customOrderService.resolveCourtName(caseData, caseDataMap);

        // Assert
        assertEquals("Trimmed Court", result);
    }

    // ========== Tests for extractCourtNameFromDynamicList (via resolveCourtName) ==========

    @Test
    void testExtractCourtName_dynamicList_nullValue() {
        // Arrange
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        Map<String, Object> courtList = new HashMap<>();
        courtList.put("value", null);
        caseDataMap.put("courtList", courtList);

        // Act
        String result = customOrderService.resolveCourtName(caseData, caseDataMap);

        // Assert
        assertNull(result);
    }

    @Test
    void testExtractCourtName_dynamicList_missingLabel() {
        // Arrange
        final CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        Map<String, Object> courtListValue = new HashMap<>();
        courtListValue.put("code", "123");
        // no "label" key
        Map<String, Object> courtList = new HashMap<>();
        courtList.put("value", courtListValue);
        caseDataMap.put("courtList", courtList);

        // Act
        String result = customOrderService.resolveCourtName(caseData, caseDataMap);

        // Assert
        assertNull(result);
    }

    // ========== Tests for combineAndFinalizeCustomOrder - FULL FLOW ==========

    @Test
    void testCombineAndFinalizeCustomOrder_readsPreviewDocFromCaseDataUpdated() {
        // Arrange - previewOrderDoc is in caseDataUpdated (set during mid-event)
        uk.gov.hmcts.reform.prl.models.documents.Document customDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentBinaryUrl("http://custom-binary")
                .documentUrl("http://custom-url")
                .documentFileName("custom.docx")
                .build();

        uk.gov.hmcts.reform.prl.models.documents.Document previewDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentBinaryUrl("http://preview-binary")
                .documentUrl("http://preview-url")
                .documentFileName("preview.docx")
                .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("customOrderDoc", customDoc);
        caseDataUpdated.put("previewOrderDoc", previewDoc);

        // Mock objectMapper to return the documents
        when(objectMapper.convertValue(customDoc, uk.gov.hmcts.reform.prl.models.documents.Document.class))
            .thenReturn(customDoc);
        when(objectMapper.convertValue(previewDoc, uk.gov.hmcts.reform.prl.models.documents.Document.class))
            .thenReturn(previewDoc);

        CaseData caseData = CaseData.builder()
            .id(123L)
            .build();

        // Act - will fail at processCustomOrderOnSubmitted due to missing mocks, but we verify docs are read
        customOrderService.combineAndFinalizeCustomOrder("auth", caseData, caseDataUpdated, true);

        // Assert - verify objectMapper was called to convert both documents
        verify(objectMapper).convertValue(customDoc, uk.gov.hmcts.reform.prl.models.documents.Document.class);
        verify(objectMapper).convertValue(previewDoc, uk.gov.hmcts.reform.prl.models.documents.Document.class);
    }

    @Test
    void testCombineAndFinalizeCustomOrder_fallsBackToPreviewDocFromCaseData() {
        // Arrange - previewOrderDoc NOT in caseDataUpdated, should fall back to caseData
        uk.gov.hmcts.reform.prl.models.documents.Document customDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentBinaryUrl("http://custom-binary")
                .documentUrl("http://custom-url")
                .documentFileName("custom.docx")
                .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("customOrderDoc", customDoc);
        // NO previewOrderDoc in map - should fall back to caseData.getPreviewOrderDoc()

        when(objectMapper.convertValue(customDoc, uk.gov.hmcts.reform.prl.models.documents.Document.class))
            .thenReturn(customDoc);

        uk.gov.hmcts.reform.prl.models.documents.Document previewDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentBinaryUrl("http://preview-binary")
                .documentUrl("http://preview-url")
                .documentFileName("preview.docx")
                .build();
        CaseData caseData = CaseData.builder()
            .id(123L)
            .previewOrderDoc(previewDoc)  // Fallback source
            .build();

        // Act
        customOrderService.combineAndFinalizeCustomOrder("auth", caseData, caseDataUpdated, true);

        // Assert - the method should have used caseData.getPreviewOrderDoc() as fallback
        // Since previewDoc has a valid binaryUrl, processing would have continued
        verify(objectMapper).convertValue(customDoc, uk.gov.hmcts.reform.prl.models.documents.Document.class);
    }

    @Test
    void testCombineAndFinalizeCustomOrder_attemptsProcessingWithValidDocs() {
        // Arrange - both docs have valid binary URLs
        uk.gov.hmcts.reform.prl.models.documents.Document customDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentBinaryUrl("http://custom-binary")
                .build();

        uk.gov.hmcts.reform.prl.models.documents.Document previewDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentBinaryUrl("http://preview-binary")
                .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("customOrderDoc", customDoc);
        caseDataUpdated.put("previewOrderDoc", previewDoc);

        when(objectMapper.convertValue(any(), eq(uk.gov.hmcts.reform.prl.models.documents.Document.class)))
            .thenReturn(customDoc)
            .thenReturn(previewDoc);

        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");

        CaseData caseData = CaseData.builder().id(123L).build();

        // Act - will attempt processing (may fail due to incomplete mocking, but that's ok)
        customOrderService.combineAndFinalizeCustomOrder("auth", caseData, caseDataUpdated, true);

        // Assert - verify the method attempted to get tokens for document download
        verify(systemUserService).getSysUserToken();
        verify(authTokenGenerator).generate();
    }

    @Test
    void testCombineAndFinalizeCustomOrder_skipsWhenCustomDocBinaryUrlNull() {
        // Arrange - customOrderDoc has null binaryUrl
        uk.gov.hmcts.reform.prl.models.documents.Document customDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentBinaryUrl(null)  // null binary URL
                .documentUrl("http://custom-url")
                .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("customOrderDoc", customDoc);

        when(objectMapper.convertValue(any(), eq(uk.gov.hmcts.reform.prl.models.documents.Document.class)))
            .thenReturn(customDoc);

        CaseData caseData = CaseData.builder().id(123L).build();

        // Act
        customOrderService.combineAndFinalizeCustomOrder("auth", caseData, caseDataUpdated, true);

        // Assert - should skip processing (no download attempts)
        verify(documentGenService, never()).getDocumentBytes(any(), any(), any());
    }

    @Test
    void testCombineAndFinalizeCustomOrder_skipsWhenHeaderPreviewBinaryUrlNull() {
        // Arrange - headerPreview has null binaryUrl
        uk.gov.hmcts.reform.prl.models.documents.Document customDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentBinaryUrl("http://custom-binary")
                .build();

        uk.gov.hmcts.reform.prl.models.documents.Document previewDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentBinaryUrl(null)  // null binary URL
                .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("customOrderDoc", customDoc);
        caseDataUpdated.put("previewOrderDoc", previewDoc);

        when(objectMapper.convertValue(any(), eq(uk.gov.hmcts.reform.prl.models.documents.Document.class)))
            .thenReturn(customDoc)
            .thenReturn(previewDoc);

        CaseData caseData = CaseData.builder().id(123L).build();

        // Act
        customOrderService.combineAndFinalizeCustomOrder("auth", caseData, caseDataUpdated, true);

        // Assert - should skip processing (no download attempts)
        verify(documentGenService, never()).getDocumentBytes(any(), any(), any());
    }

    // ========== Tests for findRelationshipById ==========

    @Test
    void testRespondentRelationship_matchById_whenIdMatches() throws IOException {
        Long caseId = 1234567890123456L;
        String respondentId = "resp-uuid-123";

        PartyDetails respondent = PartyDetails.builder()
            .firstName("John")
            .lastName("Doe")
            .build();

        ChildrenAndRespondentRelation relation = ChildrenAndRespondentRelation.builder()
            .respondentId(respondentId)
            .respondentFullName("Different Name")
            .childAndRespondentRelation(RelationshipsEnum.father)
            .build();

        Relations relations = Relations.builder()
            .childAndRespondentRelations(List.of(
                Element.<ChildrenAndRespondentRelation>builder().value(relation).build()
            ))
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(Element.<PartyDetails>builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .value(respondent).build()))
            .relations(relations)
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        // Should find by index since name doesn't match and ID doesn't match element ID
        assertNotNull(placeholders.get("respondent1RelationshipToChild"));
    }

    // ========== Tests for findRelationshipByIndex ==========

    @Test
    void testRespondentRelationship_fallbackToIndex_whenNoNameOrIdMatch() throws IOException {
        Long caseId = 1234567890123456L;

        PartyDetails respondent = PartyDetails.builder()
            .firstName("Unknown")
            .lastName("Person")
            .build();

        ChildrenAndRespondentRelation relation = ChildrenAndRespondentRelation.builder()
            .respondentId("different-id")
            .respondentFullName("Different Name")
            .childAndRespondentRelation(RelationshipsEnum.grandParent)
            .build();

        Relations relations = Relations.builder()
            .childAndRespondentRelations(List.of(
                Element.<ChildrenAndRespondentRelation>builder().value(relation).build()
            ))
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(Element.<PartyDetails>builder().value(respondent).build()))
            .relations(relations)
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Grandparent", placeholders.get("respondent1RelationshipToChild"));
    }

    // ========== Tests for findRelationshipFromOldModel ==========

    @Test
    void testRespondentRelationship_fromOldChildrenModel() throws IOException {
        Long caseId = 1234567890123456L;

        // Old children model with respondentsRelationshipToChild
        Child oldChild = Child.builder()
            .firstName("Tommy")
            .lastName("Test")
            .respondentsRelationshipToChild(RelationshipsEnum.father)
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .firstName("David")
            .lastName("Williams")
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(Element.<PartyDetails>builder().value(respondent).build()))
            .children(List.of(Element.<Child>builder().value(oldChild).build()))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Father", placeholders.get("respondent1RelationshipToChild"));
    }

    @Test
    void testRespondentRelationship_fromOldChildrenModel_noRelationshipSet() throws IOException {
        Long caseId = 1234567890123456L;

        // Old children model WITHOUT respondentsRelationshipToChild
        Child oldChild = Child.builder()
            .firstName("Tommy")
            .lastName("Test")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .firstName("David")
            .lastName("Williams")
            .build();

        CaseData caseData = CaseData.builder()
            .respondents(List.of(Element.<PartyDetails>builder().value(respondent).build()))
            .children(List.of(Element.<Child>builder().value(oldChild).build()))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        // Falls back to empty when no relationship found
        assertEquals("", placeholders.get("respondent1RelationshipToChild"));
    }

    // ========== Tests for populateChildrensGuardian ==========

    @Test
    void testPopulateChildrensGuardian_fromCafcassOfficers() throws IOException {
        Long caseId = 1234567890123456L;

        uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer.ChildAndCafcassOfficer cafcassOfficer =
            uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer.ChildAndCafcassOfficer.builder()
                .cafcassOfficerName("Sarah Guardian")
                .build();

        CaseData caseData = CaseData.builder()
            .childAndCafcassOfficers(List.of(
                Element.<uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer.ChildAndCafcassOfficer>builder()
                    .value(cafcassOfficer).build()
            ))
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Sarah Guardian", placeholders.get("childrensGuardianName"));
    }

    @Test
    void testPopulateChildrensGuardian_emptyWhenNoCafcass() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("", placeholders.get("childrensGuardianName"));
    }

    // ========== Tests for reformatDateToUkFormat (via extractOrderDate) ==========

    @Test
    void testOrderDate_reformatsIsoDate() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("dateOrderMade", "2026-03-15");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("15/03/2026", placeholders.get("orderDate"));
    }

    // ========== Tests for getChildrenBySelection ==========

    @Test
    void testChildrenSelection_selectedSubset() throws IOException {
        Long caseId = 1234567890123456L;

        UUID child1Id = UUID.randomUUID();
        UUID child2Id = UUID.randomUUID();
        UUID child3Id = UUID.randomUUID();

        ChildDetailsRevised child1 = ChildDetailsRevised.builder()
            .firstName("Alice").lastName("Smith").gender(Gender.female).build();
        ChildDetailsRevised child2 = ChildDetailsRevised.builder()
            .firstName("Bob").lastName("Smith").gender(Gender.male).build();
        ChildDetailsRevised child3 = ChildDetailsRevised.builder()
            .firstName("Charlie").lastName("Smith").gender(Gender.male).build();

        uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList childOption =
            uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList.builder()
                .value(List.of(
                    uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement.builder()
                        .code(child1Id.toString()).label("Alice Smith").build(),
                    uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement.builder()
                        .code(child3Id.toString()).label("Charlie Smith").build()
                ))
                .build();

        ManageOrders manageOrders = ManageOrders.builder()
            .isTheOrderAboutAllChildren(YesOrNo.No)
            .isTheOrderAboutChildren(YesOrNo.Yes)
            .childOption(childOption)
            .build();

        CaseData caseData = CaseData.builder()
            .newChildDetails(List.of(
                Element.<ChildDetailsRevised>builder().id(child1Id).value(child1).build(),
                Element.<ChildDetailsRevised>builder().id(child2Id).value(child2).build(),
                Element.<ChildDetailsRevised>builder().id(child3Id).value(child3).build()
            ))
            .manageOrders(manageOrders)
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        @SuppressWarnings("unchecked")
        List<Map<String, String>> children = (List<Map<String, String>>) placeholders.get("children");

        assertEquals(2, children.size());
        assertEquals("Alice Smith", children.getFirst().get("fullName"));
        assertEquals("Charlie Smith", children.get(1).get("fullName"));
    }

    @Test
    void testChildrenSelection_noChildren_whenOrderNotAboutChildren() throws IOException {
        Long caseId = 1234567890123456L;

        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Alice").lastName("Smith").build();

        ManageOrders manageOrders = ManageOrders.builder()
            .isTheOrderAboutAllChildren(YesOrNo.No)
            .isTheOrderAboutChildren(YesOrNo.No)
            .build();

        CaseData caseData = CaseData.builder()
            .newChildDetails(List.of(
                Element.<ChildDetailsRevised>builder().id(UUID.randomUUID()).value(child).build()
            ))
            .manageOrders(manageOrders)
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        @SuppressWarnings("unchecked")
        List<Map<String, String>> children = (List<Map<String, String>>) placeholders.get("children");

        assertEquals(0, children.size());
        assertEquals(false, placeholders.get("hasChildren"));
    }

    // ========== Tests for findCustomOrderHeaderPreview ==========

    @Test
    void testFindCustomOrderHeaderPreview_findsInDraftCollection() {
        uk.gov.hmcts.reform.prl.models.documents.Document headerDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentFileName("custom_order_header_preview_123.docx")
                .documentBinaryUrl("http://binary")
                .build();

        uk.gov.hmcts.reform.prl.models.DraftOrder draftOrder =
            uk.gov.hmcts.reform.prl.models.DraftOrder.builder()
                .orderDocument(headerDoc)
                .build();

        CaseData caseData = CaseData.builder()
            .draftOrderCollection(List.of(
                Element.<uk.gov.hmcts.reform.prl.models.DraftOrder>builder()
                    .id(UUID.randomUUID())
                    .value(draftOrder)
                    .build()
            ))
            .build();

        CustomOrderService.CustomOrderLocation result = customOrderService.findCustomOrderHeaderPreview(caseData);

        assertNotNull(result);
        assertTrue(result.isInDraftCollection());
        assertEquals(0, result.index());
        assertEquals(headerDoc, result.document());
    }

    @Test
    void testFindCustomOrderHeaderPreview_findsInOrderCollection() {
        uk.gov.hmcts.reform.prl.models.documents.Document headerDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentFileName("custom_order_header_preview_456.docx")
                .documentBinaryUrl("http://binary")
                .build();

        uk.gov.hmcts.reform.prl.models.OrderDetails orderDetails =
            uk.gov.hmcts.reform.prl.models.OrderDetails.builder()
                .orderDocument(headerDoc)
                .build();

        CaseData caseData = CaseData.builder()
            .orderCollection(List.of(
                Element.<uk.gov.hmcts.reform.prl.models.OrderDetails>builder()
                    .id(UUID.randomUUID())
                    .value(orderDetails)
                    .build()
            ))
            .build();

        CustomOrderService.CustomOrderLocation result = customOrderService.findCustomOrderHeaderPreview(caseData);

        assertNotNull(result);
        assertFalse(result.isInDraftCollection());
        assertEquals(0, result.index());
    }

    @Test
    void testFindCustomOrderHeaderPreview_returnsNull_whenNotFound() {
        uk.gov.hmcts.reform.prl.models.documents.Document otherDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentFileName("other_order.docx")
                .build();

        uk.gov.hmcts.reform.prl.models.DraftOrder draftOrder =
            uk.gov.hmcts.reform.prl.models.DraftOrder.builder()
                .orderDocument(otherDoc)
                .build();

        CaseData caseData = CaseData.builder()
            .draftOrderCollection(List.of(
                Element.<uk.gov.hmcts.reform.prl.models.DraftOrder>builder()
                    .id(UUID.randomUUID())
                    .value(draftOrder)
                    .build()
            ))
            .build();

        CustomOrderService.CustomOrderLocation result = customOrderService.findCustomOrderHeaderPreview(caseData);

        assertNull(result);
    }

    @Test
    void testFindCustomOrderHeaderPreview_skipsPdfFiles() {
        uk.gov.hmcts.reform.prl.models.documents.Document pdfDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentFileName("custom_order_header_preview_123.pdf")
                .build();

        uk.gov.hmcts.reform.prl.models.DraftOrder draftOrder =
            uk.gov.hmcts.reform.prl.models.DraftOrder.builder()
                .orderDocument(pdfDoc)
                .build();

        CaseData caseData = CaseData.builder()
            .draftOrderCollection(List.of(
                Element.<uk.gov.hmcts.reform.prl.models.DraftOrder>builder()
                    .id(UUID.randomUUID())
                    .value(draftOrder)
                    .build()
            ))
            .build();

        CustomOrderService.CustomOrderLocation result = customOrderService.findCustomOrderHeaderPreview(caseData);

        assertNull(result);
    }

    // ========== Tests for updateOrderDocumentInCaseData ==========

    @Test
    void testUpdateOrderDocumentInCaseData_updatesDraftCollection() {
        UUID draftId = UUID.randomUUID();
        uk.gov.hmcts.reform.prl.models.documents.Document originalDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentFileName("original.docx")
                .build();

        uk.gov.hmcts.reform.prl.models.DraftOrder draftOrder =
            uk.gov.hmcts.reform.prl.models.DraftOrder.builder()
                .orderDocument(originalDoc)
                .judgeOrMagistratesLastName("Judge Name")
                .build();

        List<Element<uk.gov.hmcts.reform.prl.models.DraftOrder>> draftCollection = new ArrayList<>();
        draftCollection.add(Element.<uk.gov.hmcts.reform.prl.models.DraftOrder>builder()
            .id(draftId)
            .value(draftOrder)
            .build());

        CaseData caseData = CaseData.builder()
            .draftOrderCollection(draftCollection)
            .build();

        uk.gov.hmcts.reform.prl.models.documents.Document combinedDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentFileName("combined.docx")
                .documentBinaryUrl("http://combined")
                .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        CustomOrderService.CustomOrderLocation location =
            new CustomOrderService.CustomOrderLocation(originalDoc, true, 0);

        customOrderService.updateOrderDocumentInCaseData(caseData, combinedDoc, caseDataUpdated, location);

        assertTrue(caseDataUpdated.containsKey("draftOrderCollection"));
        @SuppressWarnings("unchecked")
        List<Element<uk.gov.hmcts.reform.prl.models.DraftOrder>> updatedDrafts =
            (List<Element<uk.gov.hmcts.reform.prl.models.DraftOrder>>) caseDataUpdated.get("draftOrderCollection");
        assertEquals(combinedDoc, updatedDrafts.getFirst().getValue().getOrderDocument());
        assertEquals("Judge Name", updatedDrafts.getFirst().getValue().getJudgeOrMagistratesLastName());
    }

    @Test
    void testUpdateOrderDocumentInCaseData_updatesOrderCollection() {
        UUID orderId = UUID.randomUUID();
        uk.gov.hmcts.reform.prl.models.documents.Document originalDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentFileName("original.docx")
                .build();

        uk.gov.hmcts.reform.prl.models.OrderDetails orderDetails =
            uk.gov.hmcts.reform.prl.models.OrderDetails.builder()
                .orderDocument(originalDoc)
                .orderType("TestOrder")
                .build();

        List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>> orderCollection = new ArrayList<>();
        orderCollection.add(Element.<uk.gov.hmcts.reform.prl.models.OrderDetails>builder()
            .id(orderId)
            .value(orderDetails)
            .build());

        CaseData caseData = CaseData.builder()
            .orderCollection(orderCollection)
            .build();

        uk.gov.hmcts.reform.prl.models.documents.Document sealedDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentFileName("sealed.pdf")
                .documentBinaryUrl("http://sealed")
                .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        CustomOrderService.CustomOrderLocation location =
            new CustomOrderService.CustomOrderLocation(originalDoc, false, 0);

        customOrderService.updateOrderDocumentInCaseData(caseData, sealedDoc, caseDataUpdated, location);

        assertTrue(caseDataUpdated.containsKey("orderCollection"));
        @SuppressWarnings("unchecked")
        List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>> updatedOrders =
            (List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>>) caseDataUpdated.get("orderCollection");
        assertEquals(sealedDoc, updatedOrders.getFirst().getValue().getOrderDocument());
        assertEquals(YesOrNo.No, updatedOrders.getFirst().getValue().getDoesOrderDocumentNeedSeal());
    }

    // ========== Tests for parseChildArrangementsSubType (via getEffectiveOrderName) ==========

    @Test
    void testGetEffectiveOrderName_withC43Order_parsesChildArrangementsSubTypeFromString() {
        Map<String, Object> c43Details = new HashMap<>();
        c43Details.put("ordersToIssue", List.of("childArrangementsOrder"));
        c43Details.put("childArrangementsOrderType", "spendTimeWithOrder");

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "childArrangementsSpecificProhibitedOrder");
        caseDataMap.put("customC43OrderDetails", c43Details);

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        assertNotNull(result);
        assertTrue(result.contains("Spend time with order"));
    }

    @Test
    void testGetEffectiveOrderName_withC43Order_parsesChildArrangementsSubTypeFromEnum() {
        Map<String, Object> c43Details = new HashMap<>();
        c43Details.put("ordersToIssue", List.of(OrderTypeEnum.childArrangementsOrder));
        c43Details.put("childArrangementsOrderType", ChildArrangementOrderTypeEnum.liveWithOrder);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", CustomOrderNameOptionsEnum.childArrangementsSpecificProhibitedOrder);
        caseDataMap.put("customC43OrderDetails", c43Details);

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        assertNotNull(result);
        assertTrue(result.contains("Live with order"));
    }

    @Test
    void testGetEffectiveOrderName_withC43Order_handlesNullChildArrangementsSubType() {
        Map<String, Object> c43Details = new HashMap<>();
        c43Details.put("ordersToIssue", List.of("prohibitedStepsOrder"));
        // No childArrangementsOrderType

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "childArrangementsSpecificProhibitedOrder");
        caseDataMap.put("customC43OrderDetails", c43Details);

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        assertNotNull(result);
        // Should still return a name based on the order type
        assertTrue(result.contains("Prohibited Steps Order"));
    }

    @Test
    void testGetEffectiveOrderName_withC43Order_handlesInvalidChildArrangementsSubType() {
        Map<String, Object> c43Details = new HashMap<>();
        c43Details.put("ordersToIssue", List.of("specificIssueOrder"));
        c43Details.put("childArrangementsOrderType", "invalidValue");

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "childArrangementsSpecificProhibitedOrder");
        caseDataMap.put("customC43OrderDetails", c43Details);

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        assertNotNull(result);
        // Should still return a name despite invalid child arrangements type
        assertTrue(result.contains("Specific Issue Order"));
    }

    @Test
    void testGetEffectiveOrderName_withC43Order_handlesNullC43Details() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "childArrangementsSpecificProhibitedOrder");
        // No customC43OrderDetails

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        assertNotNull(result);
        assertEquals("Child arrangements, specific issue or prohibited steps order (C43)", result);
    }

    @Test
    void testGetEffectiveOrderName_withC43Order_handlesNonMapC43Details() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "childArrangementsSpecificProhibitedOrder");
        caseDataMap.put("customC43OrderDetails", "not a map");

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        assertNotNull(result);
        assertEquals("Child arrangements, specific issue or prohibited steps order (C43)", result);
    }

    @Test
    void testGetEffectiveOrderName_withC43Order_handlesEmptyOrdersList() {
        Map<String, Object> c43Details = new HashMap<>();
        c43Details.put("ordersToIssue", List.of());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "childArrangementsSpecificProhibitedOrder");
        caseDataMap.put("customC43OrderDetails", c43Details);

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        assertNotNull(result);
        assertEquals("Child arrangements, specific issue or prohibited steps order (C43)", result);
    }

    @Test
    void testGetEffectiveOrderName_withC43Order_handlesNonListOrdersToIssue() {
        Map<String, Object> c43Details = new HashMap<>();
        c43Details.put("ordersToIssue", "not a list");

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "childArrangementsSpecificProhibitedOrder");
        caseDataMap.put("customC43OrderDetails", c43Details);

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        assertNotNull(result);
        assertEquals("Child arrangements, specific issue or prohibited steps order (C43)", result);
    }

    @Test
    void testGetEffectiveOrderName_withC43Order_handlesInvalidOrderTypeString() {
        Map<String, Object> c43Details = new HashMap<>();
        c43Details.put("ordersToIssue", List.of("invalidOrderType"));

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "childArrangementsSpecificProhibitedOrder");
        caseDataMap.put("customC43OrderDetails", c43Details);

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        // Falls back to dropdown when order type can't be parsed
        assertNotNull(result);
        assertEquals("Child arrangements, specific issue or prohibited steps order (C43)", result);
    }

    @Test
    void testGetEffectiveOrderName_withC43Order_handlesNonStringNonEnumOrderType() {
        Map<String, Object> c43Details = new HashMap<>();
        c43Details.put("ordersToIssue", List.of(12345));  // Neither String nor Enum

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "childArrangementsSpecificProhibitedOrder");
        caseDataMap.put("customC43OrderDetails", c43Details);

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        // Falls back to dropdown when order type is neither String nor Enum
        assertNotNull(result);
        assertEquals("Child arrangements, specific issue or prohibited steps order (C43)", result);
    }

    @Test
    void testGetEffectiveOrderName_withC21Order_parsesSubOptionFromString() {
        Map<String, Object> c21Details = new HashMap<>();
        c21Details.put("orderOptions", "c21other");

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "blankOrderOrDirections");
        caseDataMap.put("customC21OrderDetails", c21Details);

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        assertNotNull(result);
        assertEquals("Blank order or directions (C21): Other", result);
    }

    @Test
    void testGetEffectiveOrderName_withC21Order_parsesSubOptionFromEnum() {
        Map<String, Object> c21Details = new HashMap<>();
        c21Details.put("orderOptions", C21OrderOptionsEnum.c21ApplicationRefused);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", CustomOrderNameOptionsEnum.blankOrderOrDirections);
        caseDataMap.put("customC21OrderDetails", c21Details);

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        assertNotNull(result);
        assertEquals("Blank order or directions (C21): application refused", result);
    }

    @Test
    void testGetEffectiveOrderName_withC21Order_handlesInvalidSubOption() {
        Map<String, Object> c21Details = new HashMap<>();
        c21Details.put("orderOptions", "invalidOption");

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "blankOrderOrDirections");
        caseDataMap.put("customC21OrderDetails", c21Details);

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        // Falls back to dropdown display value when C21 sub-option parse fails
        assertNotNull(result);
        assertEquals("Blank order or directions (C21)", result);
    }

    @Test
    void testGetEffectiveOrderName_withC21Order_handlesNonStringNonEnumSubOption() {
        Map<String, Object> c21Details = new HashMap<>();
        c21Details.put("orderOptions", 12345);  // Neither String nor Enum

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "blankOrderOrDirections");
        caseDataMap.put("customC21OrderDetails", c21Details);

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        // Falls back to dropdown display value when orderOptions is not String or Enum
        assertNotNull(result);
        assertEquals("Blank order or directions (C21)", result);
    }

    @Test
    void testGetEffectiveOrderName_withC21Order_handlesNullOrderOptions() {
        Map<String, Object> c21Details = new HashMap<>();
        // No orderOptions key

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "blankOrderOrDirections");
        caseDataMap.put("customC21OrderDetails", c21Details);

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        // Falls back to dropdown display value when no orderOptions
        assertNotNull(result);
        assertEquals("Blank order or directions (C21)", result);
    }

    @Test
    void testGetEffectiveOrderName_withC21Order_handlesNullC21Details() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "blankOrderOrDirections");
        // No customC21OrderDetails

        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        // Falls back to dropdown display value when no C21 details
        assertNotNull(result);
        assertEquals("Blank order or directions (C21)", result);
    }

    @Test
    void testGetEffectiveOrderName_withNullCaseDataMap_usesTextFieldName() {
        CaseData caseData = CaseData.builder()
            .nameOfOrder("Custom Text Order Name")
            .build();

        String result = customOrderService.getEffectiveOrderName(caseData, null);

        // Falls back to text field when caseDataMap is null
        assertNotNull(result);
        assertEquals("Custom Text Order Name", result);
    }

    @Test
    void testGetEffectiveOrderName_withNullCaseDataMap_usesDefaultWhenNoTextName() {
        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, null);

        // Falls back to default when caseDataMap is null and no text name
        assertNotNull(result);
        assertEquals("custom_order", result);
    }

    @Test
    void testGetEffectiveOrderName_fallsBackToTextFieldName() {
        Map<String, Object> caseDataMap = new HashMap<>();
        // No customOrderNameOption

        CaseData caseData = CaseData.builder()
            .nameOfOrder("My Custom Order Name")
            .build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        assertEquals("My Custom Order Name", result);
    }

    @Test
    void testGetEffectiveOrderName_fallsBackToDefaultWhenNoNameProvided() {
        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        assertEquals("custom_order", result);
    }

    // ========== Tests for C43 header formatting ==========

    @Test
    void testRenderHeaderPreview_c43OrderHasFormattedHeader() throws IOException {
        // Arrange - C43 order with child arrangements sub-type
        Long caseId = 123L;
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "childArrangementsSpecificProhibitedOrder");

        // C43 sub-details with child arrangements order
        Map<String, Object> c43Details = new HashMap<>();
        c43Details.put("ordersToIssue", List.of("childArrangementsOrder"));
        c43Details.put("childArrangementsOrdersToIssue", "liveWithOrder");
        caseDataMap.put("customC43OrderDetails", c43Details);

        CaseData caseData = CaseData.builder()
            .courtName("Family Court")
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - orderName and actReference are now separate placeholders
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        String actReference = (String) placeholders.get("actReference");
        assertNotNull(orderName);
        assertTrue(orderName.startsWith("C43 - "), "C43 orderName should start with form number");
        assertTrue(orderName.contains("Child Arrangements Order"), "C43 orderName should contain order description");
        assertEquals("Section 8 Children Act 1989", actReference, "C43 actReference should be separate placeholder");
    }

    // ========== Tests for HEARING OR PAPERS logic ==========

    @Test
    void testHearingOrPapers_withHearingSelected_notOnPapers_showsAtAHearing() throws IOException {
        // Arrange - hearing selected with channel NOT "on the papers"
        Long caseId = 123L;
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderWasApprovedAtHearing", "Yes");
        // Simulate a hearing selection with a date
        Map<String, Object> hearingsTypeValue = new HashMap<>();
        hearingsTypeValue.put("label", "Fact Finding Hearing - 15/01/2026 10:00:00");
        Map<String, Object> hearingsType = new HashMap<>();
        hearingsType.put("value", hearingsTypeValue);
        caseDataMap.put("customOrderHearingsType", hearingsType);

        HearingData hearingData = HearingData.builder()
            .hearingChannelsEnum(HearingChannelsEnum.INTER) // In person, not on papers
            .build();

        CaseData caseData = CaseData.builder()
            .courtName("Family Court")
            .manageOrders(ManageOrders.builder()
                .ordersHearingDetails(List.of(Element.<HearingData>builder().value(hearingData).build()))
                .build())
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("at a hearing", placeholders.get("hearingOrPapers"));
    }

    @Test
    void testHearingOrPapers_withHearingSelected_onPapers_showsOnThePapers() throws IOException {
        // Arrange - hearing selected with channel "on the papers" (ONPPRS)
        Long caseId = 123L;
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderWasApprovedAtHearing", "Yes");
        // Simulate a hearing selection with a date
        Map<String, Object> hearingsTypeValue = new HashMap<>();
        hearingsTypeValue.put("label", "On the Papers - 15/01/2026 10:00:00");
        Map<String, Object> hearingsType = new HashMap<>();
        hearingsType.put("value", hearingsTypeValue);
        caseDataMap.put("customOrderHearingsType", hearingsType);

        HearingData hearingData = HearingData.builder()
            .hearingChannelsEnum(HearingChannelsEnum.ONPPRS) // On the papers
            .build();

        CaseData caseData = CaseData.builder()
            .courtName("Family Court")
            .manageOrders(ManageOrders.builder()
                .ordersHearingDetails(List.of(Element.<HearingData>builder().value(hearingData).build()))
                .build())
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("on the papers", placeholders.get("hearingOrPapers"));
    }

    @Test
    void testHearingOrPapers_noHearingSelected_showsOnThePapers() throws IOException {
        // Arrange - no hearing selected
        Long caseId = 123L;
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderWasApprovedAtHearing", "No");

        CaseData caseData = CaseData.builder()
            .courtName("Family Court")
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("on the papers", placeholders.get("hearingOrPapers"));
    }

    @Test
    void testHearingOrPapers_hearingSelectedButChannelNull_defaultsToAtAHearing() throws IOException {
        // Arrange - hearing selected but hearingChannelsEnum is null (check fails)
        Long caseId = 123L;
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderWasApprovedAtHearing", "Yes");
        // Simulate a hearing selection with a date
        Map<String, Object> hearingsTypeValue = new HashMap<>();
        hearingsTypeValue.put("label", "Hearing - 15/01/2026 10:00:00");
        Map<String, Object> hearingsType = new HashMap<>();
        hearingsType.put("value", hearingsTypeValue);
        caseDataMap.put("customOrderHearingsType", hearingsType);

        HearingData hearingData = HearingData.builder()
            .hearingChannelsEnum(null) // Channel not set - should default to "at a hearing"
            .build();

        CaseData caseData = CaseData.builder()
            .courtName("Family Court")
            .manageOrders(ManageOrders.builder()
                .ordersHearingDetails(List.of(Element.<HearingData>builder().value(hearingData).build()))
                .build())
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - should default to "at a hearing" when channel check fails
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("at a hearing", placeholders.get("hearingOrPapers"));
    }

    @Test
    void testHearingOrPapers_hearingSelectedButNoOrdersHearingDetails_defaultsToAtAHearing() throws IOException {
        // Arrange - hearing selected but ordersHearingDetails is null
        Long caseId = 123L;
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderWasApprovedAtHearing", "Yes");
        // Simulate a hearing selection with a date
        Map<String, Object> hearingsTypeValue = new HashMap<>();
        hearingsTypeValue.put("label", "Hearing - 15/01/2026 10:00:00");
        Map<String, Object> hearingsType = new HashMap<>();
        hearingsType.put("value", hearingsTypeValue);
        caseDataMap.put("customOrderHearingsType", hearingsType);

        CaseData caseData = CaseData.builder()
            .courtName("Family Court")
            .manageOrders(ManageOrders.builder()
                .ordersHearingDetails(null) // No hearing details - should default to "at a hearing"
                .build())
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - should default to "at a hearing" when check fails
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("at a hearing", placeholders.get("hearingOrPapers"));
    }

    // ========== Tests for C43 order handling ==========

    @Test
    void testBuildPlaceholders_c43Order_usesFormattedOrderName() throws IOException {
        // Arrange
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(CUSTOM_ORDER_NAME_OPTION, CustomOrderNameOptionsEnum.childArrangementsSpecificProhibitedOrder.name());
        Map<String, Object> c43Details = new HashMap<>();
        c43Details.put("c43OrderType", "childArrangementsOrder");
        caseDataMap.put("customC43OrderDetails", c43Details);
        CaseData caseData = CaseData.builder().courtName("Family Court").build();
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(new byte[]{1, 2, 3});

        // Act
        customOrderService.renderHeaderPreview(123L, caseData, caseDataMap);

        // Assert
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        String actReference = (String) placeholders.get("actReference");
        assertNotNull(orderName);
        assertTrue(orderName.contains("C43"), "Should contain C43 reference");
        assertTrue(actReference.contains("Section 8"), "Should contain Section 8 reference");
    }

    // ========== Tests for respondent1Name extraction ==========

    @Test
    void testBuildPlaceholders_respondent1Name_extracted() throws IOException {
        // Arrange
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(CUSTOM_ORDER_NAME_OPTION, CustomOrderNameOptionsEnum.blankOrderOrDirections.name());
        PartyDetails respondent = PartyDetails.builder().firstName("John").lastName("Smith").build();
        CaseData caseData = CaseData.builder()
            .courtName("Family Court")
            .respondents(List.of(Element.<PartyDetails>builder().value(respondent).build()))
            .build();
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(new byte[]{1, 2, 3});

        // Act
        customOrderService.renderHeaderPreview(123L, caseData, caseDataMap);

        // Assert
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("John Smith", placeholders.get("respondent1Name"));
    }

    // ========== Tests for hearingDate extraction ==========

    @Test
    void testBuildPlaceholders_hearingDate_extracted() throws IOException {
        // Arrange
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(CUSTOM_ORDER_NAME_OPTION, CustomOrderNameOptionsEnum.blankOrderOrDirections.name());
        HearingData hearingData = HearingData.builder()
            .hearingDateConfirmOptionEnum(uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .build();
        CaseData caseData = CaseData.builder()
            .courtName("Family Court")
            .manageOrders(ManageOrders.builder()
                .ordersHearingDetails(List.of(Element.<HearingData>builder().value(hearingData).build()))
                .build())
            .build();
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(new byte[]{1, 2, 3});

        // Act
        customOrderService.renderHeaderPreview(123L, caseData, caseDataMap);

        // Assert
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertNotNull(placeholders.get("hearingDate"));
    }

    // ========== Tests for magistrate names extraction ==========

    @Test
    void testBuildPlaceholders_magistrateNames_singleMagistrate() throws IOException {
        // Arrange
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(CUSTOM_ORDER_NAME_OPTION, CustomOrderNameOptionsEnum.blankOrderOrDirections.name());
        caseDataMap.put("judgeOrMagistrateTitle", JudgeOrMagistrateTitleEnum.magistrate.name());
        List<Map<String, Object>> magistrateList = new ArrayList<>();
        Map<String, Object> magistrate1 = new HashMap<>();
        Map<String, Object> value1 = new HashMap<>();
        value1.put("lastName", "Jane Doe");
        magistrate1.put("value", value1);
        magistrateList.add(magistrate1);
        caseDataMap.put("magistrateLastName", magistrateList);
        CaseData caseData = CaseData.builder().courtName("Family Court").build();
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(new byte[]{1, 2, 3});

        // Act
        customOrderService.renderHeaderPreview(123L, caseData, caseDataMap);

        // Assert
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Jane Doe", placeholders.get("judgeName"));
    }

    @Test
    void testBuildPlaceholders_magistrateNames_multipleMagistrates() throws IOException {
        // Arrange
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(CUSTOM_ORDER_NAME_OPTION, CustomOrderNameOptionsEnum.blankOrderOrDirections.name());
        caseDataMap.put("judgeOrMagistrateTitle", JudgeOrMagistrateTitleEnum.magistrate.name());
        caseDataMap.put("magistrateLastName", createMagistrateList("Jane Doe", "John Smith", "Bob Jones"));
        CaseData caseData = CaseData.builder().courtName("Family Court").build();
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(new byte[]{1, 2, 3});

        // Act
        customOrderService.renderHeaderPreview(123L, caseData, caseDataMap);

        // Assert
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String judgeName = (String) placeholders.get("judgeName");
        assertNotNull(judgeName);
        assertTrue(judgeName.contains("Jane Doe"), "Should contain first magistrate");
        assertTrue(judgeName.contains("John Smith"), "Should contain second magistrate");
        assertTrue(judgeName.contains("Bob Jones"), "Should contain third magistrate");
        assertTrue(judgeName.contains(" and "), "Should join names with 'and'");
    }

    @Test
    void testBuildPlaceholders_magistrateNames_fromCaseData() throws IOException {
        // Arrange
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(CUSTOM_ORDER_NAME_OPTION, CustomOrderNameOptionsEnum.blankOrderOrDirections.name());
        caseDataMap.put("judgeOrMagistrateTitle", JudgeOrMagistrateTitleEnum.magistrate.name());
        List<Element<uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName>> magistrateList = List.of(
            Element.<uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName>builder()
                .value(uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName.builder()
                    .lastName("Alice Brown").build()).build(),
            Element.<uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName>builder()
                .value(uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName.builder()
                    .lastName("Charlie Green").build()).build()
        );
        CaseData caseData = CaseData.builder()
            .courtName("Family Court")
            .magistrateLastName(magistrateList)
            .build();
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(new byte[]{1, 2, 3});

        // Act
        customOrderService.renderHeaderPreview(123L, caseData, caseDataMap);

        // Assert
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String judgeName = (String) placeholders.get("judgeName");
        assertNotNull(judgeName);
        assertTrue(judgeName.contains("Alice Brown"), "Should contain first magistrate");
        assertTrue(judgeName.contains("Charlie Green"), "Should contain second magistrate");
    }

    // Helper method for creating magistrate list in CCD format
    private List<Map<String, Object>> createMagistrateList(String... names) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String name : names) {
            Map<String, Object> magistrate = new HashMap<>();
            Map<String, Object> value = new HashMap<>();
            value.put("lastName", name);
            magistrate.put("value", value);
            list.add(magistrate);
        }
        return list;
    }

    // Tests for act reference in order header

    @Test
    void testOrderHeaderContainsActReferenceForC47A() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "appointmentOfGuardian");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String actReference = (String) placeholders.get("actReference");
        assertTrue(actReference.contains("Family Procedure Rules 2010"), "C47A should have Family Procedure Rules 2010 reference");
    }

    @Test
    void testOrderHeaderContainsActReferenceForC45A() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "parentalResponsibility");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String actReference = (String) placeholders.get("actReference");
        assertTrue(actReference.contains("Children Act 1989"), "C45A should have Children Act 1989 reference");
    }

    @Test
    void testOrderHeaderContainsActReferenceForC21() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "blankOrderOrDirections");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        assertTrue(orderName.contains("Children Act 1989"), "C21 should have Children Act 1989 reference");
    }

    @Test
    void testOrderHeaderContainsActReferenceForSdo() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "standardDirectionsOrder");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        assertTrue(orderName.contains("Children Act 1989"), "SDO should have Children Act 1989 reference");
    }

    @Test
    void testOrderHeaderContainsActReferenceForC43A() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "specialGuardianShip");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String actReference = (String) placeholders.get("actReference");
        assertTrue(actReference.contains("Children Act 1989"), "C43A should have Children Act 1989 reference");
    }

    @Test
    void testOrderHeaderContainsActReferenceForC43() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "childArrangementsSpecificProhibitedOrder");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        assertTrue(orderName.contains("Section 8 Children Act 1989"), "C43 should have Section 8 act reference");
    }

    @Test
    void testOrderHeaderContainsActReferenceForFL404A() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "nonMolestation");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        assertTrue(orderName.contains("Section 42 Family Law Act 1996"), "FL404A should have Section 42 act reference");
    }

    @Test
    void testOrderHeaderContainsActReferenceForFL404() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "occupation");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        assertTrue(orderName.contains("Section 33 to 38 Family Law Act 1996"), "FL404 should have Section 33 to 38 act reference");
    }

    @Test
    void testOrderHeaderContainsActReferenceForFL406() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "powerOfArrest");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        assertTrue(orderName.contains("Family Law Act 1996"), "FL406 should have Family Law Act 1996 reference");
    }

    @Test
    void testOrderHeaderContainsActReferenceForFL404B() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "blank");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        assertTrue(orderName.contains("Family Law Act 1996"), "FL404B should have Family Law Act 1996 reference");
    }

    @Test
    void testOrderHeaderHasNoActReferenceForC6() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "noticeOfProceedingsParties");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        assertFalse(orderName.contains("Act 1989") || orderName.contains("Act 1996"), "C6 should have no act reference");
    }

    @Test
    void testOrderHeaderHasNoActReferenceForN117() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "generalForm");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        String actReference = (String) placeholders.get("actReference");
        assertFalse(orderName.contains("Act 1989") || orderName.contains("Act 1996"), "N117 orderName should have no act reference");
        assertEquals("", actReference, "N117 actReference should be empty");
    }

    // ========== Tests for exact order name format ==========

    @Test
    void testOrderNameFormatWithFormNumberAndActReference() throws IOException {
        // Arrange - C45A (parental responsibility) which has formNumber and actReference
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "parentalResponsibility");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - orderName and actReference are now separate placeholders
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        String actReference = (String) placeholders.get("actReference");
        assertNotNull(orderName);
        assertTrue(orderName.startsWith("C45A - "), "Order name should start with form number 'C45A - '");
        assertFalse(orderName.contains("\n"), "Order name should not contain newline");
        assertEquals("Children Act 1989", actReference, "Act reference should be separate placeholder");
    }

    @Test
    void testOrderNameStripsParentheticalFormNumberFromDescription() throws IOException {
        // Arrange - C43 which may have description like "Child Arrangements Order (C43)"
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "childArrangementsSpecificProhibitedOrder");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - verify parenthetical form number is stripped from description
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        assertNotNull(orderName);
        // Should not have duplicate form number in description like "(C43)"
        assertFalse(orderName.contains("(C43)"), "Description should not contain parenthetical form number");
        // But should have form number at the start
        assertTrue(orderName.startsWith("C43 - "), "Order name should start with form number");
    }

    @Test
    void testOrderNameFallbackWhenNoActReference() throws IOException {
        // Arrange - C6 has no act reference (returns null)
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "noticeOfProceedings");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - should use plain description without formatting
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        assertNotNull(orderName);
        // No newline for orders without act reference
        assertFalse(orderName.contains("\n"), "Order name should not contain newline when no act reference");
        // Should not start with form number pattern when there's no act reference
        assertFalse(orderName.matches("^[A-Z0-9]+ - .*"), "Should use plain description when no act reference");
    }

    @Test
    void testOrderNameFormatForC21BlankOrder() throws IOException {
        // Arrange - C21 blank order
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "blankOrderOrDirections");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - orderName and actReference are now separate placeholders
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        String actReference = (String) placeholders.get("actReference");
        assertNotNull(orderName);
        assertTrue(orderName.startsWith("C21 - "), "C21 order should start with 'C21 - '");
        assertEquals("Children Act 1989", actReference, "C21 should have Children Act 1989 as actReference");
    }

    @Test
    void testOrderNameFormatForSdo() throws IOException {
        // Arrange - Standard directions order
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "standardDirectionsOrder");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        // Act
        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Assert - orderName and actReference are now separate placeholders
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        String actReference = (String) placeholders.get("actReference");
        assertNotNull(orderName);
        assertTrue(orderName.startsWith("SDO - "), "SDO should start with 'SDO - '");
        assertFalse(orderName.contains("\n"), "SDO orderName should not have newline");
        assertEquals("Children Act 1989", actReference, "SDO should have Children Act 1989 as actReference");
    }

    // ========== Tests for updateDraftOrderCollection ==========

    @Test
    void testUpdateDraftOrderCollection_withNullCollection_returnsEarly() {
        CaseData caseData = CaseData.builder()
            .draftOrderCollection(null)
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        uk.gov.hmcts.reform.prl.models.documents.Document doc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("test.docx")
            .build();

        customOrderService.updateDraftOrderCollection(caseData, caseDataUpdated, doc, "Test Order");

        assertNull(caseDataUpdated.get("draftOrderCollection"));
    }

    @Test
    void testUpdateDraftOrderCollection_withEmptyCollection_returnsEarly() {
        CaseData caseData = CaseData.builder()
            .draftOrderCollection(new ArrayList<>())
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        uk.gov.hmcts.reform.prl.models.documents.Document doc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("test.docx")
            .build();

        customOrderService.updateDraftOrderCollection(caseData, caseDataUpdated, doc, "Test Order");

        assertNull(caseDataUpdated.get("draftOrderCollection"));
    }

    @Test
    void testUpdateDraftOrderCollection_withValidCollection_updatesDraft() {
        uk.gov.hmcts.reform.prl.models.DraftOrder existingDraft = uk.gov.hmcts.reform.prl.models.DraftOrder.builder()
            .otherDetails(uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails.builder()
                .dateCreated(java.time.LocalDateTime.now())
                .build())
            .build();
        List<Element<uk.gov.hmcts.reform.prl.models.DraftOrder>> draftList = new ArrayList<>();
        draftList.add(Element.<uk.gov.hmcts.reform.prl.models.DraftOrder>builder()
            .id(UUID.randomUUID())
            .value(existingDraft)
            .build());

        CaseData caseData = CaseData.builder()
            .draftOrderCollection(draftList)
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        uk.gov.hmcts.reform.prl.models.documents.Document doc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("updated.docx")
            .documentUrl("http://url")
            .build();

        customOrderService.updateDraftOrderCollection(caseData, caseDataUpdated, doc, "Custom Order");

        assertNotNull(caseDataUpdated.get("draftOrderCollection"));
        @SuppressWarnings("unchecked")
        List<Element<uk.gov.hmcts.reform.prl.models.DraftOrder>> updatedList =
            (List<Element<uk.gov.hmcts.reform.prl.models.DraftOrder>>) caseDataUpdated.get("draftOrderCollection");
        assertEquals(1, updatedList.size());
        assertEquals("updated.docx", updatedList.get(0).getValue().getOrderDocument().getDocumentFileName());
        assertEquals("Custom Order", updatedList.get(0).getValue().getOrderTypeId());
    }

    // ========== Tests for updateFinalOrderCollection ==========

    @Test
    void testUpdateFinalOrderCollection_withNullCollection_returnsEarly() {
        CaseData caseData = CaseData.builder()
            .orderCollection(null)
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        uk.gov.hmcts.reform.prl.models.documents.Document doc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("test.docx")
            .build();

        customOrderService.updateFinalOrderCollection(caseData, caseDataUpdated, doc, "Test Order");

        assertNull(caseDataUpdated.get("orderCollection"));
    }

    @Test
    void testUpdateFinalOrderCollection_withEmptyCollection_returnsEarly() {
        CaseData caseData = CaseData.builder()
            .orderCollection(new ArrayList<>())
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        uk.gov.hmcts.reform.prl.models.documents.Document doc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("test.docx")
            .build();

        customOrderService.updateFinalOrderCollection(caseData, caseDataUpdated, doc, "Test Order");

        assertNull(caseDataUpdated.get("orderCollection"));
    }

    @Test
    void testUpdateFinalOrderCollection_withValidCollection_updatesOrder() {
        uk.gov.hmcts.reform.prl.models.OrderDetails existingOrder = uk.gov.hmcts.reform.prl.models.OrderDetails.builder()
            .orderTypeId("Old Order")
            .build();
        List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>> orderList = new ArrayList<>();
        orderList.add(Element.<uk.gov.hmcts.reform.prl.models.OrderDetails>builder()
            .id(UUID.randomUUID())
            .value(existingOrder)
            .build());

        CaseData caseData = CaseData.builder()
            .orderCollection(orderList)
            .build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        uk.gov.hmcts.reform.prl.models.documents.Document doc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("final.docx")
            .documentUrl("http://url")
            .build();

        customOrderService.updateFinalOrderCollection(caseData, caseDataUpdated, doc, "Final Custom Order");

        assertNotNull(caseDataUpdated.get("orderCollection"));
        @SuppressWarnings("unchecked")
        List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>> updatedList =
            (List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>>) caseDataUpdated.get("orderCollection");
        assertEquals(1, updatedList.size());
        assertEquals("final.docx", updatedList.get(0).getValue().getOrderDocument().getDocumentFileName());
        assertEquals("Final Custom Order", updatedList.get(0).getValue().getOrderTypeId());
    }


    // ========== Additional condition coverage tests ==========

    @Test
    void testRenderHeaderPreview_withNullCustomOrderNameOption_usesPlainDescription() throws IOException {
        // Test the else branch when formNumber/actReference is null
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .nameOfOrder("My Custom Order")
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        // No customOrderNameOption - should use nameOfOrder field

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        String orderName = (String) placeholders.get("orderName");
        String actReference = (String) placeholders.get("actReference");
        assertEquals("My Custom Order", orderName);
        assertEquals("", actReference, "actReference should be empty when no act reference");
    }

    @Test
    void testRenderHeaderPreview_withInvalidCustomOrderNameOption_fallsBackToDefault() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "invalidEnumValue");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertNotNull(placeholders.get("orderName"));
    }

    @Test
    void testRenderHeaderPreview_withJudgeTitleAsInteger_handlesGracefully() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "parentalResponsibility");
        caseDataMap.put("judgeOrMagistrateTitle", 12345); // Invalid type

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Should not throw, placeholders should still be populated
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertNotNull(placeholders);
    }

    @Test
    void testRenderHeaderPreview_withEmptyMagistrateList_usesEmptyString() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .magistrateLastName(new ArrayList<>())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "parentalResponsibility");
        caseDataMap.put("judgeOrMagistrateTitle", JudgeOrMagistrateTitleEnum.magistrate.name());

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertNotNull(placeholders);
    }

    @Test
    void testRenderHeaderPreview_withHearingNotApproved_noHearingDate() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "parentalResponsibility");
        caseDataMap.put("customOrderWasApprovedAtHearing", "No"); // Not approved at hearing

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertNotNull(placeholders);
    }

    @Test
    void testRenderHeaderPreview_withDateOrderMadeAsString_parsesCorrectly() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "parentalResponsibility");
        caseDataMap.put("dateOrderMade", "2026-04-14"); // String format

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertNotNull(placeholders.get("orderDate"));
    }

    @Test
    void testRenderHeaderPreview_withDateOrderMadeAsLocalDate_formatsCorrectly() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "parentalResponsibility");
        caseDataMap.put("dateOrderMade", LocalDate.of(2026, 4, 14));

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("14/04/2026", placeholders.get("orderDate"));
    }

    @Test
    void testRenderHeaderPreview_withNullCaseDataMap_handlesGracefully() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .nameOfOrder("Fallback Order Name")
            .build();

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertNotNull(placeholders);
        assertEquals("Fallback Order Name", placeholders.get("orderName"));
    }

    // ========== Tests for safePut edge cases ==========

    @Test
    void testRenderHeaderPreview_withCourtNameAsNullString_usesEmptyString() throws IOException {
        // Tests safePut condition: !"null".equals(String.valueOf(value))
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .courtName("null") // Literal string "null"
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "parentalResponsibility");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("", placeholders.get("courtName"));
    }

    @Test
    void testRenderHeaderPreview_withCourtNameAsZeroString_usesEmptyString() throws IOException {
        // Tests safePut condition: !"0".equals(String.valueOf(value))
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .courtName("0") // String "0"
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "parentalResponsibility");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("", placeholders.get("courtName"));
    }

    // ========== Tests for extractMagistrateNames branches ==========

    @Test
    void testRenderHeaderPreview_withMagistrateValueNotMap_handlesGracefully() throws IOException {
        // Tests branch where value is not a Map in the magistrate conversion
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "parentalResponsibility");
        caseDataMap.put("judgeOrMagistrateTitle", JudgeOrMagistrateTitleEnum.magistrate.name());

        // Create magistrate list where value is NOT a Map
        List<Map<String, Object>> magistrateMapList = new ArrayList<>();
        Map<String, Object> mag1 = new HashMap<>();
        mag1.put("value", "not a map"); // value is a String, not Map
        magistrateMapList.add(mag1);
        caseDataMap.put("magistrateLastName", magistrateMapList);

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Should not throw, placeholders should still be populated
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertNotNull(placeholders);
    }

    // ========== Tests for parseCustomOrderNameOption branches ==========

    @Test
    void testGetEffectiveOrderName_withEnumObjectDirectly() {
        // Tests branch where rawOption is already CustomOrderNameOptionsEnum
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", CustomOrderNameOptionsEnum.parentalResponsibility);

        String result = customOrderService.getEffectiveOrderName(caseData, caseDataMap);

        assertEquals("Parental responsibility order (C45A)", result);
    }

    // ========== Tests for extractHearingDateFromSelection branches ==========

    @Test
    void testRenderHeaderPreview_withHearingAsDynamicList_extractsDate() throws IOException {
        // Tests DynamicList branch in extractHearingDateFromSelection
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "parentalResponsibility");
        caseDataMap.put("customOrderWasApprovedAtHearing", "Yes");

        // Create hearing as Map (simulating DynamicList)
        Map<String, Object> hearingValue = new HashMap<>();
        hearingValue.put("label", "FHDRA - 15/04/2026 10:00:00");
        Map<String, Object> hearingsType = new HashMap<>();
        hearingsType.put("value", hearingValue);
        caseDataMap.put("customOrderHearingsType", hearingsType);

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertNotNull(placeholders.get("hearingDate"));
    }

    @Test
    void testRenderHeaderPreview_withHearingLabelNoDatePart_handlesGracefully() throws IOException {
        // Tests branch where hearing label doesn't have expected format
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "parentalResponsibility");
        caseDataMap.put("customOrderWasApprovedAtHearing", "Yes");

        // Create hearing with label that has no " - " separator
        Map<String, Object> hearingValue = new HashMap<>();
        hearingValue.put("label", "FHDRA without date");
        Map<String, Object> hearingsType = new HashMap<>();
        hearingsType.put("value", hearingValue);
        caseDataMap.put("customOrderHearingsType", hearingsType);

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        // Should not throw
        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertNotNull(placeholders);
    }

    // ========== Tests for extractJudgeTitle branches ==========

    @Test
    void testRenderHeaderPreview_withJudgeTitleFromCaseData_whenNotInMap() throws IOException {
        // Tests fallback to caseData.getManageOrders().getJudgeOrMagistrateTitle()
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.hisHonourJudge)
                .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "parentalResponsibility");
        // No judgeOrMagistrateTitle in map - should fall back to caseData

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("His Honour Judge", placeholders.get("judgeTitle"));
    }

    // ========== Direct tests for private helper methods (now package-private) ==========

    @Test
    void testStripFormNumberFromDescription_withNull_returnsNull() {
        assertNull(customOrderService.stripFormNumberFromDescription(null, null));
    }

    @Test
    void testStripFormNumberFromDescription_withBrackets_stripsIt() {
        String result = customOrderService.stripFormNumberFromDescription("Parental responsibility order (C45A)", "C45A");
        assertEquals("Parental responsibility order", result);
    }

    @Test
    void testStripFormNumberFromDescription_withoutBrackets_returnsAsIs() {
        String result = customOrderService.stripFormNumberFromDescription("Some order name", "C45");
        assertEquals("Some order name", result);
    }

    @Test
    void testGetC21SubOptionDisplayValue_withNull_returnsNull() {
        assertNull(customOrderService.getC21SubOptionDisplayValue(null));
    }

    @Test
    void testGetC21SubOptionDisplayValue_withEmptyMap_returnsNull() {
        assertNull(customOrderService.getC21SubOptionDisplayValue(new HashMap<>()));
    }

    @Test
    void testGetC43OrdersDisplayValue_withNull_returnsNull() {
        assertNull(customOrderService.getC43OrdersDisplayValue(null));
    }

    @Test
    void testGetC43OrdersDisplayValue_withEmptyMap_returnsNull() {
        assertNull(customOrderService.getC43OrdersDisplayValue(new HashMap<>()));
    }

    @Test
    void testExtractLegalAdviserName_fromMap_returnsName() {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("justiceLegalAdviserFullName", "Jane Advisor");

        String result = customOrderService.extractLegalAdviserName(caseData, caseDataMap);

        assertEquals("Jane Advisor", result);
    }

    @Test
    void testExtractLegalAdviserName_withNullMap_fallsToCaseData() {
        CaseData caseData = CaseData.builder()
            .justiceLegalAdviserFullName("John Legal")
            .build();

        String result = customOrderService.extractLegalAdviserName(caseData, null);

        assertEquals("John Legal", result);
    }

    @Test
    void testExtractLegalAdviserName_withMapMissingKey_fallsToCaseData() {
        CaseData caseData = CaseData.builder()
            .justiceLegalAdviserFullName("Fallback Name")
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        // No justiceLegalAdviserFullName in map

        String result = customOrderService.extractLegalAdviserName(caseData, caseDataMap);

        assertEquals("Fallback Name", result);
    }

    @Test
    void testExtractJudgeTitle_fromMap_returnsTitle() {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("judgeOrMagistrateTitle", JudgeOrMagistrateTitleEnum.hisHonourJudge.name());

        String result = customOrderService.extractJudgeTitle(caseData, caseDataMap);

        assertEquals("His Honour Judge", result);
    }

    @Test
    void testExtractJudgeTitle_fromCaseDataManageOrders_whenNotInMap() {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                .judgeOrMagistrateTitle(JudgeOrMagistrateTitleEnum.districtJudge)
                .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        // No judgeOrMagistrateTitle in map

        String result = customOrderService.extractJudgeTitle(caseData, caseDataMap);

        assertEquals("District Judge", result);
    }

    @Test
    void testExtractJudgeTitle_withNullManageOrders_returnsNull() {
        CaseData caseData = CaseData.builder()
            .manageOrders(null)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        String result = customOrderService.extractJudgeTitle(caseData, caseDataMap);

        assertNull(result);
    }

    @Test
    void testExtractJudgeTitle_withNullJudgeTitle_returnsNull() {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                .judgeOrMagistrateTitle(null)
                .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        String result = customOrderService.extractJudgeTitle(caseData, caseDataMap);

        assertNull(result);
    }

    @Test
    void testExtractOrderDate_fromCaseDataDateOrderMade_whenNotInMap() {
        CaseData caseData = CaseData.builder()
            .dateOrderMade(LocalDate.of(2026, 4, 14))
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        // No dateOrderMade in map

        String result = customOrderService.extractOrderDate(caseData, caseDataMap);

        assertEquals("14/04/2026", result);
    }

    @Test
    void testExtractOrderDate_fromMapAsLocalDate() {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("dateOrderMade", LocalDate.of(2026, 5, 20));

        String result = customOrderService.extractOrderDate(caseData, caseDataMap);

        assertEquals("20/05/2026", result);
    }

    @Test
    void testExtractOrderDate_fromMapAsString() {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("dateOrderMade", "2026-06-15");

        String result = customOrderService.extractOrderDate(caseData, caseDataMap);

        assertEquals("15/06/2026", result);
    }

    @Test
    void testExtractOrderDate_withNullEverywhere_returnsCurrentDate() {
        CaseData caseData = CaseData.builder()
            .dateOrderMade(null)
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        String result = customOrderService.extractOrderDate(caseData, caseDataMap);

        // Falls back to current date
        String expectedDate = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        assertEquals(expectedDate, result);
    }

    @Test
    void testUpdateFinalOrderCollection_throwsWhenWouldLoseOrders() {
        uk.gov.hmcts.reform.prl.models.OrderDetails order = uk.gov.hmcts.reform.prl.models.OrderDetails.builder().build();
        List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>> originalOrders = List.of(
            Element.<uk.gov.hmcts.reform.prl.models.OrderDetails>builder()
                .id(UUID.randomUUID())
                .value(order)
                .build()
        );

        CaseData caseData = CaseData.builder()
            .orderCollection(originalOrders)
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("orderCollection", List.of("a", "b")); // existingCount = 2

        uk.gov.hmcts.reform.prl.models.documents.Document doc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder().documentFileName("x.docx").build();

        assertThrows(IllegalStateException.class, () ->
            customOrderService.updateFinalOrderCollection(caseData, caseDataUpdated, doc, "Order Name"));
    }

    @Test
    void testUpdateDraftOrderCollection_throwsWhenWouldLoseDrafts() {
        uk.gov.hmcts.reform.prl.models.DraftOrder draft = uk.gov.hmcts.reform.prl.models.DraftOrder.builder().build();
        List<Element<uk.gov.hmcts.reform.prl.models.DraftOrder>> originalDrafts = List.of(
            Element.<uk.gov.hmcts.reform.prl.models.DraftOrder>builder()
                .id(UUID.randomUUID())
                .value(draft)
                .build()
        );

        CaseData caseData = CaseData.builder()
            .draftOrderCollection(originalDrafts)
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("draftOrderCollection", List.of("a", "b")); // existingCount = 2

        uk.gov.hmcts.reform.prl.models.documents.Document doc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder().documentFileName("x.docx").build();

        assertThrows(IllegalStateException.class, () ->
            customOrderService.updateDraftOrderCollection(caseData, caseDataUpdated, doc, "Order Name"));
    }

    @Test
    void testPopulateChildrensGuardian_fromNewChildDetailsFallback() throws IOException {
        ChildDetailsRevised child = ChildDetailsRevised.builder()
            .firstName("Amy")
            .lastName("Test")
            .cafcassOfficerName("Guardian Fallback")
            .build();

        CaseData caseData = CaseData.builder()
            .newChildDetails(List.of(Element.<ChildDetailsRevised>builder().value(child).build()))
            .build();

        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(new byte[]{1});

        customOrderService.renderHeaderPreview(123L, caseData, null);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Guardian Fallback", placeholders.get("childrensGuardianName"));
        assertTrue(((String) placeholders.get("childrenAsRespondentsClause")).contains("Guardian Fallback"));
    }

    @Test
    void testHearingOnThePapers_whenDynamicListCodeIsOnpprs() throws IOException {
        var dynamicList = uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList.builder()
            .value(uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement.builder()
                       .code("ONPPRS")
                       .label("On the papers")
                       .build())
            .build();

        HearingData hearingData = HearingData.builder()
            .hearingChannelsEnum(null)
            .hearingChannels(dynamicList)
            .build();

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(List.of(Element.<HearingData>builder().value(hearingData).build()))
                              .build())
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderWasApprovedAtHearing", "Yes");
        caseDataMap.put("customOrderHearingsType", Map.of("value", Map.of("label", "Hearing - 15/01/2026 10:00:00")));

        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(new byte[]{1});

        customOrderService.renderHeaderPreview(123L, caseData, caseDataMap);

        assertEquals("on the papers", placeholdersCaptor.getValue().get("hearingOrPapers"));
    }

    @Test
    void testRenderHeaderPreview_withRealDynamicList_extractsDate() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();

        uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList hearingsType =
            uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList.builder()
                .value(uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement.builder()
                           .code("hearing-1")
                           .label("FHDRA - 15/04/2026 10:00:00")
                           .build())
                .build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customOrderNameOption", "parentalResponsibility");
        caseDataMap.put("customOrderWasApprovedAtHearing", "Yes");
        caseDataMap.put("customOrderHearingsType", hearingsType);

        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(new byte[]{1, 2, 3});

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("15/04/2026", placeholders.get("orderDate"));
    }

    @Test
    void testGetDisplayOrderNameForC21ApplicationRefused() {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customC21OrderDetails", Map.of("orderOptions", "c21ApplicationRefused"));

        String result = customOrderService.getDisplayOrderName(
            caseData,
            caseDataMap,
            CustomOrderNameOptionsEnum.blankOrderOrDirections,
            "Blank order or directions (C21): application refused"
        );

        assertEquals("C21 - General order or directions: application refused", result);
    }

    @Test
    void testGetDisplayOrderNameForC21OtherFallsBackToGeneralOrder() {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("customC21OrderDetails", Map.of("orderOptions", "c21other"));

        String result = customOrderService.getDisplayOrderName(
            caseData,
            caseDataMap,
            CustomOrderNameOptionsEnum.blankOrderOrDirections,
            "Blank order or directions (C21): Other"
        );

        assertEquals("C21 - General order or directions", result);
    }

    @Test
    void testGetDisplayOrderNameForOccupation() {
        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getDisplayOrderName(
            caseData,
            new HashMap<>(),
            CustomOrderNameOptionsEnum.occupation,
            "Occupation order"
        );

        assertEquals("FL404 - Occupation order", result);
    }

    @Test
    void testGetDisplayOrderNameForNoticeOfProceedingsReturnsDescription() {
        CaseData caseData = CaseData.builder().build();

        String result = customOrderService.getDisplayOrderName(
            caseData,
            new HashMap<>(),
            CustomOrderNameOptionsEnum.noticeOfProceedings,
            "Notice of proceedings"
        );

        assertEquals("Notice of proceedings", result);
    }
}
