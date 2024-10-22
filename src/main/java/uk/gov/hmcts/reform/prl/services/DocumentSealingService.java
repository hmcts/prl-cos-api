package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND;
import static org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DUMMY;
import static uk.gov.hmcts.reform.prl.utils.ResourceReader.readBytes;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DocumentSealingService {

    private static final float POINTS_PER_INCH = 72;
    private static final float POINTS_PER_MM = 1 / (10 * 2.54f) * POINTS_PER_INCH;
    private static final long SEAL_HEIGHT = mm2pt(25);
    private static final long SEAL_WIDTH = mm2pt(25);
    private static final long MARGIN_TOP = mm2pt(10);
    private static final long MARGIN_RIGHT = mm2pt(19.0);
    private static final String USER_IMAGE_COURT_SEAL_BILINGUAL = "[userImage:familycourtseal-bilingual.png]";
    private static final String USER_IMAGE_COURT_SEAL = "[userImage:familycourtseal.png]";
    private static final String COURT_SEAL_BILINGUAL = "/familycourtseal-bilingual.png";
    private static final String COURT_SEAL = "/familycourtseal.png";

    private final DgsApiClient dgsApiClient;
    private final DocumentGenService documentGenService;
    private final AuthTokenGenerator authTokenGenerator;

    public Document sealDocument(Document document, CaseData caseData, String authorisation) {
        if (documentGenService.checkFileFormat(document.getDocumentFileName())) {
            document = documentGenService.convertToPdf(authorisation, document);
        }

        String s2sToken = authTokenGenerator.generate();
        byte[] downloadedPdf = documentGenService.getDocumentBytes(
            document.getDocumentUrl(),
            authorisation,
            s2sToken
        );

        byte[] seal = readBytes(getCourtSealImage(caseData.getCourtSeal()));
        byte[] sealedDocument = addSealToDocument(downloadedPdf, seal);

        Map<String, Object> tempCaseDetails = new HashMap<>();
        tempCaseDetails.put("fileName", sealedDocument);
        GeneratedDocumentInfo generatedDocumentInfo = dgsApiClient.convertDocToPdf(
            document.getDocumentFileName(),
            authorisation, GenerateDocumentRequest
                .builder().template(DUMMY).values(tempCaseDetails).build()
        );

        return Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentFileName(generatedDocumentInfo.getDocName())
            .build();

    }

    private byte[] addSealToDocument(byte[] binaries, byte[] seal) {
        try (final PDDocument document = PDDocument.load(binaries)) {
            final PDPage firstPage = document.getPage(0);
            final PDRectangle pageSize = firstPage.getTrimBox();

            try (PDPageContentStream pdfStream = new PDPageContentStream(document, firstPage, APPEND, true, true)) {
                final PDImageXObject courtSealImage = createFromByteArray(document, seal, null);
                pdfStream.drawImage(
                    courtSealImage,
                    pageSize.getUpperRightX() - (SEAL_WIDTH + MARGIN_RIGHT),
                    pageSize.getUpperRightY() - (SEAL_HEIGHT + MARGIN_TOP),
                    SEAL_WIDTH,
                    SEAL_HEIGHT
                );
            }

            return getBinary(document);
        } catch (IllegalStateException ise) {
            throw ise;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] getBinary(PDDocument document) throws IOException {
        try (final ByteArrayOutputStream outputBytes = new ByteArrayOutputStream()) {
            document.save(outputBytes);
            return outputBytes.toByteArray();
        }
    }

    private static long mm2pt(double mm) {
        return Math.round(POINTS_PER_MM * mm);
    }

    private static String getCourtSealImage(String seal) {
        String courtSeal = "";
        switch (seal) {
            case USER_IMAGE_COURT_SEAL_BILINGUAL:
                courtSeal = COURT_SEAL_BILINGUAL;
                break;
            case USER_IMAGE_COURT_SEAL:
                courtSeal = COURT_SEAL;
                break;
            default:
                break;
        }

        return courtSeal;
    }
}
