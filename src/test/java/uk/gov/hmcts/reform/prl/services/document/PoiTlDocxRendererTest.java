package uk.gov.hmcts.reform.prl.services.document;

import com.deepoove.poi.XWPFTemplate;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PoiTlDocxRendererTest {

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
}


