package uk.gov.hmcts.reform.prl.services.acro;

import com.sendgrid.SendGrid;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("AcroZipService Tests")
class AcroZipServiceTest {

    private AcroZipService acroZipService;
    private Path tempSourceDir;
    private Path tempExportDir;

    @Mock
    private SendGrid sendGrid;

    @BeforeEach
    void setUp() throws Exception {
        acroZipService = new AcroZipService(sendGrid);
        tempSourceDir = Files.createTempDirectory("acrozip-src");
        tempExportDir = Files.createTempDirectory("acrozip-exp");
        ReflectionTestUtils.setField(acroZipService, "password", "TestPassword123");

        ReflectionTestUtils.setField(acroZipService, "sourceDirectory", tempSourceDir.toString());
        ReflectionTestUtils.setField(acroZipService, "outputDirectory", tempExportDir.toString());
        ReflectionTestUtils.setField(acroZipService, "password", "ReviewPassword123");

    }

    @Test
    @DisplayName("Should create archive with valid zip format signature")
    void shouldCreateArchiveWithValid7ZipFormatSignature() throws Exception {
        Path file1 = Files.createFile(tempSourceDir.resolve("file1.txt"));
        Files.writeString(file1, "Test content");

        String archivePath = acroZipService.zip();
        File archiveFile = new File(archivePath);

        byte[] header = new byte[4];
        try (var fis = Files.newInputStream(archiveFile.toPath())) {
            int bytesRead = fis.read(header);
            assertEquals(4, bytesRead);
        }

        byte[] expectedSignature = {0x50, 0x4B, 0x03, 0x04}; // ZIP file signature
        assertArrayEquals(expectedSignature, header, "File should have ZIP format signature");
        assertTrue(archivePath.endsWith(".zip"), "Archive should have .zip extension");
    }

    @Test
    @DisplayName("Should create archive with correct PRL_ORDERS naming format")
    void shouldCreateArchiveWithCorrectPrlOrdersNamingFormat() throws Exception {
        Path file1 = Files.createFile(tempSourceDir.resolve("file1.txt"));
        Files.writeString(file1, "Test content");

        String archivePath = acroZipService.zip();
        File archiveFile = new File(archivePath);

        String fileName = archiveFile.getName();
        assertTrue(
            fileName.matches("PRL_ORDERS_\\d{8}_\\d{4}\\.zip"),
            "Archive name should match format PRL_ORDERS_YYYYMMDD_HHMM.zip"
        );
    }

    @Test
    @DisplayName("Should handle multiple files and subdirectories")
    void shouldHandleMultipleFilesAndSubdirectories() throws Exception {
        Path subDir = Files.createDirectory(tempSourceDir.resolve("subdir"));
        Files.writeString(Files.createFile(tempSourceDir.resolve("file1.txt")), "Content 1");
        Files.writeString(Files.createFile(subDir.resolve("file2.txt")), "Content 2");
        Files.writeString(Files.createFile(subDir.resolve("file3.txt")), "Content 3");

        String archivePath = acroZipService.zip();
        File archiveFile = new File(archivePath);

        assertTrue(archiveFile.exists(), "Archive should be created");
        assertTrue(archiveFile.length() > 100, "Archive should contain multiple files");
    }

    @Test
    @DisplayName("Should throw exception when source directory does not exist")
    void shouldThrowExceptionWhenSourceDirectoryDoesNotExist() {
        ReflectionTestUtils.setField(acroZipService, "sourceDirectory", "/non/existent/path");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> acroZipService.zip()
        );

        assertTrue(exception.getMessage().contains("Source must be an existing directory"));
    }

    @Test
    @DisplayName("Should throw exception when export directory does not exist")
    void shouldThrowExceptionWhenExportDirectoryDoesNotExist() {
        ReflectionTestUtils.setField(acroZipService, "outputDirectory", "/non/existent/export");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> acroZipService.zip()
        );

        assertTrue(exception.getMessage().contains("Export folder must be an existing directory"));
    }

    @Test
    @DisplayName("Should handle files with special characters in names")
    void shouldHandleFilesWithSpecialCharactersInNames() throws Exception {
        Files.writeString(Files.createFile(tempSourceDir.resolve("file with spaces.txt")), "Content");
        Files.writeString(Files.createFile(tempSourceDir.resolve("file-with-dashes.txt")), "Content");

        String archivePath = acroZipService.zip();
        File archiveFile = new File(archivePath);

        assertTrue(archiveFile.exists(), "Archive should handle special characters in filenames");
    }

    @Test
    @DisplayName("Should unzip files with given password")
    void testZipWithPasswordProtection() throws Exception {
        Files.writeString(Files.createFile(tempSourceDir.resolve("secret.txt")), "Sensitive data");
        String password = "TemporaryPassword";
        ReflectionTestUtils.setField(acroZipService, "password", password);

        String archivePath = acroZipService.zip();

        try (ZipFile zipFile = new ZipFile(archivePath)) {
            assertTrue(zipFile.isEncrypted(), "Archive should be encrypted");

            assertThrows(
                ZipException.class, () -> zipFile.extractAll(tempExportDir.toString())
            );
        }

        try (ZipFile zipFileWithPassword = new ZipFile(archivePath, password.toCharArray())) {
            zipFileWithPassword.extractAll(tempExportDir.toString());
            assertTrue(
                Files.exists(tempExportDir.resolve("secret.txt")),
                "File should be extracted with password"
            );
        }
    }

    @Test
    @DisplayName("Should zip large file")
    void testZipLargeFile() throws Exception {
        Path largeFile = Files.createFile(tempSourceDir.resolve("large.txt"));
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            content.append("This is line ").append(i).append(" of the large file.\n");
        }
        Files.writeString(largeFile, content.toString());

        String archiveLocation = acroZipService.zip();

        Path archivePath = Path.of(archiveLocation);
        assertTrue(Files.exists(archivePath));
        assertTrue(
            Files.size(archivePath) < Files.size(largeFile),
            "Archive should be smaller than original file"
        );
    }

    //@Test
    void manualReviewZipFile() throws Exception {
        Files.writeString(Files.createFile(tempSourceDir.resolve("manual.txt")), "Manual review content");
        String archivePath = acroZipService.zip();

        Path reviewPath = Path.of(System.getProperty("user.home"), "Desktop", "manual-review.zip");
        Files.copy(Path.of(archivePath), reviewPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        System.out.println("ZIP file saved for review at: " + reviewPath);
        System.out.println("password: ReviewPassword123");
    }
}
