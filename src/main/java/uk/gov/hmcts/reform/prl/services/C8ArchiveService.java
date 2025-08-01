package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.UUID.randomUUID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_ARCHIVED_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_ARCHIVED_DOCUMENT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_ARCHIVED_WELSH_DOCUMENT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C8ArchiveService {

    private final ObjectMapper objectMapper;
    private final ConfidentialDetailsChangeHelper confidentialDetailsChangeHelper;

    public void archiveC8DocumentIfConfidentialChanged(CallbackRequest callbackRequest,
                                                       CaseData caseData,
                                                       Map<String, Object> caseDataUpdated) {
        CaseData caseDataBefore = CaseUtils.getCaseData(callbackRequest.getCaseDetailsBefore(), objectMapper);
        boolean confidentialDetailsChanged = confidentialDetailsChangeHelper.haveConfidentialDetailsChanged(caseData, caseDataBefore);

        log.info("Confidential details changed: {}", confidentialDetailsChanged);

        archiveC8DocumentsIfPresent(caseData, caseDataUpdated, confidentialDetailsChanged);
    }


    public void archiveC8DocumentIfConfidentialChangedFromCitizen(CaseData caseData,
                                                                  CitizenUpdatedCaseData citizenUpdatedCaseData,
                                                                  Map<String, Object> caseDataMapTobeUpdated) {

        if (caseData.getCaseTypeOfApplication().equals(C100_CASE_TYPE)) {
            PartyDetails previousApplicantDetails = caseData.getApplicants().get(0).getValue();
            PartyDetails currentApplicantDetails = citizenUpdatedCaseData.getPartyDetails();

            boolean confidentialDetailsChanged = confidentialDetailsChangeHelper.haveContactDetailsChanged(
                previousApplicantDetails,
                currentApplicantDetails);
            log.info("Confidential details changed: {}", confidentialDetailsChanged);
            archiveC8DocumentsIfPresent(caseData, caseDataMapTobeUpdated, confidentialDetailsChanged);
        }

        if (caseData.getCaseTypeOfApplication().equals(FL401_CASE_TYPE)) {
            PartyDetails previousApplicantDetails = caseData.getApplicantsFL401();
            PartyDetails currentApplicantDetails = citizenUpdatedCaseData.getPartyDetails();

            boolean confidentialDetailsChanged = confidentialDetailsChangeHelper.haveContactDetailsChanged(
                previousApplicantDetails,
                currentApplicantDetails);

            log.info("Confidential details changed: {}", confidentialDetailsChanged);

            archiveC8DocumentsIfPresent(caseData, caseDataMapTobeUpdated, confidentialDetailsChanged);
        }
    }

    private void archiveC8DocumentsIfPresent(CaseData caseData, Map<String, Object> caseDataUpdated, boolean confidentialDetailsChanged) {
        if (confidentialDetailsChanged) {
            Document c8ToArchiveEng = caseData.getC8Document();
            Document c8ToArchivedWelsh = caseData.getC8WelshDocument();

            if (c8ToArchiveEng != null || c8ToArchivedWelsh != null) {
                List<Element<Document>> archivedDocuments = new ArrayList<>();

                if (caseData.getC8ArchivedDocuments() != null) {
                    archivedDocuments.addAll(caseData.getC8ArchivedDocuments());
                }

                archiveDocumentIfNotNull(c8ToArchiveEng,C8_ARCHIVED_DOCUMENT_NAME, archivedDocuments);
                archiveDocumentIfNotNull(c8ToArchivedWelsh,C8_ARCHIVED_WELSH_DOCUMENT_NAME, archivedDocuments);

                caseDataUpdated.put(C8_ARCHIVED_DOCUMENTS, archivedDocuments);
            } else {
                log.info("Confidential details changed, but no C8 document found to archive.");
            }
        }
    }



    private void archiveDocumentIfNotNull(Document document, String fileName, List<Element<Document>> archivedDocuments) {
        if (document != null) {
            Document archivedocument = Document.builder()
                .documentUrl(document.getDocumentUrl())
                .documentBinaryUrl(document.getDocumentBinaryUrl())
                .documentFileName(fileName)
                .uploadTimeStamp(LocalDateTime.now())
                .build();

            archivedDocuments.add(buildElement(archivedocument));
            log.info("Archiving C8 Document - File Name: {}, URL: {}", document.getDocumentFileName(), document.getDocumentUrl());
        }
    }

    private Element<Document> buildElement(Document document) {
        return Element.<Document>builder()
            .id(randomUUID())
            .value(document)
            .build();
    }


}
