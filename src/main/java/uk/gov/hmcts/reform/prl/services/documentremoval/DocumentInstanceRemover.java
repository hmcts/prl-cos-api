package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_BINARY_URL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_UPLOAD_TIMESTAMP;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_URL;

@Service
@RequiredArgsConstructor
public class DocumentInstanceRemover {

    private final ObjectMapper objectMapper;

    public Map<String, Object> removeDocumentInstance(Map<String, Object> caseData, String documentId,
                                                      List<String> pathsToRemove) throws IOException {
        JsonNode root = objectMapper.valueToTree(caseData);

        pathsToRemove.forEach(path -> removeDocumentFromPath(root, documentId, path));

        JsonParser jsonParser = objectMapper.treeAsTokens(root);
        return objectMapper.readValue(jsonParser, Map.class);
    }

    private void removeDocumentFromPath(JsonNode root, String documentId, String path) {
        if (path.endsWith("value")) {
            removeCollectionElement(root, documentId, path);
            return;
        }

        List<DocumentNode> documentNodes = new ArrayList<>();
        traverseNodes(root, root, documentId, new ArrayList<>(), documentNodes);

        List<DocumentNode> documentsToDelete = documentNodes.stream()
            .filter(documentNode -> documentNode.getDocumentId().equals(documentId)
                && documentNode.getParentPath().equals(path))
            .toList();

        documentsToDelete.forEach(documentNode -> {
            JsonNode parentNode = documentNode.getParent();

            String parentKey = documentNode.getParentKeys().getLast();
            if (parentNode.isObject()) {

                if ("value".equals(parentKey) && parentNode.size() == 2 && parentNode.has("id") && parentNode.has("value")) {
                    // This means the parent node is a wrapper object for an element in a collection,
                    // so we should remove the entire wrapper object
                    ((ObjectNode) parentNode).removeAll();
                } else {
                    ((ObjectNode) parentNode).remove(parentKey);
                }
            }
        });
    }

    private void traverseNodes(JsonNode node, JsonNode parent, String documentId, List<String> parentKeys,
                               List<DocumentNode> result) {
        if (node.isObject()) {
            if (isNodeForDocumentId(node, documentId)) {
                DocumentNode docNode = new DocumentNode();
                docNode.setDocumentUrl(node.get(DOCUMENT_URL).asText());
                docNode.setDocumentFilename(node.get(DOCUMENT_FILENAME).asText());
                docNode.setDocumentBinaryUrl(node.get(DOCUMENT_BINARY_URL).asText());
                if (node.has(DOCUMENT_UPLOAD_TIMESTAMP)) {
                    docNode.setUploadTimestamp(node.get(DOCUMENT_UPLOAD_TIMESTAMP).asText());
                }
                String[] documentUrlAsArray = docNode.getDocumentUrl().split("/");
                docNode.setDocumentId(documentUrlAsArray[documentUrlAsArray.length - 1]);
                docNode.setDocument(node);
                docNode.setParent(parent);
                docNode.setParentKeys(new ArrayList<>(parentKeys));
                result.add(docNode);
            }
            node.fields().forEachRemaining(entry -> {
                List<String> newParentKeys = new ArrayList<>(parentKeys);
                newParentKeys.add(entry.getKey());
                traverseNodes(entry.getValue(), node, documentId, newParentKeys, result);
            });
        } else if (node.isArray()) {
            node.forEach(child -> traverseNodes(child, node, documentId, parentKeys, result));
        }
    }

    private void removeCollectionElement(JsonNode root, String documentId, String path) {
        String[] parts = path.split("\\.");
        String collectionField = parts[parts.length - 2];

        JsonNode collectionNode = this.findNodesWithField(root, collectionField).getFirst().get(collectionField);


        if (collectionNode != null && collectionNode.isArray()) {
            for (int i = 0; i < collectionNode.size(); i++) {
                JsonNode elementNode = collectionNode.get(i);
                if (elementNode.has("value")) {
                    JsonNode valueNode = elementNode.get("value");
                    if (isNodeForDocumentId(valueNode, documentId)) {
                        ((ArrayNode)collectionNode).remove(i);
                        break;
                    }
                }
            }
        }

    }

    /**
     * Traverses the given JsonNode tree and returns all nodes that contain the specified field name.
     * @param root the root JsonNode to start traversal from
     * @param fieldName the field name to search for
     * @return a list of JsonNode objects that contain the specified field name
     */
    private List<JsonNode> findNodesWithField(JsonNode root, String fieldName) {
        List<JsonNode> result = new ArrayList<>();
        findNodesWithFieldRecursive(root, fieldName, result);
        return result;
    }

    private void findNodesWithFieldRecursive(JsonNode node, String fieldName, List<JsonNode> result) {
        if (node.isObject()) {
            if (node.has(fieldName)) {
                result.add(node);
            }
            node.fields().forEachRemaining(entry -> {
                findNodesWithFieldRecursive(entry.getValue(), fieldName, result);
            });
        } else if (node.isArray()) {
            node.forEach(child -> findNodesWithFieldRecursive(child, fieldName, result));
        }
    }

    private boolean isNodeForDocumentId(JsonNode node, String documentId) {
        if (node.has(DOCUMENT_URL)) {
            String nodeDocumentId = getDocumentId(node);
            return nodeDocumentId.equals(documentId);
        }
        return false;
    }

    private String getDocumentId(JsonNode documentNode) {
        String documentUrl = documentNode.get(DOCUMENT_URL).asText();
        String[] urlParts = documentUrl.split("/");
        return urlParts[urlParts.length - 1];
    }
}
