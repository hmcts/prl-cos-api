package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentRemovalWrapper;
import uk.gov.hmcts.reform.prl.services.DeleteDocumentService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.documentremoval.postabouttosubmitaction.DocumentRemovalAboutToSubmitAction;
import uk.gov.hmcts.reform.prl.services.documentremoval.submittedaction.DocumentRemovalSubmittedAction;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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
