package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.documentremoval.DocumentToKeep;
import uk.gov.hmcts.reform.prl.models.documentremoval.DocumentToKeepCollection;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@Service
@AllArgsConstructor
@Slf4j
public class DocumentRemovalService {

    private ObjectMapper objectMapper;
    private final DocumentRetriever documentRetriever;
    private final DocumentRemover documentRemover;

    public Map<String, Object> getDocumentsToKeepCollection(CaseDetails caseDetails) {
        List<DocumentToKeepCollection> documents = getDocuments(caseDetails);
        return Map.of("documentToKeepCollection", documents);
    }

    public List<DocumentToKeepCollection> getDocuments(CaseDetails caseDetails) {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        return documentRetriever.getDocuments(caseData);
    }

    public Map<String, Object> removeDocuments(CaseDetails caseDetails) throws IOException {
        List<DocumentToKeep> documentsToDelete = getDocumentsToDelete(caseDetails);
        if (documentsToDelete.isEmpty()) {
            log.info(format("No documents to delete for case ID %s", caseDetails.getId()));
            return caseDetails.getData();
        }

        JsonNode root = objectMapper.valueToTree(caseDetails.getData());
        documentsToDelete.forEach(documentToKeep -> documentRemover.removeDocumentFromJson(root, documentToKeep));

        JsonParser jsonParser = objectMapper.treeAsTokens(root);
        return objectMapper.readValue(jsonParser, Map.class);
    }

    private List<DocumentToKeep> getDocumentsToDelete(CaseDetails caseDetails) {
        Map<String, Object> caseDataMap = caseDetails.getData();
        List<String> documentsToKeep = ((List<LinkedHashMap>) caseDataMap.get("documentToKeepCollection")).stream()
            .map(map -> map.get("value"))
            .map(value -> objectMapper.convertValue(value, DocumentToKeep.class))
            .map(DocumentToKeep::getDocumentId)
            .toList();

        return getDocuments(caseDetails).stream()
            .map(DocumentToKeepCollection::getValue)
            .filter(document -> !documentsToKeep.contains(document.getDocumentId()))
            .toList();
    }

//    private Map<String, Object> deepCopyMap(Map<String, Object> original) {
//        // Use ObjectMapper for deep copy to avoid mutating input
//        return objectMapper.convertValue(
//            objectMapper.valueToTree(original),
//            Map.class
//        );
//    }
}
