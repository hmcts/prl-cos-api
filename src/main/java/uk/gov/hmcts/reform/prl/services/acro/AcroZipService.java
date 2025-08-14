package uk.gov.hmcts.reform.prl.services.acro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AcroZipService {
    private static final Logger log = LoggerFactory.getLogger(AcroZipService.class);

    public String zip(File sourceFolder, File exportFolder) throws Exception {
        Path sourcePath = sourceFolder.toPath();

        if (!Files.isDirectory(sourcePath)) {
            throw new IllegalArgumentException("Please provide a folder. Source : " + sourceFolder);
        }

        String archivePath = exportFolder + "/" + createZipFileName(sourcePath);

        File archiveFile = new File(archivePath);

        List<Path> filesToCompress = collectFiles(sourcePath);

        if (filesToCompress.isEmpty()) {
            log.warn("No files found in source folder: {}", sourceFolder);
            return archivePath;
        }

        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(archivePath))) {
            Files.walkFileTree(sourcePath, new java.nio.file.SimpleFileVisitor<>() {
                @Override
                public java.nio.file.FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attributes) {
                    if (attributes.isSymbolicLink()) {
                        return java.nio.file.FileVisitResult.CONTINUE;
                    }

                    try (java.io.FileInputStream fis = new java.io.FileInputStream(file.toFile())) {
                        Path targetFile = sourcePath.relativize(file);

                        log.debug("File targeted : {}", file);

                            zos.putNextEntry(new java.util.zip.ZipEntry(targetFile.toString()));

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
                    return java.nio.file.FileVisitResult.CONTINUE;
                }

                @Override
                public java.nio.file.FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.error("Unable to zip : {}", file, exc);
                    return java.nio.file.FileVisitResult.CONTINUE;
                }
            });

            return archivePath;
        }
    }

    private String createZipFileName(Path sourcePath) {
        return sourcePath.getFileName().toString() + ".zip";
    }

    private List<Path> collectFiles(Path sourcePath) throws IOException {
        try (java.util.stream.Stream<java.nio.file.Path> files = Files.walk(sourcePath)) {
            return files
                .filter(Files::isRegularFile)
                .toList();
        }
    }


}
