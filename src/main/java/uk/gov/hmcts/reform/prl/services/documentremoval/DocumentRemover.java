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

    private static final String COLLECTION_VALUE_KEY = "value";
    private static final String CAMEL_CASE_DOCUMENT_URL = "documentUrl";
    private static final String CITIZEN_FRONTEND_DOCUMENT_ID = "id";
    private static final String CITIZEN_FRONTEND_DOCUMENT_URL = "url";
    private static final String CITIZEN_FRONTEND_BINARY_URL = "binaryUrl";
    private static final String CITIZEN_FRONTEND_FILENAME = "filename";

    public Map<String, Object> removeDocument(Map<String, Object> caseData, String documentId) throws IOException {
        JsonNode root = objectMapper.valueToTree(caseData);
        removeDocumentFromJson(root, documentId);

        JsonParser jsonParser = objectMapper.treeAsTokens(root);
        return objectMapper.readValue(jsonParser, Map.class);
    }

    public boolean hasDocument(Map<String, Object> caseData, String documentId) {
        JsonNode root = objectMapper.valueToTree(caseData);
        return containsDocument(root, documentId);
    }

    private void removeDocumentFromJson(JsonNode root, String documentId) {
        log.info("Removing document {} from JSON node", documentId);
        List<DocumentNode> documentNodesToRemove = new ArrayList<>();

        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);

                if (isNodeForDocumentId(fieldValue, documentId)) {
                    DocumentNode documentNode = new DocumentNode();
                    documentNode.setDocumentId(documentId);
                    documentNode.setDocument(fieldValue);
                    documentNode.setParent(root);
                    documentNode.setDocumentKey(fieldName);
                    documentNodesToRemove.add(documentNode);
                } else if (isJsonTextNodeContainingDocument(fieldValue, documentId)) {
                    updateJsonTextField((ObjectNode) root, fieldName, fieldValue, documentId);
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

            log.info("Removed document id {}. Parent node size before: {}, after: {}", documentNode.getDocumentId(),
                     sizeBefore, sizeAfter);
        }
    }

    /**
     * Processes an array node assumed to represent a CCD collection.
     * Iterates through each element (expected to be an object with "id" and "value" properties),
     * and removes any element whose "value" node contains a document with the specified documentId.
     *
     * @param root       the array node to process
     * @param documentId the document id to remove from the array
     */
    private void processArrayNode(JsonNode root, String documentId) {
        ArrayNode arrayNode = (ArrayNode) root;
        Iterator<JsonNode> it = arrayNode.elements();
        while (it.hasNext()) {
            JsonNode element = it.next();
            if (element.has(COLLECTION_VALUE_KEY)) {
                JsonNode valueObject = element.get(COLLECTION_VALUE_KEY);
                if (isNodeForDocumentId(valueObject, documentId)) {
                    it.remove();
                } else if (isJsonTextNodeContainingDocument(valueObject, documentId)) {
                    replaceArrayElement(arrayNode, element, valueObject, documentId);
                } else {
                    removeDocumentFromJson(valueObject, documentId);
                }
            } else if (isNodeForDocumentId(element, documentId)) {
                it.remove();
            } else if (isJsonTextNodeContainingDocument(element, documentId)) {
                replaceArrayElement(arrayNode, element, element, documentId);
            } else {
                removeDocumentFromJson(element, documentId);
            }
        }
    }

    private boolean isNodeForDocumentId(JsonNode node, String documentId) {
        return hasDocumentUrlForDocumentId(node, DOCUMENT_URL, documentId)
            || hasDocumentUrlForDocumentId(node, CAMEL_CASE_DOCUMENT_URL, documentId)
            || hasDocumentUrlForDocumentId(node, CITIZEN_FRONTEND_DOCUMENT_URL, documentId)
            || hasDocumentUrlContainingDocumentId(node, CITIZEN_FRONTEND_BINARY_URL, documentId)
            || hasCitizenFrontendDocumentId(node, documentId);
    }

    private boolean hasDocumentUrlForDocumentId(JsonNode node, String documentUrlField, String documentId) {
        return node.has(documentUrlField)
            && node.get(documentUrlField).asText().endsWith(documentId);
    }

    private boolean hasDocumentUrlContainingDocumentId(JsonNode node, String documentUrlField, String documentId) {
        return node.has(documentUrlField)
            && node.get(documentUrlField).asText().contains(documentId);
    }

    private boolean hasCitizenFrontendDocumentId(JsonNode node, String documentId) {
        return node.has(CITIZEN_FRONTEND_DOCUMENT_ID)
            && node.get(CITIZEN_FRONTEND_DOCUMENT_ID).asText().equalsIgnoreCase(documentId)
            && (node.has(CITIZEN_FRONTEND_DOCUMENT_URL)
            || node.has(CITIZEN_FRONTEND_BINARY_URL)
            || node.has(CITIZEN_FRONTEND_FILENAME));
    }

    private boolean isJsonTextNodeContainingDocument(JsonNode node, String documentId) {
        return node != null
            && node.isTextual()
            && isJsonText(node.asText())
            && containsDocument(readJsonText(node.asText()), documentId);
    }

    private boolean isJsonText(String text) {
        String trimmedText = text.trim();
        return trimmedText.startsWith("{") || trimmedText.startsWith("[");
    }

    private JsonNode readJsonText(String text) {
        try {
            return objectMapper.readTree(text);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Failed to read JSON text field while removing document reference", exception);
        }
    }

    private void updateJsonTextField(ObjectNode root, String fieldName, JsonNode fieldValue, String documentId) {
        JsonNode jsonTextValue = readJsonText(fieldValue.asText());
        removeDocumentFromJson(jsonTextValue, documentId);
        try {
            root.put(fieldName, objectMapper.writeValueAsString(jsonTextValue));
        } catch (IOException exception) {
            throw new IllegalArgumentException("Failed to write JSON text field while removing document reference", exception);
        }
    }

    private void replaceArrayElement(ArrayNode arrayNode, JsonNode element, JsonNode valueObject, String documentId) {
        int index = indexOf(arrayNode, element);
        if (index < 0) {
            return;
        }
        JsonNode jsonTextValue = readJsonText(valueObject.asText());
        removeDocumentFromJson(jsonTextValue, documentId);
        try {
            arrayNode.set(index, objectMapper.getNodeFactory().textNode(objectMapper.writeValueAsString(jsonTextValue)));
        } catch (IOException exception) {
            throw new IllegalArgumentException("Failed to write JSON text array item while removing document reference", exception);
        }
    }

    private int indexOf(ArrayNode arrayNode, JsonNode element) {
        for (int index = 0; index < arrayNode.size(); index++) {
            if (arrayNode.get(index) == element) {
                return index;
            }
        }
        return -1;
    }

    private boolean containsDocument(JsonNode root, String documentId) {
        if (isNodeForDocumentId(root, documentId)) {
            return true;
        }

        if (isJsonTextNodeContainingDocument(root, documentId)) {
            return true;
        }

        if (root.isObject()) {
            Iterator<JsonNode> fields = root.elements();
            while (fields.hasNext()) {
                if (containsDocument(fields.next(), documentId)) {
                    return true;
                }
            }
        } else if (root.isArray()) {
            Iterator<JsonNode> elements = root.elements();
            while (elements.hasNext()) {
                if (containsDocument(elements.next(), documentId)) {
                    return true;
                }
            }
        }

        return false;
    }
}
