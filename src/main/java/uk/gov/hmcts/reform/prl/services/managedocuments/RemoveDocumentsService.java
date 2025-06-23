package uk.gov.hmcts.reform.prl.services.managedocuments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.RemovableDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_TIME_PATTERN;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RemoveDocumentsService {

    private final ObjectMapper objectMapper;
    private final CaseDocumentClient caseDocumentClient;
    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;

    public CaseData populateRemovalList(CaseData caseData) {
        return caseData.toBuilder()
            .removableDocuments(getRemovableDocuments(caseData))
            .build();
    }

    private List<Element<RemovableDocument>> getRemovableDocuments(CaseData caseData) {
        return Stream.of(
                caseData.getReviewDocuments().getAllRemovableDocuments(),
                caseData.getDocumentManagementDetails().getRemovableDocuments()
            )
            .flatMap(List::stream)
            .map(doc -> Element.<RemovableDocument>builder()
                .id(doc.getId())
                .value(convertQuarantineDoc(doc.getValue()))
                .build())
            .toList();
    }

    public List<Element<RemovableDocument>> docsBeingRemoved(CaseData caseData, CaseData old) {
        List<Element<RemovableDocument>> current = caseData.getRemovableDocuments();
        List<Element<RemovableDocument>> previous = getRemovableDocuments(old);
        return previous.stream()
            .filter(prevDoc -> current.stream()
                .noneMatch(currDoc -> currDoc.getId().equals(prevDoc.getId())))
            .toList();
    }

    public String docsBeingRemovedString(CaseData caseData, CaseData old) {
        List<Element<RemovableDocument>> removedDocs = docsBeingRemoved(caseData, old);
        if (removedDocs.isEmpty()) {
            return "No documents removed.";
        }
        return removedDocs.stream()
            .map(doc -> String.format(" • %s - %s, %s (%s)",
                doc.getValue().getCategoryName(),
                doc.getValue().getDocument().getDocumentFileName(),
                doc.getValue().getDocumentParty(),
                CommonUtils.formatDateTime(DATE_TIME_PATTERN, doc.getValue().getDocumentUploadedDate())
                ))
            .reduce((doc1, doc2) -> doc1 + ", \n" + doc2)
            .orElse("");
    }

    public CaseData removeDocuments(CaseData caseData, List<Element<RemovableDocument>> documentsToRemove) {
        DocumentManagementDetails docMgmt = caseData.getDocumentManagementDetails();
        ReviewDocuments reviewDocs = caseData.getReviewDocuments();

        // Remove from DocumentManagementDetails collections
        DocumentManagementDetails updatedDocMgmt = docMgmt.toBuilder()
            .legalProfQuarantineDocsList(removeById(docMgmt.getLegalProfQuarantineDocsList(), documentsToRemove))
            .courtStaffQuarantineDocsList(removeById(docMgmt.getCourtStaffQuarantineDocsList(), documentsToRemove))
            .cafcassQuarantineDocsList(removeById(docMgmt.getCafcassQuarantineDocsList(), documentsToRemove))
            .citizenQuarantineDocsList(removeById(docMgmt.getCitizenQuarantineDocsList(), documentsToRemove))
            .courtNavQuarantineDocumentList(removeById(docMgmt.getCourtNavQuarantineDocumentList(), documentsToRemove))
            .build();

        // Remove from ReviewDocuments collections
        ReviewDocuments updatedReviewDocs = reviewDocs.toBuilder()
            .legalProfUploadDocListDocTab(removeById(reviewDocs.getLegalProfUploadDocListDocTab(), documentsToRemove))
            .cafcassUploadDocListDocTab(removeById(reviewDocs.getCafcassUploadDocListDocTab(), documentsToRemove))
            .courtStaffUploadDocListDocTab(removeById(reviewDocs.getCourtStaffUploadDocListDocTab(), documentsToRemove))
            .bulkScannedDocListDocTab(removeById(reviewDocs.getBulkScannedDocListDocTab(), documentsToRemove))
            .citizenUploadedDocListDocTab(removeById(reviewDocs.getCitizenUploadedDocListDocTab(), documentsToRemove))
            .courtNavUploadedDocListDocTab(removeById(reviewDocs.getCourtNavUploadedDocListDocTab(), documentsToRemove))
            .restrictedDocuments(removeById(reviewDocs.getRestrictedDocuments(), documentsToRemove))
            .confidentialDocuments(removeById(reviewDocs.getConfidentialDocuments(), documentsToRemove))
            .build();

        return caseData.toBuilder()
            .documentManagementDetails(updatedDocMgmt)
            .reviewDocuments(updatedReviewDocs)
            .removableDocuments(null) // clear the removable documents list as temporary
            .documentsToBeRemoved(null)
            .build();
    }

    /**
     * Removes elements from the source list whose IDs match any in the toRemove list.
     */
    private List<Element<QuarantineLegalDoc>> removeById(List<Element<QuarantineLegalDoc>> source, List<Element<RemovableDocument>> toRemove) {
        if (source == null) {
            return null;
        }
        return source.stream()
            .filter(doc -> toRemove.stream().noneMatch(r -> r.getId().equals(doc.getId())))
            .toList();
    }

    public RemovableDocument convertQuarantineDoc(QuarantineLegalDoc quarantineLegalDoc) {
        Map<String, Object> docObject = objectMapper.convertValue(quarantineLegalDoc, new TypeReference<Map<String, Object>>() {});
        String documentFieldName = DocumentUtils.populateAttributeNameFromCategoryId(quarantineLegalDoc.getCategoryId(), null);
        Document doc;
        try {
            doc = objectMapper.convertValue(docObject.get(documentFieldName), Document.class);
        } catch (NullPointerException e) {
            log.error("Field {} did not exist in QuarantineLegalDoc", documentFieldName, e);
            return null; // todo how to handle this if the document isn't where we expect - could this actually happen?
        }
        return RemovableDocument.builder()
            .categoryName(quarantineLegalDoc.getCategoryName())
            .documentParty(quarantineLegalDoc.getDocumentParty())
            .documentUploadedDate(quarantineLegalDoc.getDocumentUploadedDate())
            .uploadedBy(quarantineLegalDoc.getUploadedBy())
            .document(doc)
            .build();
    }

    public void deleteDocumentsInCdam(CaseData caseData, CaseData old) {
        String systemAuthorisation = systemUserService.getSysUserToken();

        // Get the changed (removable) documents on the case - so we know what to delete in CDAM
        List<Element<RemovableDocument>> current = getRemovableDocuments(caseData);
        List<Element<RemovableDocument>> previous = getRemovableDocuments(old);

        List<Element<RemovableDocument>> removed = previous.stream()
            .filter(prevDoc -> current.stream()
                .noneMatch(currDoc -> currDoc.getId().equals(prevDoc.getId())))
            .toList();

        if (removed.isEmpty()) {
            log.info("No documents to remove in CDAM for case ID: {}", caseData.getId());
            return;
        }

        // Hard delete the document from CDAM now it's been unlinked from the case (for history tab)
        removed.forEach(removedDoc -> {
            UUID documentId = UUID.fromString(DocumentUtils.getDocumentId(
                removedDoc.getValue().getDocument().getDocumentUrl()));
            try {
                log.info("Deleting document with ID: {} from CDAM for case ID: {}", documentId, caseData.getId());
                caseDocumentClient.deleteDocument(systemAuthorisation, authTokenGenerator.generate(), documentId, true);
            } catch (Exception e) {
                log.error("Failed to delete document with ID: {} from CDAM for case ID: {}. Error: {}",
                    documentId, caseData.getId(), e.getMessage());
            }
        });
    }

}
