package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_UPLOAD_TIMESTAMP;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_URL;

@Service
@RequiredArgsConstructor
public class DocumentIdRetriever {

    private final ObjectMapper objectMapper;

    private final DateTimeFormatter uploadTimestampFormatter = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
        .optionalStart()
        .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
        .optionalEnd()
        .toFormatter();

    public List<CaseDocument> getCaseDocuments(CaseData caseData) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        traverseNodes(objectMapper.valueToTree(caseData), caseDocuments);
        return caseDocuments;
    }

    private void traverseNodes(JsonNode node, List<CaseDocument> documents) {
        if (node.isObject()) {
            if (node.has(DOCUMENT_URL)) {
                CaseDocument caseDocument = createCaseDocument(node);
                if (!containsDocumentId(caseDocument.documentId(), documents)) {
                    documents.add(caseDocument);
                } else {
                    // Document ID already exists in the list, ensure upload timestamp is present
                    if (caseDocument.uploadTimestamp() != null) {
                        documents.stream()
                            .filter(doc -> doc.documentId().equals(caseDocument.documentId()) && doc.uploadTimestamp() == null)
                            .findFirst()
                            .ifPresent(doc -> {
                                documents.remove(doc);
                                documents.add(caseDocument);
                            });
                    }
                }

            }
            node.fields().forEachRemaining(entry -> traverseNodes(entry.getValue(), documents));
        } else if (node.isArray()) {
            node.forEach(child -> traverseNodes(child, documents));
        }
    }

    private CaseDocument createCaseDocument(JsonNode node) {
        String documentId = getDocumentId(node);
        LocalDateTime uploadTimestamp = getUploadTimestamp(node);
        String documentFilename = node.get(DOCUMENT_FILENAME).asText();
        return new CaseDocument(documentId, documentFilename, uploadTimestamp);
    }


    private LocalDateTime getUploadTimestamp(JsonNode node) {
        if (node.has(DOCUMENT_UPLOAD_TIMESTAMP)) {
            String timestampStr = node.get(DOCUMENT_UPLOAD_TIMESTAMP).asText();
            try {
                return LocalDateTime.parse(timestampStr, uploadTimestampFormatter);
            } catch (Exception e) {
                // Handle parsing exception if needed
                return null;
            }
        }
        return null;
    }

    private boolean containsDocumentId(String documentId, Collection<CaseDocument> documents) {
        return documents.stream().anyMatch(doc -> doc.documentId().equals(documentId));
    }

    private String getDocumentId(JsonNode documentNode) {
        String documentUrl = documentNode.get(DOCUMENT_URL).asText();
        String[] urlParts = documentUrl.split("/");
        return urlParts[urlParts.length - 1];
    }
}
