package uk.gov.hmcts.reform.prl.services;

import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.ResourceReader;

import java.io.UncheckedIOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ResourceReader.readBytes;

@ExtendWith(MockitoExtension.class)
public class DocumentSealingServiceTest {


    @Mock
    private DgsApiClient dgsApiClient;
    @Mock
    private DocumentGenService documentGenService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Captor
    private ArgumentCaptor<GenerateDocumentRequest> actualDocumentRequest;

    @InjectMocks
    DocumentSealingService documentSealingService;

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
        GeneratedDocumentInfo documentInfo = GeneratedDocumentInfo.builder().url("/test").docName(newFileName).binaryUrl(
            "/test/binary").build();

        when(authTokenGenerator.generate()).thenReturn("s2s token");
        when(documentGenService.getDocumentBytes(
            inputDocumentPdf.getDocumentUrl(),
            "testAuth",
            "s2s token"
        )).thenReturn(inputDocumentBinaries);
        when(dgsApiClient.convertDocToPdf(anyString(), anyString(), any())).thenReturn(documentInfo);
        MockedStatic<ResourceReader> mockResourceReader = mockStatic(ResourceReader.class);
        mockResourceReader.when(() -> ResourceReader.readBytes("/familycourtseal.png")).thenReturn(sealBinaries);
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

        verify(dgsApiClient).convertDocToPdf(eq(newFileName), eq("testAuth"), actualDocumentRequest.capture());
        assertThat(actualSealedDocumentReference).isEqualTo(sealedDocument);
        assertThat(inputDocumentBinaries).isNotEqualTo(actualDocumentRequest.getValue().getValues().get("fileName"));
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
        GeneratedDocumentInfo documentInfo = GeneratedDocumentInfo.builder().url("/test").docName(newFileName).binaryUrl(
            "/test/binary").build();

        when(documentGenService.checkFileFormat("test.doc")).thenReturn(true);
        when(documentGenService.convertToPdf("testAuth", inputDocument))
            .thenReturn(inputDocumentPdf);
        when(authTokenGenerator.generate()).thenReturn("s2s token");
        when(documentGenService.getDocumentBytes(
            inputDocumentPdf.getDocumentUrl(),
            "testAuth",
            "s2s token"
        )).thenReturn(inputDocumentBinaries);
        when(dgsApiClient.convertDocToPdf(anyString(), anyString(), any())).thenReturn(documentInfo);

        MockedStatic<ResourceReader> mockResourceReader = mockStatic(ResourceReader.class);
        mockResourceReader.when(() -> ResourceReader.readBytes("/familycourtseal-bilingual.png")).thenReturn(
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

        final Document actualSealedDocumentReference = documentSealingService
            .sealDocument(inputDocument, caseData, "testAuth");

        verify(dgsApiClient).convertDocToPdf(eq(newFileName), eq("testAuth"), actualDocumentRequest.capture());
        assertThat(actualSealedDocumentReference).isEqualTo(sealedDocument);
        assertThat(inputDocumentBinaries).isNotEqualTo(actualDocumentRequest.getValue().getValues().get("fileName"));
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
        mockResourceReader.when(() -> ResourceReader.readBytes("/familycourtseal-bilingual.png")).thenReturn(
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
}
