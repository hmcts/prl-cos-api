package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_BINARY_URL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_UPLOAD_TIMESTAMP;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_URL;

@Service
@RequiredArgsConstructor
public class DocumentInstanceRetriever {

    private final ObjectMapper objectMapper;

    public Document getCaseDocument(CaseData caseData, String documentId) {
        // TODO refactor
        List<DocumentNode> documentNodes = getDocumentNodes(caseData, documentId);
        if (documentNodes.isEmpty()) {
            return null;
        }

        DocumentNode documentNode = documentNodes.getFirst();
        return Document.builder()
            .documentBinaryUrl(documentNode.getDocumentBinaryUrl())
            .documentFileName(documentNode.getDocumentFilename())
            .documentUrl(documentNode.getDocumentUrl())
            .build();
    }

//    public DocumentInstances getDocumentInstance(CaseData caseData, String documentId) {
//        List<DocumentNode> documentNodes = getDocumentNodes(caseData, documentId);
//
//        DocumentNode documentNode = documentNodes.getFirst();
//        Document document = Document.builder()
//            .documentBinaryUrl(documentNode.getDocumentBinaryUrl())
//            .documentFileName(documentNode.getDocumentFilename())
//            .documentUrl(documentNode.getDocumentUrl())
//            .build();
//
//        List<String> instances = documentNodes.stream()
//            .map(DocumentNode::getParentPath)
//            .toList();
//
//        return DocumentInstances.builder()
//            .document(document)
//            .instances(instances)
//            .build();
//    }

    private List<DocumentNode> getDocumentNodes(CaseData caseData, String documentId) {
        List<DocumentNode> documentNodes = new ArrayList<>();
        JsonNode root = objectMapper.valueToTree(caseData);
        traverseNodesForDocumentId(root, root, documentId, new ArrayList<>(), documentNodes);
        return documentNodes;
    }

    private void traverseNodesForDocumentId(JsonNode node, JsonNode parent, String documentId, List<String> parentKeys, List<DocumentNode> result) {
        if (node.isObject()) {
            if (isNodeForDocumentId(node, documentId)) {
                DocumentNode docNode = new DocumentNode();
                docNode.setDocumentUrl(node.get(DOCUMENT_URL).asText());
                docNode.setDocumentFilename(node.get(DOCUMENT_FILENAME).asText());
                docNode.setDocumentBinaryUrl(node.get(DOCUMENT_BINARY_URL).asText());
                if (node.has(DOCUMENT_UPLOAD_TIMESTAMP)) {
                    docNode.setUploadTimestamp(node.get(DOCUMENT_UPLOAD_TIMESTAMP).asText());
                }
                docNode.setDocumentId(getDocumentId(node));
                docNode.setDocument(node);
                docNode.setParent(parent);
                docNode.setParentKeys(new ArrayList<>(parentKeys));
                result.add(docNode);
            }
            node.fields().forEachRemaining(entry -> {
                List<String> newParentKeys = new ArrayList<>(parentKeys);
                newParentKeys.add(entry.getKey());
                traverseNodesForDocumentId(entry.getValue(), node, documentId, newParentKeys, result);
            });
        } else if (node.isArray()) {
            node.forEach(child -> traverseNodesForDocumentId(child, node, documentId, parentKeys, result));
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
