package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentRemovalWrapper;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.services.DeleteDocumentService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.documentremoval.postabouttosubmitaction.DocumentRemovalAboutToSubmitAction;
import uk.gov.hmcts.reform.prl.services.documentremoval.submittedaction.DocumentRemovalSubmittedAction;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_REMOVAL_CASE_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_REMOVAL_CONFIRM_OPTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_REMOVAL_DOCUMENT_TO_REMOVE;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentRemovalService {
    private final ObjectMapper objectMapper;
    private final DocumentExtractor documentRetriever;
    private final DocumentRemover documentRemover;
    private final DeleteDocumentService deleteDocumentService;
    private final SystemUserService systemUserService;
    private final List<DocumentRemovalAboutToSubmitAction> aboutToSubmitActions;
    private final List<DocumentRemovalSubmittedAction> submittedActions;

    private static final DateTimeFormatter UPLOAD_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    private static final String LEGAL_PROF_QUARANTINE_DOC_LIST = "legalProfQuarantineDocsList";
    private static final String COURT_STAFF_QUARANTINE_DOC_LIST = "courtStaffQuarantineDocsList";
    private static final String CAFCASS_QUARANTINE_DOC_LIST = "cafcassQuarantineDocsList";
    private static final String CITIZEN_QUARANTINE_DOC_LIST = "citizenQuarantineDocsList";
    private static final String COURT_NAV_QUARANTINE_DOCUMENT_LIST = "courtNavQuarantineDocumentList";
    private static final String LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB = "legalProfUploadDocListDocTab";
    private static final String CAFCASS_UPLOAD_DOC_LIST_DOC_TAB = "cafcassUploadDocListDocTab";
    private static final String LOCAL_AUTHORITY_UPLOAD_DOC_LIST_DOC_TAB = "localAuthorityUploadDocListDocTab";
    private static final String COURT_STAFF_UPLOAD_DOC_LIST_DOC_TAB = "courtStaffUploadDocListDocTab";
    private static final String BULK_SCANNED_DOC_LIST_DOC_TAB = "bulkScannedDocListDocTab";
    private static final String CITIZEN_UPLOADED_DOC_LIST_DOC_TAB = "citizenUploadedDocListDocTab";
    private static final String COURT_NAV_UPLOADED_DOC_LIST_DOC_TAB = "courtNavUploadedDocListDocTab";
    private static final String RESTRICTED_DOCUMENTS = "restrictedDocuments";
    private static final String CONFIDENTIAL_DOCUMENTS = "confidentialDocuments";

    /**
     * Gets a list of all documents on the case.
     *
     * @param caseDetails the case details
     * @return Map containing list of all documents on the case keyed by "documentRemovalCaseDocuments"
     */
    public Map<String, Object> getCaseDocuments(CaseDetails caseDetails) {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<Document> caseDocuments = documentRetriever.getCaseDocuments(caseData);

        DynamicList caseDocumentsDynamicList = DynamicList.builder()
            .listItems(caseDocuments.stream()
                           .map(doc -> DynamicListElement.builder()
                               .code(doc.getDocumentId())
                               .label(formatSelectDocumentLabel(doc))
                               .build())
                           .toList())
            .build()
            .withSortedListItemsByLabel();

        return Map.of(DOCUMENT_REMOVAL_CASE_DOCUMENTS, caseDocumentsDynamicList);
    }

    public Map<String, Object> getCaseDocumentSelectedForRemoval(CaseDetails caseDetails) {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        DocumentRemovalWrapper wrapper = caseData.getDocumentRemovalWrapper();
        DynamicListElement selectedDocument = wrapper.getDocumentRemovalCaseDocuments().getValue();
        String selectedDocumentId = selectedDocument.getCode();

        Document document = documentRetriever.getCaseDocuments(caseData).stream()
            .filter(doc -> doc.getDocumentId().equals(selectedDocumentId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Selected document not found in case documents"));

        return Map.of(DOCUMENT_REMOVAL_CASE_DOCUMENTS, caseData.getDocumentRemovalWrapper().getDocumentRemovalCaseDocuments(),
                      DOCUMENT_REMOVAL_DOCUMENT_TO_REMOVE, document);
    }

    public Map<String, Object> removeDocumentFromCaseData(CaseDetails caseDetails) throws IOException {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        DocumentRemovalWrapper wrapper = caseData.getDocumentRemovalWrapper();
        String documentIdToRemove = wrapper.getDocumentRemovalCaseDocuments().getValueCode();

        DocumentManagementDetails docMgmt = Optional.ofNullable(caseData.getDocumentManagementDetails())
            .orElseGet(() -> DocumentManagementDetails.builder().build());
        ReviewDocuments reviewDocs = Optional.ofNullable(caseData.getReviewDocuments())
            .orElseGet(() -> ReviewDocuments.builder().build());

        Map<String, Object> updatedCaseData = documentRemover.removeDocument(caseDetails.getData(), documentIdToRemove);

        // update the map using the field names
        updatedCaseData.put(
            LEGAL_PROF_QUARANTINE_DOC_LIST,
            removeById(docMgmt.getLegalProfQuarantineDocsList(), documentIdToRemove)
        );
        updatedCaseData.put(
            COURT_STAFF_QUARANTINE_DOC_LIST,
            removeById(docMgmt.getCourtStaffQuarantineDocsList(), documentIdToRemove)
        );
        updatedCaseData.put(
            CAFCASS_QUARANTINE_DOC_LIST,
            removeById(docMgmt.getCafcassQuarantineDocsList(), documentIdToRemove)
        );
        updatedCaseData.put(
            CITIZEN_QUARANTINE_DOC_LIST,
            removeById(docMgmt.getCitizenQuarantineDocsList(), documentIdToRemove)
        );
        updatedCaseData.put(
            COURT_NAV_QUARANTINE_DOCUMENT_LIST,
            removeById(docMgmt.getCourtNavQuarantineDocumentList(), documentIdToRemove)
        );

        updatedCaseData.put(
            LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB,
            removeById(reviewDocs.getLegalProfUploadDocListDocTab(), documentIdToRemove)
        );
        updatedCaseData.put(
            CAFCASS_UPLOAD_DOC_LIST_DOC_TAB,
            removeById(reviewDocs.getCafcassUploadDocListDocTab(), documentIdToRemove)
        );
        updatedCaseData.put(
            LOCAL_AUTHORITY_UPLOAD_DOC_LIST_DOC_TAB,
            removeById(reviewDocs.getLocalAuthorityUploadDocListDocTab(), documentIdToRemove)
        );
        updatedCaseData.put(
            COURT_STAFF_UPLOAD_DOC_LIST_DOC_TAB,
            removeById(reviewDocs.getCourtStaffUploadDocListDocTab(), documentIdToRemove)
        );
        updatedCaseData.put(
            BULK_SCANNED_DOC_LIST_DOC_TAB,
            removeById(reviewDocs.getBulkScannedDocListDocTab(), documentIdToRemove)
        );
        updatedCaseData.put(
            CITIZEN_UPLOADED_DOC_LIST_DOC_TAB,
            removeById(reviewDocs.getCitizenUploadedDocListDocTab(), documentIdToRemove)
        );
        updatedCaseData.put(
            COURT_NAV_UPLOADED_DOC_LIST_DOC_TAB,
            removeById(reviewDocs.getCourtNavUploadedDocListDocTab(), documentIdToRemove)
        );
        updatedCaseData.put(RESTRICTED_DOCUMENTS, removeById(reviewDocs.getRestrictedDocuments(), documentIdToRemove));
        updatedCaseData.put(
            CONFIDENTIAL_DOCUMENTS,
            removeById(reviewDocs.getConfidentialDocuments(), documentIdToRemove)
        );

        updatedCaseData.remove(DOCUMENT_REMOVAL_DOCUMENT_TO_REMOVE);
        updatedCaseData.remove(DOCUMENT_REMOVAL_CONFIRM_OPTIONS);
        // Cannot remove DOCUMENT_REMOVAL_CASE_DOCUMENTS as the selected document id is needed in the
        // submitted callback to delete the document from cdam

        caseDetails.setData(updatedCaseData);
        CaseData caseDataUpdated = CaseUtils.getCaseData(caseDetails, objectMapper);
        aboutToSubmitActions.forEach(action -> action.onAboutToSubmit(caseDataUpdated, updatedCaseData));

        return updatedCaseData;
    }

    private List<Element<QuarantineLegalDoc>> removeById(List<Element<QuarantineLegalDoc>> source, String toRemove) {
        if (source == null) {
            return null;
        }

        log.info("Removing document {} from document collection", toRemove);

        return source.stream()
            .filter(doc -> !Objects.requireNonNull(getDocumentFieldFromCollection(doc))
                .getDocumentId().equals(toRemove))
            .toList();
    }

    Document getDocumentFieldFromCollection(Element<QuarantineLegalDoc> quarantineLegalDocElement) {
        QuarantineLegalDoc quarantineLegalDoc = quarantineLegalDocElement.getValue();

        Map<String, Object> docObject = objectMapper.convertValue(quarantineLegalDoc, new TypeReference<>() {});

        String documentFieldName = DocumentUtils.populateAttributeNameFromCategoryId(
            quarantineLegalDoc.getCategoryId(),
            null
        );

        Document document;

        try {
            document = objectMapper.convertValue(docObject.get(documentFieldName), Document.class);
        } catch (NullPointerException e) {
            log.error("Field {} did not exist in QuarantineLegalDoc", documentFieldName, e);
            return null;
        }

        return document;
    }

    public void deleteDocument(CaseDetails caseDetails) throws IOException {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        DocumentRemovalWrapper wrapper = caseData.getDocumentRemovalWrapper();
        String documentId = wrapper.getDocumentRemovalCaseDocuments().getValueCode();

        String authToken = systemUserService.getSysUserToken();
        log.info("Deleting document with id {} from document store", documentId);
        deleteDocumentService.deleteDocument(authToken, documentId);
    }

    public void executePostSubmittedActions(CallbackRequest request) {
        submittedActions.forEach(action -> action.onSubmitted(request));
    }

    private String formatSelectDocumentLabel(Document caseDocument) {
        return caseDocument.getUploadTimeStamp() != null
            ? caseDocument.getDocumentFileName() + " (" + UPLOAD_TIMESTAMP_FORMATTER.format(caseDocument.getUploadTimeStamp()) + ")"
            : caseDocument.getDocumentFileName();
    }
}
