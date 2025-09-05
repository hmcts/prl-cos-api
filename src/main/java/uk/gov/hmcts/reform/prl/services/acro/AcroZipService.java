package uk.gov.hmcts.reform.prl.services.acro;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class AcroZipService {

    public String zip(File sourceFolder, File exportFolder, String password) throws Exception {
        Path sourcePath = sourceFolder.toPath();

        if (!Files.isDirectory(sourcePath)) {
            throw new IllegalArgumentException("Please provide a folder. Source : " + sourceFolder);
        }

        String archivePath = exportFolder + "/" + createZipFileName(sourcePath);

        ZipFile zipFile = password == null || password.isEmpty()
                ? new ZipFile(archivePath)
                : new ZipFile(archivePath, password.toCharArray());

        List<Path> filesToCompress = collectFiles(sourcePath);

        if (filesToCompress.isEmpty()) {
            log.warn("No files found in source folder: {}", sourceFolder);
            boolean created = new File(archivePath).createNewFile();
            log.info("Empty archive file {}: {}", created ? "created" : "already exists", archivePath);
            return archivePath;
        }

        for (Path file : filesToCompress) {
            ZipParameters parameters = new ZipParameters();
            parameters.setFileNameInZip(sourcePath.relativize(file).toString());
            if (password != null && !password.isEmpty()) {
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
            }
            zipFile.addFile(file.toFile(), parameters);
        }
        return archivePath;
    }

    private String createZipFileName(Path sourcePath) {
        return sourcePath.getFileName().toString() + ".zip";
    }

    private List<Path> collectFiles(Path sourcePath) throws IOException {
        try (Stream<java.nio.file.Path> files = Files.walk(sourcePath)) {
            return files
                .filter(Files::isRegularFile)
                .toList();
        }
    }


}
