package uk.gov.hmcts.reform.prl.services.acro;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void testZipCreatesZipFormat() throws Exception {
        Path file1 = Files.createFile(tempSourceDir.resolve("file1.txt"));
        Files.writeString(file1, "Test content");

        String archivePath = acroZipService.zip(tempSourceDir.toFile(), tempExportDir.toFile());
        File archiveFile = new File(archivePath);

        byte[] header = new byte[4]; // ZIP signature is 4 bytes
        try (var fis = Files.newInputStream(archiveFile.toPath())) {
            int bytesRead = fis.read(header);
            assertEquals(4, bytesRead);
        }

        byte[] expectedSignature = {0x50, 0x4B, 0x03, 0x04}; // ZIP file signature
        assertArrayEquals(expectedSignature, header, "File should have ZIP format signature");
        assertTrue(archivePath.endsWith(".zip"), "Archive should have .zip extension");
    }
}
