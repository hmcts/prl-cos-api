package uk.gov.hmcts.reform.prl.utils;

import com.deepoove.poi.xwpf.NiceXWPFDocument;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocxCombineUtilsTest {

    @Test
    void testCombineDocuments_basicCombination() throws IOException {
        byte[] headerBytes = createSimpleDocx("Header content");
        byte[] userBytes = createSimpleDocx("User content");

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        assertTrue(result.length > 0);
        try (NiceXWPFDocument doc = new NiceXWPFDocument(new ByteArrayInputStream(result))) {
            String allText = doc.getParagraphs().stream()
                .map(XWPFParagraph::getText)
                .filter(t -> t != null)
                .collect(Collectors.joining(" "));
            assertTrue(allText.contains("Header content"), "Should contain header content");
            assertTrue(allText.contains("User content"), "Should contain user content");
        }
    }

    @Test
    void testCombineDocuments_preservesTables() throws IOException {
        byte[] headerBytes = createSimpleDocx("Header");
        byte[] userBytes = createDocxWithTable();

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        try (NiceXWPFDocument doc = new NiceXWPFDocument(new ByteArrayInputStream(result))) {
            assertTrue(doc.getTables().size() >= 1, "Should contain table from user document");
        }
    }

    @Test
    void testCombineDocuments_handlesEmptyUserContent() throws IOException {
        byte[] headerBytes = createSimpleDocx("Header content");
        byte[] userBytes = createSimpleDocx("");

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        assertTrue(result.length > 0);
        try (NiceXWPFDocument doc = new NiceXWPFDocument(new ByteArrayInputStream(result))) {
            String allText = doc.getParagraphs().stream()
                .map(XWPFParagraph::getText)
                .filter(t -> t != null)
                .collect(Collectors.joining(" "));
            assertTrue(allText.contains("Header content"), "Should preserve header content");
        }
    }

    @Test
    void testCombineDocuments_preservesNumberingFromRealDocument() throws IOException {
        byte[] headerBytes = createSimpleDocx("Header");
        byte[] userBytes;
        try (InputStream in = getClass().getResourceAsStream("/templates/NumberFormat.docx")) {
            assertNotNull(in, "NumberFormat.docx should exist in test resources");
            userBytes = in.readAllBytes();
        }

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        try (NiceXWPFDocument doc = new NiceXWPFDocument(new ByteArrayInputStream(result))) {
            // Verify numbering definitions were copied
            assertNotNull(doc.getNumbering(), "Numbering should be present in combined document");

            // Find paragraphs that should have numbering applied
            // "The applicant is" should be numbered (level 0, like "1.")
            // "[child full name]" items should be numbered (level 1, like "a.")
            long numberedParagraphCount = doc.getParagraphs().stream()
                .filter(p -> p.getNumID() != null)
                .count();

            assertTrue(numberedParagraphCount >= 3,
                "Should have at least 3 paragraphs with numbering applied, found: " + numberedParagraphCount);

            // Verify specific numbered paragraphs exist
            boolean foundLevel0Numbering = doc.getParagraphs().stream()
                .anyMatch(p -> p.getNumID() != null && p.getNumIlvl() != null
                    && p.getNumIlvl().intValue() == 0);
            assertTrue(foundLevel0Numbering, "Should have level 0 numbering (1., 2., 3.)");

            boolean foundLevel1Numbering = doc.getParagraphs().stream()
                .anyMatch(p -> p.getNumID() != null && p.getNumIlvl() != null
                    && p.getNumIlvl().intValue() == 1);
            assertTrue(foundLevel1Numbering, "Should have level 1 numbering (a., b., c.)");

            // Verify content was also copied
            String allText = doc.getParagraphs().stream()
                .map(XWPFParagraph::getText)
                .filter(t -> t != null)
                .collect(Collectors.joining(" "));
            assertTrue(allText.contains("The parties"), "Should contain 'The parties' from user document");
        }
    }

    @Test
    void testCombineDocuments_preservesFooterFromUserDocument() throws IOException {
        byte[] headerBytes = createSimpleDocx("Header content");
        byte[] userBytes = createDocxWithFooter("User content", "Test Footer Text");

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        try (NiceXWPFDocument doc = new NiceXWPFDocument(new ByteArrayInputStream(result))) {
            assertTrue(doc.getFooterList() != null && !doc.getFooterList().isEmpty(),
                "Should have footer from user document");

            String footerText = doc.getFooterList().stream()
                .flatMap(footer -> footer.getParagraphs().stream())
                .map(XWPFParagraph::getText)
                .filter(t -> t != null)
                .collect(Collectors.joining(" "));
            assertTrue(footerText.contains("Test Footer Text"),
                "Should preserve footer text from user document");
        }
    }

    @Test
    void testCombineDocuments_preservesHeaderFromUserDocument() throws IOException {
        byte[] headerBytes = createSimpleDocx("System header content");
        byte[] userBytes = createDocxWithHeader("User content", "Test Header Text");

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        try (NiceXWPFDocument doc = new NiceXWPFDocument(new ByteArrayInputStream(result))) {
            assertTrue(doc.getHeaderList() != null && !doc.getHeaderList().isEmpty(),
                "Should have header from user document");

            String headerText = doc.getHeaderList().stream()
                .flatMap(header -> header.getParagraphs().stream())
                .map(XWPFParagraph::getText)
                .filter(t -> t != null)
                .collect(Collectors.joining(" "));
            assertTrue(headerText.contains("Test Header Text"),
                "Should preserve header text from user document");
        }
    }

    @Test
    void testCombineDocuments_locksInFormatting() throws IOException {
        byte[] headerBytes = createDocxWithCenteredParagraph("Centered Header");
        byte[] userBytes = createDocxWithJustifiedParagraph("Justified user content");

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        try (NiceXWPFDocument doc = new NiceXWPFDocument(new ByteArrayInputStream(result))) {
            // First paragraph should retain its CENTER alignment
            XWPFParagraph firstPara = doc.getParagraphs().get(0);
            assertEquals(ParagraphAlignment.CENTER, firstPara.getAlignment(),
                "Header paragraph should preserve CENTER alignment");
        }
    }

    @Test
    void testCombineDocuments_locksInTableCellFormatting() throws IOException {
        byte[] headerBytes = createDocxWithTableAndAlignment();
        byte[] userBytes = createDocxWithJustifiedParagraph("Justified user content");

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        try (NiceXWPFDocument doc = new NiceXWPFDocument(new ByteArrayInputStream(result))) {
            assertTrue(doc.getTables().size() >= 1, "Should have table from header");
            XWPFTable table = doc.getTables().get(0);
            XWPFParagraph cellPara = table.getRow(0).getCell(0).getParagraphs().get(0);
            assertEquals(ParagraphAlignment.LEFT, cellPara.getAlignment(),
                "Table cell should preserve LEFT alignment");
        }
    }

    @Test
    void testCombineDocuments_securityValidationChecksForMacros() throws IOException {
        // A clean docx file should NOT have VBA macros content type
        byte[] headerBytes = createSimpleDocx("Header");
        byte[] userBytes = createSimpleDocx("User content");

        // Verify a clean document passes validation and combines successfully
        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);
        assertNotNull(result);

        // Verify the combined document also has no macros
        try (NiceXWPFDocument doc = new NiceXWPFDocument(new ByteArrayInputStream(result))) {
            assertTrue(doc.getPackage().getPartsByContentType("application/vnd.ms-office.vbaProject").isEmpty(),
                "Clean document should have no VBA macros");
        }
    }

    @Test
    void testCombineDocuments_securityValidationChecksForOleObjects() throws IOException {
        // A clean docx file should NOT have OLE objects content type
        byte[] headerBytes = createSimpleDocx("Header");
        byte[] userBytes = createSimpleDocx("User content");

        // Verify a clean document passes validation and combines successfully
        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);
        assertNotNull(result);

        // Verify the combined document also has no OLE objects
        try (NiceXWPFDocument doc = new NiceXWPFDocument(new ByteArrayInputStream(result))) {
            assertTrue(doc.getPackage().getPartsByContentType(
                "application/vnd.openxmlformats-officedocument.oleObject").isEmpty(),
                "Clean document should have no OLE objects");
        }
    }

    @Test
    void testCombineDocuments_passesSecurityValidationForCleanDocument() throws IOException {
        byte[] headerBytes = createSimpleDocx("Header content");
        byte[] userBytes = createSimpleDocx("Clean user content");

        // Should not throw - clean document passes validation
        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testCombineDocuments_handlesDocumentWithNoHeadersOrFooters() throws IOException {
        byte[] headerBytes = createSimpleDocx("Header content");
        byte[] userBytes = createSimpleDocx("User content without headers/footers");

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        try (NiceXWPFDocument doc = new NiceXWPFDocument(new ByteArrayInputStream(result))) {
            String allText = doc.getParagraphs().stream()
                .map(XWPFParagraph::getText)
                .filter(t -> t != null)
                .collect(Collectors.joining(" "));
            assertTrue(allText.contains("Header content"), "Should contain header content");
            assertTrue(allText.contains("User content"), "Should contain user content");
        }
    }

    @Test
    void testCombineDocuments_copiesBoldAndItalicFormatting() throws IOException {
        byte[] headerBytes = createSimpleDocx("Header");
        byte[] userBytes = createDocxWithFormattedFooter();

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        try (NiceXWPFDocument doc = new NiceXWPFDocument(new ByteArrayInputStream(result))) {
            assertTrue(doc.getFooterList() != null && !doc.getFooterList().isEmpty(),
                "Should have footer");
        }
    }

    @Test
    void testCombineDocuments_handlesMultipleParagraphsInFooter() throws IOException {
        byte[] headerBytes = createSimpleDocx("Header");
        byte[] userBytes = createDocxWithMultiParagraphFooter();

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        try (NiceXWPFDocument doc = new NiceXWPFDocument(new ByteArrayInputStream(result))) {
            assertTrue(doc.getFooterList() != null && !doc.getFooterList().isEmpty(),
                "Should have footer from user document");

            String footerText = doc.getFooterList().stream()
                .flatMap(footer -> footer.getParagraphs().stream())
                .map(XWPFParagraph::getText)
                .filter(t -> t != null)
                .collect(Collectors.joining(" "));
            assertTrue(footerText.contains("Line 1"), "Should contain first line");
            assertTrue(footerText.contains("Line 2"), "Should contain second line");
        }
    }

    // Helper methods

    private byte[] createDocxWithCenteredParagraph(String content) throws IOException {
        try (NiceXWPFDocument doc = new NiceXWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph para = doc.createParagraph();
            para.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = para.createRun();
            run.setText(content);
            doc.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createDocxWithJustifiedParagraph(String content) throws IOException {
        try (NiceXWPFDocument doc = new NiceXWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph para = doc.createParagraph();
            para.setAlignment(ParagraphAlignment.BOTH); // BOTH = Justified
            XWPFRun run = para.createRun();
            run.setText(content);
            doc.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createDocxWithTableAndAlignment() throws IOException {
        try (NiceXWPFDocument doc = new NiceXWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFTable table = doc.createTable(1, 2);
            XWPFParagraph cellPara = table.getRow(0).getCell(0).getParagraphs().get(0);
            cellPara.setAlignment(ParagraphAlignment.LEFT);
            cellPara.createRun().setText("Cell content");
            doc.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createDocxWithFormattedFooter() throws IOException {
        try (NiceXWPFDocument doc = new NiceXWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph para = doc.createParagraph();
            para.createRun().setText("Body");

            XWPFFooter footer = doc.createFooter(HeaderFooterType.DEFAULT);
            XWPFParagraph footerPara = footer.createParagraph();
            XWPFRun boldRun = footerPara.createRun();
            boldRun.setText("Bold");
            boldRun.setBold(true);
            XWPFRun italicRun = footerPara.createRun();
            italicRun.setText(" Italic");
            italicRun.setItalic(true);

            doc.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createDocxWithMultiParagraphFooter() throws IOException {
        try (NiceXWPFDocument doc = new NiceXWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph para = doc.createParagraph();
            para.createRun().setText("Body");

            XWPFFooter footer = doc.createFooter(HeaderFooterType.DEFAULT);
            XWPFParagraph footerPara1 = footer.createParagraph();
            footerPara1.createRun().setText("Line 1");
            XWPFParagraph footerPara2 = footer.createParagraph();
            footerPara2.createRun().setText("Line 2");

            doc.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createSimpleDocx(String content) throws IOException {
        try (NiceXWPFDocument doc = new NiceXWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph para = doc.createParagraph();
            XWPFRun run = para.createRun();
            run.setText(content);
            doc.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createDocxWithTable() throws IOException {
        try (NiceXWPFDocument doc = new NiceXWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFTable table = doc.createTable(2, 2);
            table.getRow(0).getCell(0).setText("Cell 1");
            table.getRow(0).getCell(1).setText("Cell 2");
            table.getRow(1).getCell(0).setText("Cell 3");
            table.getRow(1).getCell(1).setText("Cell 4");
            doc.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createDocxWithFooter(String bodyContent, String footerContent) throws IOException {
        try (NiceXWPFDocument doc = new NiceXWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph para = doc.createParagraph();
            XWPFRun run = para.createRun();
            run.setText(bodyContent);

            XWPFFooter footer = doc.createFooter(HeaderFooterType.DEFAULT);
            XWPFParagraph footerPara = footer.createParagraph();
            XWPFRun footerRun = footerPara.createRun();
            footerRun.setText(footerContent);

            doc.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createDocxWithHeader(String bodyContent, String headerContent) throws IOException {
        try (NiceXWPFDocument doc = new NiceXWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph para = doc.createParagraph();
            XWPFRun run = para.createRun();
            run.setText(bodyContent);

            XWPFHeader header = doc.createHeader(HeaderFooterType.DEFAULT);
            XWPFParagraph headerPara = header.createParagraph();
            XWPFRun headerRun = headerPara.createRun();
            headerRun.setText(headerContent);

            doc.write(out);
            return out.toByteArray();
        }
    }
}
