package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfExtractorServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private Document orderDocument;

    @Mock
    private Document orderDocumentWelsh;

    @Mock
    private Resource resource;

    @InjectMocks
    private PdfExtractorService service;

    private static final String CASE_ID = "1234567890";
    private static final String USER_TOKEN = "userToken";
    private static final String SERVICE_TOKEN = "serviceToken";
    private static final String FILE_NAME = "test_document.pdf";
    private static final String DOCUMENT_URL = "http://example.com/document";

    @Test
    void downloadFl404aDocuments_shouldReturnBothDocuments_whenBothAreValid() throws IOException {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        setupValidDocument(orderDocument);
        setupValidDocument(orderDocumentWelsh);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            setupSuccessfulDownload();
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)))
                .thenReturn(1L);

            Optional<List<File>> result = service.downloadFl404aDocuments(CASE_ID, USER_TOKEN, FILE_NAME, orderDocument, orderDocumentWelsh);

            assertTrue(result.isPresent());
            assertEquals(2, result.get().size());
            verify(caseDocumentClient, times(2)).getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL);
        }
    }

    @ParameterizedTest
    @MethodSource("singleValidDocumentScenarios")
    @DisplayName("Should return one document when only one is valid")
    void downloadFl404aDocuments_shouldReturnOneDocument_whenOnlyOneIsValid(String testName,
                                                                            boolean englishValid,
                                                                            boolean welshValid) throws IOException {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);

        if (englishValid) {
            setupValidDocument(orderDocument);
        } else {
            when(orderDocument.getDocumentBinaryUrl()).thenReturn(null);
        }

        if (welshValid) {
            setupValidDocument(orderDocumentWelsh);
        } else {
            when(orderDocumentWelsh.getDocumentBinaryUrl()).thenReturn(null);
        }

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            setupSuccessfulDownload();
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)))
                .thenReturn(1L);

            Optional<List<File>> result = service.downloadFl404aDocuments(CASE_ID, USER_TOKEN, FILE_NAME, orderDocument, orderDocumentWelsh);

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            verify(caseDocumentClient, times(1)).getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL);
        }
    }

    private static java.util.stream.Stream<Arguments> singleValidDocumentScenarios() {
        return java.util.stream.Stream.of(
            Arguments.of("English document valid, Welsh document null", true, false),
            Arguments.of("English document null, Welsh document valid", false, true)
        );
    }

    @Test
    void downloadFl404aDocuments_shouldReturnEmpty_whenNoDocumentsAreValid() {
        when(orderDocument.getDocumentBinaryUrl()).thenReturn(null);
        when(orderDocumentWelsh.getDocumentBinaryUrl()).thenReturn(null);

        Optional<List<File>> result = service.downloadFl404aDocuments(CASE_ID, USER_TOKEN, FILE_NAME, orderDocument, orderDocumentWelsh);

        assertFalse(result.isPresent());
        verify(caseDocumentClient, never()).getDocumentBinary(anyString(), anyString(), anyString());
    }

    @Test
    void downloadFl404aDocuments_shouldReturnEmpty_whenDocumentsAreNull() {
        Optional<List<File>> result = service.downloadFl404aDocuments(CASE_ID, USER_TOKEN, FILE_NAME, null, null);

        assertFalse(result.isPresent());
        verify(caseDocumentClient, never()).getDocumentBinary(anyString(), anyString(), anyString());
    }

    @Test
    void downloadFl404aDocument_shouldReturnFile_whenDocumentIsValid() throws IOException {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        setupValidDocument(orderDocument);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            setupSuccessfulDownload();
            filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)))
                .thenReturn(1L);

            Optional<File> result = service.downloadFl404aDocument(CASE_ID, USER_TOKEN, FILE_NAME, orderDocument);

            assertTrue(result.isPresent());
            assertEquals(FILE_NAME, result.get().getName());
            verify(caseDocumentClient).getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL);
        }
    }

    @Test
    void downloadFl404aDocument_shouldReturnEmpty_whenDocumentIsNull() {
        Optional<File> result = service.downloadFl404aDocument(CASE_ID, USER_TOKEN, FILE_NAME, null);

        assertFalse(result.isPresent());
        verify(caseDocumentClient, never()).getDocumentBinary(anyString(), anyString(), anyString());
    }

    @Test
    void downloadFl404aDocument_shouldReturnEmpty_whenDocumentUrlIsNull() {
        when(orderDocument.getDocumentBinaryUrl()).thenReturn(null);

        Optional<File> result = service.downloadFl404aDocument(CASE_ID, USER_TOKEN, FILE_NAME, orderDocument);

        assertFalse(result.isPresent());
        verify(caseDocumentClient, never()).getDocumentBinary(anyString(), anyString(), anyString());
    }

    @Test
    void downloadFl404aDocument_shouldReturnEmpty_whenDownloadFails() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        setupValidDocument(orderDocument);
        when(caseDocumentClient.getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL))
            .thenThrow(new RuntimeException("Download failed"));

        Optional<File> result = service.downloadFl404aDocument(CASE_ID, USER_TOKEN, FILE_NAME, orderDocument);

        assertFalse(result.isPresent());
    }

    @Test
    void downloadFl404aDocument_shouldReturnEmpty_whenResponseBodyIsNull() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        setupValidDocument(orderDocument);
        ResponseEntity<Resource> response = ResponseEntity.ok().build();
        when(caseDocumentClient.getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL))
            .thenReturn(response);

        Optional<File> result = service.downloadFl404aDocument(CASE_ID, USER_TOKEN, FILE_NAME, orderDocument);

        assertFalse(result.isPresent());
    }

    private void setupValidDocument(Document document) {
        when(document.getDocumentBinaryUrl()).thenReturn(DOCUMENT_URL);
    }

    private void setupSuccessfulDownload() throws IOException {
        ResponseEntity<Resource> response = ResponseEntity.ok(resource);
        when(caseDocumentClient.getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL))
            .thenReturn(response);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));
    }
}
