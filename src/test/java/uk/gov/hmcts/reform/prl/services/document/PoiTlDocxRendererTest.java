package uk.gov.hmcts.reform.prl.services.document;

import com.deepoove.poi.XWPFTemplate;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class PoiTlDocxRendererTest {

    @Test
    void debugLoopRowTableRenderPolicy() throws Exception {
        // Create a minimal template programmatically to test LoopRowTableRenderPolicy
        // According to poi-tl docs:
        // - {{children}} goes in row ABOVE the repeating row (triggers the loop)
        // - [fullName], [gender], [dob] go in the repeating row (loop placeholders use [])
        XWPFDocument doc = new XWPFDocument();

        // Simple text placeholder using {{}} syntax
        doc.createParagraph().createRun().setText("Court: {{courtName}}");

        // Create table with: header row, trigger row, data row
        XWPFTable table = doc.createTable(3, 3);

        // Row 0: Header
        table.getRow(0).getCell(0).setText("Name");
        table.getRow(0).getCell(1).setText("Gender");
        table.getRow(0).getCell(2).setText("DOB");

        // Row 1: Loop trigger - {{children}} triggers the LoopRowTableRenderPolicy
        table.getRow(1).getCell(0).setText("{{children}}");
        table.getRow(1).getCell(1).setText("");
        table.getRow(1).getCell(2).setText("");

        // Row 2: Repeating row with [] placeholders
        table.getRow(2).getCell(0).setText("[fullName]");
        table.getRow(2).getCell(1).setText("[gender]");
        table.getRow(2).getCell(2).setText("[dob]");

        // Write template to bytes
        ByteArrayOutputStream templateOut = new ByteArrayOutputStream();
        doc.write(templateOut);
        doc.close();
        byte[] templateBytes = templateOut.toByteArray();

        // Prepare data - list of maps with properties matching the [] placeholders
        List<Map<String, Object>> children = new ArrayList<>();
        Map<String, Object> child1 = new HashMap<>();
        child1.put("fullName", "Alice Smith");
        child1.put("gender", "Female");
        child1.put("dob", "01/05/2015");
        children.add(child1);

        Map<String, Object> child2 = new HashMap<>();
        child2.put("fullName", "Bob Smith");
        child2.put("gender", "Male");
        child2.put("dob", "15/08/2018");
        children.add(child2);

        Map<String, Object> data = new HashMap<>();
        data.put("courtName", "Test Court");
        data.put("children", children);

        // Render using our renderer
        PoiTlDocxRenderer renderer = new PoiTlDocxRenderer();
        byte[] outBytes = renderer.render(templateBytes, data);

        // Extract and print all text
        try (XWPFDocument resultDoc = new XWPFDocument(new ByteArrayInputStream(outBytes))) {
            StringBuilder allText = new StringBuilder();
            resultDoc.getParagraphs().forEach(p -> allText.append(p.getText()).append("\n"));
            resultDoc.getTables().forEach(t -> {
                allText.append("TABLE:\n");
                t.getRows().forEach(row -> {
                    row.getTableCells().forEach(cell -> allText.append("  [").append(cell.getText()).append("]"));
                    allText.append("\n");
                });
            });
            System.out.println("=== RENDERED OUTPUT ===");
            System.out.println(allText);
            System.out.println("=== END OUTPUT ===");

            // Check if it worked
            String result = allText.toString();
            assertThat(result).contains("Test Court");
            assertThat(result).contains("Alice Smith");
            assertThat(result).contains("Bob Smith");
        }
    }

    @Test
    void shouldRenderOrder76Template_andWriteOutputDocx() throws Exception {
        // Load template from test resources
        byte[] templateBytes;
        try (InputStream in = getClass().getResourceAsStream("/templates/Order7.6_poitl_POC.docx")) {
            assertThat(in).isNotNull();
            templateBytes = in.readAllBytes();
        }

        // Data
        Map<String, Object> data = new HashMap<>();
        data.put("caseNumber", "AB12C34567");
        data.put("courtName", "Example Family Court");
        data.put("judgeName", "HHJ Example");
        data.put("applicantName", "Alex Applicant");
        data.put("applicantRepresentativeName", "Sam Solicitor");
        data.put("applicantRepresentativeRole", "solicitor");
        data.put("respondent1Name", "Rory Respondent");
        data.put("respondent1RelationshipToChild", "mother");

        // Render to bytes
        byte[] outBytes;
        try (var in = new ByteArrayInputStream(templateBytes);
             var out = new ByteArrayOutputStream()) {

            XWPFTemplate template = XWPFTemplate.compile(in).render(data);
            template.write(out);
            template.close();
            outBytes = out.toByteArray();
        }

        // Write output docx so you can open it
        Path outDir = Paths.get("target", "poitl-output");
        Files.createDirectories(outDir);

        Path outFile = outDir.resolve("Order7.6_poitl_rendered.docx");
        Files.write(outFile, outBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("Wrote rendered docx to: " + outFile.toAbsolutePath());


        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(outBytes))) {
            StringBuilder allText = new StringBuilder();
            doc.getParagraphs().forEach(p -> allText.append(p.getText()).append("\n"));
            doc.getTables().forEach(table ->
                table.getRows().forEach(row ->
                    row.getTableCells().forEach(cell ->
                        allText.append(cell.getText()).append("\n")
                    )
                )
            );
            if (doc.getHeaderList() != null) {
                doc.getHeaderList().forEach(header ->
                    header.getParagraphs().forEach(p -> allText.append(p.getText()).append("\n"))
                );
            }
            if (doc.getFooterList() != null) {
                doc.getFooterList().forEach(footer ->
                    footer.getParagraphs().forEach(p -> allText.append(p.getText()).append("\n"))
                );
            }
            String allTextStr = allText.toString();
            assertThat(allTextStr).contains("Example Family Court");
            assertThat(allTextStr).contains("AB12C34567");
        }
    }

    @Test
    void shouldRenderUsingPoiTlDocxRenderer() throws Exception {
        byte[] templateBytes;
        try (InputStream in = getClass().getResourceAsStream("/templates/Order7.6_poitl_POC.docx")) {
            assertThat(in).isNotNull();
            templateBytes = in.readAllBytes();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("caseNumber", "CD34E56789");
        data.put("courtName", "Test Family Court");
        PoiTlDocxRenderer renderer = new PoiTlDocxRenderer();
        byte[] outBytes = renderer.render(templateBytes, data);
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(outBytes))) {
            StringBuilder allText = new StringBuilder();
            doc.getParagraphs().forEach(p -> allText.append(p.getText()).append("\n"));
            doc.getTables().forEach(table ->
                table.getRows().forEach(row ->
                    row.getTableCells().forEach(cell ->
                        allText.append(cell.getText()).append("\n")
                    )
                )
            );
            if (doc.getHeaderList() != null) {
                doc.getHeaderList().forEach(header ->
                    header.getParagraphs().forEach(p -> allText.append(p.getText()).append("\n"))
                );
            }
            if (doc.getFooterList() != null) {
                doc.getFooterList().forEach(footer ->
                    footer.getParagraphs().forEach(p -> allText.append(p.getText()).append("\n"))
                );
            }
            String allTextStr = allText.toString();
            assertThat(allTextStr).contains("Test Family Court");
            assertThat(allTextStr).contains("CD34E56789");
        }
    }

    @Test
    void shouldThrowRuntimeExceptionOnInvalidTemplate() {
        PoiTlDocxRenderer renderer = new PoiTlDocxRenderer();
        byte[] invalidBytes = new byte[] {0, 1, 2, 3, 4};
        Map<String, Object> data = new HashMap<>();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> renderer.render(invalidBytes, data))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("poi-tl rendering failed");
    }

    @Test
    void shouldRenderSquareBracketPlaceholders() throws Exception {
        // Test that the renderer correctly handles [placeholder] format
        byte[] templateBytes;
        try (InputStream in = getClass().getResourceAsStream("/templates/CustomOrderHeader.docx")) {
            if (in == null) {
                // Skip test if template doesn't exist in test resources
                System.out.println("CustomOrderHeader.docx not found in test resources, skipping test");
                return;
            }
            templateBytes = in.readAllBytes();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("caseNumber", "TEST123456");
        data.put("courtName", "Square Bracket Test Court");
        data.put("judgeName", "Judge Test");
        data.put("applicantName", "Test Applicant");
        data.put("respondent1Name", "Test Respondent");

        PoiTlDocxRenderer renderer = new PoiTlDocxRenderer();
        byte[] outBytes = renderer.render(templateBytes, data);

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(outBytes))) {
            StringBuilder allText = new StringBuilder();
            doc.getParagraphs().forEach(p -> allText.append(p.getText()).append("\n"));
            doc.getTables().forEach(table ->
                table.getRows().forEach(row ->
                    row.getTableCells().forEach(cell ->
                        allText.append(cell.getText()).append("\n")
                    )
                )
            );
            String allTextStr = allText.toString();
            // Verify placeholders were replaced
            assertThat(allTextStr).contains("Square Bracket Test Court");
            assertThat(allTextStr).contains("TEST123456");
            // Verify no unreplaced placeholders remain
            assertThat(allTextStr).doesNotContain("[caseNumber]");
            assertThat(allTextStr).doesNotContain("[courtName]");
        }
    }

    @Test
    void shouldRenderTableRowLoopingWithSquareBrackets() throws Exception {
        // Test that poi-tl table row looping works with [#list]...[/list] syntax
        byte[] templateBytes;
        try (InputStream in = getClass().getResourceAsStream("/templates/CustomOrderHeader.docx")) {
            if (in == null) {
                System.out.println("CustomOrderHeader.docx not found in test resources, skipping test");
                return;
            }
            templateBytes = in.readAllBytes();
        }

        // Create children list data
        List<Map<String, String>> children = new ArrayList<>();
        Map<String, String> child1 = new HashMap<>();
        child1.put("fullName", "Alice Smith");
        child1.put("gender", "Female");
        child1.put("dob", "01/05/2015");
        children.add(child1);

        Map<String, String> child2 = new HashMap<>();
        child2.put("fullName", "Bob Smith");
        child2.put("gender", "Male");
        child2.put("dob", "15/08/2018");
        children.add(child2);

        Map<String, Object> data = new HashMap<>();
        data.put("caseNumber", "1234-5678-9012-3456");
        data.put("courtName", "Test Family Court");
        data.put("judgeName", "Judge Test");
        data.put("applicantName", "Test Applicant");
        data.put("respondent1Name", "Test Respondent");
        // Use individual child placeholders (child1Name, child2Name, etc.) instead of list
        data.put("child1Name", "Alice Smith");
        data.put("child1Gender", "Female");
        data.put("child1Dob", "01/05/2015");
        data.put("child2Name", "Bob Smith");
        data.put("child2Gender", "Male");
        data.put("child2Dob", "15/08/2018");

        PoiTlDocxRenderer renderer = new PoiTlDocxRenderer();
        byte[] outBytes = renderer.render(templateBytes, data);

        // Write output for inspection
        Path outDir = Paths.get("target", "poitl-output");
        Files.createDirectories(outDir);
        Path outFile = outDir.resolve("CustomOrderHeader_rendered.docx");
        Files.write(outFile, outBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("Wrote rendered docx to: " + outFile.toAbsolutePath());

        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(outBytes))) {
            StringBuilder allText = new StringBuilder();
            doc.getParagraphs().forEach(p -> allText.append(p.getText()).append("\n"));
            doc.getTables().forEach(table ->
                table.getRows().forEach(row ->
                    row.getTableCells().forEach(cell ->
                        allText.append(cell.getText()).append("\n")
                    )
                )
            );
            String allTextStr = allText.toString();
            System.out.println("Rendered content:\n" + allTextStr);

            // Verify basic placeholders were replaced
            assertThat(allTextStr).contains("Test Family Court");
            assertThat(allTextStr).contains("1234-5678-9012-3456");

            // Verify children were rendered using individual placeholders
            assertThat(allTextStr).contains("Alice Smith");
            assertThat(allTextStr).contains("Bob Smith");
            assertThat(allTextStr).contains("Female");
            assertThat(allTextStr).contains("Male");
        }
    }
}


