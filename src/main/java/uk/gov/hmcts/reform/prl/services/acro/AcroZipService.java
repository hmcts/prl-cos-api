package uk.gov.hmcts.reform.prl.services.acro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AcroZipService {
    private static final Logger log = LoggerFactory.getLogger(AcroZipService.class);

    public String zip(File sourceFolder, File exportFolder) throws Exception {
        Path sourcePath = sourceFolder.toPath();

        if (!Files.isDirectory(sourcePath)) {
            throw new IllegalArgumentException("Please provide a folder. Source : " + sourceFolder);
        }

        String archivePath = exportFolder + "/" + createZipFileName(sourcePath);

        List<Path> filesToCompress = collectFiles(sourcePath);

        if (filesToCompress.isEmpty()) {
            log.warn("No files found in source folder: {}", sourceFolder);
            return archivePath;
        }

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archivePath))) {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    if (attributes.isSymbolicLink()) {
                        return FileVisitResult.CONTINUE;
                    }

                    try (FileInputStream fis = new FileInputStream(file.toFile())) {
                        Path targetFile = sourcePath.relativize(file);

                        log.debug("File targeted : {}", file);

                            zos.putNextEntry(new ZipEntry(targetFile.toString()));

                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                            zos.closeEntry();

                            log.debug("File zipped : {}", file);


                    } catch (IOException e) {
                        log.error("Error:", e);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.error("Unable to zip : {}", file, exc);
                    return FileVisitResult.CONTINUE;
                }
            });

            return archivePath;
        }
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
