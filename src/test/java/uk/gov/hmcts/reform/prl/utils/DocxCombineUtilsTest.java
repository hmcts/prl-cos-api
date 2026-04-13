package uk.gov.hmcts.reform.prl.utils;

import com.deepoove.poi.xwpf.NiceXWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

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

    // Helper methods

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
}
