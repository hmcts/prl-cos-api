package uk.gov.hmcts.reform.prl.services.renamedocument;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.sendandreply.SendAndReplyService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CHILD_IMPACT_REPORT_1_LA;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CHILD_IMPACT_REPORT_2_LA;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CIR_EXTENSION_REQUEST_LA;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.CIR_TRANSFER_REQUEST_LA;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.LOCAL_AUTHORITY_INVOLVEMENT_LA;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SECTION_47_LA;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SECTION_7_ADDENDUM_REPORT_LA;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.SECTION_7_REPORT_LA;
import static uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc.quarantineCategoriesToRemove;
import static uk.gov.hmcts.reform.prl.services.sendandreply.SendAndReplyService.ARROW_SEPARATOR;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RenameDocumentService {

    private final ObjectMapper objectMapper;
    private final SendAndReplyService sendAndReplyService;
    private final UserService userService;
    private final RoleAssignmentService roleAssignmentService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    private static final List<String> EXCLUDED_LA_DOCS_LIST_FOR_ADMIN = List.of(
        CHILD_IMPACT_REPORT_1_LA,
        CHILD_IMPACT_REPORT_2_LA,
        SECTION_7_REPORT_LA,
        SECTION_7_ADDENDUM_REPORT_LA,
        LOCAL_AUTHORITY_INVOLVEMENT_LA,
        SECTION_47_LA,
        CIR_EXTENSION_REQUEST_LA,
        CIR_TRANSFER_REQUEST_LA
    );

    public Map<String, Object> handleAboutToStart(String authorisation,
                                                  CallbackRequest callbackRequest) {
        log.info("Entering handleAboutToStart for case: {}", callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        DynamicList documentsList = sendAndReplyService.getCategoriesAndDocuments(
            authorisation,
            String.valueOf(caseData.getId())
        );
        log.info(
            "Found {} documents for dynamic list",
            documentsList.getListItems() != null ? documentsList.getListItems().size() : 0
        );
        caseDataMap.put("renameDocumentsList", documentsList);

        UserDetails userDetails = userService.getUserDetails(authorisation);
        boolean isUserRoleLA = isUserAllocatedRoleForCaseLA(String.valueOf(caseData.getId()), userDetails.getId());
        log.info("Is user Local Authority: {}", isUserRoleLA);

        DynamicList categoriesAndDocumentsList = getCategoriesSubcategories(
            authorisation,
            String.valueOf(caseData.getId()),
            isUserRoleLA
        );
        caseDataMap.put("categoryDocumentsList", categoriesAndDocumentsList);

        return caseDataMap;
    }

    public Map<String, Object> handleAboutToSubmit(String authorisation, CallbackRequest callbackRequest) {
        log.info("Entering handleAboutToSubmit for case: {}", callbackRequest.getCaseDetails().getId());
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);

        if (caseData.getRenameDocument() != null && caseData.getRenameDocument().getRenameDocumentsList() != null) {
            DynamicList selectedList = caseData.getRenameDocument().getRenameDocumentsList();
            log.info(
                "Selected document from dynamic list: {}",
                selectedList.getValue() != null ? selectedList.getValue().getCode() : "null"
            );

            if (null != selectedList.getValue() && null != selectedList.getValue().getCode()) {
                String documentCode = selectedList.getValue().getCode();
                String[] codes = documentCode.split(ARROW_SEPARATOR);
                String documentId = codes[codes.length - 1];

                String newName = caseData.getRenameDocument().getNewNameForDocument();

                if (isNotBlank(newName)) {
                    log.info("Searching for document with ID: {}, to be rename: {}", documentId, newName);
                }
                findAndRenameDocument(caseDataMap, newName, documentId);
            }
        } else {
            log.warn("RenameDocument or RenameDocumentsList is null in caseData");
        }

        caseDataMap.remove("renameDocument");
        return caseDataMap;
    }

    private void findAndRenameDocument(Object data, String newName, String documentId) {
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
                    return;
                }
            }

            for (Object value : map.values()) {
                findAndRenameDocument(value, newName, documentId);
            }

        } else if (data instanceof List) {
            List<?> list = (List<?>) data;
            for (Object item : list) {
                findAndRenameDocument(item, newName, documentId);
            }
        }

    }

    private uk.gov.hmcts.reform.ccd.client.model.Document getSelectedDocumentFromDynamicList(String authorisation,
                                                                                             DynamicList selectedDocument,
                                                                                             String caseId) {
        log.info("Fetching document for ID: {}", selectedDocument.getValue().getCode());
        try {
            CategoriesAndDocuments categoriesAndDocuments = coreCaseDataApi.getCategoriesAndDocuments(
                authorisation,
                authTokenGenerator.generate(),
                caseId
            );
            uk.gov.hmcts.reform.ccd.client.model.Document selectedDoc = getSelectedDocumentFromCategories(
                categoriesAndDocuments.getCategories(),
                selectedDocument
            );
            if (selectedDoc == null) {
                log.info("Document not found in categories, searching in uncategorised documents...");
                for (uk.gov.hmcts.reform.ccd.client.model.Document document : categoriesAndDocuments.getUncategorisedDocuments()) {
                    if (sendAndReplyService.fetchDocumentIdFromUrl(document.getDocumentURL())
                        .equalsIgnoreCase(selectedDocument.getValue().getCode())) {
                        selectedDoc = document;
                        break;
                    }
                }
            }
            return selectedDoc;
        } catch (FeignException e) {
            log.error("Feign error in getCategoriesAndDocuments: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error in getCategoriesAndDocuments: {}", e.getMessage());
        }
        return null;
    }

    private uk.gov.hmcts.reform.ccd.client.model.Document getSelectedDocumentFromCategories(List<Category> categoryList,
                                                                                            DynamicList selectedDocument) {
        uk.gov.hmcts.reform.ccd.client.model.Document documentSelected = null;

        for (Category category : categoryList) {
            log.info("Searching in category: {}", category.getCategoryId());
            if (category.getDocuments() != null) {
                for (uk.gov.hmcts.reform.ccd.client.model.Document document : category.getDocuments()) {
                    String[] codes = selectedDocument.getValue().getCode().split(ARROW_SEPARATOR);
                    String docId = codes[codes.length - 1];

                    if (sendAndReplyService.fetchDocumentIdFromUrl(document.getDocumentURL())
                        .equalsIgnoreCase(docId)) {
                        log.info(
                            "Matched document: {} in category: {}",
                            document.getDocumentFilename(),
                            category.getCategoryId()
                        );
                        documentSelected = document;
                        break;
                    }
                }
            }
            if (null == documentSelected && category.getSubCategories() != null) {
                documentSelected = getSelectedDocumentFromCategories(
                    category.getSubCategories(),
                    selectedDocument
                );
            }
            if (documentSelected != null) {
                break;
            }
        }
        return documentSelected;
    }

    private uk.gov.hmcts.reform.prl.models.documents.Document applyDocumentRename(
        uk.gov.hmcts.reform.prl.models.documents.Document document,
        String documentNewName) {
        log.info("Applying rename for document: {} to new name: {}", document.getDocumentFileName(), documentNewName);
        if (isNotBlank(documentNewName) && nonNull(document)) {
            uk.gov.hmcts.reform.prl.models.documents.Document renamedDocument = document.toBuilder()
                .documentFileName(determineChangedDocumentFileName(documentNewName, document))
                .build();
            log.info("Successfully created renamed document object: {}", renamedDocument.getDocumentFileName());
            return renamedDocument;
        }
        log.warn("Either documentNewName is blank or document is null. No rename applied.");
        return document;
    }

    private String determineChangedDocumentFileName(String newDocumentName, uk.gov.hmcts.reform.prl.models.documents.Document document) {
        String originalName = document.getDocumentFileName();
        String extension = FilenameUtils.getExtension(originalName);
        String newName = isNotBlank(extension) ? newDocumentName + "." + extension : newDocumentName;
        log.info("Renaming document name {} with new name {}", originalName, newName);
        return newName;
    }


    private DynamicList getCategoriesSubcategories(String authorisation, String caseReference, boolean isUserRoleLA) {
        try {
            CategoriesAndDocuments categoriesAndDocuments = coreCaseDataApi.getCategoriesAndDocuments(
                authorisation,
                authTokenGenerator.generate(),
                caseReference
            );
            if (null != categoriesAndDocuments) {
                List<Category> parentCategories = nullSafeCollection(categoriesAndDocuments.getCategories())
                    .stream()
                    .filter(category -> !isUserRoleLA || category.getCategoryId().equals("localAuthorityDocuments"))
                    .sorted(Comparator.comparing(Category::getCategoryName))
                    .toList();

                List<String> docsToExclude = new ArrayList<>(List.of(quarantineCategoriesToRemove()));
                if (!isUserRoleLA) {
                    docsToExclude.addAll(EXCLUDED_LA_DOCS_LIST_FOR_ADMIN);
                }

                List<DynamicListElement> dynamicListElementList = new ArrayList<>();
                CaseUtils.createCategorySubCategoryDynamicList(
                    parentCategories,
                    dynamicListElementList,
                    docsToExclude
                );

                return DynamicList.builder().value(DynamicListElement.EMPTY)
                    .listItems(dynamicListElementList).build();
            }
        } catch (Exception e) {
            log.error("Error in getCategoriesAndDocuments method", e);
        }
        return DynamicList.builder()
            .value(DynamicListElement.EMPTY).build();
    }

    private boolean isUserAllocatedRoleForCaseLA(String caseId, String idamId) {
        return roleAssignmentService.isUserAllocatedRoleForCase(caseId, idamId, Roles.LOCAL_AUTHORITY_STAFF.getValue())
            || roleAssignmentService.isUserAllocatedRoleForCase(
            caseId,
            idamId,
            Roles.LOCAL_AUTHORITY_SOLICITOR.getValue()
        );
    }

}
