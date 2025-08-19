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
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    @DisplayName("Should create empty archive when source directory is empty")
    void shouldCreateEmptyArchiveWhenSourceDirectoryIsEmpty() throws Exception {
        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile());
        File archiveFile = new File(archivePath);

        assertTrue(archiveFile.exists(), "Archive should be created even for empty directory");
        assertTrue(archiveFile.length() > 0, "Archive should have minimal 7z structure");
    }

    @Test
    @DisplayName("Should handle multiple files and subdirectories")
    void shouldHandleMultipleFilesAndSubdirectories() throws Exception {
        Path subDir = Files.createDirectory(tempSourceDir.resolve("subdir"));
        Files.writeString(Files.createFile(tempSourceDir.resolve("file1.txt")), "Content 1");
        Files.writeString(Files.createFile(subDir.resolve("file2.txt")), "Content 2");
        Files.writeString(Files.createFile(subDir.resolve("file3.txt")), "Content 3");

        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile());
        File archiveFile = new File(archivePath);

        assertTrue(archiveFile.exists(), "Archive should be created");
        assertTrue(archiveFile.length() > 100, "Archive should contain multiple files");
    }

    @Test
    @DisplayName("Should throw exception when source directory does not exist")
    void shouldThrowExceptionWhenSourceDirectoryDoesNotExist() throws Exception {
        File nonExistentDir = new File("/non/existent/path");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> acroZipService.zip(nonExistentDir, tempExportDir.toFile()));

        assertTrue(exception.getMessage().contains("Source must be an existing directory"));
    }

    @Test
    @DisplayName("Should throw exception when export directory does not exist")
    void shouldThrowExceptionWhenExportDirectoryDoesNotExist() throws Exception {
        File nonExistentDir = new File("/non/existent/export");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> acroZipService.zip(tempSourceDir.toFile(), nonExistentDir));

        assertTrue(exception.getMessage().contains("Export folder must be an existing directory"));
    }

    @Test
    @DisplayName("Should handle files with special characters in names")
    void shouldHandleFilesWithSpecialCharactersInNames() throws Exception {
        Files.writeString(Files.createFile(tempSourceDir.resolve("file with spaces.txt")), "Content");
        Files.writeString(Files.createFile(tempSourceDir.resolve("file-with-dashes.txt")), "Content");

        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile());
        File archiveFile = new File(archivePath);

        assertTrue(archiveFile.exists(), "Archive should handle special characters in filenames");
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
