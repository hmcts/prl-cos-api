package uk.gov.hmcts.reform.prl.services.courtnav;

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
import uk.gov.hmcts.reform.ccd.client.model.*;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.clients.ccd.CoreCaseDataService;

import java.time.LocalDate;
import java.util.*;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CourtNavCaseService {

    protected static final String[] ALLOWED_FILE_TYPES = {"pdf", "jpeg", "jpg", "doc", "docx", "bmp", "png", "tiff", "txt", "tif"};
    protected static final String[] ALLOWED_TYPE_OF_DOCS = {"WITNESS_STATEMENT", "EXHIBITS_EVIDENCE", "EXHIBITS_COVERSHEET"};
    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamClient idamClient;
    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final DocumentGenService documentGenService;
    private final AllTabServiceImpl allTabService;
    private final CoreCaseDataService coreCaseDataService;
    private final SystemUserService systemUserService;

    public CaseDetails createCourtNavCase(String authToken, CaseData caseData) throws Exception {

        String authorisation = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(authorisation);

        Map<String, Object> caseDataMap = caseData.toMap(CcdObjectMapper.getObjectMapper());
        log.info("****************Creating courtnav case***************");

        CaseEvent caseEvent = CaseEvent.COURTNAV_CASE_CREATION;
        log.info("Following case event will be triggered {}", caseEvent.getValue());

        EventRequestData eventRequestData = coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId);
        StartEventResponse startEventResponse =
            coreCaseDataService.startSubmitCreate(
                authToken,
                authTokenGenerator.generate(),
                idamClient.getUserInfo(authToken).getUid(),
                eventRequestData,
                true
            );

        CaseData caseDataUpdate = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);

        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContent(
            startEventResponse,
            caseDataUpdate
        );

        return coreCaseDataService.submitCreate(
            authToken,
            authTokenGenerator.generate(),
            idamClient.getUserInfo(authToken).getUid(),
            caseDataContent,
            true
        );
    }

    public void uploadDocument(String authorisation, MultipartFile document, String typeOfDocument, String caseId) {

        String authorisationToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(authorisation);

        CaseEvent caseEvent = CaseEvent.COURTNAV_DOCUMENT_UPLOAD_EVENT_ID;
        log.info("Following case event will be triggered {}", caseEvent.getValue());

        if (null != document && null != document.getOriginalFilename()
            && checkFileFormat(document.getOriginalFilename())
            && checkTypeOfDocument(typeOfDocument)) {
            CaseDetails tempCaseDetails = checkIfCasePresent(caseId, authorisation);
            if (tempCaseDetails == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            CaseData tempCaseData = CaseUtils.getCaseData(tempCaseDetails, objectMapper);
            if (tempCaseData.getNumberOfAttachments() != null && tempCaseData.getCourtNavUploadedDocs() != null
                && Integer.valueOf(tempCaseData.getNumberOfAttachments())
                <= tempCaseData.getCourtNavUploadedDocs().size()) {
                log.error("Number of attachments size is reached {}", tempCaseData.getNumberOfAttachments());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
                authorisation,
                authTokenGenerator.generate(),
                PrlAppsConstants.CASE_TYPE,
                PrlAppsConstants.JURISDICTION,
                Arrays.asList(document)
            );
            log.info("Document uploaded successfully through caseDocumentClient");
            CaseData caseData = getCaseDataWithUploadedDocs(
                caseId,
                document.getOriginalFilename(),
                typeOfDocument,
                tempCaseData,
                uploadResponse.getDocuments().get(0)
            );

            EventRequestData eventRequestData = coreCaseDataService.eventRequest(caseEvent, systemUpdateUserId);
            StartEventResponse startEventResponse =
                coreCaseDataService.startUpdate(
                    authorisationToken,
                    eventRequestData,
                    caseId,
                    true
                );

            CaseData caseDataUpdate = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);

            CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContent(
                startEventResponse,
                caseDataUpdate
            );

            coreCaseDataService.submitUpdate(
                authorisationToken,
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

    private CaseData getCaseDataWithUploadedDocs(String caseId, String fileName, String typeOfDocument,
                                                 CaseData tempCaseData, Document document) {
        String partyName = tempCaseData.getApplicantCaseName() != null
            ? tempCaseData.getApplicantCaseName() : "COURTNAV";
        List<Element<UploadedDocuments>> uploadedDocumentsList;
        Element<UploadedDocuments> uploadedDocsElement =
            element(UploadedDocuments.builder().dateCreated(LocalDate.now())
                        .documentType(typeOfDocument)
                        .uploadedBy("COURTNAV")
                        .documentDetails(DocumentDetails.builder().documentName(fileName)
                                             .documentUploadedDate(new Date().toString()).build())
                        .partyName(partyName).isApplicant("NA_COURTNAV")
                        .parentDocumentType("NA_COURTNAV")
                        .citizenDocument(uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                                             .documentUrl(document.links.self.href)
                                             .documentBinaryUrl(document.links.binary.href)
                                             .documentHash(document.hashToken)
                                             .documentFileName(fileName).build()).build());
        if (tempCaseData.getCourtNavUploadedDocs() != null) {
            uploadedDocumentsList = tempCaseData.getCourtNavUploadedDocs();
            uploadedDocumentsList.add(uploadedDocsElement);
        } else {
            uploadedDocumentsList = new ArrayList<>();
            uploadedDocumentsList.add(uploadedDocsElement);
        }
        return CaseData.builder().id(Long.valueOf(caseId)).courtNavUploadedDocs(uploadedDocumentsList).build();
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

    public void refreshTabs(String authToken, Map<String, Object> data, Long id) throws Exception {
        data.put("id", String.valueOf(id));
        data.putAll(documentGenService.generateDocuments(authToken, objectMapper.convertValue(data, CaseData.class)));
        CaseData caseData = objectMapper.convertValue(data, CaseData.class);
        allTabService.updateAllTabsIncludingConfTab(caseData);
        log.info("**********************Tab refresh and Courtnav case creation complete**************************");
    }
}

