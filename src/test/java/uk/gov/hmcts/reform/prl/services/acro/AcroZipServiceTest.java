package uk.gov.hmcts.reform.prl.services.acro;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        deleteRecursively(tempSourceDir);
        deleteRecursively(tempExportDir);
    }

    private void deleteRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            try (var pathStream = Files.walk(path)) {
                pathStream
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            file.deleteOnExit();
                        }
                    });
            }
        }
    }

    @Test
    void testZipCreatesZipFormat() throws Exception {
        Path file1 = Files.createFile(tempSourceDir.resolve("file1.txt"));
        Files.writeString(file1, "Test content");

        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile(), null);
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
    void testZipMultipleFiles() throws Exception {
        Files.writeString(Files.createFile(tempSourceDir.resolve("file1.txt")), "Content 1");
        Files.writeString(Files.createFile(tempSourceDir.resolve("file2.txt")), "Content 2");
        Files.writeString(Files.createFile(tempSourceDir.resolve("file3.txt")), "Content 3");

        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile(), null);

        assertTrue(Files.exists(Path.of(archivePath)));

        int fileCount = 0;
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(Path.of(archivePath)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                fileCount++;
                assertTrue(entry.getName().matches("file[1-3]\\.txt"));
            }
        }
        assertEquals(3, fileCount, "Archive should contain 3 files");
    }

    @Test
    void testZipWithSubdirectories() throws Exception {
        Path subDir = Files.createDirectory(tempSourceDir.resolve("subdir"));
        Files.writeString(Files.createFile(tempSourceDir.resolve("root.txt")), "Root content");
        Files.writeString(Files.createFile(subDir.resolve("nested.txt")), "Nested content");

        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile(), null);

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(Path.of(archivePath)))) {
            ZipEntry entry;
            boolean foundRoot = false;
            boolean foundNested = false;

            while ((entry = zis.getNextEntry()) != null) {
                if ("root.txt".equals(entry.getName())) {
                    foundRoot = true;
                } else if ("subdir/nested.txt".equals(entry.getName())) {
                    foundNested = true;
                }
            }

            assertTrue(foundRoot, "Should find root.txt");
            assertTrue(foundNested, "Should find subdir/nested.txt");
        }
    }

    @Test
    void testZipEmptyFolder() throws Exception {
        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile(), null);

        assertTrue(Files.exists(Path.of(archivePath)));

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(Path.of(archivePath)))) {
            ZipEntry entry = zis.getNextEntry();
            assertNull(entry, "Empty folder should create empty archive");
        }
    }

    @Test
    void testZipFileNameGeneration() throws Exception {
        Files.writeString(Files.createFile(tempSourceDir.resolve("test.txt")), "content");

        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile(), null);
        String expectedFileName = tempSourceDir.getFileName().toString() + ".zip";

        assertTrue(archivePath.endsWith(expectedFileName),
            "Archive should be named after source folder");
    }

    @Test
    void testZipThrowsExceptionForNonDirectorySource() {
        Path tempFile = tempSourceDir.resolve("notadirectory.txt");

        assertThrows(IllegalArgumentException.class, () ->
            acroZipService.zip(tempFile.toFile(), tempExportDir.toFile(), null),
            "Should throw exception when source is not a directory");
    }

    @Test
    void testZipLargeFile() throws Exception {
        Path largeFile = Files.createFile(tempSourceDir.resolve("large.txt"));
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            content.append("This is line ").append(i).append(" of the large file.\n");
        }
        Files.writeString(largeFile, content.toString());

        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile(), null);

        assertTrue(Files.exists(Path.of(archivePath)));
        assertTrue(Files.size(Path.of(archivePath)) < Files.size(largeFile),
            "Archive should be smaller than original file");
    }

    @Test
    void testZipPreservesFileContent() throws Exception {
        String originalContent = "This is test content that should be preserved";
        Path testFile = Files.createFile(tempSourceDir.resolve("content.txt"));
        Files.writeString(testFile, originalContent);

        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile(), null);

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(Path.of(archivePath)))) {
            ZipEntry entry = zis.getNextEntry();
            assertNotNull(entry);
            assertEquals("content.txt", entry.getName());

            byte[] buffer = new byte[1024];
            int bytesRead = zis.read(buffer);
            String extractedContent = new String(buffer, 0, bytesRead);
            assertEquals(originalContent, extractedContent);
        }
    }

    @Test
    void testZipWithPasswordProtection() throws Exception {
        Files.writeString(Files.createFile(tempSourceDir.resolve("secret.txt")), "Sensitive data");
        String password = "TestPassword123";
        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile(), password);

        ZipFile zipFile = new ZipFile(archivePath);
        assertTrue(zipFile.isEncrypted(), "Archive should be encrypted");

        assertThrows(ZipException.class, () -> {
            zipFile.extractAll(tempExportDir.toString());
        });

        ZipFile zipFileWithPassword = new ZipFile(archivePath, password.toCharArray());
        zipFileWithPassword.extractAll(tempExportDir.toString());
        assertTrue(Files.exists(tempExportDir.resolve("secret.txt")), "File should be extracted with password");
    }

    //@Test
    void manualReviewZipFile() throws Exception {
        Files.writeString(Files.createFile(tempSourceDir.resolve("manual.txt")), "Manual review content");
        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile(), "ReviewPassword123");

        Path reviewPath = Path.of(System.getProperty("user.home"), "Desktop", "manual-review.zip");
        Files.copy(Path.of(archivePath), reviewPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        System.out.println("ZIP file saved for review at: " + reviewPath);
        System.out.println("password: ReviewPassword123");
    }
}
