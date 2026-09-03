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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
    static final String LEGAL_PROF_QUARANTINE_DOC_LIST = "legalProfQuarantineDocsList";
    static final String COURT_STAFF_QUARANTINE_DOC_LIST = "courtStaffQuarantineDocsList";
    static final String CAFCASS_QUARANTINE_DOC_LIST = "cafcassQuarantineDocsList";
    static final String CITIZEN_QUARANTINE_DOC_LIST = "citizenQuarantineDocsList";
    static final String COURT_NAV_QUARANTINE_DOCUMENT_LIST = "courtNavQuarantineDocumentList";
    static final String LOCAL_AUTHORITY_QUARANTINE_DOC_LIST = "localAuthorityQuarantineDocsList";
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
        updateQuarantineDocumentCollections(updatedCaseData, caseData, documentIdToRemove);
        updateCaseDocumentCollections(updatedCaseData, caseData, documentIdToRemove);
        updateScannedDocuments(updatedCaseData, caseData, documentIdToRemove);
    }

    private void updateQuarantineDocumentCollections(Map<String, Object> updatedCaseData, CaseData caseData,
                                                     String documentIdToRemove) {
        DocumentManagementDetails docMgmt = Optional.ofNullable(caseData.getDocumentManagementDetails())
            .orElseGet(() -> DocumentManagementDetails.builder().build());
        updateQuarantineDocumentCollection(updatedCaseData, LEGAL_PROF_QUARANTINE_DOC_LIST,
                                           docMgmt.getLegalProfQuarantineDocsList(), documentIdToRemove);
        updateQuarantineDocumentCollection(updatedCaseData, COURT_STAFF_QUARANTINE_DOC_LIST,
                                           docMgmt.getCourtStaffQuarantineDocsList(), documentIdToRemove);
        updateQuarantineDocumentCollection(updatedCaseData, CAFCASS_QUARANTINE_DOC_LIST,
                                           docMgmt.getCafcassQuarantineDocsList(), documentIdToRemove);
        updateQuarantineDocumentCollection(updatedCaseData, LOCAL_AUTHORITY_QUARANTINE_DOC_LIST,
                                           docMgmt.getLocalAuthorityQuarantineDocsList(), documentIdToRemove);
        updateQuarantineDocumentCollection(updatedCaseData, CITIZEN_QUARANTINE_DOC_LIST,
                                           docMgmt.getCitizenQuarantineDocsList(), documentIdToRemove);
        updateQuarantineDocumentCollection(updatedCaseData, COURT_NAV_QUARANTINE_DOCUMENT_LIST,
                                           docMgmt.getCourtNavQuarantineDocumentList(), documentIdToRemove);
    }

    private void updateQuarantineDocumentCollection(Map<String, Object> data, String key, List<Element<QuarantineLegalDoc>> source,
                                                    String documentIdToRemove) {
        if (source == null) {
            return;
        }

        List<Element<QuarantineLegalDoc>> updatedDocs = source.stream()
            .filter(doc -> shouldKeepQuarantineDocument(key, doc, documentIdToRemove))
            .toList();

        if (source.size() != updatedDocs.size()) {
            data.put(key, updatedDocs);
            log.info("Updated {} quarantine document collection in case data after removing document with id {}", key,
                     documentIdToRemove);
        }
    }

    private boolean shouldKeepQuarantineDocument(String key, Element<QuarantineLegalDoc> source, String documentIdToRemove) {
        QuarantineLegalDoc quarantineLegalDoc = source.getValue();

        Document document = switch (key) {
            case CITIZEN_QUARANTINE_DOC_LIST -> quarantineLegalDoc.getCitizenQuarantineDocument();
            case COURT_STAFF_QUARANTINE_DOC_LIST -> quarantineLegalDoc.getCourtStaffQuarantineDocument();
            case CAFCASS_QUARANTINE_DOC_LIST -> quarantineLegalDoc.getCafcassQuarantineDocument();
            case LEGAL_PROF_QUARANTINE_DOC_LIST -> quarantineLegalDoc.getDocument();
            case COURT_NAV_QUARANTINE_DOCUMENT_LIST -> quarantineLegalDoc.getCourtNavQuarantineDocument();
            case LOCAL_AUTHORITY_QUARANTINE_DOC_LIST -> quarantineLegalDoc.getLocalAuthorityQuarantineDocument();
            default -> null;
        };

        return document == null || !document.getDocumentId().equals(documentIdToRemove);
    }

    private void updateCaseDocumentCollections(Map<String, Object> updatedCaseData, CaseData caseData,
                                               String documentIdToRemove) {
        ReviewDocuments reviewDocs = Optional.ofNullable(caseData.getReviewDocuments())
            .orElseGet(() -> ReviewDocuments.builder().build());

        updateCaseDocumentsCollection(updatedCaseData, LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB,
                                      reviewDocs.getLegalProfUploadDocListDocTab(), documentIdToRemove);
        updateCaseDocumentsCollection(updatedCaseData, CAFCASS_UPLOAD_DOC_LIST_DOC_TAB,
                                      reviewDocs.getCafcassUploadDocListDocTab(), documentIdToRemove);
        updateCaseDocumentsCollection(updatedCaseData, LOCAL_AUTHORITY_UPLOAD_DOC_LIST_DOC_TAB,
                                      reviewDocs.getLocalAuthorityUploadDocListDocTab(), documentIdToRemove);
        updateCaseDocumentsCollection(updatedCaseData, COURT_STAFF_UPLOAD_DOC_LIST_DOC_TAB,
                                      reviewDocs.getCourtStaffUploadDocListDocTab(), documentIdToRemove);
        updateCaseDocumentsCollection(updatedCaseData, BULK_SCANNED_DOC_LIST_DOC_TAB,
                                      reviewDocs.getBulkScannedDocListDocTab(), documentIdToRemove);
        updateCaseDocumentsCollection(updatedCaseData, CITIZEN_UPLOADED_DOC_LIST_DOC_TAB,
                                      reviewDocs.getCitizenUploadedDocListDocTab(), documentIdToRemove);
        updateCaseDocumentsCollection(updatedCaseData, COURT_NAV_UPLOADED_DOC_LIST_DOC_TAB,
                                      reviewDocs.getCourtNavUploadedDocListDocTab(), documentIdToRemove);
        updateCaseDocumentsCollection(updatedCaseData, RESTRICTED_DOCUMENTS,
                                      reviewDocs.getRestrictedDocuments(), documentIdToRemove);
        updateCaseDocumentsCollection(updatedCaseData, CONFIDENTIAL_DOCUMENTS,
                                      reviewDocs.getConfidentialDocuments(), documentIdToRemove);
    }

    private void updateCaseDocumentsCollection(Map<String, Object> data, String key, List<Element<QuarantineLegalDoc>> source,
                              String documentIdToRemove) {
        if (source == null) {
            return;
        }

        List<Element<QuarantineLegalDoc>> updatedDocs = source.stream()
            .filter(doc -> shouldKeepCaseDocument(doc, documentIdToRemove))
            .toList();

        if (source.size() != updatedDocs.size()) {
            data.put(key, updatedDocs);
            log.info("Updated {} collection in case data after removing document with id {}", key, documentIdToRemove);
        }
    }

    private boolean shouldKeepCaseDocument(Element<QuarantineLegalDoc> source, String documentIdToRemove) {
        QuarantineLegalDoc quarantineLegalDoc = source.getValue();

        Map<String, Object> docObject = objectMapper.convertValue(quarantineLegalDoc, new TypeReference<>() {});

        String documentFieldName = DocumentUtils.populateAttributeNameFromCategoryId(
            quarantineLegalDoc.getCategoryId(),
            null
        );

        Document document = null;
        try {
            document = objectMapper.convertValue(docObject.get(documentFieldName), Document.class);
            if (document == null) {
                documentFieldName = "document";
                document = objectMapper.convertValue(docObject.get(documentFieldName), Document.class);
            }
        } catch (NullPointerException e) {
            log.error("Field {} did not exist in QuarantineLegalDoc", documentFieldName, e);
        }

        return document == null || !document.getDocumentId().equals(documentIdToRemove);
    }

    private void updateScannedDocuments(Map<String, Object> data, CaseData caseData, String documentIdToRemove) {
        List<Element<ScannedDocument>> scannedDocuments = caseData.getScannedDocuments();
        if (scannedDocuments != null) {
            List<Element<ScannedDocument>> updatedDocs = scannedDocuments.stream()
                .filter(doc -> shouldKeepScannedDocument(doc, documentIdToRemove))
                .toList();

            if (scannedDocuments.size() != updatedDocs.size()) {
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
        LocalDateTime uploadedTimestamp = caseDocument.getUploadTimeStamp();

        if (uploadedTimestamp != null) {
            ZonedDateTime ukDateTime = uploadedTimestamp
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.of("Europe/London"));
            return caseDocument.getDocumentFileName() + " (" + UPLOAD_TIMESTAMP_FORMATTER.format(ukDateTime) + ")";
        } else {
            return caseDocument.getDocumentFileName();
        }
    }
}
