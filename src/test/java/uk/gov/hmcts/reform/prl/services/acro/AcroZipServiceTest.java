package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AcroZipService Tests")
class AcroZipServiceTest {
    private AcroZipService acroZipService;
    private Path tempSourceDir;
    private Path tempExportDir;

    @BeforeEach
    void setUp() throws Exception {
        acroZipService = new AcroZipService();
        tempSourceDir = Files.createTempDirectory("acrozip-src");
        tempExportDir = Files.createTempDirectory("acrozip-exp");
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.walk(tempSourceDir).map(Path::toFile).forEach(File::delete);
        Files.walk(tempExportDir).map(Path::toFile).forEach(File::delete);
    }

    @Test
    @DisplayName("Should create archive with valid 7zip format signature")
    void shouldCreateArchiveWithValid7ZipFormatSignature() throws Exception {
        Path file1 = Files.createFile(tempSourceDir.resolve("file1.txt"));
        Files.writeString(file1, "Test content");

        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile());
        File archiveFile = new File(archivePath);

        byte[] header = new byte[6];
        try (var fis = Files.newInputStream(archiveFile.toPath())) {
            int bytesRead = fis.read(header);
            assertEquals(6, bytesRead);
        }

        byte[] expectedSignature = {0x37, 0x7A, (byte)0xBC, (byte)0xAF, 0x27, 0x1C};
        assertArrayEquals(expectedSignature, header, "File should have 7zip format signature");
        assertTrue(archivePath.endsWith(".7z"), "Archive should have .7z extension");
    }

    @Test
    @DisplayName("Should create archive with correct PRL_ORDERS naming format")
    void shouldCreateArchiveWithCorrectPrlOrdersNamingFormat() throws Exception {
        Path file1 = Files.createFile(tempSourceDir.resolve("file1.txt"));
        Files.writeString(file1, "Test content");

        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile());
        File archiveFile = new File(archivePath);

        String fileName = archiveFile.getName();
        assertTrue(fileName.matches("PRL_ORDERS_\\d{8}_\\d{4}\\.7z"),
                   "Archive name should match format PRL_ORDERS_YYYYMMDD_HHMM.7z");
    }

    // For manual review only; comment out by default
    /*
    @Test
    @DisplayName("Manual test - creates archive in project root for inspection")
    void manualTest_CreatesArchiveInProjectRootForInspection() throws Exception {
        Path file1 = Files.createFile(tempSourceDir.resolve("file1.txt"));
        Files.writeString(file1, "Test content");

        File projectRoot = new File(System.getProperty("user.dir"));
        String archivePath = acroZipService.zip(tempSourceDir.toFile(), projectRoot);

        System.out.println("7zip archive saved to: " + archivePath);
    }
    */
}
