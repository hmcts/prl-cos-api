package uk.gov.hmcts.reform.prl.services.acro;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;



@Service
@Slf4j
@RequiredArgsConstructor
public class PdfExtractorService {

    private final AuthTokenGenerator authTokenGenerator;
    private final CaseDocumentClient caseDocumentClient;

    public Optional<List<File>> downloadFl404aDocuments(String caseId, String userToken,
                                                        String fileName, Document orderDocument,
                                                        Document orderDocumentWelsh) {
        List<Document> documents = Arrays.asList(orderDocument, orderDocumentWelsh);
        List<File> downloadedFiles = new ArrayList<>();

        for (Document document : documents) {
            Optional<File> downloadedFile = downloadFl404aDocument(caseId, userToken, fileName, document);
            downloadedFile.ifPresent(downloadedFiles::add);
        }

        return downloadedFiles.isEmpty() ? Optional.empty() : Optional.of(downloadedFiles);
    }

    public Optional<File> downloadFl404aDocument(String caseId, String userToken, String fileName, Document document) {
        if (document == null || document.getDocumentBinaryUrl() == null) {
            return Optional.empty();
        }

        try {
            String serviceToken = authTokenGenerator.generate();
            ResponseEntity<Resource> response = caseDocumentClient.getDocumentBinary(
                userToken,
                serviceToken,
                document.getDocumentBinaryUrl()
            );

            if (response.getBody() != null) {
                File outputFile = new File(fileName);

                Files.copy(response.getBody().getInputStream(),
                           outputFile.toPath(),
                           StandardCopyOption.REPLACE_EXISTING);

                log.info("Downloaded FL404a PDF for case {}: {}", caseId, fileName);
                return Optional.of(outputFile);
            }
        } catch (Exception e) {
            log.error("Failed to download FL404a document for case {}: {}", caseId, e.getMessage());
        }

        return Optional.empty();
    }
}
