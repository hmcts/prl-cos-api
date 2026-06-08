package uk.gov.hmcts.reform.prl.services.renamedocument;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.DocumentCategoryService;
import uk.gov.hmcts.reform.prl.services.documentremoval.DocumentExtractor;
import uk.gov.hmcts.reform.prl.services.sendandreply.SendAndReplyService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.prl.services.sendandreply.SendAndReplyService.ARROW_SEPARATOR;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RenameDocumentService {

    private final ObjectMapper objectMapper;
    private final SendAndReplyService sendAndReplyService;
    private final DocumentExtractor documentExtractor;
    private final DocumentCategoryService documentCategoryService;


    public Map<String, Object> handleAboutToStart(String authorisation,
                                                  CallbackRequest callbackRequest) {
        log.info("Entering RenameDocument Event handleAboutToStart for case: {}", callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        DynamicList documentsList = sendAndReplyService.getCategoriesAndDocuments(
            authorisation,
            String.valueOf(caseData.getId())
        );

        documentsList = createDynamicListForRenameDocuments(caseData, documentsList);
        caseDataMap.put("renameDocumentsList", documentsList);


        DynamicList categoriesAndDocumentsList = documentCategoryService.retrieveDocumentCategories(authorisation, caseData, null);
        caseDataMap.put("categoryDocumentsList", categoriesAndDocumentsList);

        return caseDataMap;
    }

    public Map<String, Object> handleMidEvent(String authorisation,
                                              CallbackRequest callbackRequest) {
        log.info("Entering RenameDocument Event handleMidEvent for case: {}", callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        DynamicList categoriesAndDocumentsList = documentCategoryService.retrieveDocumentCategories(authorisation, caseData, null);

        if (caseData.getRenameDocument() != null && caseData.getRenameDocument().getRenameDocumentsList() != null) {
            DynamicList selectedList = caseData.getRenameDocument().getRenameDocumentsList();
            if (null != selectedList.getValue() && isNotBlank(selectedList.getValue().getCode())) {

                String documentName = selectedList.getValue().getLabel();
                caseDataMap.put("renameListDocSelected", documentName);

                String documentCode = selectedList.getValue().getCode();
                log.info("Selected document code: {}", documentCode);
                String[] codes = documentCode.split(ARROW_SEPARATOR);

                if (codes.length >= 2) {
                    String categoryIdFromCode = codes[codes.length - 2].trim();

                    categoriesAndDocumentsList.getListItems().stream()
                        .filter(element -> element.getCode().equals(categoryIdFromCode))
                        .findFirst()
                        .ifPresent(element -> {
                            log.info("Matching category found in list for code: {}. Setting as pre-selected value.", element.getLabel());
                            categoriesAndDocumentsList.setValue(element);
                        });
                }
            }
        }
        caseDataMap.put("categoryDocumentsList", categoriesAndDocumentsList);
        return caseDataMap;
    }

    public Map<String, Object> handleAboutToSubmit(CallbackRequest callbackRequest) {
        log.info("Entering RenameDocument Event handleAboutToSubmit for case: {}", callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        if (caseData.getRenameDocument() != null && caseData.getRenameDocument().getRenameDocumentsList() != null) {
            DynamicList selectedList = caseData.getRenameDocument().getRenameDocumentsList();
            if (null != selectedList.getValue() && isNotBlank(selectedList.getValue().getCode())) {
                String documentCode = selectedList.getValue().getCode();
                String[] codes = documentCode.split(ARROW_SEPARATOR);
                String documentId = codes[codes.length - 1].trim();

                String newName = caseData.getRenameDocument().getNewNameForDocument();
                String categoryId = null;
                if (caseData.getRenameDocument().getCategoryDocumentsList() != null
                    && caseData.getRenameDocument().getCategoryDocumentsList().getValue() != null) {
                    categoryId = caseData.getRenameDocument().getCategoryDocumentsList().getValue().getCode();
                }

                if (isNotBlank(newName)) {
                    findAndRenameDocument(caseDataMap, newName, documentId, categoryId);
                }
            }
        }

        caseDataMap.remove("renameDocumentsList");
        caseDataMap.remove("categoryDocumentsList");
        caseDataMap.remove("newNameForDocument");
        caseDataMap.remove("renameListDocSelected");
        caseDataMap.remove("renameDocument");
        return caseDataMap;
    }

    private void findAndRenameDocument(Object data, String newName, String documentId, String categoryId) {
        if (data instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) data;

            if (map.containsKey("document_url") && map.get("document_url") instanceof String) {
                String url = (String) map.get("document_url");
                if (url.contains(documentId)) {
                    String currentName = (String) map.get("document_filename");
                    String extension = FilenameUtils.getExtension(currentName);
                    String newUploadName = isNotBlank(extension) ? newName + "." + extension : newName;

                    log.info("Renaming the document");
                    map.put("document_filename", newUploadName);
                    map.put("category_id", categoryId);
                    return;
                }
            }
            for (Object value : map.values()) {
                findAndRenameDocument(value, newName, documentId, categoryId);
            }

        } else if (data instanceof List) {
            List<?> list = (List<?>) data;
            for (Object item : list) {
                findAndRenameDocument(item, newName, documentId, categoryId);
            }
        }
    }

    public List<String> validateRenamedField(Map<String, Object> caseDataUpdated) {
        String newNameForDocument = (String) caseDataUpdated.get("newNameForDocument");
        List<String> errors = new ArrayList<>();
        if (StringUtils.isNotBlank(newNameForDocument) && newNameForDocument.contains(".")) {
            errors.add("Document name must not include the file type");
        }
        String renameListDocSelected = (String) caseDataUpdated.get("renameListDocSelected");
        if (StringUtils.isNotBlank(renameListDocSelected)) {
            String[] parts = renameListDocSelected.split(ARROW_SEPARATOR);
            String documentName = parts[parts.length - 1].trim();
            if (documentName.startsWith("Confidential_")
                && StringUtils.isNotBlank(newNameForDocument)
                && !newNameForDocument.startsWith("Confidential_")) {
                errors.add("Please keep the prefix Confidential_ in the new file name");
            }
        }
        return errors;
    }

    private @NonNull DynamicList createDynamicListForRenameDocuments(CaseData caseData, DynamicList documentsList) {
        List<uk.gov.hmcts.reform.prl.models.documents.Document> allDocuments = documentExtractor.getCaseDocuments(
            caseData);

        List<uk.gov.hmcts.reform.prl.models.documents.Document> quarantineDocuments = documentExtractor.getCaseDocuments(
            CaseData.builder()
                .documentManagementDetails(caseData.getDocumentManagementDetails())
                .reviewDocuments(caseData.getReviewDocuments())
                .scannedDocuments(caseData.getScannedDocuments())
                .build()
        );

        List<String> quarantineDocumentIds = quarantineDocuments.stream()
            .map(uk.gov.hmcts.reform.prl.models.documents.Document::getDocumentId)
            .toList();

        List<String> existingDocumentIds = documentsList.getListItems().stream()
            .map(item -> {
                String[] codes = item.getCode().split(ARROW_SEPARATOR);
                return codes[codes.length - 1];
            })
            .toList();

        List<DynamicListElement> extraDocuments = allDocuments.stream()
            .filter(doc -> !quarantineDocumentIds.contains(doc.getDocumentId()))
            .filter(doc -> !existingDocumentIds.contains(doc.getDocumentId()))
            .map(doc -> DynamicListElement.builder()
                .code(doc.getDocumentId())
                .label(doc.getDocumentFileName())
                .build())
            .toList();

        List<DynamicListElement> allListItems = new ArrayList<>();
        if (documentsList.getListItems() != null) {
            allListItems.addAll(documentsList.getListItems().stream()
                                   .filter(item -> !item.getLabel().startsWith("Documents to be reviewed"))
                                   .toList());
        }
        allListItems.addAll(extraDocuments);

        documentsList = documentsList.toBuilder()
            .listItems(allListItems)
            .build()
            .withSortedListItemsByLabel();

        log.info(
            "Found {} documents for dynamic list",
            documentsList.getListItems() != null ? documentsList.getListItems().size() : 0
        );
        return documentsList;
    }
}
