package uk.gov.hmcts.reform.prl.services.acro;

import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.Optional;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;



@Service
@Slf4j
@RequiredArgsConstructor
public class Fl404aPdfExtractorService {

    private final AuthTokenGenerator authTokenGenerator;
    private final CaseDocumentClient caseDocumentClient;

    private final String fileName;
    private final Document orderDocument;
    private final Document orderDocumentWelsh;

//    public Optional<File> extractFL404aPdf(String caseId, CaseData caseData, String userToken) {
//        try {
//            Optional<Document> fl404aDocument = findFL404aDocument(caseData);
//
//            if (fl404aDocument.isPresent()) {
//                return downloadFl404aDocument(fl404aDocument.get(), caseId, userToken);
//            } else {
//                log.warn("No FL404a document found for case: {}", caseId);
//                return Optional.empty();
//            }
//        } catch (Exception e) {
//            log.error("Failed to extract FL404a PDF for case {}: {}", caseId, e.getMessage());
//            return Optional.empty();
//        }
//    }
//
//    private Optional<Document> findFL404aDocument(CaseData caseData) {
//        if (caseData.getOrderCollection() == null) {
//            return Optional.empty();
//        }
//        return caseData.getOrderCollection().stream()
//            .filter(order -> order.getValue().getOrderType() != null
//                && order.getValue().getOrderType().contains("FL404A"))
//            .map(order -> order.getValue().getOrderDocument())
//            .filter(doc -> doc != null)
//            .findFirst();
//    }

    private Optional<File> downloadFl404aDocument(Document document, String caseId, String userToken) {
        try {
            String serviceToken = authTokenGenerator.generate();
            ResponseEntity<Resource> response = caseDocumentClient.getDocumentBinary(
                userToken,
                serviceToken,
                document.getDocumentBinaryUrl()
            );

            if (response.getBody() != null) {
                String fileName = caseId + "_FL404a_" + System.currentTimeMillis() + ".pdf";
                File outputFile = new File("extracted_pdfs/" + fileName);

                if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
                    log.warn("Failed to create directories for file: {}", outputFile.getAbsolutePath());
                }

                Files.copy(response.getBody().getInputStream(),
                                         outputFile.toPath(),
                                         StandardCopyOption.REPLACE_EXISTING);

                log.info("Downloaded FL404a PDF for case {}: {}", caseId, fileName);
                return Optional.of(outputFile);
            }
        } catch (java.io.IOException e) {
            log.error("Failed to download FL404a document for case {}: {}", caseId, e.getMessage());
        }

        return Optional.empty();
    }
}
