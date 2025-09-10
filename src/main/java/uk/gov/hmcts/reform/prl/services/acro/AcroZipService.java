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
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
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

    @Value("${acro.zip-password}")
    private String password;

    @Value("${send-grid.rpa.email.from}")
    private String fromEmail;

    private final SendGrid sendGrid;

    public String zip() throws Exception {
        File sourceFolder = new File(sourceDirectory);
        File exportFolder = new File(outputDirectory);
        validateInputs(sourceFolder, exportFolder);

        Path sourcePath = sourceFolder.toPath();
        String archivePath = exportFolder + "/" + createZipFileName();

        List<Path> filesToCompress = collectFiles(sourcePath);

        createArchive(archivePath, sourcePath, filesToCompress);

        sendEmail(archivePath);

        log.info("Successfully created zip archive: {} with {} files", archivePath, filesToCompress.size());
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
                new Email("poojitha.nagappa2@hmcts.net"),
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

    private void createArchive(String archivePath, Path sourcePath, List<Path> filesToCompress) throws IOException {
        try (ZipFile zipFile = new ZipFile(archivePath, password.toCharArray())) {
            for (Path fileToCompress : filesToCompress) {
                Path relativePath = sourcePath.relativize(fileToCompress);
                ZipParameters parameters = new ZipParameters();
                parameters.setFileNameInZip(relativePath.toString().replace('\\', '/'));
                parameters.setEncryptFiles(true);
                parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);

                zipFile.addFile(fileToCompress.toFile(), parameters);
            }
        }
    }

    private String createZipFileName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
        String timestamp = LocalDateTime.now().format(formatter);
        return "PRL_ORDERS_" + timestamp + ".zip";
    }
}
