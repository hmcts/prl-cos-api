package uk.gov.hmcts.reform.prl.services.sealaudit;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.services.sealaudit.SealDetectionService.SealStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SealDetectionServiceTest {

    private SealDetectionService sealDetectionService;

    @BeforeEach
    void setUp() {
        sealDetectionService = new SealDetectionService();
    }

    @Test
    void shouldReturnErrorForNullBytes() {
        SealStatus status = sealDetectionService.detectSeal(null);
        assertEquals(SealStatus.ERROR, status);
    }

    @Test
    void shouldReturnErrorForEmptyBytes() {
        SealStatus status = sealDetectionService.detectSeal(new byte[0]);
        assertEquals(SealStatus.ERROR, status);
    }

    @Test
    void shouldReturnMissingForBlankPdf() throws IOException {
        byte[] blankPdf = createBlankPdf();
        SealStatus status = sealDetectionService.detectSeal(blankPdf);
        assertEquals(SealStatus.MISSING, status);
    }

    @Test
    void shouldReturnMissingForOrderWithoutSeal() throws IOException {
        try (InputStream pdfStream = getClass().getResourceAsStream("/documents/testOrderWithoutSeal.pdf")) {
            assertNotNull(pdfStream, "Test PDF not found - run TestPdfGenerator.main() to create it");
            byte[] pdfBytes = pdfStream.readAllBytes();
            SealStatus status = sealDetectionService.detectSeal(pdfBytes);
            assertEquals(SealStatus.MISSING, status);
        }
    }

    @Test
    void shouldReturnPresentForPdfWithSeal() throws IOException {
        byte[] sealedPdf = createPdfWithSeal();
        SealStatus status = sealDetectionService.detectSeal(sealedPdf);
        assertEquals(SealStatus.PRESENT, status);
    }

    @Test
    void shouldReturnPresentForRealOrderWithBilingualSeal() throws IOException {
        try (InputStream pdfStream = getClass().getResourceAsStream("/documents/exampleOrderWithSeal.pdf")) {
            assertNotNull(pdfStream, "Test PDF not found");
            byte[] pdfBytes = pdfStream.readAllBytes();
            SealStatus status = sealDetectionService.detectSeal(pdfBytes);
            assertEquals(SealStatus.PRESENT, status);
        }
    }

    @Test
    void shouldReturnPresentForRealOrderWithEnglishSeal() throws IOException {
        try (InputStream pdfStream = getClass().getResourceAsStream("/documents/exampleOrderWithEnglishSeal.pdf")) {
            assertNotNull(pdfStream, "Test PDF not found");
            byte[] pdfBytes = pdfStream.readAllBytes();
            SealStatus status = sealDetectionService.detectSeal(pdfBytes);
            assertEquals(SealStatus.PRESENT, status);
        }
    }

    @Test
    void shouldReturnPresentForCreateOrderC21() throws IOException {
        try (InputStream pdfStream = getClass().getResourceAsStream("/documents/exampleCreateOrder_Blank_Order_Directions_C21.pdf")) {
            assertNotNull(pdfStream, "Test PDF not found");
            byte[] pdfBytes = pdfStream.readAllBytes();
            SealStatus status = sealDetectionService.detectSeal(pdfBytes);
            assertEquals(SealStatus.PRESENT, status);
        }
    }

    @Test
    void shouldReturnPresentForCreateOrderC43A() throws IOException {
        try (InputStream pdfStream = getClass().getResourceAsStream("/documents/exampleCreateOrder_Special_Guardianship_Order_C43A.pdf")) {
            assertNotNull(pdfStream, "Test PDF not found");
            byte[] pdfBytes = pdfStream.readAllBytes();
            SealStatus status = sealDetectionService.detectSeal(pdfBytes);
            assertEquals(SealStatus.PRESENT, status);
        }
    }

    @Test
    void shouldReturnPresentForSamplePdf() throws IOException {
        try (InputStream pdfStream = getClass().getResourceAsStream("/documents/sample.pdf")) {
            assertNotNull(pdfStream, "Test PDF not found");
            byte[] pdfBytes = pdfStream.readAllBytes();
            SealStatus status = sealDetectionService.detectSeal(pdfBytes);
            assertEquals(SealStatus.PRESENT, status);
        }
    }

    private byte[] createBlankPdf() throws IOException {
        try (PDDocument document = new PDDocument()) {
            document.addPage(new PDPage(PDRectangle.A4));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] createPdfWithSeal() throws IOException {
        try (PDDocument document = new PDDocument();
             InputStream sealStream = getClass().getResourceAsStream("/familycourtseal.png")) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            if (sealStream != null) {
                byte[] sealBytes = sealStream.readAllBytes();
                PDImageXObject sealImage = PDImageXObject.createFromByteArray(document, sealBytes, "seal");

                float sealWidth = 71;
                float sealHeight = 71;
                float marginTop = 28;
                float marginRight = 54;

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.drawImage(
                        sealImage,
                        page.getTrimBox().getUpperRightX() - (sealWidth + marginRight),
                        page.getTrimBox().getUpperRightY() - (sealHeight + marginTop),
                        sealWidth,
                        sealHeight
                    );
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}
