package uk.gov.hmcts.reform.prl.services.renamedocument;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Optional;

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

    private static final List<String> FIELDS_FOR_UNCATEGORISED_DOCUMENTS = List.of(
        "finalServedApplicationDetailsList",
        "internalMessageAttachDocsList",
        "externalMessageAttachDocsList",
        "unServedApplicantPack",
        "unServedRespondentPack"
    );

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

        if (null != caseData.getRenameDocument() && null != caseData.getRenameDocument().getRenameDocumentsList()) {
            DynamicList selectedList = caseData.getRenameDocument().getRenameDocumentsList();
            addSelectedDocumentLabel(caseDataMap, selectedList);
            prepopulateCategoryList(categoriesAndDocumentsList, selectedList);
        }

        caseDataMap.put("categoryDocumentsList", categoriesAndDocumentsList);
        return caseDataMap;
    }

    public Map<String, Object> handleAboutToSubmit(CallbackRequest callbackRequest) {
        log.info("Entering RenameDocument Event handleAboutToSubmit for case: {}", callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        processRenamingDocument(caseData, caseDataMap);

        caseDataMap.remove("renameDocumentsList");
        caseDataMap.remove("categoryDocumentsList");
        caseDataMap.remove("newNameForDocument");
        caseDataMap.remove("renameListDocSelected");
        caseDataMap.remove("renameDocument");
        return caseDataMap;
    }

    public List<String> validateRenamedField(Map<String, Object> caseDataUpdated) {
        String newNameForDocument = (String) caseDataUpdated.get("newNameForDocument");
        List<String> errors = new ArrayList<>();
        if (StringUtils.isNotBlank(newNameForDocument) && newNameForDocument.contains(".")) {
            errors.add("Document name must not include the file type");
        }
        return errors;
    }

    private void processRenamingDocument(CaseData caseData, Map<String, Object> caseDataMap) {
        if (caseData.getRenameDocument() != null && caseData.getRenameDocument().getRenameDocumentsList() != null) {
            DynamicList selectedList = caseData.getRenameDocument().getRenameDocumentsList();
            if (null != selectedList.getValue() && isNotBlank(selectedList.getValue().getCode())) {
                String documentId = getDocumentId(selectedList);

                Optional<String> categoryId = getCategoryId(caseData);

                String newName = caseData.getRenameDocument().getNewNameForDocument();
                if (isNotBlank(newName)) {
                    findAndRenameDocument(
                        caseDataMap,
                        null,
                        false,
                        newName,
                        documentId,
                        categoryId.orElse(null),
                        isDocInstanceInCategories(caseDataMap, documentId, false));
                }
            }
        }
    }

    private Optional<String> getCategoryId(CaseData caseData) {
        String categoryId = null;
        if (caseData.getRenameDocument().getCategoryDocumentsList() != null
            && caseData.getRenameDocument().getCategoryDocumentsList().getValue() != null) {
            categoryId = caseData.getRenameDocument().getCategoryDocumentsList().getValue().getCode();
        }
        return Optional.ofNullable(categoryId);
    }

    private String getDocumentId(DynamicList selectedList) {
        String documentCode = selectedList.getValue().getCode();
        String[] codes = documentCode.split(ARROW_SEPARATOR);
        return codes[codes.length - 1].trim();
    }

    private void findAndRenameDocument(Object data, String rootFieldName, boolean isUncategorisedField,
                                       String newName, String documentId, String categoryId,
                                       boolean isCategorisedInstancePresent) {
        if (data instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) data;


            if (map.containsKey("document_url") && map.get("document_url") instanceof String) {
                String url = (String) map.get("document_url");
                if (url.contains(documentId)) {
                    updateDocumentMetadata(map, rootFieldName, isUncategorisedField, newName, categoryId, isCategorisedInstancePresent);
                    return;
                }
            }
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                String currentRootField = (rootFieldName == null) ? key : rootFieldName;
                boolean nextIsUncategorisedField = isUncategorisedField || FIELDS_FOR_UNCATEGORISED_DOCUMENTS.contains(key);
                findAndRenameDocument(entry.getValue(),
                                      currentRootField,
                                      nextIsUncategorisedField,
                                      newName,
                                      documentId,
                                      categoryId,
                                      isCategorisedInstancePresent);
            }

        } else if (data instanceof List) {
            List<?> list = (List<?>) data;
            for (Object item : list) {
                findAndRenameDocument(item, rootFieldName, isUncategorisedField, newName, documentId, categoryId, isCategorisedInstancePresent);
            }
        }
    }

    private void updateDocumentMetadata(Map<String, Object> documentMap,
                                     String rootFieldName,
                                     boolean isUncategorisedField,
                                     String newName,
                                     String categoryId,
                                     boolean isCategorisedInstancePresent) {
        String currentName = (String) documentMap.get("document_filename");
        String extension = FilenameUtils.getExtension(currentName);
        String newUploadName = checkForConfidentialPrefix(newName, currentName, extension);

        log.info("Renaming the document in field: {}", rootFieldName);
        documentMap.put("document_filename", newUploadName);

        if (!isUncategorisedField || !isCategorisedInstancePresent) {
            log.info("About to add the category id for rootField: {}", rootFieldName);
            documentMap.put("category_id", categoryId);
        }
    }

    private boolean isDocInstanceInCategories(Object data, String documentId, boolean isDocInstanceInUncategorisedDocs) {
        if (data instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) data;

            if (map.containsKey("document_url") && map.get("document_url") instanceof String) {
                String url = (String) map.get("document_url");
                if (url.contains(documentId)) {
                    return !isDocInstanceInUncategorisedDocs;
                }
            }
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                boolean nextDocInstanceInUncategorisedDocs =
                    isDocInstanceInUncategorisedDocs || FIELDS_FOR_UNCATEGORISED_DOCUMENTS.contains(entry.getKey());
                if (isDocInstanceInCategories(entry.getValue(), documentId, nextDocInstanceInUncategorisedDocs)) {
                    return true;
                }
            }
        } else if (data instanceof List) {
            List<?> list = (List<?>) data;
            for (Object item : list) {
                if (isDocInstanceInCategories(item, documentId, isDocInstanceInUncategorisedDocs)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String checkForConfidentialPrefix(String newName, String currentName, String extension) {
        String newUploadName;
        if (currentName.startsWith("Confidential_")) {
            String confidentialPrefix = "Confidential_";
            String nameWithoutPrefix = newName.startsWith(confidentialPrefix)
                ? newName.substring(confidentialPrefix.length())
                : newName;

            newUploadName = isNotBlank(extension)
                ? confidentialPrefix + nameWithoutPrefix + "." + extension
                : confidentialPrefix + nameWithoutPrefix;
        } else {
            newUploadName = isNotBlank(extension) ? newName + "." + extension : newName;
        }
        return newUploadName;
    }


    private DynamicList createDynamicListForRenameDocuments(CaseData caseData, DynamicList documentsList) {
        List<uk.gov.hmcts.reform.prl.models.documents.Document> allDocuments = documentExtractor.getCaseDocuments(caseData);
        List<String> quarantineDocumentIds = getQuarantineDocumentIds(caseData);

        List<String> existingDocumentIds = documentsList.getListItems() != null
            ? documentsList.getListItems().stream()
                .map(item -> {
                    String[] codes = item.getCode().split(ARROW_SEPARATOR);
                    return codes[codes.length - 1];
                })
                .toList()
            : List.of();

        List<DynamicListElement> extraDocuments = allDocuments.stream()
            .filter(doc -> !quarantineDocumentIds.contains(doc.getDocumentId()))
            .filter(doc -> !existingDocumentIds.contains(doc.getDocumentId()))
            .map(doc -> DynamicListElement.builder()
                .code(doc.getDocumentId())
                .label(doc.getDocumentFileName())
                .build())
            .toList();

        List<DynamicListElement> filteredExistingItems = documentsList.getListItems() != null
            ? documentsList.getListItems().stream()
                .filter(item -> !item.getLabel().startsWith("Documents to be reviewed"))
                .toList()
            : List.of();

        List<DynamicListElement> allListItems = new ArrayList<>(filteredExistingItems);
        allListItems.addAll(extraDocuments);

        return documentsList.toBuilder()
            .listItems(allListItems)
            .build()
            .withSortedListItemsByLabel();
    }

    private List<String> getQuarantineDocumentIds(CaseData caseData) {
        return documentExtractor.getCaseDocuments(
            CaseData.builder()
                .documentManagementDetails(caseData.getDocumentManagementDetails())
                .reviewDocuments(caseData.getReviewDocuments())
                .scannedDocuments(caseData.getScannedDocuments())
                .build()
        ).stream().map(uk.gov.hmcts.reform.prl.models.documents.Document::getDocumentId).toList();
    }

    private void addSelectedDocumentLabel(Map<String, Object> caseDataMap, DynamicList selectedList) {
        if (null != selectedList.getValue() && isNotBlank(selectedList.getValue().getLabel())) {
            caseDataMap.put("renameListDocSelected", selectedList.getValue().getLabel());
        }
    }

    private void prepopulateCategoryList(DynamicList categoriesAndDocumentsList, DynamicList selectedList) {
        if (null != selectedList.getValue() && isNotBlank(selectedList.getValue().getCode())) {
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
}
