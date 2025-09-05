package uk.gov.hmcts.reform.prl.services.acro;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.prl.services.SendgridService.MAIL_SEND;

@Service
@Slf4j
@RequiredArgsConstructor
public class AcroZipService {

    @Value("${acro.source-directory}")
    private String sourceDirectory;

    @Value("${acro.output-directory}")
    private String outputDirectory;

    @Value("${send-grid.rpa.email.from}")
    private String fromEmail;

    private final SendGrid sendGrid;

    public String zip() throws Exception {
        File sourceFolder = new File(sourceDirectory);
        File exportFolder = new File(outputDirectory);
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

        sendEmail(archivePath);

        log.info("Successfully created 7zip archive: {} with {} files", archivePath, filesToCompress.size());
        return archivePath;
    }

    private void sendEmail(String filePath) throws IOException {
        try {
            File archiveFile = new File(filePath);
            String subject = "Acro zip email" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            Content content = new Content("text/plain", " ");
            Attachments attachments = new Attachments();
            String data = Base64.getEncoder().encodeToString(Files.readAllBytes(archiveFile.toPath()));
            attachments.setContent(data);
            attachments.setFilename(filePath.substring(17));
            attachments.setType("application/x-7z-compressed");
            attachments.setDisposition("attachment");
            Mail mail = new Mail(
                new Email(fromEmail),
                subject,
                new Email("dharmendra.kumar1@hmcts.net"),
                content
            );
            mail.addAttachments(attachments);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint(MAIL_SEND);
            request.setBody(mail.build());
            log.info("Initiating email through sendgrid");
            Response api = sendGrid.api(request);
            log.info("Notification to RPA sent successfully");
        } catch (IOException ex) {
            throw new IOException(ex.getMessage());
        }
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
