package uk.gov.hmcts.reform.prl.services;

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
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.UploadedDocumentRequest;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UploadDocumentService {

    public static final String CITIZEN_DOCUMENT_UPLOAD_EVENT_ID = "citizenUploadedDocument";

    private final AuthTokenGenerator authTokenGenerator;
    private final CaseDocumentClient caseDocumentClient;
    private final CoreCaseDataApi coreCaseDataApi;
    private final ObjectMapper objectMapper;
    private final IdamClient idamClient;


    public Document uploadDocument(byte[] pdf, String fileName, String contentType, String authorisation) {
        MultipartFile file = new InMemoryMultipartFile("files", fileName, contentType, pdf);


        UploadResponse response = caseDocumentClient.uploadDocuments(authorisation, authTokenGenerator.generate(),
                                                                     CASE_TYPE, JURISDICTION, newArrayList(file));

        Document document = response.getDocuments().stream()
            .findFirst()
            .orElseThrow(() ->
                             new RuntimeException("Document upload failed due to empty result"));

        log.debug("Document upload resulted with links: {}, {}", document.links.self.href, document.links.binary.href);

        return document;
    }

    public Document uploadDocument(MultipartFile file, String fileName, String contentType, String authorisation) {

        UploadResponse response = caseDocumentClient.uploadDocuments(authorisation,
                                                                     authTokenGenerator.generate(),
                                                                     CASE_TYPE,
                                                                     JURISDICTION,
                                                                     newArrayList(file));

        Document document = response.getDocuments().stream()
            .findFirst()
            .orElseThrow(() ->
                             new RuntimeException("Document upload failed due to empty result"));

        log.debug("Document upload resulted with links: {}, {}", document.links.self.href, document.links.binary.href);

        return document;
    }

    public void uploadCitizenDocument(String authorisation, UploadedDocumentRequest uploadedDocumentRequest, String caseId) {

        MultipartFile document = null;

        if (uploadedDocumentRequest.getValues() != null) {
            log.info("=====trying to retrive doc data from request=====");

            document = (MultipartFile) uploadedDocumentRequest.getValues().get(
               "file") != null
                ? ((MultipartFile) uploadedDocumentRequest.getValues().get(
                "file")) : null;
        }
        if (null != document && null != document.getOriginalFilename()) {
            CaseDetails tempCaseDetails = checkIfCasePresent(caseId, authorisation);
            if (tempCaseDetails == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            CaseData tempCaseData = CaseUtils.getCaseData(tempCaseDetails, objectMapper);
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
                tempCaseData,
                uploadedDocumentRequest,
                uploadResponse.getDocuments().get(0)
            );

            StartEventResponse startEventResponse =
                coreCaseDataApi.startEventForCaseWorker(
                    authorisation,
                    authTokenGenerator.generate(),
                    idamClient.getUserInfo(authorisation).getUid(),
                    PrlAppsConstants.JURISDICTION,
                    PrlAppsConstants.CASE_TYPE,
                    caseId,
                    CITIZEN_DOCUMENT_UPLOAD_EVENT_ID
                );

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                           .id(startEventResponse.getEventId())
                           .build())
                .data(caseData).build();

            CaseDetails caseDetails = coreCaseDataApi.submitEventForCaseWorker(
                authorisation,
                authTokenGenerator.generate(),
                idamClient.getUserInfo(authorisation).getUid(),
                PrlAppsConstants.JURISDICTION,
                PrlAppsConstants.CASE_TYPE,
                caseId,
                true,
                caseDataContent
            );

            log.info("Document has been saved in caseData {}", document.getOriginalFilename());

        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public CaseDetails checkIfCasePresent(String caseId, String authorisation) {
        try {
            CaseDetails caseDetails;
            caseDetails = coreCaseDataApi.getCase(
                authorisation,
                authTokenGenerator.generate(),
                caseId
            );
            return caseDetails;
        } catch (Exception ex) {
            log.error("Error while getting the case {} {}", caseId, ex.getMessage());
        }
        return null;
    }

    private CaseData getCaseDataWithUploadedDocs(String caseId, String fileName, CaseData tempCaseData,
                                                 UploadedDocumentRequest uploadedDocumentRequest, Document document) {
        log.info("=====retreiving the case data=====");

        String partyName = uploadedDocumentRequest.getValues().containsKey("partyName")
            ? uploadedDocumentRequest.getValues().get("partyName").toString() : "CITIZEN";
        List<Element<UploadedDocuments>> uploadedDocumentsList;
        Element<UploadedDocuments> uploadedDocsElement =
            element(UploadedDocuments.builder().dateCreated(LocalDate.now())
                        .uploadedBy(uploadedDocumentRequest.getValues().get("partyId").toString())
                        .documentDetails(DocumentDetails.builder().documentName(fileName)
                                             .documentUploadedDate(new Date().toString()).build())
                        .partyName(partyName)
                        .isApplicant(uploadedDocumentRequest.getValues().get("isApplicant").toString())
                        .parentDocumentType(uploadedDocumentRequest.getValues().get("parentDocumentType").toString())
                        .documentType(uploadedDocumentRequest.getValues().get("documentType").toString())
                        .dateCreated(LocalDate.now())
                        .citizenDocument(uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                                             .documentUrl(document.links.self.href)
                                             .documentBinaryUrl(document.links.binary.href)
                                             .documentHash(document.hashToken)
                                             .documentFileName(fileName).build()).build());
        if (tempCaseData.getCitizenUploadedDocumentList() != null
            && !tempCaseData.getCitizenUploadedDocumentList().isEmpty()) {
            uploadedDocumentsList = tempCaseData.getCitizenUploadedDocumentList();
            uploadedDocumentsList.add(uploadedDocsElement);
        } else {
            uploadedDocumentsList = new ArrayList<>();
            uploadedDocumentsList.add(uploadedDocsElement);
        }
        return CaseData.builder().id(Long.valueOf(caseId)).citizenUploadedDocumentList(uploadedDocumentsList).build();
    }

}
