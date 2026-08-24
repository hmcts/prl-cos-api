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
import uk.gov.hmcts.reform.prl.models.complextypes.ScannedDocument;
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

        // Remove documents where the parent collection item should also be removed
        // as it serves no purpose without a document
        // e.g. Documents to be reviewed, Draft orders
        updateDocumentCollections(caseDetails.getData(), caseData, documentIdToRemove);

        Map<String, Object> updatedCaseData = documentRemover.removeDocument(caseDetails.getData(), documentIdToRemove);

        updatedCaseData.remove(DOCUMENT_REMOVAL_DOCUMENT_TO_REMOVE);
        updatedCaseData.remove(DOCUMENT_REMOVAL_CONFIRM_OPTIONS);
        // Cannot remove DOCUMENT_REMOVAL_CASE_DOCUMENTS as the selected document id is needed in the
        // submitted callback to delete the document from cdam

        caseDetails.setData(updatedCaseData);
        CaseData caseDataUpdated = CaseUtils.getCaseData(caseDetails, objectMapper);
        aboutToSubmitActions.forEach(action -> action.onAboutToSubmit(caseDataUpdated, updatedCaseData));

        return updatedCaseData;
    }

    private void updateDocumentCollections(Map<String, Object> updatedCaseData, CaseData caseData,
                                           String documentIdToRemove) {

        DocumentManagementDetails docMgmt = Optional.ofNullable(caseData.getDocumentManagementDetails())
            .orElseGet(() -> DocumentManagementDetails.builder().build());
        ReviewDocuments reviewDocs = Optional.ofNullable(caseData.getReviewDocuments())
            .orElseGet(() -> ReviewDocuments.builder().build());

        List<Element<ScannedDocument>> scannedDocuments = caseData.getScannedDocuments();
        putUpdatedScannedDocs(updatedCaseData, scannedDocuments, documentIdToRemove);

        putIfNotNull(updatedCaseData, LEGAL_PROF_QUARANTINE_DOC_LIST,
                     docMgmt.getLegalProfQuarantineDocsList(), documentIdToRemove);
        putIfNotNull(updatedCaseData, COURT_STAFF_QUARANTINE_DOC_LIST,
                     docMgmt.getCourtStaffQuarantineDocsList(), documentIdToRemove);
        putIfNotNull(updatedCaseData, CAFCASS_QUARANTINE_DOC_LIST,
                     docMgmt.getCafcassQuarantineDocsList(), documentIdToRemove);
        putIfNotNull(updatedCaseData, CITIZEN_QUARANTINE_DOC_LIST,
                     docMgmt.getCitizenQuarantineDocsList(), documentIdToRemove);
        putIfNotNull(updatedCaseData, COURT_NAV_QUARANTINE_DOCUMENT_LIST,
                     docMgmt.getCourtNavQuarantineDocumentList(), documentIdToRemove);
        putIfNotNull(updatedCaseData, LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB,
                     reviewDocs.getLegalProfUploadDocListDocTab(), documentIdToRemove);
        putIfNotNull(updatedCaseData, CAFCASS_UPLOAD_DOC_LIST_DOC_TAB,
                     reviewDocs.getCafcassUploadDocListDocTab(), documentIdToRemove);
        putIfNotNull(updatedCaseData, LOCAL_AUTHORITY_UPLOAD_DOC_LIST_DOC_TAB,
                     reviewDocs.getLocalAuthorityUploadDocListDocTab(), documentIdToRemove);
        putIfNotNull(updatedCaseData, COURT_STAFF_UPLOAD_DOC_LIST_DOC_TAB,
                     reviewDocs.getCourtStaffUploadDocListDocTab(), documentIdToRemove);
        putIfNotNull(updatedCaseData, BULK_SCANNED_DOC_LIST_DOC_TAB,
                     reviewDocs.getBulkScannedDocListDocTab(), documentIdToRemove);
        putIfNotNull(updatedCaseData, CITIZEN_UPLOADED_DOC_LIST_DOC_TAB,
                     reviewDocs.getCitizenUploadedDocListDocTab(), documentIdToRemove);
        putIfNotNull(updatedCaseData, COURT_NAV_UPLOADED_DOC_LIST_DOC_TAB,
                     reviewDocs.getCourtNavUploadedDocListDocTab(), documentIdToRemove);
        putIfNotNull(updatedCaseData, RESTRICTED_DOCUMENTS,
                     reviewDocs.getRestrictedDocuments(), documentIdToRemove);
        putIfNotNull(updatedCaseData, CONFIDENTIAL_DOCUMENTS,
                     reviewDocs.getConfidentialDocuments(), documentIdToRemove);
    }

    private void putIfNotNull(Map<String, Object> data, String key, List<Element<QuarantineLegalDoc>> source,
                              String documentIdToRemove) {
        if (source != null) {
            List<Element<QuarantineLegalDoc>> updatedDocs = removeById(source, documentIdToRemove);
            if (source.size() != updatedDocs.size()) {
                data.put(key, updatedDocs);
                log.info("Updated {} collection in case data after removing document with id {}", key, documentIdToRemove);
            }
        }
    }

    private void putUpdatedScannedDocs(Map<String, Object> data, List<Element<ScannedDocument>> source,
                              String documentIdToRemove) {
        if (source != null) {
            List<Element<ScannedDocument>> updatedDocs = source.stream()
                .filter(doc -> shouldKeepScannedDocument(doc, documentIdToRemove))
                .toList();

            if (source.size() != updatedDocs.size()) {
                data.put("scannedDocuments", updatedDocs);
                log.info("Updated scannedDocuments collection in case data after removing scanned document with id {}",
                         documentIdToRemove);
            }
        }
    }

    private boolean shouldKeepScannedDocument(Element<ScannedDocument> element, String documentIdToRemove) {
        ScannedDocument scanned = element.getValue();
        if (scanned == null || scanned.url == null) {
            return true;
        }
        String docId = scanned.url.getDocumentId();
        return docId == null || !docId.equals(documentIdToRemove);
    }

    private List<Element<QuarantineLegalDoc>> removeById(List<Element<QuarantineLegalDoc>> source, String toRemove) {
        return source.stream()
            .filter(doc -> getDocumentFieldFromCollection(doc) == null || !getDocumentFieldFromCollection(doc)
                .getDocumentId().equals(toRemove))
            .toList();
    }

    private Document getDocumentFieldFromCollection(Element<QuarantineLegalDoc> quarantineLegalDocElement) {
        QuarantineLegalDoc quarantineLegalDoc = quarantineLegalDocElement.getValue();

        Map<String, Object> docObject = objectMapper.convertValue(quarantineLegalDoc, new TypeReference<>() {});

        String documentFieldName = DocumentUtils.populateAttributeNameFromCategoryId(
            quarantineLegalDoc.getCategoryId(),
            null
        );

        Document document;

        try {
            document = objectMapper.convertValue(docObject.get(documentFieldName), Document.class);
            if (document == null) {
                documentFieldName = "document";
                document = objectMapper.convertValue(docObject.get(documentFieldName), Document.class);
            }
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
