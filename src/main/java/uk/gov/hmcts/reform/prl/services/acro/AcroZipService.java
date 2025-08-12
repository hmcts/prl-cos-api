package uk.gov.hmcts.reform.prl.services.acro;

import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class AcroZipService {
    private static final Logger log = LoggerFactory.getLogger(AcroZipService.class);

    /**
     * Compresses all files in a folder to a 7zip archive
     * @param sourceFolder The folder to compress
     * @param exportFolder The destination folder for the archive
     * @return The path to the created archive
     * @throws Exception If compression fails
     */
    public String zip(File sourceFolder, File exportFolder) throws Exception {
        validateInputs(sourceFolder, exportFolder);

        Path sourcePath = sourceFolder.toPath();
        String archivePath = exportFolder + "/" + createSevenZipFileName(sourcePath);
        File archiveFile = new File(archivePath);

        List<Path> filesToCompress = collectFiles(sourcePath);

        if (filesToCompress.isEmpty()) {
            log.warn("No files found in source folder: {}", sourceFolder);
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

    private void createSevenZipArchive(File archiveFile, Path sourcePath, List<Path> filesToCompress) throws Exception {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(archiveFile, "rw");
             IOutCreateArchive7z outArchive = SevenZip.openOutArchive7z()) {

            outArchive.createArchive(new RandomAccessFileOutStream(randomAccessFile),
                filesToCompress.size(), new ArchiveCallback(sourcePath, filesToCompress));
        }
    }

    private String createSevenZipFileName(Path source) {
        return source.getFileName().toString() + ".7z";
    }

    private static class ArchiveCallback implements IOutCreateCallback<IOutItem7z> {
        private final Path sourcePath;
        private final List<Path> filesToCompress;

        public ArchiveCallback(Path sourcePath, List<Path> filesToCompress) {
            this.sourcePath = sourcePath;
            this.filesToCompress = filesToCompress;
        }

        @Override
        public void setOperationResult(boolean operationResultOk) {
            // No action needed
        }

        @Override
        public void setTotal(long total) {
            // No action needed
        }

        @Override
        public void setCompleted(long complete) {
            // No action needed
        }

        public IOutItem7z getItemInformation(int index, net.sf.sevenzipjbinding.impl.OutItemFactory<net.sf.sevenzipjbinding.IOutItem7z> outItemFactory) {
            Path fileToCompress = filesToCompress.get(index);
            Path relativePath = sourcePath.relativize(fileToCompress);
            IOutItem7z outItem = outItemFactory.createOutItem();
            outItem.setPropertyPath(relativePath.toString().replace('\\', '/'));
            try {
                outItem.setDataSize(Files.size(fileToCompress));
            } catch (IOException e) {
                throw new RuntimeException("Failed to get file size for: " + fileToCompress, e);
            }
            return outItem;
        }

        @Override
        public ISequentialInStream getStream(int index) throws SevenZipException {
            Path fileToCompress = filesToCompress.get(index);
            try {
                byte[] fileContent = Files.readAllBytes(fileToCompress);
                return new FileInStream(fileContent);
            } catch (IOException e) {
                throw new SevenZipException("Failed to read file: " + fileToCompress, e);
            }
        }
    }

    private static class FileInStream implements ISequentialInStream {
        private final byte[] data;
        private int position = 0;

        public FileInStream(byte[] data) {
            this.data = data;
        }

        @Override
        public int read(byte[] buffer) {
            if (position >= data.length) {
                return 0;
            }

            int bytesToRead = Math.min(buffer.length, data.length - position);
            System.arraycopy(data, position, buffer, 0, bytesToRead);
            position += bytesToRead;

            return bytesToRead;
        }

        @Override
        public void close() {
            // No resources to close
        }
    }
}
