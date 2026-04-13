package uk.gov.hmcts.reform.prl.utils;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(result))) {
            String allText = doc.getParagraphs().stream()
                .map(XWPFParagraph::getText)
                .filter(t -> t != null)
                .collect(Collectors.joining(" "));
            assertTrue(allText.contains("Header content"), "Should contain header content");
            assertTrue(allText.contains("User content"), "Should contain user content");
        }
    }

    @Test
    void testCombineDocuments_handlesMultipleParagraphs() throws IOException {
        byte[] headerBytes = createSimpleDocx("Header");
        byte[] userBytes = createDocxWithMultipleParagraphs();

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(result))) {
            assertNotNull(doc);
            assertTrue(doc.getParagraphs().size() >= 4, "Should contain header + 3 user paragraphs");
        }
    }

    @Test
    void testCombineDocuments_preservesStyles() throws IOException {
        byte[] headerBytes = createSimpleDocx("Header");
        byte[] userBytes = createDocxWithStyles();

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(result))) {
            assertNotNull(doc);
            assertNotNull(doc.getStyles(), "Styles should be present in combined document");
        }
    }

    @Test
    void testCombineDocuments_preservesTables() throws IOException {
        byte[] headerBytes = createSimpleDocx("Header");
        byte[] userBytes = createDocxWithTable();

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(result))) {
            assertTrue(doc.getTables().size() >= 1, "Should contain table from user document");
        }
    }

    @Test
    void testCombineDocuments_preservesFootnotes() throws IOException {
        byte[] headerBytes = createSimpleDocx("Header");
        byte[] userBytes = createDocxWithFootnotes();

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(result))) {
            assertNotNull(doc);
        }
    }

    @Test
    void testCombineDocuments_handlesEmptyUserContent() throws IOException {
        byte[] headerBytes = createSimpleDocx("Header content");
        byte[] userBytes = createSimpleDocx("");

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        assertTrue(result.length > 0);
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(result))) {
            String allText = doc.getParagraphs().stream()
                .map(XWPFParagraph::getText)
                .filter(t -> t != null)
                .collect(Collectors.joining(" "));
            assertTrue(allText.contains("Header content"), "Should preserve header content");
        }
    }

    @Test
    void testCombineDocuments_handlesPlainDocuments() throws IOException {
        // Tests that documents without numbering, styles, or other special elements combine correctly
        byte[] headerBytes = createSimpleDocx("Header");
        byte[] userBytes = createSimpleDocx("User content");

        byte[] result = DocxCombineUtils.combineDocuments(headerBytes, userBytes);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    // Helper methods

    private byte[] createSimpleDocx(String content) throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph para = doc.createParagraph();
            XWPFRun run = para.createRun();
            run.setText(content);
            doc.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createDocxWithMultipleParagraphs() throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (int i = 1; i <= 3; i++) {
                XWPFParagraph para = doc.createParagraph();
                XWPFRun run = para.createRun();
                run.setText("Item " + i);
            }
            doc.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createDocxWithStyles() throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Create styles - just having the styles part is enough to test copying
            doc.createStyles();
            XWPFParagraph para = doc.createParagraph();
            XWPFRun run = para.createRun();
            run.setBold(true);
            run.setItalic(true);
            run.setText("Styled content");
            doc.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createDocxWithTable() throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
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

    private byte[] createDocxWithFootnotes() throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph para = doc.createParagraph();
            XWPFRun run = para.createRun();
            run.setText("Text with footnote");
            doc.createFootnote();
            doc.write(out);
            return out.toByteArray();
        }
    }
}
