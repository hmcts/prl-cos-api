package uk.gov.hmcts.reform.prl.services.cafcass;

import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.DocumentsHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_PARTY;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

public class CafcassServiceUtil {

    public static boolean checkTypeOfDocument(String typeOfDocument, List<String> allowedTypeOfDocs) {
        if (typeOfDocument != null) {
            return allowedTypeOfDocs.stream().anyMatch(s -> s.equalsIgnoreCase(typeOfDocument));
        }
        return false;
    }

    public static boolean checkFileFormat(String fileName, List<String> allowedFileTypes) {

        return allowedFileTypes.stream().anyMatch(extension -> DocumentsHelper.hasExtension(fileName, extension));
    }

    public static CaseData getCaseDataWithUploadedDocs(String caseId, String fileName, String typeOfDocument,
                                                 CaseData caseData, Document document) {
        String partyName = caseData.getApplicantCaseName() != null
            ? caseData.getApplicantCaseName() : CAFCASS_PARTY;
        List<Element<UploadedDocuments>> uploadedDocumentsList;
        Element<UploadedDocuments> uploadedDocsElement =
            element(UploadedDocuments.builder().dateCreated(LocalDate.now())
                        .documentType(typeOfDocument)
                        .uploadedBy(CAFCASS_PARTY)
                        .documentDetails(DocumentDetails.builder().documentName(fileName)
                                             .documentUploadedDate(new Date().toString()).build())
                        .partyName(partyName).isApplicant(CAFCASS_PARTY)
                        .parentDocumentType("Safe_guarding_Letter")
                        .cafcassDocument(uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                                             .documentUrl(document.links.self.href)
                                             .documentBinaryUrl(document.links.binary.href)
                                             .documentHash(document.hashToken)
                                             .documentFileName(fileName).build()).build());
        if (caseData.getCafcassUploadedDocs() != null) {
            uploadedDocumentsList = caseData.getCafcassUploadedDocs();
            uploadedDocumentsList.add(uploadedDocsElement);
        } else {
            uploadedDocumentsList = new ArrayList<>();
            uploadedDocumentsList.add(uploadedDocsElement);
        }
        return CaseData.builder().id(Long.parseLong(caseId)).cafcassUploadedDocs(uploadedDocumentsList).build();
    }

    private CafcassServiceUtil() {
    }
}
