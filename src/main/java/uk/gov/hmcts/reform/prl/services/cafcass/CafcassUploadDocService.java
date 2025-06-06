package uk.gov.hmcts.reform.prl.services.cafcass;

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
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.managedocuments.CafcassReportAndGuardianEnum;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.documents.Document.DocumentBuilder;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.cafcass.CafcassAppConstants.INVALID_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassServiceUtil.checkFileFormat;
import static uk.gov.hmcts.reform.prl.services.cafcass.CafcassServiceUtil.checkTypeOfDocument;
import static uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService.MANAGE_DOCUMENTS_TRIGGERED_BY;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassUploadDocService {

    public static final List<String> ALLOWED_FILE_TYPES = List.of("pdf", "docx");
    public static final List<String> ALLOWED_TYPE_OF_DOCS = List.of(
        "16_4_Report", "CR_1", "CR_2", "CIR_Part1", "CIR_Part2", "CIR_Review", "CMO_report",
        "Contact_Centre_Recordings", "Correspondence", "Direct_work", "Enforcement_report",
        "FAO_Report", "FAO_Workplan", "Letter_from_Child", "Other_Non_Section_7_Report",
        "Position_Statement", "Positive_Parenting_Programme_Report", "Re_W_Report",
        "S_11H_Monitoring", "S_16A_Risk_Assessment", "Safeguarding_Letter",
        "Safeguarding_Letter_Returner", "Safeguarding_Letter_Shorter_Template",
        "Safeguarding_Letter_Update", "Second_Gatekeeping_Safeguarding_Letter",
        "Section7_Addendum_Report", "Section7_Report_Child_Impact_Analysis", "Suitability_report"
    );

    private static final Map<String, CafcassReportAndGuardianEnum> DOCUMENT_TYPE_CATEGORY_MAP = createDocumentTypeMap();

    private final CoreCaseDataApi coreCaseDataApi;
    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final AllTabServiceImpl allTabService;
    private final ManageDocumentsService manageDocumentsService;

    public void uploadDocument(String authorisation, MultipartFile document,
                               String typeOfDocument, String caseId) {
        if (isValidDocument(document, typeOfDocument)) {
            CaseDetails caseDetails = checkIfCasePresent(caseId, authorisation);
            if (caseDetails == null) {
                log.error("caseId does not exist");
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
                authorisation,
                authTokenGenerator.generate(),
                PrlAppsConstants.CASE_TYPE,
                PrlAppsConstants.JURISDICTION,
                Collections.singletonList(document)
            );
            log.info("Document uploaded successfully through caseDocumentClient");
            updateCcdAfterUploadingDocument(document, typeOfDocument, caseId, uploadResponse);
        }
    }

    private void updateCcdAfterUploadingDocument(MultipartFile document, String typeOfDocument,
                                                 String caseId, UploadResponse uploadResponse) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificEvent(
                caseId,
                CaseEvent.CAFCASS_ENGLAND_DOCUMENT_UPLOAD.getValue()
            );

        Map<String, Object> caseDataUpdated = startAllTabsUpdateDataContent.caseDataMap();
        QuarantineLegalDoc quarantineLegalDoc = createQuarantineDocFromCafcassUploadedDoc(
            typeOfDocument,
            uploadResponse.getDocuments().get(0)
        );

        manageDocumentsService.setFlagsForWaTask(
            startAllTabsUpdateDataContent.caseData(),
            caseDataUpdated,
            CAFCASS,
            quarantineLegalDoc
        );

        caseDataUpdated.putIfAbsent(MANAGE_DOCUMENTS_TRIGGERED_BY, null);

        manageDocumentsService.moveDocumentsToQuarantineTab(
            quarantineLegalDoc,
            startAllTabsUpdateDataContent.caseData(),
            caseDataUpdated,
            CAFCASS
        );

        allTabService.submitAllTabsUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataUpdated
        );

        log.info("Document has been saved in CCD {}", document.getOriginalFilename());
    }

    private QuarantineLegalDoc createQuarantineDocFromCafcassUploadedDoc(String typeOfDocument,
                                                                         Document document) {
        DocumentBuilder uploadedDoc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentUrl(document.links.self.href)
            .documentBinaryUrl(document.links.binary.href)
            .documentHash(document.hashToken)
            .documentFileName(document.originalDocumentName);

        QuarantineLegalDoc.QuarantineLegalDocBuilder builder = QuarantineLegalDoc.builder()
            .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
            .documentType(typeOfDocument)
            .isConfidential(Yes)
            .uploadedBy(CAFCASS)
            .uploaderRole(CAFCASS)
            .documentName(document.originalDocumentName)
            .documentParty(DocumentPartyEnum.CAFCASS.getDisplayedValue())
            .cafcassQuarantineDocument(uploadedDoc.build());

        CafcassReportAndGuardianEnum category = DOCUMENT_TYPE_CATEGORY_MAP.get(typeOfDocument);
        if (category == null) {
            log.warn("No category mapping found for document type: {}", typeOfDocument);
            return builder.build();
        }

        return builder.categoryId(category.geCategoryId()).categoryName(category.getCategoryName()).build();
    }

    public CaseDetails checkIfCasePresent(String caseId, String authorisation) {
        try {
            return coreCaseDataApi.getCase(
                authorisation,
                authTokenGenerator.generate(),
                caseId
            );
        } catch (Exception ex) {
            log.error("Error while getting the case {} {}", caseId, ex.getMessage());
        }
        return null;
    }

    private boolean isValidDocument(MultipartFile document, String typeOfDocument) {
        if (document == null || document.getOriginalFilename() == null) {
            log.error("Document is null or has no filename.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_DOCUMENT_TYPE.formatted(typeOfDocument));
        }

        boolean isValidFormat = checkFileFormat(document.getOriginalFilename(), ALLOWED_FILE_TYPES);
        boolean isValidType = checkTypeOfDocument(typeOfDocument, ALLOWED_TYPE_OF_DOCS);

        if (isValidFormat && isValidType) {
            return true;
        }

        log.error("Unacceptable format/type of document: {}", typeOfDocument);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_DOCUMENT_TYPE.formatted(typeOfDocument));
    }

    private static Map<String, CafcassReportAndGuardianEnum> createDocumentTypeMap() {
        Map<String, CafcassReportAndGuardianEnum> map = new HashMap<>();
        map.put("16_4_Report", CafcassReportAndGuardianEnum.guardianReport);
        map.put("CR_1", CafcassReportAndGuardianEnum.childImpactReport1);
        map.put("CR_2", CafcassReportAndGuardianEnum.childImpactReport2);
        map.put("CIR_Part1", CafcassReportAndGuardianEnum.section7Report);
        map.put("CIR_Part2", CafcassReportAndGuardianEnum.section7Report);
        map.put("CIR_Review", CafcassReportAndGuardianEnum.section7Report);
        map.put("CMO_report", CafcassReportAndGuardianEnum.otherDocs);
        map.put("Contact_Centre_Recordings", CafcassReportAndGuardianEnum.otherDocs);
        map.put("Correspondence", CafcassReportAndGuardianEnum.otherDocs);
        map.put("Direct_work", CafcassReportAndGuardianEnum.otherDocs);
        map.put("Enforcement_report", CafcassReportAndGuardianEnum.otherDocs);
        map.put("FAO_Report", CafcassReportAndGuardianEnum.otherDocs);
        map.put("FAO_Workplan", CafcassReportAndGuardianEnum.otherDocs);
        map.put("Letter_from_Child", CafcassReportAndGuardianEnum.otherDocs);
        map.put("Other_Non_Section_7_Report", CafcassReportAndGuardianEnum.section7Report);
        map.put("Position_Statement", CafcassReportAndGuardianEnum.otherDocs);
        map.put("Positive_Parenting_Programme_Report", CafcassReportAndGuardianEnum.otherDocs);
        map.put("Re_W_Report", CafcassReportAndGuardianEnum.otherDocs);
        map.put("S_11H_Monitoring", CafcassReportAndGuardianEnum.otherDocs);
        map.put("S_16A_Risk_Assessment", CafcassReportAndGuardianEnum.riskAssessment);
        map.put("Safeguarding_Letter", CafcassReportAndGuardianEnum.safeguardingLetter);
        map.put("Safeguarding_Letter_Returner", CafcassReportAndGuardianEnum.safeguardingLetter);
        map.put("Safeguarding_Letter_Shorter_Template", CafcassReportAndGuardianEnum.safeguardingLetter);
        map.put("Safeguarding_Letter_Update", CafcassReportAndGuardianEnum.safeguardingLetter);
        map.put("Second_Gatekeeping_Safeguarding_Letter", CafcassReportAndGuardianEnum.safeguardingLetter);
        map.put("Section7_Addendum_Report", CafcassReportAndGuardianEnum.section7Report);
        map.put("Section7_Report_Child_Impact_Analysis", CafcassReportAndGuardianEnum.section7Report);
        map.put("Suitability_report", CafcassReportAndGuardianEnum.otherDocs);
        return map;
    }
}
