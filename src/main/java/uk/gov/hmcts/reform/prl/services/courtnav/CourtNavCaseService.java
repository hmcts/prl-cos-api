package uk.gov.hmcts.reform.prl.services.courtnav;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURTNAV;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;
import static uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService.MANAGE_DOCUMENTS_TRIGGERED_BY;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourtNavCaseService {

    protected static final String[] ALLOWED_FILE_TYPES = {"pdf", "jpeg", "jpg", "doc", "docx", "bmp", "png", "tiff", "txt", "tif"};
    protected static final String[] ALLOWED_TYPE_OF_DOCS = {"WITNESS_STATEMENT", "EXHIBITS_EVIDENCE", "EXHIBITS_COVERSHEET", "C8_DOCUMENT"};
    private final CcdCoreCaseDataService coreCaseDataService;
    private final IdamClient idamClient;
    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final DocumentGenService documentGenService;
    private final AllTabServiceImpl allTabService;
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;

    private final ManageDocumentsService manageDocumentsService;

    public CaseDetails createCourtNavCase(String authToken, CaseData caseData) {
        Map<String, Object> caseDataMap = caseData.toMap(CcdObjectMapper.getObjectMapper());
        EventRequestData eventRequestData = coreCaseDataService.eventRequest(
            CaseEvent.COURTNAV_CASE_CREATION,
            idamClient.getUserInfo(authToken).getUid()
        );
        StartEventResponse startEventResponse = coreCaseDataService.startSubmitCreate(
            authToken,
            authTokenGenerator.generate(),
            eventRequestData,
            true
        );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(caseDataMap).build();

        return coreCaseDataService.submitCreate(
            authToken,
            authTokenGenerator.generate(),
            idamClient.getUserInfo(authToken).getUid(),
            caseDataContent,
            true
        );
    }

    public void uploadDocument(String authorisation, MultipartFile document, String typeOfDocument, String caseId) {

        if (null != document && null != document.getOriginalFilename()
            && checkFileFormat(document.getOriginalFilename())
            && checkTypeOfDocument(typeOfDocument)) {
            EventRequestData eventRequestData = coreCaseDataService.eventRequest(
                CaseEvent.COURTNAV_DOCUMENT_UPLOAD_EVENT_ID,
                idamClient.getUserInfo(authorisation).getUid()
            );
            StartEventResponse startEventResponse = checkIfCasePresent(caseId, authorisation, eventRequestData);
            if (startEventResponse == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            CaseData tempCaseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);
            int alreadyUploadedCourtNavDocSize = getAlreadyUploadedCourtNavDocsSize(tempCaseData);
            if (tempCaseData.getNumberOfAttachments() != null
                && Integer.parseInt(tempCaseData.getNumberOfAttachments()) <= alreadyUploadedCourtNavDocSize) {
                log.error("Number of attachments size is reached {}", tempCaseData.getNumberOfAttachments());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
                authorisation,
                authTokenGenerator.generate(),
                PrlAppsConstants.CASE_TYPE,
                PrlAppsConstants.JURISDICTION,
                List.of(document)
            );
            log.info("Document uploaded successfully through caseDocumentClient");

            Map<String, Object> fields = new HashMap<>();

            QuarantineLegalDoc courtNavQuarantineLegalDoc = getCourtNavQuarantineDocument(
                document.getOriginalFilename(),
                tempCaseData,
                uploadResponse.getDocuments().getFirst(),
                typeOfDocument
            );

            manageDocumentsService.moveDocumentsToQuarantineTab(
                courtNavQuarantineLegalDoc,
                tempCaseData,
                fields,
                COURTNAV
            );

            manageDocumentsService.setFlagsForWaTask(
                tempCaseData,
                fields,
                COURTNAV,
                courtNavQuarantineLegalDoc
            );

            //Changes to generate one WA task for courtnav upload document
            if (!fields.containsKey(MANAGE_DOCUMENTS_TRIGGERED_BY)) {
                fields.put(MANAGE_DOCUMENTS_TRIGGERED_BY, null);
            }

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                           .id(startEventResponse.getEventId())
                           .build())
                .data(fields).build();

            coreCaseDataService.submitUpdate(
                authorisation,
                eventRequestData,
                caseDataContent,
                caseId,
                true
            );

            log.info("Document has been saved in caseData {}", document.getOriginalFilename());
        } else {
            log.error("Un acceptable format/type of document {}", typeOfDocument);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private static int getAlreadyUploadedCourtNavDocsSize(CaseData tempCaseData) {
        int alreadyUploadedCourtNavDocSize = !CollectionUtils.isEmpty(tempCaseData.getReviewDocuments().getCourtNavUploadedDocListDocTab())
            ? tempCaseData.getReviewDocuments().getCourtNavUploadedDocListDocTab().size() : 0;
        if (!CollectionUtils.isEmpty(tempCaseData.getReviewDocuments().getRestrictedDocuments())) {
            for (Element<QuarantineLegalDoc> restrictedDocument : tempCaseData.getReviewDocuments().getRestrictedDocuments()) {
                if (COURTNAV.equalsIgnoreCase(restrictedDocument.getValue().getUploadedBy())) {
                    alreadyUploadedCourtNavDocSize++;
                }
            }
        }
        if (!CollectionUtils.isEmpty(tempCaseData.getDocumentManagementDetails().getCourtNavQuarantineDocumentList())) {
            alreadyUploadedCourtNavDocSize = alreadyUploadedCourtNavDocSize
                + tempCaseData.getDocumentManagementDetails().getCourtNavQuarantineDocumentList().size();
        }
        return alreadyUploadedCourtNavDocSize;
    }

    public StartEventResponse checkIfCasePresent(String caseId, String authorisation, EventRequestData eventRequestData) {
        try {
            return coreCaseDataService.startUpdate(authorisation, eventRequestData, caseId, true);
        } catch (Exception ex) {
            log.error("Error while getting the case {} {}", caseId, ex.getMessage());
        }
        return null;
    }

    private QuarantineLegalDoc getCourtNavQuarantineDocument(String fileName,
                                                             CaseData caseData,
                                                             Document uploadedDocument,
                                                             String typeOfDocument) {

        String partyName = caseData.getApplicantsFL401() != null
            ? caseData.getApplicantsFL401().getLabelForDynamicList() : COURTNAV;

        uk.gov.hmcts.reform.prl.models.documents.Document courtNavDocument = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentUrl(uploadedDocument.links.self.href)
            .documentBinaryUrl(uploadedDocument.links.binary.href)
            .documentHash(uploadedDocument.hashToken)
            .documentFileName(fileName).build();

        return QuarantineLegalDoc.builder()
            .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)))
            .documentType(typeOfDocument)
            .categoryId("applicantStatements")
            .categoryName("Applicant's statements")
            .isConfidential(Yes)
            .fileName(fileName)
            .uploadedBy(COURTNAV)
            .uploaderRole(COURTNAV)
            .courtNavQuarantineDocument(courtNavDocument)
            .documentParty(partyName)
            .build();
    }

    private boolean checkTypeOfDocument(String typeOfDocument) {
        if (typeOfDocument != null) {
            return Arrays.stream(ALLOWED_TYPE_OF_DOCS).anyMatch(s -> s.equalsIgnoreCase(typeOfDocument));
        }
        return false;
    }

    private boolean checkFileFormat(String fileName) {
        String format = "";
        if (null != fileName) {
            int i = fileName.lastIndexOf('.');
            if (i >= 0) {
                format = fileName.substring(i + 1);
            }
            String finalFormat = format;
            return Arrays.stream(ALLOWED_FILE_TYPES).anyMatch(s -> s.equalsIgnoreCase(finalFormat));
        } else {
            return false;
        }
    }

    public void refreshTabs(String authToken, String caseId) throws Exception {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartAllTabsUpdate(caseId);
        Map<String, Object> data = startAllTabsUpdateDataContent.caseDataMap();
        data.put("id", caseId);
        data.putAll(documentGenService.generateDocuments(authToken, objectMapper.convertValue(data, CaseData.class)));
        data.putAll(noticeOfChangePartiesService.generate(startAllTabsUpdateDataContent.caseData(), DARESPONDENT));
        data.putAll(noticeOfChangePartiesService.generate(startAllTabsUpdateDataContent.caseData(), DAAPPLICANT));
        OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder().orgPolicyCaseAssignedRole("[APPLICANTSOLICITOR]").build();
        data.put("applicantOrganisationPolicy", applicantOrganisationPolicy);
        data.putAll(partyLevelCaseFlagsService.generateFl401PartyCaseFlags(startAllTabsUpdateDataContent.caseData(),
                                                                                  PartyRole.Representing.DARESPONDENT));
        data.putAll(partyLevelCaseFlagsService.generateFl401PartyCaseFlags(startAllTabsUpdateDataContent.caseData(),
                                                                                  PartyRole.Representing.DAAPPLICANT));
        data.put("caseFlags", Flags.builder().build());
        CaseData caseData = objectMapper.convertValue(data, CaseData.class);

        allTabService.mapAndSubmitAllTabsUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseData
        );

        // Tech debt: need to pick this extra transaction as a tech debt.
        // In the previous one, case data conversion is removing multiple must to have fields.
        updateCommonSetUpForNoCAndCaseFlags(caseId);
        log.info("**********************Tab refresh, CC setup and CourtNav case creation complete**************************");
    }

    public void updateCommonSetUpForNoCAndCaseFlags(String caseId) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartAllTabsUpdate(caseId);
        Map<String, Object> caseDataMap = startAllTabsUpdateDataContent.caseDataMap();
        caseDataMap.putAll(noticeOfChangePartiesService.generate(startAllTabsUpdateDataContent.caseData(), DARESPONDENT));
        caseDataMap.putAll(noticeOfChangePartiesService.generate(startAllTabsUpdateDataContent.caseData(), DAAPPLICANT));
        OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder().orgPolicyCaseAssignedRole("[APPLICANTSOLICITOR]").build();
        caseDataMap.put("applicantOrganisationPolicy", applicantOrganisationPolicy);
        caseDataMap.putAll(partyLevelCaseFlagsService.generateFl401PartyCaseFlags(startAllTabsUpdateDataContent.caseData(),
                                                                           PartyRole.Representing.DARESPONDENT));
        caseDataMap.putAll(partyLevelCaseFlagsService.generateFl401PartyCaseFlags(startAllTabsUpdateDataContent.caseData(),
                                                                           PartyRole.Representing.DAAPPLICANT));
        caseDataMap.put("caseFlags", Flags.builder().build());

        allTabService.submitAllTabsUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataMap
        );
        log.info("Common component setup for NoC and case flags is completed");
    }

    public void validateCaseManagementLocation(CaseData caseData) {
        CaseManagementLocation location = caseData.getCaseManagementLocation();

        if (location == null
            || isBlank(location.getRegion())
            || isBlank(location.getBaseLocation())
            || isBlank(location.getRegionName())
            || isBlank(location.getBaseLocationName())) {

            log.warn("Case management location is invalid: one or more required fields are missing or blank.");

            //  applicant postcode or court name fields are missing or blank
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Case management location is invalid.");
        }
    }
}

