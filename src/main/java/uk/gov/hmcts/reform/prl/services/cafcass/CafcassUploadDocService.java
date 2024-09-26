package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.cafcass.CafcassAppConstants.INVALID_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassServiceUtil.checkFileFormat;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassServiceUtil.checkTypeOfDocument;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassServiceUtil.getCaseDataWithUploadedDocs;
import static uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService.MANAGE_DOCUMENTS_TRIGGERED_BY;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassUploadDocService {

    public static final List<String> ALLOWED_FILE_TYPES = List.of("pdf", "docx");
    public static final List<String> ALLOWED_TYPE_OF_DOCS = List.of("16_4_Report", "CIR_Part1", "CIR_Part2", "CIR_Review", "CMO_report",
        "Contact_Centre_Recordings", "Correspondence", "Direct_work", "Enforcement_report", "Enquiry_from_Foreign_Court", "FAO_Report",
        "FAO_Workplan", "High_Court_Team_Template", "Letter_from_Child", "Other_Non_Section_7_Report", "Parental_Order_Report", "Position_Statement",
        "Positive_Parenting_Programme_Report", "Re_W_Report", "S_11H_Monitoring", "S_16A_Risk_Assessment", "Safeguarding_Letter",
        "Safeguarding_Letter_Returner", "Safeguarding_Letter_Shorter_Template", "Safeguarding_Letter_Update",
        "Second_Gatekeeping_Safeguarding_Letter", "Section7_Addendum_Report", "Section7_Report_Child_Impact_Analysis", "Suitability_report");
    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamClient idamClient;
    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final AllTabServiceImpl allTabService;

    private final ManageDocumentsService manageDocumentsService;

    private final UserService userService;

    public void uploadDocument(String authorisation, MultipartFile document, String typeOfDocument, String caseId) {

        if (isValidDocument(document, typeOfDocument)) {
            CaseDetails caseDetails = checkIfCasePresent(caseId, authorisation);
            if (caseDetails == null) {
                log.error("caseId does not exist");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            // upload document
            UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
                authorisation,
                authTokenGenerator.generate(),
                PrlAppsConstants.CASE_TYPE,
                PrlAppsConstants.JURISDICTION,
                Arrays.asList(document)
            );
            log.info("Document uploaded successfully through caseDocumentClient");
            //updateCcdAfterUploadingDocument(document, typeOfDocument, caseId, caseData, uploadResponse);

            moveCafcassUploadedDocsToQuarantine(document, typeOfDocument, caseId, uploadResponse);

        }
    }

    private void moveCafcassUploadedDocsToQuarantine(MultipartFile document,
                                                   String typeOfDocument,
                                                   String caseId,
                                                   UploadResponse uploadResponse) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificEvent(
            String.valueOf(
                caseId),

            CaseEvent.CAFCASS_ENGLAND_DOCUMENT_UPLOAD.getValue()
        );

        QuarantineLegalDoc courtNavQuarantineLegalDoc = getCafcassQuarantineDocument(
            document.getOriginalFilename(),
            startAllTabsUpdateDataContent.caseData(),
            uploadResponse.getDocuments().get(0),
            typeOfDocument
        );

        Map<String, Object> caseDataUpdated = startAllTabsUpdateDataContent.caseDataMap();
        manageDocumentsService.moveDocumentsToQuarantineTab(
            courtNavQuarantineLegalDoc,
            startAllTabsUpdateDataContent.caseData(),
            caseDataUpdated,
            CAFCASS
        );

        manageDocumentsService.setFlagsForWaTask(
            startAllTabsUpdateDataContent.caseData(),
            caseDataUpdated,
            CAFCASS,
            courtNavQuarantineLegalDoc
        );

        //Changes to generate one WA task for courtnav upload document
        if (!caseDataUpdated.containsKey(MANAGE_DOCUMENTS_TRIGGERED_BY)) {
            caseDataUpdated.put(MANAGE_DOCUMENTS_TRIGGERED_BY, null);
        }

        allTabService.submitAllTabsUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            String.valueOf(caseId),
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataUpdated
        );
    }

    private void updateCcdAfterUploadingDocument(MultipartFile document, String typeOfDocument, String caseId,
                                                 CaseData tempCaseData, UploadResponse uploadResponse) {

        // get the existing CCD record with all the uploaded documents

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificEvent(
            String.valueOf(
                caseId),
            CaseEvent.CAFCASS_ENGLAND_DOCUMENT_UPLOAD.getValue()
        );
        Map<String, Object> caseDataUpdated = startAllTabsUpdateDataContent.caseDataMap();
        caseDataUpdated.put("cafcassUploadedDocs", getCaseDataWithUploadedDocs(
            caseId,
            document.getOriginalFilename(),
            typeOfDocument,
            tempCaseData,
            uploadResponse.getDocuments().get(0)
        ).getCafcassUploadedDocs());

        allTabService.submitAllTabsUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            String.valueOf(caseId),
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataUpdated
        );

        log.info("Document has been saved in CCD {}", document.getOriginalFilename());
    }

    public CaseDetails checkIfCasePresent(String caseId, String authorisation) {
        try {
            return coreCaseDataApi.getCase(
                authorisation,
                authTokenGenerator.generate(),
                caseId
            );
        } catch (Exception ex) {
            log.error("Error while getting the case {} {}", caseId, ex.getMessage(), ex);
        }
        return null;
    }


    private boolean isValidDocument(MultipartFile document, String typeOfDocument) {
        if (null != document && null != document.getOriginalFilename()
            && checkFileFormat(document.getOriginalFilename(), ALLOWED_FILE_TYPES)
            && checkTypeOfDocument(typeOfDocument, ALLOWED_TYPE_OF_DOCS)) {
            return true;
        }
        log.error("Un acceptable format/type of document {}", typeOfDocument);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(INVALID_DOCUMENT_TYPE, typeOfDocument));
    }

    private QuarantineLegalDoc getCafcassQuarantineDocument(String fileName,
                                                            CaseData caseData,
                                                            Document uploadedDocument,
                                                            String typeOfDocument) {

        uk.gov.hmcts.reform.prl.models.documents.Document cafcassDocument = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentUrl(uploadedDocument.links.self.href)
            .documentBinaryUrl(uploadedDocument.links.binary.href)
            .documentHash(uploadedDocument.hashToken)
            .documentFileName(fileName).build();
        String categoryId = "safeguardingLetter";
        String categoryName = "Safeguarding letter/Safeguarding Enquiries Report (SER)";

        if (YesOrNo.Yes.equals(caseData.getIsPathfinderCase())) {
            categoryId = "pathfinder";
            categoryName = "Pathfinder";
        }

        return QuarantineLegalDoc.builder()
            .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
            .documentType(typeOfDocument)
            .categoryId(categoryId)
            .categoryName(categoryName)
            .isConfidential(Yes)
            .fileName(fileName)
            .uploadedBy(CAFCASS)
            .uploaderRole(CAFCASS)
            .cafcassQuarantineDocument(cafcassDocument)
            .documentParty(uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum.CAFCASS.getDisplayedValue())
            .build();
    }
}
