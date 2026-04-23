package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.exception.DocumentExtractorException;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_URL;

@Service
@RequiredArgsConstructor
public class DocumentExtractor {

    private final ObjectMapper objectMapper;

    public List<Document> getCaseDocuments(CaseData caseData) {
        List<Document> documents = new ArrayList<>();
        traverseNodes(objectMapper.valueToTree(caseData), documents);
        return documents;
    }

    private void traverseNodes(JsonNode node, List<Document> documents) {
        if (node.isObject()) {
            handleObjectNode(node, documents);
        } else if (node.isArray()) {
            handleArrayNode(node, documents);
        }
    }

    private void handleObjectNode(JsonNode node, List<Document> documents) {
        if (node.has(DOCUMENT_URL)) {
            Document document;
            try {
                document = objectMapper.treeToValue(node, Document.class);
            } catch (JsonProcessingException e) {
                throw new DocumentExtractorException(e);
            }
            if (!containsDocumentId(document, documents)) {
                documents.add(document);
            } else {
                // Document ID already exists in the list, ensure upload timestamp is present
                // as sometimes documents are added to a case without the timestamp
                if (document.getUploadTimeStamp() != null) {
                    updateWithUploadTimestamp(documents, document);
                }
            }

        }
        node.fields().forEachRemaining(entry -> traverseNodes(entry.getValue(), documents));
    }

    private void updateWithUploadTimestamp(List<Document> documents, Document document) {
        documents.stream()
            .filter(doc -> doc.getDocumentId().equals(document.getDocumentId()) && doc.getUploadTimeStamp() == null)
            .findFirst()
            .ifPresent(doc -> {
                documents.remove(doc);
                documents.add(document);
            });
    }

    private boolean containsDocumentId(Document document, Collection<Document> documents) {
        String documentId = document.getDocumentId();
        return documents.stream()
            .anyMatch(d -> d.getDocumentId().equals(documentId));
    }

    private void handleArrayNode(JsonNode node, List<Document> documents) {
        node.forEach(child -> traverseNodes(child, documents));
    }
}
