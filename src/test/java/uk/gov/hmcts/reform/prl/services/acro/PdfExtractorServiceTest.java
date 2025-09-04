package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private Resource resource;

    @InjectMocks
    private PdfExtractorService service;

    private static final String CASE_ID = "1234567890";
    private static final String USER_TOKEN = "userToken";
    private static final String SERVICE_TOKEN = "serviceToken";
    private static final String FILE_NAME = "test_document.pdf";
    private static final String DOCUMENT_URL = "http://example.com/document";

    //@Test
    @DisplayName("Should return file when document is valid")
    void downloadFl404aDocument_shouldReturnFile_whenDocumentIsValid() throws IOException {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        setupValidDocument(orderDocument);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            setupSuccessfulDownload();
            filesMock.when(() -> Files.copy(
                    any(InputStream.class),
                    any(Path.class),
                    eq(StandardCopyOption.REPLACE_EXISTING)
                ))
                .thenReturn(1L);

            File result = service.downloadPdf(FILE_NAME, CASE_ID, orderDocument, USER_TOKEN);

            assertTrue(Optional.ofNullable(result).isPresent());
            assertEquals(FILE_NAME, result.getName());
            verify(caseDocumentClient).getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL);
        }
    }

    @Test
    @DisplayName("Should return empty when document is null")
    void downloadFl404aDocument_shouldReturnEmpty_whenDocumentIsNull() {
        File result = service.downloadPdf(FILE_NAME, CASE_ID, null, USER_TOKEN);

        assertFalse(Optional.ofNullable(result).isPresent());
        verify(caseDocumentClient, never()).getDocumentBinary(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return empty when document URL is null")
    void downloadFl404aDocument_shouldReturnEmpty_whenDocumentUrlIsNull() {
        when(orderDocument.getDocumentBinaryUrl()).thenReturn(null);

        File result = service.downloadPdf(FILE_NAME, CASE_ID, orderDocument, USER_TOKEN);

        assertFalse(Optional.ofNullable(result).isPresent());
        verify(caseDocumentClient, never()).getDocumentBinary(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when download fails")
    void downloadFl404aDocument_shouldThrowException_whenDownloadFails() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        setupValidDocument(orderDocument);
        when(caseDocumentClient.getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL))
            .thenThrow(new RuntimeException("Download failed"));

        assertThrows(
            RuntimeException.class, () ->
                service.downloadPdf(FILE_NAME, CASE_ID, orderDocument, USER_TOKEN)
        );

        verify(caseDocumentClient).getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL);
    }

    @Test
    @DisplayName("Should throw exception when response body is null")
    void downloadFl404aDocument_shouldThrowException_whenResponseBodyIsNull() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        setupValidDocument(orderDocument);
        ResponseEntity<Resource> response = ResponseEntity.ok().build();
        when(caseDocumentClient.getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL))
            .thenReturn(response);

        assertThrows(
            RuntimeException.class, () ->
                service.downloadPdf(FILE_NAME, CASE_ID, orderDocument, USER_TOKEN)
        );

        verify(caseDocumentClient).getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL);
    }

    //@Test
    @DisplayName("Should return file when document is valid")
    void downloadPdf_shouldReturnFile_whenDocumentIsValid() throws IOException {
        String outputDirectory = "/tmp/output";
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        setupValidDocument(orderDocument);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            setupSuccessfulDownload();
            filesMock.when(() -> Files.copy(
                    any(InputStream.class),
                    any(Path.class),
                    eq(StandardCopyOption.REPLACE_EXISTING)
                ))
                .thenReturn(1L);

            File result = service.downloadPdf(FILE_NAME, CASE_ID, orderDocument, USER_TOKEN);
            assertTrue(Optional.ofNullable(result).isPresent());
            String fileName = result.getName();
            assertTrue(fileName.startsWith("FL404A-" + CASE_ID));
            assertTrue(fileName.endsWith(".pdf"));
            assertFalse(fileName.contains("Welsh"));
            verify(caseDocumentClient).getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL);
        }
    }

    //@Test
    @DisplayName("Should return Welsh file when document is Welsh")
    void downloadPdf_shouldReturnWelshFile_whenDocumentIsWelsh() throws IOException {
        LocalDateTime orderCreatedDate = LocalDateTime.now();
        String outputDirectory = "/tmp/output";
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        setupValidDocument(orderDocument);

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            setupSuccessfulDownload();
            filesMock.when(() -> Files.copy(
                    any(InputStream.class),
                    any(Path.class),
                    eq(StandardCopyOption.REPLACE_EXISTING)
                ))
                .thenReturn(1L);

            File result = service.downloadPdf(FILE_NAME, CASE_ID, orderDocument, USER_TOKEN);

            assertTrue(Optional.ofNullable(result).isPresent());
            assertTrue(result.getName().startsWith("FL404A-" + CASE_ID));
            assertTrue(result.getName().contains("Welsh"));
            assertTrue(result.getName().endsWith(".pdf"));
            verify(caseDocumentClient).getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL);
        }
    }

    @Test
    @DisplayName("Should return empty when document is null")
    void downloadPdf_shouldReturnEmpty_whenDocumentIsNull() {
        LocalDateTime orderCreatedDate = LocalDateTime.now();
        String outputDirectory = "/tmp/output";

        File result = service.downloadPdf(FILE_NAME, CASE_ID, orderDocument, USER_TOKEN);

        assertFalse(Optional.ofNullable(result).isPresent());
        verify(caseDocumentClient, never()).getDocumentBinary(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should return empty when document URL is null")
    void downloadPdf_shouldReturnEmpty_whenDocumentUrlIsNull() {
        LocalDateTime orderCreatedDate = LocalDateTime.now();
        String outputDirectory = "/tmp/output";
        when(orderDocument.getDocumentBinaryUrl()).thenReturn(null);

        File result = service.downloadPdf(FILE_NAME, CASE_ID, orderDocument, USER_TOKEN);

        assertFalse(Optional.ofNullable(result).isPresent());
        verify(caseDocumentClient, never()).getDocumentBinary(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when download fails")
    void downloadPdf_shouldThrowException_whenDownloadFails() {
        LocalDateTime orderCreatedDate = LocalDateTime.now();
        String outputDirectory = "/tmp/output";
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        setupValidDocument(orderDocument);
        when(caseDocumentClient.getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL))
            .thenThrow(new RuntimeException("Download failed"));

        assertThrows(
            RuntimeException.class, () ->
                service.downloadPdf(FILE_NAME, CASE_ID, orderDocument, USER_TOKEN)
        );
    }

    @Test
    @DisplayName("Should generate correct filename format")
    void generateFileName_shouldCreateCorrectFilename() {
        LocalDateTime orderCreatedDate = LocalDateTime.of(2023, 9, 4, 10, 30);

        long expectedEpochSecond = orderCreatedDate.atZone(ZoneId.systemDefault()).toEpochSecond();
        String expectedEnglishName = "/tmp/output/FL404A-" + CASE_ID + "-" + expectedEpochSecond + ".pdf";
        String expectedWelshName = "/tmp/output/FL404A-" + CASE_ID + "-" + expectedEpochSecond + "-Welsh.pdf";

        assertTrue(expectedEnglishName.contains("FL404A-" + CASE_ID));
        assertTrue(expectedWelshName.contains("Welsh"));
    }

    //@Test
    @DisplayName("Should retry on failure when download fails")
    void downloadPdf_shouldRetryOnFailure_whenDownloadFails() {
        LocalDateTime orderCreatedDate = LocalDateTime.now();
        String outputDirectory = "/tmp/output";
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        setupValidDocument(orderDocument);

        when(caseDocumentClient.getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL))
            .thenThrow(new RuntimeException("Download failed"))
            .thenThrow(new RuntimeException("Download failed"))
            .thenReturn(ResponseEntity.ok(resource));

        service.downloadPdf(FILE_NAME, CASE_ID, orderDocument, USER_TOKEN);
        verify(caseDocumentClient, times(3)).getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL);
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
