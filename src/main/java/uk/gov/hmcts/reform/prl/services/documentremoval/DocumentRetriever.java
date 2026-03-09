package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.documentremoval.DocumentToKeep;
import uk.gov.hmcts.reform.prl.models.documentremoval.DocumentToKeepCollection;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_BINARY_URL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_UPLOAD_TIMESTAMP;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_URL;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentRetriever {

    private final ObjectMapper objectMapper;

    public List<DocumentToKeepCollection> getDocuments(CaseData caseData) {
        JsonNode root = objectMapper.valueToTree(caseData);
        List<JsonNode> documentNodes = new ArrayList<>();
        retrieveDocumentNodes(root, documentNodes);
        return buildCaseDocumentList(documentNodes);
    }

    private void retrieveDocumentNodes(JsonNode root, List<JsonNode> documentNodes) {
        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);
                if (fieldValue.has(DOCUMENT_URL)) {
                    documentNodes.add(fieldValue);
                } else {
                    retrieveDocumentNodes(fieldValue, documentNodes);
                }
            }
        } else if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                retrieveDocumentNodes(arrayElement, documentNodes);
            }
        }
    }

    private List<DocumentToKeepCollection> buildCaseDocumentList(List<JsonNode> documentNodes) {
        List<DocumentToKeepCollection> documentsCollection = new ArrayList<>();

        for (JsonNode documentNode : documentNodes) {
            String docUrl = documentNode.get(DOCUMENT_URL).asText();
            String[] documentUrlAsArray = docUrl.split("/");
            String docId = documentUrlAsArray[documentUrlAsArray.length - 1];

            LocalDateTime uploadTimestamp = getUploadTimestampFromDocumentNode(documentNode);

            documentsCollection.add(
                DocumentToKeepCollection.builder()
                    .value(DocumentToKeep.builder()
                               .documentId(docId)
                               .caseDocument(Document.builder()
                                                 .documentFileName(documentNode.get(DOCUMENT_FILENAME).asText())
                                                 .documentUrl(documentNode.get(DOCUMENT_URL).asText())
                                                 .documentBinaryUrl(documentNode.get(DOCUMENT_BINARY_URL).asText())
                                                 .uploadTimeStamp(uploadTimestamp)
                                                 .build())
                               .caseDocumentUploadedDate(uploadTimestamp)
                               .build())
                    .build());
        }

        documentsCollection.sort(Comparator.comparing(
            DocumentToKeepCollection::getValue,
            Comparator.comparing(DocumentToKeep::getCaseDocument,
                                 Comparator.comparing(
                                     Document::getUploadTimeStamp,
                                     Comparator.nullsLast(
                                         Comparator.reverseOrder())))));

        return documentsCollection.stream().distinct().toList();
    }

    private LocalDateTime getUploadTimestampFromDocumentNode(JsonNode documentNode) {
        LocalDateTime documentNodeUploadTimestamp;
        try {
            documentNodeUploadTimestamp =
                Objects.isNull(documentNode.get(DOCUMENT_UPLOAD_TIMESTAMP)) ? null :
                    LocalDateTime.parse(documentNode.get(DOCUMENT_UPLOAD_TIMESTAMP).asText());
        } catch (Exception e) {
            log.error("Error getting upload timestamp for document url: {}", documentNode.get(DOCUMENT_URL).asText());
            documentNodeUploadTimestamp = null;
        }
        return documentNodeUploadTimestamp;
    }
}
