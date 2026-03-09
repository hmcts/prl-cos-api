package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.documentremoval.DocumentToKeep;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_URL;

@Service
@Slf4j
public class DocumentRemover {

    private static final String VALUE_KEY = "value";

    public void removeDocumentFromJson(JsonNode root, DocumentToKeep documentToDelete) {
        List<String> fieldsToRemove = new ArrayList<>();

        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();

            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);

                if (shouldRemoveDocument(fieldValue,
                                         documentToDelete.getCaseDocument().getDocumentUrl())) {
                    log.info(String.format("Deleting doc from CaseData JSON root with url %s", documentToDelete.getCaseDocument().getDocumentUrl()));
                    fieldsToRemove.add(fieldName);
                } else {
                    removeDocumentFromJson(fieldValue, documentToDelete);
                }
            }
        } else if (root.isArray()) {
            processArrayNode(root, documentToDelete);
        }

        for (String fieldName : fieldsToRemove) {
            ((ObjectNode) root).remove(fieldName);
        }
    }

    private void processArrayNode(JsonNode root, DocumentToKeep documentToDelete) {
        ArrayNode arrayNode = (ArrayNode) root;
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode arrayElement = arrayNode.get(i);
            if (arrayElement.has(VALUE_KEY)) {
                JsonNode valueObject = arrayElement.get(VALUE_KEY);
                Iterator<String> fieldNames = valueObject.fieldNames();

                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode fieldValue = valueObject.get(fieldName);

                    if (fieldValue.asText().equals(
                        documentToDelete.getCaseDocument().getDocumentUrl())
                        || shouldRemoveDocument(fieldValue,
                                                documentToDelete.getCaseDocument().getDocumentUrl())) {
                        log.info(String.format("Deleting doc from CaseData JSON array node with url %s",
                                               documentToDelete.getCaseDocument().getDocumentUrl()));
                        ((ArrayNode) root).remove(i);
                    }
                }
            }
            removeDocumentFromJson(arrayElement, documentToDelete);
        }
    }

    private boolean shouldRemoveDocument(JsonNode fieldValue, String documentToKeepUrl) {
        return fieldValue.has(DOCUMENT_URL)
            && fieldValue.get(DOCUMENT_URL).asText().equals(documentToKeepUrl);
    }
}
