package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassUploadDocService.DOC_TYPE_CIR_EXTENSION;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassUploadDocService.DOC_TYPE_CIR_TRANSFER;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassUploadDocService.DOC_TYPE_S16A_RISK_ASSESSMENT;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassUploadDocService.MANAGE_DOC_UPLOADED_CATEGORY;
import static uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService.MANAGE_DOCUMENTS_TRIGGERED_BY;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CafcassUploadDocServiceTest {

    private final String authToken = "Bearer abc";
    private final String s2sToken = "s2s token";
    private static final String randomAlphaNumeric = "Abc123EFGH";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private AllTabServiceImpl allTabService;

    @Mock
    private ManageDocumentsService manageDocumentsService;

    @InjectMocks
    private CafcassUploadDocService cafcassUploadDocService;

    private CaseData caseData;
    private MultipartFile file;

    @BeforeEach
    void setup() {
        caseData = CaseData.builder().id(Long.parseLong(TEST_CASE_ID)).applicantCaseName("xyz").build();
        file = new MockMultipartFile(
            "file",
            "private-law.pdf",
            MediaType.TEXT_PLAIN_VALUE,
            "Some case data".getBytes()
        );
    }

    @Test
    void shouldUploadDocumentWhenAllFieldsAreCorrect() {
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        Document document = testDocument();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        CaseDetails caseDetails = CaseDetails.builder().id(Long.parseLong(TEST_CASE_ID)).build();
        StartAllTabsUpdateDataContent updateData = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(new ObjectMapper()),
            caseData,
            null
        );

        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(updateData);

        cafcassUploadDocService.uploadDocument(authToken, file, "16_4_Report", TEST_CASE_ID);

        verify(allTabService, times(1)).submitAllTabsUpdate(any(), any(), any(), any(), any());
    }

    @Test
    void shouldUploadDocumentWhenAllFieldsAreCorrectForPathFinderCases() {
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        Document document = testDocument();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        CaseDetails caseDetails = CaseDetails.builder().id(Long.parseLong(TEST_CASE_ID)).build();

        caseData = caseData.toBuilder().isPathfinderCase(YesOrNo.Yes).build();
        StartAllTabsUpdateDataContent updateData = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(new ObjectMapper()),
            caseData,
            null
        );

        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(updateData);

        cafcassUploadDocService.uploadDocument(authToken, file, "16_4_Report", TEST_CASE_ID);

        verify(allTabService, times(1)).submitAllTabsUpdate(any(), any(), any(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenInvalidTypeOfDocumentIsPassed() {
        MultipartFile invalidFile = new MockMultipartFile(
            "file",
            "private-law.json",
            MediaType.TEXT_PLAIN_VALUE,
            "FL401 case".getBytes()
        );

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            cafcassUploadDocService.uploadDocument(authToken, invalidFile, "FL401", TEST_CASE_ID)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldNotUploadDocumentWhenInvalidCaseId() {
        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(null);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            cafcassUploadDocService.uploadDocument(authToken, file, "CIR_Part1", TEST_CASE_ID)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldThrowExceptionWhenFileIsNull() {
        assertThrows(ResponseStatusException.class, () ->
            cafcassUploadDocService.uploadDocument(authToken, null, "16_4_Report", TEST_CASE_ID)
        );
    }

    @Test
    void shouldThrowExceptionWhenFilenameIsNull() {
        MultipartFile fileWithNoName = new MockMultipartFile(
            "file",
            null,
            MediaType.APPLICATION_PDF_VALUE,
            "dummy content".getBytes()
        );
        assertThrows(ResponseStatusException.class, () ->
            cafcassUploadDocService.uploadDocument(authToken, fileWithNoName, "16_4_Report", TEST_CASE_ID)
        );
    }

    @Test
    void shouldAcceptCirTransferDocumentType() {
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        Document document = testDocument();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        CaseDetails caseDetails = CaseDetails.builder().id(Long.parseLong(TEST_CASE_ID)).build();
        StartAllTabsUpdateDataContent updateData = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(new ObjectMapper()),
            caseData,
            null
        );

        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(updateData);

        cafcassUploadDocService.uploadDocument(authToken, file, DOC_TYPE_CIR_TRANSFER, TEST_CASE_ID);

        verify(allTabService, times(1)).submitAllTabsUpdate(any(), any(), any(), any(), any());
    }

    @Test
    void shouldAcceptCirExtensionDocumentType() {
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        Document document = testDocument();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        CaseDetails caseDetails = CaseDetails.builder().id(Long.parseLong(TEST_CASE_ID)).build();
        StartAllTabsUpdateDataContent updateData = new StartAllTabsUpdateDataContent(
            authToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(new ObjectMapper()),
            caseData,
            null
        );

        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(updateData);

        cafcassUploadDocService.uploadDocument(authToken, file, DOC_TYPE_CIR_EXTENSION, TEST_CASE_ID);

        verify(allTabService, times(1)).submitAllTabsUpdate(any(), any(), any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSetUrgentDocTypeForCirTransfer() {
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        UploadResponse uploadResponse = new UploadResponse(List.of(testDocument()));
        CaseDetails caseDetails = CaseDetails.builder().id(Long.parseLong(TEST_CASE_ID)).build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent updateData = new StartAllTabsUpdateDataContent(
            authToken, EventRequestData.builder().build(), StartEventResponse.builder().build(),
            caseDataMap, caseData, null
        );

        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(updateData);

        cafcassUploadDocService.uploadDocument(authToken, file, DOC_TYPE_CIR_TRANSFER, TEST_CASE_ID);

        assertEquals(List.of(DOC_TYPE_CIR_TRANSFER), caseDataMap.get(MANAGE_DOC_UPLOADED_CATEGORY));
        assertEquals("CAFCASS", caseDataMap.get(MANAGE_DOCUMENTS_TRIGGERED_BY));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSetUrgentDocTypeForCirExtension() {
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        UploadResponse uploadResponse = new UploadResponse(List.of(testDocument()));
        CaseDetails caseDetails = CaseDetails.builder().id(Long.parseLong(TEST_CASE_ID)).build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent updateData = new StartAllTabsUpdateDataContent(
            authToken, EventRequestData.builder().build(), StartEventResponse.builder().build(),
            caseDataMap, caseData, null
        );

        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(updateData);

        cafcassUploadDocService.uploadDocument(authToken, file, DOC_TYPE_CIR_EXTENSION, TEST_CASE_ID);

        assertEquals(List.of(DOC_TYPE_CIR_EXTENSION), caseDataMap.get(MANAGE_DOC_UPLOADED_CATEGORY));
        assertEquals("CAFCASS", caseDataMap.get(MANAGE_DOCUMENTS_TRIGGERED_BY));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSetUrgentDocTypeFor16aRiskAssessment() {
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        UploadResponse uploadResponse = new UploadResponse(List.of(testDocument()));
        CaseDetails caseDetails = CaseDetails.builder().id(Long.parseLong(TEST_CASE_ID)).build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent updateData = new StartAllTabsUpdateDataContent(
            authToken, EventRequestData.builder().build(), StartEventResponse.builder().build(),
            caseDataMap, caseData, null
        );

        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(updateData);

        cafcassUploadDocService.uploadDocument(authToken, file, DOC_TYPE_S16A_RISK_ASSESSMENT, TEST_CASE_ID);

        assertEquals(List.of("16aRiskAssessment"), caseDataMap.get(MANAGE_DOC_UPLOADED_CATEGORY));
        assertEquals("CAFCASS", caseDataMap.get(MANAGE_DOCUMENTS_TRIGGERED_BY));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotCreateDuplicateTaskWhenSameUrgentDocTypeAlreadyInQuarantine() {
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        UploadResponse uploadResponse = new UploadResponse(List.of(testDocument()));
        CaseDetails caseDetails = CaseDetails.builder().id(Long.parseLong(TEST_CASE_ID)).build();

        Element<QuarantineLegalDoc> existingDoc = element(QuarantineLegalDoc.builder()
            .categoryId("cirTransferRequest")
            .build());
        CaseData caseDataWithExistingQuarantineDoc = caseData.toBuilder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                .cafcassQuarantineDocsList(List.of(existingDoc))
                .build())
            .build();

        Map<String, Object> caseDataMap = caseDataWithExistingQuarantineDoc.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent updateData = new StartAllTabsUpdateDataContent(
            authToken, EventRequestData.builder().build(), StartEventResponse.builder().build(),
            caseDataMap, caseDataWithExistingQuarantineDoc, null
        );

        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(updateData);

        cafcassUploadDocService.uploadDocument(authToken, file, DOC_TYPE_CIR_TRANSFER, TEST_CASE_ID);

        assertNull(caseDataMap.get(MANAGE_DOC_UPLOADED_CATEGORY));
        assertNull(caseDataMap.get(MANAGE_DOCUMENTS_TRIGGERED_BY));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotCreateDuplicateTaskWhenSameCirExtensionTypeAlreadyInQuarantine() {
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        UploadResponse uploadResponse = new UploadResponse(List.of(testDocument()));
        CaseDetails caseDetails = CaseDetails.builder().id(Long.parseLong(TEST_CASE_ID)).build();

        Element<QuarantineLegalDoc> existingDoc = element(QuarantineLegalDoc.builder()
            .categoryId("cirExtensionRequest")
            .build());
        CaseData caseDataWithExistingQuarantineDoc = caseData.toBuilder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                .cafcassQuarantineDocsList(List.of(existingDoc))
                .build())
            .build();

        Map<String, Object> caseDataMap = caseDataWithExistingQuarantineDoc.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent updateData = new StartAllTabsUpdateDataContent(
            authToken, EventRequestData.builder().build(), StartEventResponse.builder().build(),
            caseDataMap, caseDataWithExistingQuarantineDoc, null
        );

        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(updateData);

        cafcassUploadDocService.uploadDocument(authToken, file, DOC_TYPE_CIR_EXTENSION, TEST_CASE_ID);

        assertNull(caseDataMap.get(MANAGE_DOC_UPLOADED_CATEGORY));
        assertNull(caseDataMap.get(MANAGE_DOCUMENTS_TRIGGERED_BY));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSetUrgentDocTypeToNullForNonUrgentDocumentType() {
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        UploadResponse uploadResponse = new UploadResponse(List.of(testDocument()));
        CaseDetails caseDetails = CaseDetails.builder().id(Long.parseLong(TEST_CASE_ID)).build();
        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent updateData = new StartAllTabsUpdateDataContent(
            authToken, EventRequestData.builder().build(), StartEventResponse.builder().build(),
            caseDataMap, caseData, null
        );

        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(updateData);

        cafcassUploadDocService.uploadDocument(authToken, file, "16_4_Report", TEST_CASE_ID);

        assertNull(caseDataMap.get(MANAGE_DOC_UPLOADED_CATEGORY));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotCreateDuplicateTaskWhenSame16aRiskAssessmentAlreadyInQuarantine() {
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        UploadResponse uploadResponse = new UploadResponse(List.of(testDocument()));
        CaseDetails caseDetails = CaseDetails.builder().id(Long.parseLong(TEST_CASE_ID)).build();

        Element<QuarantineLegalDoc> existingDoc = element(QuarantineLegalDoc.builder()
            .categoryId("16aRiskAssessment")
            .build());
        CaseData caseDataWithExistingQuarantineDoc = caseData.toBuilder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                .cafcassQuarantineDocsList(List.of(existingDoc))
                .build())
            .build();

        Map<String, Object> caseDataMap = caseDataWithExistingQuarantineDoc.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent updateData = new StartAllTabsUpdateDataContent(
            authToken, EventRequestData.builder().build(), StartEventResponse.builder().build(),
            caseDataMap, caseDataWithExistingQuarantineDoc, null
        );

        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(updateData);

        cafcassUploadDocService.uploadDocument(authToken, file, DOC_TYPE_S16A_RISK_ASSESSMENT, TEST_CASE_ID);

        assertNull(caseDataMap.get(MANAGE_DOC_UPLOADED_CATEGORY));
        assertNull(caseDataMap.get(MANAGE_DOCUMENTS_TRIGGERED_BY));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateTaskWhenDifferentUrgentDocTypeAlreadyInQuarantine() {
        // cirExtensionRequest is in quarantine but we upload cirTransferRequest — different category, task must fire
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        UploadResponse uploadResponse = new UploadResponse(List.of(testDocument()));
        CaseDetails caseDetails = CaseDetails.builder().id(Long.parseLong(TEST_CASE_ID)).build();

        Element<QuarantineLegalDoc> existingDoc = element(QuarantineLegalDoc.builder()
            .categoryId("cirExtensionRequest")
            .build());
        CaseData caseDataWithOtherCategory = caseData.toBuilder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                .cafcassQuarantineDocsList(List.of(existingDoc))
                .build())
            .build();

        Map<String, Object> caseDataMap = caseDataWithOtherCategory.toMap(new ObjectMapper());
        StartAllTabsUpdateDataContent updateData = new StartAllTabsUpdateDataContent(
            authToken, EventRequestData.builder().build(), StartEventResponse.builder().build(),
            caseDataMap, caseDataWithOtherCategory, null
        );

        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(caseDetails);
        when(caseDocumentClient.uploadDocuments(any(), any(), any(), any(), any())).thenReturn(uploadResponse);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(updateData);

        cafcassUploadDocService.uploadDocument(authToken, file, DOC_TYPE_CIR_TRANSFER, TEST_CASE_ID);

        assertEquals(List.of(DOC_TYPE_CIR_TRANSFER), caseDataMap.get(MANAGE_DOC_UPLOADED_CATEGORY));
        assertEquals("CAFCASS", caseDataMap.get(MANAGE_DOCUMENTS_TRIGGERED_BY));
    }

    @Test
    void shouldReturnNullIfCasePresentThrowsException() {
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(coreCaseDataApi.getCase(any(), any(), any())).thenThrow(new RuntimeException("CCD down"));
        assertNull(cafcassUploadDocService.checkIfCasePresent(TEST_CASE_ID, authToken));
    }

    @Test
    void shouldNotUploadDocumentForInvalidCaseScenario1() {
        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID)).thenReturn(null);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            cafcassUploadDocService.uploadDocument(authToken, file, "16_4_Report", TEST_CASE_ID)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldNotUploadDocumentForInvalidCaseScenario2() {
        when(coreCaseDataApi.getCase(authToken, s2sToken, TEST_CASE_ID))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
            cafcassUploadDocService.uploadDocument(authToken, file, "16_4_Report", TEST_CASE_ID)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private Document testDocument() {
        Document.Link binaryLink = new Document.Link();
        binaryLink.href = randomAlphaNumeric;
        Document.Link selfLink = new Document.Link();
        selfLink.href = randomAlphaNumeric;
        Document.Links links = new Document.Links();
        links.binary = binaryLink;
        links.self = selfLink;
        Document document = Document.builder().build();
        document.links = links;
        document.originalDocumentName = randomAlphaNumeric;
        return document;
    }
}
