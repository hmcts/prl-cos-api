package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PDF Extractor Service Tests")
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

    @Nested
    @DisplayName("Happy Path Scenarios")
    class HappyPathTests {

        @Test
        @DisplayName("Should successfully download PDF when document is valid")
        void downloadPdf_shouldReturnFile_whenDocumentIsValid() throws IOException {
            when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
            when(orderDocument.getDocumentBinaryUrl()).thenReturn(DOCUMENT_URL);

            try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
                setupSuccessfulDownload();
                filesMock.when(() -> Files.copy(
                        any(InputStream.class),
                        any(Path.class),
                        eq(StandardCopyOption.REPLACE_EXISTING)
                    ))
                    .thenReturn(1L);

                File result = service.downloadPdf(FILE_NAME, CASE_ID, orderDocument, USER_TOKEN);

                assertAll(
                    "Successful download validation",
                    () -> assertTrue(Optional.ofNullable(result).isPresent(), "Result should not be null"),
                    () -> assertEquals(FILE_NAME, result.getName(), "Filename should match what we passed in"),
                    () -> verify(caseDocumentClient).getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL)
                );
            }
        }

        @ParameterizedTest(name = "Should successfully download {0}")
        @MethodSource("uk.gov.hmcts.reform.prl.services.acro.PdfExtractorServiceTest#downloadPdfTestCases")
        @DisplayName("Should successfully download different document types")
        void downloadPdf_shouldReturnFile_forDifferentDocumentTypes(String documentType, String fileName, Document document) throws IOException {
            when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
            when(document.getDocumentBinaryUrl()).thenReturn(DOCUMENT_URL);

            try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
                setupSuccessfulDownload();
                filesMock.when(() -> Files.copy(
                        any(InputStream.class),
                        any(Path.class),
                        eq(StandardCopyOption.REPLACE_EXISTING)
                    ))
                    .thenReturn(1L);

                File result = service.downloadPdf(fileName, CASE_ID, document, USER_TOKEN);

                assertAll(
                    "File download validation for " + documentType,
                    () -> assertTrue(Optional.ofNullable(result).isPresent(), "Result should not be null"),
                    () -> assertEquals(fileName, result.getName(), "Filename should match what we passed in"),
                    () -> verify(caseDocumentClient).getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL)
                );
            }
        }
    }

    @Nested
    @DisplayName("Exception Scenarios")
    class ExceptionTests {

        @Test
        @DisplayName("Should return null when document is null")
        void downloadPdf_shouldReturnNull_whenDocumentIsNull() {
            File result = service.downloadPdf(FILE_NAME, CASE_ID, null, USER_TOKEN);

            assertAll(
                "Null document handling",
                () -> assertFalse(Optional.ofNullable(result).isPresent(), "Result should be null"),
                () -> verify(caseDocumentClient, never()).getDocumentBinary(anyString(), anyString(), anyString())
            );
        }

        @Test
        @DisplayName("Should return null when document URL is null")
        void downloadPdf_shouldReturnNull_whenDocumentUrlIsNull() {
            when(orderDocument.getDocumentBinaryUrl()).thenReturn(null);

            File result = service.downloadPdf(FILE_NAME, CASE_ID, orderDocument, USER_TOKEN);

            assertAll(
                "Null document URL handling",
                () -> assertFalse(Optional.ofNullable(result).isPresent(), "Result should be null"),
                () -> verify(caseDocumentClient, never()).getDocumentBinary(anyString(), anyString(), anyString())
            );
        }

        @Test
        @DisplayName("Should throw exception when download fails")
        void downloadPdf_shouldThrowException_whenDownloadFails() {
            when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
            when(orderDocument.getDocumentBinaryUrl()).thenReturn(DOCUMENT_URL);
            when(caseDocumentClient.getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL))
                .thenThrow(new RuntimeException("Download failed"));

            assertThrows(
                RuntimeException.class,
                () -> service.downloadPdf(FILE_NAME, CASE_ID, orderDocument, USER_TOKEN),
                "Should throw RuntimeException when download fails"
            );

            verify(caseDocumentClient).getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL);
        }

        @Test
        @DisplayName("Should throw exception when response body is null")
        void downloadPdf_shouldThrowException_whenResponseBodyIsNull() {
            when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
            when(orderDocument.getDocumentBinaryUrl()).thenReturn(DOCUMENT_URL);
            ResponseEntity<Resource> response = ResponseEntity.ok().build();
            when(caseDocumentClient.getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL))
                .thenReturn(response);

            assertThrows(
                RuntimeException.class,
                () -> service.downloadPdf(FILE_NAME, CASE_ID, orderDocument, USER_TOKEN),
                "Should throw RuntimeException when response body is null"
            );

            verify(caseDocumentClient).getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL);
        }
    }

    static Stream<Arguments> downloadPdfTestCases() {
        return Stream.of(
            Arguments.of("English document", "FL404A-1234567890-1693747200.pdf", mock(Document.class)),
            Arguments.of("Welsh document", "FL404A-1234567890-1693747200-Welsh.pdf", mock(Document.class))
        );
    }

    private void setupSuccessfulDownload() throws IOException {
        ResponseEntity<Resource> response = ResponseEntity.ok(resource);
        when(caseDocumentClient.getDocumentBinary(USER_TOKEN, SERVICE_TOKEN, DOCUMENT_URL))
            .thenReturn(response);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));
    }
}
