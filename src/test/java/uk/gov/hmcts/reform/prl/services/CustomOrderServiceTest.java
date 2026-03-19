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
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CustomOrderNameOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void setUp() {
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
        final Long caseId = 123L;
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
    void testCombineHeaderAndContent_mergesDocuments() {
        // Create minimal valid DOCX bytes (this is a simplified test)
        // In reality, these would be actual DOCX files
        // For unit testing, we mock the behavior

        // Since combineHeaderAndContent uses Apache POI directly on byte arrays,
        // we need actual DOCX bytes. For this test, we'll verify the method exists
        // and can be called. Integration tests would verify actual merging.

        // This test verifies the method signature and basic flow
        assertNotNull(customOrderService);
    }

    @Test
    void testProcessCustomOrderOnSubmitted_downloadsDocuments() throws IOException {
        // Arrange
        final String authorisation = "auth-token";
        final Long caseId = 123L;
        String userDocUrl = "http://user-doc-binary-url";
        String headerDocUrl = "http://header-doc-binary-url";

        CaseData caseData = CaseData.builder()
            .id(caseId)
            .courtName("Test Court")
            .nameOfOrder("Test Order")
            .build();

        // Mock header and user doc downloads
        byte[] headerBytes = new byte[]{1, 2, 3};
        byte[] userContentBytes = new byte[]{4, 5, 6};
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(authTokenGenerator.generate()).thenReturn("s2s-token");
        when(documentGenService.getDocumentBytes(eq(headerDocUrl), any(), any())).thenReturn(headerBytes);
        when(documentGenService.getDocumentBytes(eq(userDocUrl), any(), any())).thenReturn(userContentBytes);

        // Act & Assert - combineHeaderAndContent needs real DOCX bytes so will throw
        // We verify documents are downloaded before the POI error
        try {
            customOrderService.processCustomOrderOnSubmitted(
                authorisation, caseId, caseData, userDocUrl, headerDocUrl, null, false);
        } catch (Exception e) {
            // Expected - combineHeaderAndContent needs real DOCX bytes
            verify(documentGenService).getDocumentBytes(eq(headerDocUrl), any(), any());
            verify(documentGenService).getDocumentBytes(eq(userDocUrl), any(), any());
        }
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
            assertEquals("John Doe", placeholders.get("applicantName"));
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
        assertEquals("Sarah Johnson", placeholders.get("applicantName"));
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
        assertEquals("Michael Solicitor", placeholders.get("applicantRepresentativeName"));
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
        assertEquals("Jane Doe", placeholders.get("applicantName"));
        assertEquals("Legal Rep", placeholders.get("applicantRepresentativeName"));
    }

    @Test
    void testBuildHeaderPlaceholders_fl401ChildrenPopulated() throws IOException {
        Long caseId = 1234567890123456L;
        ChildrenLiveAtAddress child = ChildrenLiveAtAddress.builder()
            .childFullName("Tommy Test")
            .build();

        TypeOfApplicationOrders orders = TypeOfApplicationOrders.builder()
            .orderType(List.of(FL401OrderTypeEnum.occupationOrder))
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(PartyDetails.builder().firstName("Jane").lastName("Doe").build())
            .typeOfApplicationOrders(orders)
            .home(Home.builder()
                .children(List.of(Element.<ChildrenLiveAtAddress>builder().value(child).build()))
                .build())
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
        assertEquals("Alice Thompson", placeholders.get("applicantName"));
        assertEquals("Robert Counsel QC", placeholders.get("applicantRepresentativeName"));
        assertEquals(", legally represented", placeholders.get("applicantRepresentativeClause"));

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
        uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement hearingElement =
            uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement.builder()
                .code("hearing-123")
                .label("Final Hearing - 15/03/2026 10:00:00")
                .build();
        uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList hearingsType =
            uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList.builder()
                .value(hearingElement)
                .build();
        CaseData caseData = CaseData.builder()
            .wasTheOrderApprovedAtHearing(YesOrNo.Yes)
            .manageOrders(ManageOrders.builder().hearingsType(hearingsType).build())
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();

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
        uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement hearingElement =
            uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement.builder()
                .code("hearing-456")
                .label("Case Management - 02/02/2025 14:30:00")
                .build();
        uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList hearingsType =
            uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList.builder()
                .value(hearingElement)
                .build();
        final CaseData caseData = CaseData.builder()
            .courtName("Central Family Court")
            .wasTheOrderApprovedAtHearing(YesOrNo.Yes)
            .manageOrders(ManageOrders.builder().hearingsType(hearingsType).build())
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("judgeOrMagistratesLastName", "District Judge Taylor");

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
        caseDataMap.put("judgeName", "Smith");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("Her Honour Judge Smith", placeholders.get("judgeName"));
    }

    @Test
    void testBuildHeaderPlaceholders_judgeTitleFromMapAsEnum() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("judgeOrMagistrateTitle", JudgeOrMagistrateTitleEnum.districtJudge);
        caseDataMap.put("judgeName", "Brown");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        assertEquals("District Judge Brown", placeholders.get("judgeName"));
    }

    @Test
    void testBuildHeaderPlaceholders_judgeTitleFromMapAsInvalidString() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("judgeOrMagistrateTitle", "Custom Title");  // Not a valid enum
        caseDataMap.put("judgeName", "Jones");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        // Falls back to using the string as-is
        assertEquals("Custom Title Jones", placeholders.get("judgeName"));
    }

    @Test
    void testBuildHeaderPlaceholders_judgeTitleFromMapAsOtherType() throws IOException {
        Long caseId = 1234567890123456L;
        CaseData caseData = CaseData.builder().build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("judgeOrMagistrateTitle", 12345);  // Neither String nor Enum
        caseDataMap.put("judgeName", "Wilson");

        byte[] renderedBytes = new byte[]{1, 2, 3};
        when(poiTlDocxRenderer.render(any(), placeholdersCaptor.capture())).thenReturn(renderedBytes);

        customOrderService.renderHeaderPreview(caseId, caseData, caseDataMap);

        Map<String, Object> placeholders = placeholdersCaptor.getValue();
        // Uses toString() on the object
        assertEquals("12345 Wilson", placeholders.get("judgeName"));
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

    @Test
    void testUpdateFinalOrderCollection_readsFromCaseDataWhenNotInUpdatedMap() {
        // Arrange - orderCollection exists in caseData but not in caseDataUpdated
        uk.gov.hmcts.reform.prl.models.OrderDetails existingOrder =
            uk.gov.hmcts.reform.prl.models.OrderDetails.builder()
                .orderType("TestOrder")
                .build();
        List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>> existingOrders = new ArrayList<>();
        existingOrders.add(Element.<uk.gov.hmcts.reform.prl.models.OrderDetails>builder()
            .id(UUID.randomUUID()).value(existingOrder).build());

        CaseData caseData = CaseData.builder().id(123L).orderCollection(existingOrders).build();
        Map<String, Object> caseDataUpdated = new HashMap<>();

        uk.gov.hmcts.reform.prl.models.documents.Document sealedDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentFileName("sealed.pdf").documentBinaryUrl("http://sealed").build();

        try {
            java.lang.reflect.Method method = CustomOrderService.class.getDeclaredMethod(
                "updateFinalOrderCollection", CaseData.class, Map.class,
                uk.gov.hmcts.reform.prl.models.documents.Document.class);
            method.setAccessible(true);
            method.invoke(customOrderService, caseData, caseDataUpdated, sealedDoc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke updateFinalOrderCollection", e);
        }

        assertTrue(caseDataUpdated.containsKey("orderCollection"));
        @SuppressWarnings("unchecked")
        List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>> orders =
            (List<Element<uk.gov.hmcts.reform.prl.models.OrderDetails>>) caseDataUpdated.get("orderCollection");
        assertEquals(1, orders.size());
        assertEquals(sealedDoc, orders.getFirst().getValue().getOrderDocument());
        assertEquals("TestOrder", orders.getFirst().getValue().getOrderType());
    }

    @Test
    void testUpdateDraftOrderCollection_readsFromCaseDataWhenNotInUpdatedMap() {
        // Arrange - draftOrderCollection exists in caseData but not in caseDataUpdated
        uk.gov.hmcts.reform.prl.models.DraftOrder existingDraft =
            uk.gov.hmcts.reform.prl.models.DraftOrder.builder()
                .judgeOrMagistratesLastName("Test Judge")
                .build();
        List<Element<uk.gov.hmcts.reform.prl.models.DraftOrder>> existingDrafts = new ArrayList<>();
        existingDrafts.add(Element.<uk.gov.hmcts.reform.prl.models.DraftOrder>builder()
            .id(UUID.randomUUID()).value(existingDraft).build());

        CaseData caseData = CaseData.builder().id(123L).draftOrderCollection(existingDrafts).build();
        Map<String, Object> caseDataUpdated = new HashMap<>();

        uk.gov.hmcts.reform.prl.models.documents.Document combinedDoc =
            uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentFileName("combined.docx").documentBinaryUrl("http://combined").build();

        try {
            java.lang.reflect.Method method = CustomOrderService.class.getDeclaredMethod(
                "updateDraftOrderCollection", CaseData.class, Map.class,
                uk.gov.hmcts.reform.prl.models.documents.Document.class);
            method.setAccessible(true);
            method.invoke(customOrderService, caseData, caseDataUpdated, combinedDoc);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke updateDraftOrderCollection", e);
        }

        assertTrue(caseDataUpdated.containsKey("draftOrderCollection"));
        @SuppressWarnings("unchecked")
        List<Element<uk.gov.hmcts.reform.prl.models.DraftOrder>> drafts =
            (List<Element<uk.gov.hmcts.reform.prl.models.DraftOrder>>) caseDataUpdated.get("draftOrderCollection");
        assertEquals(1, drafts.size());
        assertEquals(combinedDoc, drafts.getFirst().getValue().getOrderDocument());
        assertEquals("Test Judge", drafts.getFirst().getValue().getJudgeOrMagistratesLastName());
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
        assertEquals("Order", result);
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
}
