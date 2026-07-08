package uk.gov.hmcts.reform.prl.services.document.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.DocmosisClient;
import uk.gov.hmcts.reform.prl.exception.PdfConversionException;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.services.UploadDocumentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGenerationService {

    private final DocmosisClient docmosisClient;
    private final UploadDocumentService uploadDocumentService;

    public Document generateAndStore(PdfGenerationRequest pdfGenerationRequest) {
        try {
            String outputFilename = FilenameUtils.getBaseName(pdfGenerationRequest.getSourceFilename()) + ".pdf";
            byte[] pdf = generatePdf(pdfGenerationRequest.getSourceFilename(), outputFilename,
                                     pdfGenerationRequest.getFileContent());
            return storeDocument(pdf, pdfGenerationRequest.getAuthToken(), outputFilename);
        } catch (Exception e) {
            throw new PdfConversionException("Case ID " + pdfGenerationRequest.getCaseId()
                                                 + ": Failed to generate and store PDF", e);
        }
    }

    private byte[] generatePdf(String sourceFilename, String outputFilename, byte[] content) {
        return docmosisClient.convert(content, sourceFilename, outputFilename);
    }

    private Document storeDocument(byte[] pdf, String authToken, String filename) {
        var document = uploadDocumentService.uploadDocument(pdf, filename, MediaType.APPLICATION_PDF_VALUE, authToken);

        return Document.builder()
            .documentUrl(document.links.self.href)
            .documentBinaryUrl(document.links.binary.href)
            .documentFileName(document.originalDocumentName)
            .build();
    }
}
