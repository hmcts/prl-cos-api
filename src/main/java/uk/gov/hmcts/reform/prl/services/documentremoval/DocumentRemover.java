package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_URL;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentRemover {

    private final ObjectMapper objectMapper;

    private static final String VALUE_KEY = "value";

    public Map<String, Object> removeDocument(Map<String, Object> caseData, String documentId) throws IOException {
        JsonNode root = objectMapper.valueToTree(caseData);
        removeDocumentFromJson(root, documentId);

        JsonParser jsonParser = objectMapper.treeAsTokens(root);
        return objectMapper.readValue(jsonParser, Map.class);
    }

    private void removeDocumentFromJson(JsonNode root, String documentId) {
        List<DocumentNode> documentNodesToRemove = new ArrayList<>();

        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);

                if (shouldRemoveDocument(fieldValue, documentId)) {
                    DocumentNode documentNode = new DocumentNode();
                    documentNode.setDocumentId(documentId);
                    documentNode.setDocument(fieldValue);
                    documentNode.setParent(root);
                    documentNode.setDocumentKey(fieldName);
                    documentNodesToRemove.add(documentNode);
                } else {
                    removeDocumentFromJson(fieldValue, documentId);
                }
            }
        } else if (root.isArray()) {
            processArrayNode(root, documentId);
        }

        for (DocumentNode documentNode : documentNodesToRemove) {
            JsonNode parentNode  = documentNode.getParent();
            int sizeBefore = parentNode.size();
            ((ObjectNode)documentNode.getParent()).remove(documentNode.getDocumentKey());
            int sizeAfter = parentNode.size();

            log.info("Removed document node with id {}. Parent node size before: {}, after: {}", documentNode.getDocumentId(), sizeBefore, sizeAfter);
        }
    }

    private void processArrayNode(JsonNode root, String documentId) {
        ArrayNode arrayNode = (ArrayNode) root;
        List<Integer> indexesToRemove = new ArrayList<>();
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode arrayElement = arrayNode.get(i);
            if (arrayElement.has(VALUE_KEY)) {
                JsonNode valueObject = arrayElement.get(VALUE_KEY);

                if (isNodeForDocumentId(valueObject, documentId)) {
                    indexesToRemove.add(i);
                } else {
                    removeDocumentFromJson(valueObject, documentId);
                }
            }
        }

        if (!indexesToRemove.isEmpty()) {
            // Remove elements in reverse order to avoid index shifting
            for (int j = indexesToRemove.size() - 1; j >= 0; j--) {
                int indexToRemove = indexesToRemove.get(j);
                ((ArrayNode) root).remove(indexToRemove);
                log.info("Removed array element at index {} containing document with id {}", indexToRemove, documentId);
            }
        }
    }

    private boolean isNodeForDocumentId(JsonNode node, String documentId) {
        return node.has(DOCUMENT_URL)
            && node.get(DOCUMENT_URL).asText().endsWith(documentId);
    }

    private boolean shouldRemoveDocument(JsonNode fieldValue, String documentId) {
        return fieldValue.has(DOCUMENT_URL)
            && fieldValue.get(DOCUMENT_URL).asText().endsWith(documentId);
    }
}
