package uk.gov.hmcts.reform.prl.services.acro;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class AcroZipService {

    public String zip(File sourceFolder, File exportFolder) throws Exception {
        validateInputs(sourceFolder, exportFolder);

        Path sourcePath = sourceFolder.toPath();
        String archivePath = exportFolder + "/" + createSevenZipFileName();
        File archiveFile = new File(archivePath);

        List<Path> filesToCompress = collectFiles(sourcePath);

        if (filesToCompress.isEmpty()) {
            log.warn("No files found in source folder: {}", sourceFolder);
            createEmptyArchive(archiveFile);
            return archivePath;
        }

        createSevenZipArchive(archiveFile, sourcePath, filesToCompress);

        log.info("Successfully created 7zip archive: {} with {} files", archivePath, filesToCompress.size());
        return archivePath;
    }

    private void validateInputs(File sourceFolder, File exportFolder) {
        if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
            throw new IllegalArgumentException("Source must be an existing directory: " + sourceFolder);
        }
        if (!exportFolder.exists() || !exportFolder.isDirectory()) {
            throw new IllegalArgumentException("Export folder must be an existing directory: " + exportFolder);
        }
    }

    private List<Path> collectFiles(Path sourcePath) throws IOException {
        try (Stream<Path> files = Files.walk(sourcePath)) {
            return files
                .filter(Files::isRegularFile)
                .toList();
        }
    }

    private void createSevenZipArchive(File archiveFile, Path sourcePath, List<Path> filesToCompress) throws IOException {
        try (SevenZOutputFile sevenZOutput = new SevenZOutputFile(archiveFile)) {
            for (Path fileToCompress : filesToCompress) {
                Path relativePath = sourcePath.relativize(fileToCompress);

                SevenZArchiveEntry entry = new SevenZArchiveEntry();
                entry.setName(relativePath.toString().replace('\\', '/'));
                entry.setSize(Files.size(fileToCompress));

                sevenZOutput.putArchiveEntry(entry);
                sevenZOutput.write(Files.readAllBytes(fileToCompress));
                sevenZOutput.closeArchiveEntry();
            }
        }
    }

    private void createEmptyArchive(File archiveFile) throws IOException {
        try (SevenZOutputFile sevenZOutput = new SevenZOutputFile(archiveFile)) {
            // Create empty archive - no entries added
        }
    }

    private String createSevenZipFileName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
        String timestamp = LocalDateTime.now().format(formatter);
        return "PRL_ORDERS_" + timestamp + ".7z";
    }
}
