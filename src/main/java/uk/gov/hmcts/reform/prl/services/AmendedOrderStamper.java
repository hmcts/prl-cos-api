package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.util.Matrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.services.time.Time;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static uk.gov.hmcts.reform.prl.utils.DocumentsHelper.hasExtension;
import static uk.gov.hmcts.reform.prl.utils.ResourceReader.readBytes;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AmendedOrderStamper {

    private static final String PDF = "pdf";
    private static final float FONT_SIZE = 16f;
    private static final String FONT_LOCATION = "fonts/arial_bold.ttf";

    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final Time time;

    public byte[] amendDocument(Document original, String authorisation) throws IOException {
        if (!hasExtension(original, PDF)) {
            throw new UnsupportedOperationException(
                "Can only amend documents that are pdf, requested document was of type: "
                    + getExtension(original.getDocumentFileName())
            );
        }

        ResponseEntity<Resource> downloadedDocument = caseDocumentClient.getDocumentBinary(authorisation,
                                                                                           authTokenGenerator.generate(),
                                                                                           original.getDocumentUrl());

        Optional<Resource> documentResponse = ofNullable(downloadedDocument.getBody());
        if (documentResponse.isPresent()) {
            byte[] documentContents = documentResponse.get().getInputStream().readAllBytes();
            try {
                return amendDocument(documentContents);
            } catch (IOException e) {
                log.error("Could not add amendment text to {}", original, e);
                throw new UncheckedIOException(e);
            }
        } else {
            throw new IOException("Unable to amend PDF");
        }
    }

    private byte[] amendDocument(byte[] binaries) throws IOException {
        try (PDDocument document = PDDocument.load(binaries)) {
            final ByteArrayInputStream fontBinaries = new ByteArrayInputStream(readBytes(FONT_LOCATION));
            final PDFont font = PDType0Font.load(document, fontBinaries);

            final PDPage page = document.getPage(0);

            // build message
            final LocalDate now = time.now().toLocalDate();
            final String message = "Amended under the slip rule - " + now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH));

            // message properties
            final PDRectangle pageSize = page.getMediaBox();
            final float messageWidth = font.getStringWidth(message) * FONT_SIZE / 1000f;
            final float messageHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() * FONT_SIZE / 1000f;
            final float x = (pageSize.getWidth() - messageWidth) / 2f; // centred
            final float y = pageSize.getHeight() - messageHeight * 2f; // second line
            final Matrix messageLocation = Matrix.getTranslateInstance(x, y);

            final PDPageContentStream content = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);

            // hide previous amendment message
            content.setNonStrokingColor(Color.WHITE);
            content.addRect(x, y, messageWidth, messageHeight);
            content.fill();

            // write new amendment message
            content.beginText();
            content.setNonStrokingColor(Color.RED);
            content.setFont(font, FONT_SIZE);
            content.setTextMatrix(messageLocation);
            content.showText(message);
            content.endText();

            content.close();

            return save(document);
        }
    }

    private static byte[] save(PDDocument document) throws IOException {
        try (final ByteArrayOutputStream outputBytes = new ByteArrayOutputStream()) {
            document.save(outputBytes);
            return outputBytes.toByteArray();
        }
    }
}
