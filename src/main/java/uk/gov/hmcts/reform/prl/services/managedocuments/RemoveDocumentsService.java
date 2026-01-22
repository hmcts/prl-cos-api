package uk.gov.hmcts.reform.prl.services.managedocuments;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_TIME_PATTERN;

@Slf4j
@Service
@RequiredArgsConstructor
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
        Stream<Element<RemovableDocument>> removableReviewDocs = caseData.getReviewDocuments() != null
            ? caseData.getReviewDocuments().getRemovableDocuments().stream()
            .map(this::convertQuarantineDoc)
            : Stream.empty();

        Stream<Element<RemovableDocument>> removableDocMgmtDocs = caseData.getDocumentManagementDetails() != null
            ? caseData.getDocumentManagementDetails().getRemovableDocuments().stream()
            .map(this::convertQuarantineDoc)
            : Stream.empty();

        return Stream.concat(removableReviewDocs, removableDocMgmtDocs)
            .filter(Objects::nonNull)
            .toList();
    }

    public List<Element<RemovableDocument>> getDocsBeingRemoved(CaseData caseData, CaseData old) {
        List<Element<RemovableDocument>> current =
            Optional.ofNullable(caseData.getRemovableDocuments()).orElse(List.of());
        List<Element<RemovableDocument>> previous =
            Optional.ofNullable(getRemovableDocuments(old)).orElse(List.of());
        return previous.stream()
            .filter(prevDoc -> current.stream()
                .noneMatch(currDoc -> currDoc.getId().equals(prevDoc.getId())))
            .toList();
    }

    public String getConfirmationTextForDocsBeingRemoved(CaseData caseData, CaseData old) {
        List<Element<RemovableDocument>> removedDocs = getDocsBeingRemoved(caseData, old);
        if (removedDocs.isEmpty()) {
            return "No documents removed.";
        }
        return removedDocs.stream()
            .map(doc -> String.format(
                " • %s - %s, %s (%s)",
                doc.getValue().getCategoryName(),
                doc.getValue().getDocument().getDocumentFileName(),
                doc.getValue().getDocumentParty(),
                CommonUtils.formatDateTime(DATE_TIME_PATTERN, doc.getValue().getDocumentUploadedDate())
            ))
            .reduce((doc1, doc2) -> doc1 + ", \n" + doc2)
            .orElse("");
    }

    public Map<String, Object> removeDocuments(CaseData caseData, List<Element<RemovableDocument>> documentsToRemove) {
        DocumentManagementDetails docMgmt = Optional.ofNullable(caseData.getDocumentManagementDetails())
            .orElseGet(() -> DocumentManagementDetails.builder().build());
        ReviewDocuments reviewDocs = Optional.ofNullable(caseData.getReviewDocuments())
            .orElseGet(() -> ReviewDocuments.builder().build());

        Map<String, Object> updatedCaseData = new HashMap<>();

        // update the map using the field names
        updatedCaseData.put(
            "legalProfQuarantineDocsList",
            removeById(docMgmt.getLegalProfQuarantineDocsList(), documentsToRemove)
        );
        updatedCaseData.put(
            "courtStaffQuarantineDocsList",
            removeById(docMgmt.getCourtStaffQuarantineDocsList(), documentsToRemove)
        );
        updatedCaseData.put(
            "cafcassQuarantineDocsList",
            removeById(docMgmt.getCafcassQuarantineDocsList(), documentsToRemove)
        );
        updatedCaseData.put(
            "citizenQuarantineDocsList",
            removeById(docMgmt.getCitizenQuarantineDocsList(), documentsToRemove)
        );
        updatedCaseData.put(
            "courtNavQuarantineDocumentList",
            removeById(docMgmt.getCourtNavQuarantineDocumentList(), documentsToRemove)
        );

        updatedCaseData.put(
            "legalProfUploadDocListDocTab",
            removeById(reviewDocs.getLegalProfUploadDocListDocTab(), documentsToRemove)
        );
        updatedCaseData.put(
            "cafcassUploadDocListDocTab",
            removeById(reviewDocs.getCafcassUploadDocListDocTab(), documentsToRemove)
        );
        updatedCaseData.put(
            "courtStaffUploadDocListDocTab",
            removeById(reviewDocs.getCourtStaffUploadDocListDocTab(), documentsToRemove)
        );
        updatedCaseData.put(
            "bulkScannedDocListDocTab",
            removeById(reviewDocs.getBulkScannedDocListDocTab(), documentsToRemove)
        );
        updatedCaseData.put(
            "citizenUploadedDocListDocTab",
            removeById(reviewDocs.getCitizenUploadedDocListDocTab(), documentsToRemove)
        );
        updatedCaseData.put(
            "courtNavUploadedDocListDocTab",
            removeById(reviewDocs.getCourtNavUploadedDocListDocTab(), documentsToRemove)
        );
        updatedCaseData.put("restrictedDocuments", removeById(reviewDocs.getRestrictedDocuments(), documentsToRemove));
        updatedCaseData.put(
            "confidentialDocuments",
            removeById(reviewDocs.getConfidentialDocuments(), documentsToRemove)
        );

        return updatedCaseData;
    }

    private List<Element<QuarantineLegalDoc>> removeById(List<Element<QuarantineLegalDoc>> source, List<Element<RemovableDocument>> toRemove) {
        if (source == null) {
            return null;
        }
        List<UUID> toRemoveIds = toRemove.stream()
            .map(Element::getId)
            .toList();

        return source.stream()
            .filter(doc -> !toRemoveIds.contains(doc.getId()))
            .toList();
    }

    Element<RemovableDocument> convertQuarantineDoc(Element<QuarantineLegalDoc> quarantineLegalDocElement) {
        QuarantineLegalDoc quarantineLegalDoc = quarantineLegalDocElement.getValue();

        Map<String, Object> docObject = objectMapper.convertValue(quarantineLegalDoc, new TypeReference<>() {});

        String documentFieldName;
        if (isNotEmpty(quarantineLegalDoc.getUrl())) {
            // bulk scan documents use a url field not an "attribute name" field
            documentFieldName = "url";
        } else {
            log.info("Doc category ID: " + quarantineLegalDoc.getCategoryId());
            documentFieldName = DocumentUtils.populateAttributeNameFromCategoryId(
                quarantineLegalDoc.getCategoryId(),
                null
            );
        }

        Document doc;

        try {
            doc = objectMapper.convertValue(docObject.get(documentFieldName), Document.class);
        } catch (NullPointerException e) {
            log.error("Field {} did not exist in QuarantineLegalDoc", documentFieldName, e);
            return null;
        }

        // make a new element with the same ID
        return new Element<>(
            quarantineLegalDocElement.getId(), RemovableDocument.builder()
            .categoryName(quarantineLegalDoc.getCategoryName())
            .documentParty(quarantineLegalDoc.getDocumentParty())
            .documentUploadedDate(quarantineLegalDoc.getDocumentUploadedDate())
            .uploadedBy(quarantineLegalDoc.getUploadedBy())
            .document(doc)
            .build()
        );
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
                log.error(
                    "Failed to delete document with ID: {} from CDAM for case ID: {}, {}",
                    documentId, caseData.getId(), e.getMessage()
                );
            }
        });
    }

}
