package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentRemovalWrapper;
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
    private final DocumentIdRetriever documentRetriever;
    private final DocumentInstanceRetriever documentInstanceRetriever;
    private final DocumentRemover documentRemover;

    private static final DateTimeFormatter UPLOAD_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public Map<String, Object> getCaseDocuments(CaseDetails caseDetails) {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<CaseDocument> caseDocuments = documentRetriever.getCaseDocuments(caseData);

        DynamicList caseDocumentsDynamicList = DynamicList.builder()
            .listItems(caseDocuments.stream()
                           .map(doc -> DynamicListElement.builder()
                               .code(doc.documentId())
                               .label(formatLabel(doc))
                               .build())
                           .toList())
            .build()
            .withSortedListItemsByLabel();

        return Map.of(DOCUMENT_REMOVAL_CASE_DOCUMENTS, caseDocumentsDynamicList);
    }

    public Map<String, Object> getCaseDocumentSelectedForRemoval(CaseDetails caseDetails) {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        DocumentRemovalWrapper wrapper = caseData.getDocumentRemovalWrapper();
        DynamicListElement selectedCaseDocument = wrapper.getDocumentRemovalCaseDocuments().getValue();

        Document document = documentInstanceRetriever.getCaseDocument(caseData, selectedCaseDocument.getCode());

        return Map.of(DOCUMENT_REMOVAL_CASE_DOCUMENTS, caseData.getDocumentRemovalWrapper().getDocumentRemovalCaseDocuments(),
                      DOCUMENT_REMOVAL_DOCUMENT_TO_REMOVE, document);
     }

//    public Map<String, Object> getCaseDocumentInstances(CaseDetails caseDetails) {
//        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
//
//        DocumentRemovalWrapper wrapper = caseData.getDocumentRemovalWrapper();
//        DynamicListElement selectedCaseDocument = wrapper.getDocumentRemovalCaseDocuments().getValue();
//
//        DocumentInstances documentInstances = documentInstanceRetriever.getDocumentInstance(caseData,
//                                                                                            selectedCaseDocument.getCode());
//
//        DynamicMultiSelectList instances = DynamicMultiSelectList.builder()
//            .listItems(documentInstances.getInstances().stream()
//                           .map(instance -> DynamicMultiselectListElement.builder()
//                               .code(instance)
//                               .label(formatInstanceLabel(instance))
//                               .build())
//                           .toList())
//            .build();
//
//        return Map.of(DOCUMENT_REMOVAL_CASE_DOCUMENTS, caseData.getDocumentRemovalWrapper().getDocumentRemovalCaseDocuments(),
//                      DOCUMENT_TO_REMOVE, documentInstances.getDocument(),
//                      DOCUMENT_TO_REMOVE_INSTANCES, instances);
//    }

    public Map<String, Object> removeDocument(CaseDetails caseDetails) throws IOException {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        DocumentRemovalWrapper wrapper = caseData.getDocumentRemovalWrapper();
        String documentIdToRemove = wrapper.getDocumentRemovalCaseDocuments().getValueCode();

        Map<String, Object> updatedCaseData = documentRemover.removeDocument(caseDetails.getData(), documentIdToRemove);
        updatedCaseData.remove(DOCUMENT_REMOVAL_CASE_DOCUMENTS);
        updatedCaseData.remove(DOCUMENT_REMOVAL_DOCUMENT_TO_REMOVE);
        updatedCaseData.remove(DOCUMENT_REMOVAL_CONFIRM_OPTIONS);

        return updatedCaseData;
    }

    public Map<String, Object> removeDocumentInstances(CaseDetails caseDetails) throws IOException {
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

//        DocumentRemovalWrapper wrapper = caseData.getDocumentRemovalWrapper();
//
//        String documentIdToRemove = wrapper.getDocumentRemovalCaseDocuments().getValueCode();
//        List<String> selectedInstancePaths = wrapper.getDocumentToRemoveInstances().getValue().stream()
//            .map(DynamicMultiselectListElement::getCode)
//            .toList();
//
//        Map<String, Object> updatedCaseData = documentInstanceRemover.removeDocumentInstance(caseDetails.getData(),
//                                                                                             documentIdToRemove,
//                                                                                             selectedInstancePaths);
//        updatedCaseData.remove(DOCUMENT_REMOVAL_CASE_DOCUMENTS);
//        updatedCaseData.remove(DOCUMENT_TO_REMOVE);
//        updatedCaseData.remove(DOCUMENT_TO_REMOVE_INSTANCES);

        //return updatedCaseData;
        return null;
    }

    private String formatLabel(CaseDocument caseDocument) {
        return caseDocument.uploadTimestamp() != null
            ? caseDocument.filename() + " (" + UPLOAD_TIMESTAMP_FORMATTER.format(caseDocument.uploadTimestamp()) + ")"
            : caseDocument.filename();
    }

    private String formatInstanceLabel(String instance) {
        return DocumentFieldLabels.getLabelForField(instance);
    }
}
