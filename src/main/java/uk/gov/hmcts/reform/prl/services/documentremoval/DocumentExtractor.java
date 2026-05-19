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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_URL;

@Service
@RequiredArgsConstructor
public class DocumentExtractor {

    private final ObjectMapper objectMapper;

    public List<Document> getCaseDocuments(CaseData caseData) {
        Map<String, Document> documents = new HashMap<>();
        traverseNodes(objectMapper.valueToTree(caseData), documents);
        return new ArrayList<>(documents.values());
    }

    private void traverseNodes(JsonNode node, Map<String, Document> documents) {
        if (node.isObject()) {
            handleObjectNode(node, documents);
        } else if (node.isArray()) {
            handleArrayNode(node, documents);
        }
    }

    private void handleObjectNode(JsonNode node, Map<String, Document> documents) {
        if (isDocumentNode(node)) {
            try {
                Document document = objectMapper.treeToValue(node, Document.class);
                String documentId = document.getDocumentId();
                Document existingDocument = documents.get(documentId);
                if (existingDocument == null || shouldReplaceDocument(existingDocument, document)) {
                    documents.put(documentId, document);
                }
            } catch (JsonProcessingException e) {
                throw new DocumentExtractorException(e);
            }
        }
        node.fields().forEachRemaining(entry -> traverseNodes(entry.getValue(), documents));
    }

    private boolean isDocumentNode(JsonNode node) {
        return node.isObject() && node.has(DOCUMENT_URL);
    }

    private boolean shouldReplaceDocument(Document existingDocument, Document candidateDocument) {
        return existingDocument.getUploadTimeStamp() == null && candidateDocument.getUploadTimeStamp() != null;
    }

    private void handleArrayNode(JsonNode node, Map<String, Document> documents) {
        node.forEach(child -> traverseNodes(child, documents));
    }
}
