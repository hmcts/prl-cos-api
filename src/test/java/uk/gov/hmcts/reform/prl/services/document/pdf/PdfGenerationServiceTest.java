package uk.gov.hmcts.reform.prl.services.document.pdf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.clients.DocmosisClient;
import uk.gov.hmcts.reform.prl.exception.PdfConversionException;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.services.UploadDocumentService;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfGenerationServiceTest {

    @Mock
    private DocmosisClient docmosisClient;

    @Mock
    private UploadDocumentService uploadDocumentService;

    private PdfGenerationService pdfGenerationService;

    @BeforeEach
    void setUp() {
        pdfGenerationService = new PdfGenerationService(docmosisClient, uploadDocumentService);
    }

    @Test
    void shouldGenerateAndStorePdf() {
        byte[] sourceContent = new byte[]{1, 2, 3};
        byte[] generatedPdf = new byte[]{9, 8, 7};
        String authToken = "auth-token";

        PdfGenerationRequest request = PdfGenerationRequest.builder()
            .sourceFilename("my-input.docx")
            .fileContent(sourceContent)
            .authToken(authToken)
            .build();

        uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument = uploadedDocument("generated.pdf");

        when(docmosisClient.convert(sourceContent, "my-input.docx", "my-input.pdf")).thenReturn(generatedPdf);
        when(uploadDocumentService.uploadDocument(
            generatedPdf,
            "my-input.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            authToken
        ))
            .thenReturn(uploadedDocument);

        Document result = pdfGenerationService.generateAndStore(request);

        assertEquals("http://doc/self", result.getDocumentUrl());
        assertEquals("http://doc/binary", result.getDocumentBinaryUrl());
        assertEquals("generated.pdf", result.getDocumentFileName());

        ArgumentCaptor<byte[]> uploadedBytesCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(uploadDocumentService).uploadDocument(
            uploadedBytesCaptor.capture(),
            eq("my-input.pdf"),
            eq(MediaType.APPLICATION_PDF_VALUE),
            eq(authToken)
        );
        assertArrayEquals(generatedPdf, uploadedBytesCaptor.getValue());
    }

    @Test
    void shouldUseBaseNameForOutputPdfWhenSourceNameContainsMultipleDots() {
        byte[] sourceContent = new byte[]{4, 5, 6};
        byte[] generatedPdf = new byte[]{6, 5, 4};
        PdfGenerationRequest request = PdfGenerationRequest.builder()
            .sourceFilename("draft.v2.template.docx")
            .fileContent(sourceContent)
            .authToken("auth-token")
            .build();

        when(docmosisClient.convert(any(), any(), any())).thenReturn(generatedPdf);
        when(uploadDocumentService.uploadDocument(
            any(),
            any(),
            any(),
            any()
        )).thenReturn(uploadedDocument("draft.pdf"));

        pdfGenerationService.generateAndStore(request);

        verify(docmosisClient).convert(sourceContent, "draft.v2.template.docx", "draft.v2.template.pdf");
        verify(uploadDocumentService).uploadDocument(
            generatedPdf,
            "draft.v2.template.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "auth-token"
        );
    }

    @Test
    void shouldWrapExceptionsWithPdfConversionException() {
        PdfGenerationRequest request = PdfGenerationRequest.builder()
            .sourceFilename("my-input.docx")
            .fileContent(new byte[]{1})
            .authToken("auth-token")
            .build();

        RuntimeException rootCause = new RuntimeException("docmosis unavailable");
        when(docmosisClient.convert(any(), any(), any())).thenThrow(rootCause);

        PdfConversionException exception = assertThrows(
            PdfConversionException.class,
            () -> pdfGenerationService.generateAndStore(request)
        );

        assertEquals("Failed to generate and store PDF", exception.getMessage());
        assertSame(rootCause, exception.getCause());
    }

    private uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument(String originalDocumentName) {
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link selfLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        selfLink.href = "http://doc/self";
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link binaryLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        binaryLink.href = "http://doc/binary";

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Links links = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        links.self = selfLink;
        links.binary = binaryLink;

        uk.gov.hmcts.reform.ccd.document.am.model.Document document = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();
        document.links = links;
        document.originalDocumentName = originalDocumentName;
        return document;
    }
}
