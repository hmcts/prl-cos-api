package uk.gov.hmcts.reform.prl.services.document;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.jupiter.api.Disabled;
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
    void testLoopRowTableRenderPolicy_simple() throws Exception {
        // Create a minimal template with a table containing the loop placeholder
        // With onSameLine=true: {{children}} marker and [fieldName] placeholders are on same row
        XWPFDocument templateDoc = new XWPFDocument();

        // Create a 2-row, 3-column table
        // Row 0: Header
        // Row 1: Template row with {{children}} marker + [fieldName] placeholders
        XWPFTable table = templateDoc.createTable(2, 3);

        // Header row
        XWPFTableRow headerRow = table.getRow(0);
        headerRow.getCell(0).setText("Name");
        headerRow.getCell(1).setText("Gender");
        headerRow.getCell(2).setText("DOB");

        // Template row - {{children}} triggers loop, [fieldName] are field placeholders
        XWPFTableRow templateRow = table.getRow(1);
        templateRow.getCell(0).setText("{{children}}[fullName]");
        templateRow.getCell(1).setText("[gender]");
        templateRow.getCell(2).setText("[dob]");

        // Convert to bytes
        byte[] templateBytes;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            templateDoc.write(out);
            templateBytes = out.toByteArray();
        }
        templateDoc.close();

        // Configure poi-tl with LoopRowTableRenderPolicy (onSameLine=true)
        // Use DEFAULT grammar {{}} for main tags, [fieldName] is built-in for loop row fields
        LoopRowTableRenderPolicy loopPolicy = new LoopRowTableRenderPolicy(true);
        Configure config = Configure.builder()
            .bind("children", loopPolicy)
            .build();

        // Prepare data

        Map<String, String> child1 = new HashMap<>();
        child1.put("fullName", "Alice Smith");
        child1.put("gender", "Female");
        child1.put("dob", "01/05/2015");
        List<Map<String, String>> childrenList = new ArrayList<>();
        childrenList.add(child1);

        Map<String, String> child2 = new HashMap<>();
        child2.put("fullName", "Bob Smith");
        child2.put("gender", "Male");
        child2.put("dob", "15/08/2018");
        childrenList.add(child2);
        Map<String, Object> data = new HashMap<>();
        data.put("children", childrenList);

        // Render
        byte[] outputBytes;
        try (ByteArrayInputStream in = new ByteArrayInputStream(templateBytes);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFTemplate template = XWPFTemplate.compile(in, config).render(data);
            template.write(out);
            template.close();
            outputBytes = out.toByteArray();
        }

        // Verify output
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(outputBytes))) {
            StringBuilder allText = new StringBuilder();
            doc.getTables().forEach(tbl ->
                tbl.getRows().forEach(row ->
                    row.getTableCells().forEach(cell ->
                        allText.append(cell.getText()).append(" | ")
                    )
                )
            );
            String content = allText.toString();
            System.out.println("Rendered table content: " + content);

            // Should contain children data
            assertThat(content).contains("Alice Smith");
            assertThat(content).contains("Bob Smith");

            // Table should have 3 rows: header + 2 children (template row duplicated)
            assertThat(doc.getTables()).hasSize(1);
            assertThat(doc.getTables().get(0).getRows()).hasSize(3);
            System.out.println("Table rows: " + doc.getTables().get(0).getRows().size());
        }
    }

    @Test
    void debugLoopRowTableRenderPolicy() throws Exception {
        // Create a minimal template programmatically to test placeholder rendering
        // PoiTlDocxRenderer uses default {{placeholder}} syntax
        XWPFDocument doc = new XWPFDocument();

        // Simple text placeholder using {{}} syntax
        doc.createParagraph().createRun().setText("Court: {{courtName}}");
        doc.createParagraph().createRun().setText("Judge: {{judgeName}}");

        // Create table with header row and data row with placeholders
        XWPFTable table = doc.createTable(2, 3);

        // Row 0: Header
        table.getRow(0).getCell(0).setText("Name");
        table.getRow(0).getCell(1).setText("Gender");
        table.getRow(0).getCell(2).setText("DOB");

        // Row 1: Data row with placeholders (using {{}} for non-loop tables)
        table.getRow(1).getCell(0).setText("{{childName}}");
        table.getRow(1).getCell(1).setText("{{childGender}}");
        table.getRow(1).getCell(2).setText("{{childDob}}");

        // Write template to bytes
        ByteArrayOutputStream templateOut = new ByteArrayOutputStream();
        doc.write(templateOut);
        doc.close();
        byte[] templateBytes = templateOut.toByteArray();

        // Prepare data - keys must match template placeholders
        Map<String, Object> data = new HashMap<>();
        data.put("courtName", "Test Court");
        data.put("judgeName", "HHJ Smith");
        data.put("childName", "Alice Smith");
        data.put("childGender", "Female");
        data.put("childDob", "01/05/2015");

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

            // Check if placeholders were replaced
            String result = allText.toString();
            assertThat(result).contains("Test Court");
            assertThat(result).contains("HHJ Smith");
            assertThat(result).contains("Alice Smith");
            assertThat(result).contains("Female");
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
        // Create a template programmatically with {{placeholder}} syntax
        XWPFDocument doc = new XWPFDocument();
        doc.createParagraph().createRun().setText("In the Family Court sitting at {{courtName}}");
        doc.createParagraph().createRun().setText("Case No: {{caseNumber}}");
        doc.createParagraph().createRun().setText("Before {{judgeName}}");
        doc.createParagraph().createRun().setText("Applicant: {{applicantName}}");
        doc.createParagraph().createRun().setText("Respondent: {{respondent1Name}}");

        ByteArrayOutputStream templateOut = new ByteArrayOutputStream();
        doc.write(templateOut);
        doc.close();
        byte[] templateBytes = templateOut.toByteArray();

        Map<String, Object> data = new HashMap<>();
        data.put("caseNumber", "CD34E56789");
        data.put("courtName", "Test Family Court");
        data.put("judgeName", "HHJ Richardson");
        data.put("applicantName", "John Smith");
        data.put("respondent1Name", "Jane Smith");

        PoiTlDocxRenderer renderer = new PoiTlDocxRenderer();
        byte[] outBytes = renderer.render(templateBytes, data);

        try (XWPFDocument resultDoc = new XWPFDocument(new ByteArrayInputStream(outBytes))) {
            StringBuilder allText = new StringBuilder();
            resultDoc.getParagraphs().forEach(p -> allText.append(p.getText()).append("\n"));
            resultDoc.getTables().forEach(table ->
                table.getRows().forEach(row ->
                    row.getTableCells().forEach(cell ->
                        allText.append(cell.getText()).append("\n")
                    )
                )
            );
            String allTextStr = allText.toString();
            assertThat(allTextStr).contains("Test Family Court");
            assertThat(allTextStr).contains("CD34E56789");
            assertThat(allTextStr).contains("HHJ Richardson");
            assertThat(allTextStr).contains("John Smith");
            assertThat(allTextStr).contains("Jane Smith");
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

    @Disabled("Mem usage test is off")
    @Test
    void memoryUsageSanityCheck() throws Exception {
        // Load the actual CustomOrderHeader template
        byte[] templateBytes;
        try (InputStream in = getClass().getResourceAsStream("/templates/CustomOrderHeader.docx")) {
            assertThat(in).isNotNull();
            templateBytes = in.readAllBytes();
        }

        // Use children list for LoopRowTableRenderPolicy
        Map<String, Object> data = new HashMap<>();
        data.put("caseNumber", "AB12C34567");
        data.put("courtName", "Example Family Court");
        data.put("judgeName", "HHJ Example");
        data.put("applicantName", "Alex Applicant");
        data.put("respondent1Name", "Rory Respondent");
        data.put("orderName", "Child Arrangements Order");

        // Children list for LoopRowTableRenderPolicy
        Map<String, String> child1 = new HashMap<>();
        child1.put("fullName", "Alice Example");
        child1.put("gender", "Female");
        child1.put("dob", "01/05/2015");

        Map<String, String> child2 = new HashMap<>();
        child2.put("fullName", "Bob Example");
        child2.put("gender", "Male");
        child2.put("dob", "15/08/2018");

        List<Map<String, String>> childrenList = new ArrayList<>();
        childrenList.add(child1);
        childrenList.add(child2);

        data.put("children", childrenList);

        PoiTlDocxRenderer renderer = new PoiTlDocxRenderer();
        final Runtime runtime = Runtime.getRuntime();
        final int totalRenders = 100;
        final int concurrentThreads = 100;

        // Warm up - run a few times to let JIT compile
        for (int i = 0; i < 5; i++) {
            renderer.render(templateBytes, data);
        }

        // Force GC and measure baseline
        System.gc();
        Thread.sleep(100);
        long baselineMemory = runtime.totalMemory() - runtime.freeMemory();

        // Track peak memory during execution
        java.util.concurrent.atomic.AtomicLong peakMemory = new java.util.concurrent.atomic.AtomicLong(baselineMemory);
        java.util.concurrent.atomic.AtomicInteger failureCount = new java.util.concurrent.atomic.AtomicInteger(0);

        // Run renders in parallel using ExecutorService
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(concurrentThreads);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(totalRenders);

        final long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalRenders; i++) {
            executor.submit(() -> {
                try {
                    byte[] result = renderer.render(templateBytes, data);
                    if (result == null || result.length == 0) {
                        failureCount.incrementAndGet();
                    }
                    // Sample memory periodically
                    long currentMemory = runtime.totalMemory() - runtime.freeMemory();
                    peakMemory.updateAndGet(prev -> Math.max(prev, currentMemory));
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all tasks to complete
        latch.await(60, java.util.concurrent.TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);

        long endTime = System.currentTimeMillis();

        // Force GC and measure final memory
        System.gc();
        Thread.sleep(100);
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();

        long peakMemoryMb = peakMemory.get() / 1024 / 1024;
        long memoryDeltaKb = (finalMemory - baselineMemory) / 1024;
        long totalTimeMs = endTime - startTime;

        System.out.println("=== POI-TL PARALLEL MEMORY CHECK ===");
        System.out.println("Total renders: " + totalRenders);
        System.out.println("Concurrent threads: " + concurrentThreads);
        System.out.println("Template size: " + templateBytes.length / 1024 + " KB");
        System.out.println("Baseline memory: " + baselineMemory / 1024 / 1024 + " MB");
        System.out.println("Peak memory during execution: " + peakMemoryMb + " MB");
        System.out.println("Final memory (after GC): " + finalMemory / 1024 / 1024 + " MB");
        System.out.println("Memory delta (final - baseline): " + memoryDeltaKb + " KB");
        System.out.println("Total time: " + totalTimeMs + " ms");
        System.out.println("Throughput: " + (totalRenders * 1000 / totalTimeMs) + " renders/sec");
        System.out.println("Failures: " + failureCount.get());
        System.out.println("=====================================");

        // Sanity assertions
        assertThat(failureCount.get())
            .as("All renders should succeed")
            .isZero();

        assertThat(peakMemoryMb)
            .as("Peak memory should stay reasonable under concurrent load")
            .isLessThan(750); // 750 MB max peak for 100 concurrent threads

        assertThat(memoryDeltaKb)
            .as("Memory should not leak after concurrent execution")
            .isLessThan(50 * 1024); // 50 MB max retained growth
    }

    @Test
    void shouldRenderMustachePlaceholders() throws Exception {
        // Test that the renderer correctly handles {{placeholder}} format
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
        data.put("courtName", "Mustache Test Court");
        data.put("judgeName", "Judge Test");
        data.put("applicantName", "Test Applicant");
        data.put("respondent1Name", "Test Respondent");
        data.put("orderDate", "01/01/2025");
        data.put("applicantRepresentativeClause", "represented by Test Solicitor");
        data.put("respondent1RelationshipToChild", "Father");
        data.put("respondent1RepresentativeName", "");
        data.put("orderName", "Test Order");

        // Children list for LoopRowTableRenderPolicy
        Map<String, String> child = new HashMap<>();
        child.put("fullName", "Test Child");
        child.put("gender", "Female");
        child.put("dob", "01/01/2020");
        List<Map<String, String>> childrenList = new ArrayList<>();
        childrenList.add(child);
        data.put("children", childrenList);

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
            assertThat(allTextStr).contains("Mustache Test Court");
            assertThat(allTextStr).contains("TEST123456");
            // Verify no unreplaced placeholders remain
            assertThat(allTextStr).doesNotContain("{{caseNumber}}");
            assertThat(allTextStr).doesNotContain("{{courtName}}");
        }
    }

    @Test
    void shouldRenderTableRowLoopingWithChildren() throws Exception {
        // Test LoopRowTableRenderPolicy for dynamic children table rows
        // Uses actual template file - regenerate with GenerateCustomOrderHeaderTemplate if this fails
        byte[] templateBytes;
        try (InputStream in = getClass().getResourceAsStream("/templates/CustomOrderHeader.docx")) {
            if (in == null) {
                System.out.println("CustomOrderHeader.docx not found in test resources, skipping test");
                return;
            }
            templateBytes = in.readAllBytes();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("caseNumber", "1234-5678-9012-3456");
        data.put("courtName", "Test Family Court");
        data.put("judgeName", "Judge Test");
        data.put("applicantName", "Test Applicant");
        data.put("respondent1Name", "Test Respondent");
        data.put("orderDate", "15/01/2025");
        data.put("applicantRepresentativeClause", "");
        data.put("respondent1RelationshipToChild", "Mother");
        data.put("respondent1RepresentativeName", "Respondent Solicitor");
        data.put("orderName", "Child Arrangements Order");

        // Children list for LoopRowTableRenderPolicy - supports any number of children
        Map<String, String> child1 = new HashMap<>();
        child1.put("fullName", "Alice Smith");
        child1.put("gender", "Female");
        child1.put("dob", "01/05/2015");
        List<Map<String, String>> childrenList = new ArrayList<>();
        childrenList.add(child1);

        Map<String, String> child2 = new HashMap<>();
        child2.put("fullName", "Bob Smith");
        child2.put("gender", "Male");
        child2.put("dob", "15/08/2018");
        childrenList.add(child2);

        data.put("children", childrenList);

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
            doc.getTables().forEach(tbl ->
                tbl.getRows().forEach(row ->
                    row.getTableCells().forEach(cell ->
                        allText.append(cell.getText()).append(" ")
                    )
                )
            );
            String allTextStr = allText.toString();
            System.out.println("Rendered content:\n" + allTextStr);

            // Verify basic placeholders were replaced
            assertThat(allTextStr).contains("Test Family Court");
            assertThat(allTextStr).contains("1234-5678-9012-3456");

            // Verify children were rendered
            assertThat(allTextStr).contains("Alice Smith");
            assertThat(allTextStr).contains("Bob Smith");
            assertThat(allTextStr).contains("Female");
            assertThat(allTextStr).contains("Male");

            // Verify table has 3 rows (1 header + 2 children)
            assertThat(doc.getTables()).isNotEmpty();
            assertThat(doc.getTables().get(0).getRows()).hasSize(3);
        }
    }
}


