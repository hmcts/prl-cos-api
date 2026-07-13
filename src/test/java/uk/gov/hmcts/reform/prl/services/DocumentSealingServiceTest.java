package uk.gov.hmcts.reform.prl.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.document.pdf.PdfGenerationRequest;
import uk.gov.hmcts.reform.prl.services.document.pdf.PdfGenerationService;
import uk.gov.hmcts.reform.prl.utils.ResourceReader;

import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ResourceReader.readBytes;

@RunWith(MockitoJUnitRunner.class)
public class DocumentSealingServiceTest {
    @Mock
    private PdfGenerationService pdfGenerationService;
    @Mock
    private DocumentGenService documentGenService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Captor
    private ArgumentCaptor<PdfGenerationRequest> actualDocumentRequest;

    @InjectMocks
    DocumentSealingService documentSealingService;

    private static final String CASE_ID = "4534758712128976";
    private final String newFileName = "test.pdf";

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void sealDocumentShouldReturnSealedDocumentWhenPdf() {
        final byte[] inputDocumentBinaries = readBytes("documents/document.pdf");
        final byte[] sealBinaries = readBytes("familycourtseal.png");
        final Document sealedDocument = Document.builder().documentFileName(newFileName).documentBinaryUrl(
            "/test/binary").documentUrl("/test").build();

        Document inputDocument = Document.builder()
            .documentUrl("/test").documentBinaryUrl("/test/binary").documentFileName("test.pdf").build();
        Document inputDocumentPdf = Document.builder()
            .documentUrl("/test").documentBinaryUrl("/test/binary").documentFileName("test.pdf").build();

        when(authTokenGenerator.generate()).thenReturn("s2s token");
        when(documentGenService.getDocumentBytes(
            inputDocumentPdf.getDocumentUrl(),
            "testAuth",
            "s2s token"
        )).thenReturn(inputDocumentBinaries);

        Document outputDocument = Document.builder()
            .documentUrl("/test")
            .documentBinaryUrl("/test/binary")
            .documentFileName("test.pdf")
            .build();
        when(pdfGenerationService.generateAndStore(actualDocumentRequest.capture())).thenReturn(outputDocument);

        MockedStatic<ResourceReader> mockResourceReader = mockStatic(ResourceReader.class);
        mockResourceReader.when(() -> ResourceReader.readBytes("familycourtseal.png")).thenReturn(sealBinaries);
        CaseData caseData = CaseData.builder().courtSeal("[userImage:familycourtseal.png]")
            .caseManagementLocation(CaseManagementLocation.builder()
                                        .region("2")
                                        .regionId(null)
                                        .regionName("Midlands")
                                        .baseLocation("123456789")
                                        .baseLocationId(null)
                                        .baseLocationName("Birmingham")
                                        .build())
            .build();

        final Document actualSealedDocumentReference = documentSealingService
            .sealDocument(inputDocument, caseData, "testAuth");

        assertThat(actualSealedDocumentReference).isEqualTo(sealedDocument);
        assertThat(inputDocumentBinaries).isNotEqualTo(actualDocumentRequest.getValue().getFileContent());
        mockResourceReader.close();
    }

    @Test
    public void sealDocumentShouldReturnSealedDocumentWhenNotPdf() {
        final byte[] inputDocumentBinaries = readBytes("documents/document.pdf");
        final byte[] sealBinaries = readBytes("familycourtseal-bilingual.png");
        final Document sealedDocument = Document.builder().documentFileName(newFileName).documentBinaryUrl(
            "/test/binary").documentUrl("/test").build();

        Document inputDocument = Document.builder()
            .documentUrl("/test").documentBinaryUrl("/test/binary").documentFileName("test.doc").build();
        Document inputDocumentPdf = Document.builder()
            .documentUrl("/test").documentBinaryUrl("/test/binary").documentFileName("test.pdf").build();

        when(documentGenService.checkFileFormat("test.doc")).thenReturn(true);
        when(documentGenService.convertToPdf(CASE_ID, "testAuth", inputDocument))
            .thenReturn(inputDocumentPdf);
        when(authTokenGenerator.generate()).thenReturn("s2s token");
        when(documentGenService.getDocumentBytes(
            inputDocumentPdf.getDocumentUrl(),
            "testAuth",
            "s2s token"
        )).thenReturn(inputDocumentBinaries);

        Document outputDocument = Document.builder()
            .documentUrl("/test")
            .documentBinaryUrl("/test/binary")
            .documentFileName("test.pdf")
            .build();
        when(pdfGenerationService.generateAndStore(actualDocumentRequest.capture())).thenReturn(outputDocument);

        MockedStatic<ResourceReader> mockResourceReader = mockStatic(ResourceReader.class);
        mockResourceReader.when(() -> ResourceReader.readBytes("familycourtseal-bilingual.png")).thenReturn(
            sealBinaries);

        CaseData caseData = CaseData.builder()
            .id(Long.parseLong(CASE_ID))
            .courtSeal("[userImage:familycourtseal-bilingual.png]")
            .caseManagementLocation(CaseManagementLocation.builder()
                                        .region("7")
                                        .regionId(null)
                                        .regionName("Wales")
                                        .baseLocation("234946")
                                        .baseLocationId(null)
                                        .baseLocationName("Swansea")
                                        .build())
            .build();

        final Document actualSealedDocumentReference = documentSealingService
            .sealDocument(inputDocument, caseData, "testAuth");

        assertThat(actualSealedDocumentReference).isEqualTo(sealedDocument);
        assertThat(inputDocumentBinaries).isNotEqualTo(actualDocumentRequest.getValue().getFileContent());
        mockResourceReader.close();
    }

    @Test
    public void sealDocumentShouldCatchIoException() {
        final byte[] inputDocumentBinaries = readBytes("documents/Document.docx");
        final byte[] sealBinaries = readBytes("familycourtseal-bilingual.png");


        Document inputDocumentPdf = Document.builder()
            .documentUrl("/test").documentBinaryUrl("/test/binary").documentFileName("test.pdf").build();

        when(authTokenGenerator.generate()).thenReturn("s2s token");
        when(documentGenService.getDocumentBytes(
            inputDocumentPdf.getDocumentUrl(),
            "testAuth",
            "s2s token"
        )).thenReturn(inputDocumentBinaries);

        MockedStatic<ResourceReader> mockResourceReader = mockStatic(ResourceReader.class);
        mockResourceReader.when(() -> ResourceReader.readBytes("familycourtseal-bilingual.png")).thenReturn(
            sealBinaries);

        CaseData caseData = CaseData.builder().courtSeal("[userImage:familycourtseal-bilingual.png]")
            .caseManagementLocation(CaseManagementLocation.builder()
                                        .region("7")
                                        .regionId(null)
                                        .regionName("Wales")
                                        .baseLocation("234946")
                                        .baseLocationId(null)
                                        .baseLocationName("Swansea")
                                        .build())
            .build();
        Document inputDocument = Document.builder()
            .documentUrl("/test").documentBinaryUrl("/test/binary").documentFileName("test.pdf").build();

        assertExpectedException(() -> {
            documentSealingService
                .sealDocument(inputDocument, caseData, "testAuth");
        }, UncheckedIOException.class, "java.io.IOException: Missing root object specification in trailer.");
        mockResourceReader.close();
    }

    @Test
    public void sealDocumentShouldRethrowIllegalStateException() {
        final byte[] sealBinaries = readBytes("familycourtseal.png");

        Document inputDocument = Document.builder()
            .documentUrl("/test").documentBinaryUrl("/test/binary").documentFileName("test.pdf").build();

        when(authTokenGenerator.generate()).thenReturn("s2s token");
        when(documentGenService.getDocumentBytes(
            inputDocument.getDocumentUrl(),
            "testAuth",
            "s2s token"
        )).thenReturn(new byte[]{1, 2, 3}); // Invalid PDF bytes

        MockedStatic<ResourceReader> mockResourceReader = mockStatic(ResourceReader.class);
        mockResourceReader.when(() -> ResourceReader.readBytes("familycourtseal.png")).thenReturn(sealBinaries);

        MockedStatic<PDDocument> mockPdDocument = mockStatic(PDDocument.class);
        mockPdDocument.when(() -> PDDocument.load(any(byte[].class)))
            .thenThrow(new IllegalStateException("Test illegal state"));

        CaseData caseData = CaseData.builder()
            .caseManagementLocation(CaseManagementLocation.builder()
                .region("2")
                .build())
            .build();

        assertExpectedException(() -> {
            documentSealingService.sealDocument(inputDocument, caseData, "testAuth");
        }, IllegalStateException.class, "Test illegal state");

        mockPdDocument.close();
        mockResourceReader.close();
    }

    @Test
    public void sealDocumentShouldHandleNullCaseManagementLocation() {
        final byte[] inputDocumentBinaries = readBytes("documents/document.pdf");
        final byte[] sealBinaries = readBytes("familycourtseal.png");

        Document inputDocument = Document.builder()
            .documentUrl("/test")
            .documentBinaryUrl("/test/binary")
            .documentFileName("test.pdf")
            .build();

        when(authTokenGenerator.generate()).thenReturn("s2s token");
        when(documentGenService.getDocumentBytes(
            inputDocument.getDocumentUrl(),
            "testAuth",
            "s2s token"
        )).thenReturn(inputDocumentBinaries);

        Document outputDocument = Document.builder()
            .documentUrl("/test")
            .documentBinaryUrl("/test/binary")
            .documentFileName("test.pdf")
            .build();
        when(pdfGenerationService.generateAndStore(any(PdfGenerationRequest.class))).thenReturn(outputDocument);

        try (MockedStatic<ResourceReader> mockResourceReader = mockStatic(ResourceReader.class)) {
            mockResourceReader.when(() -> ResourceReader.readBytes("familycourtseal.png"))
                .thenReturn(sealBinaries);

            CaseData caseData = CaseData.builder()
                .courtSeal("[userImage:familycourtseal.png]")
                .caseManagementLocation(null)
                .build();

            Document result = documentSealingService.sealDocument(inputDocument, caseData, "testAuth");

            assertNotNull(result);
        }
    }
}
