package uk.gov.hmcts.reform.prl.services.courtnav;

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
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.FL401Case;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseService {

    public static final String COURTNAV_DOCUMENT_UPLOAD_EVENT_ID = "courtnav-document-upload";
    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamClient idamClient;
    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator authTokenGenerator;

    public CaseDetails createCourtNavCase(String authToken, CaseData testInput) {
        log.info("Roles of the calling user {}", idamClient.getUserInfo(authToken).getRoles());
        log.info("Name of the calling user {}", idamClient.getUserInfo(authToken).getName());
        log.info("ApplicantCaseName::::: {}", testInput.getApplicantCaseName());
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("applicantCaseName", testInput.getApplicantCaseName());
        log.info("****************executing caseworker flow***************");
        StartEventResponse startEventResponse =
            coreCaseDataApi.startForCaseworker(
                authToken,
                authTokenGenerator.generate(),
                idamClient.getUserInfo(authToken).getUid(),
                PrlAppsConstants.JURISDICTION,
                PrlAppsConstants.CASE_TYPE,
                "courtnav-case-creation"
            );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .data(FL401Case.builder().applicantCaseName(testInput.getApplicantCaseName()).build()).build();

        return coreCaseDataApi.submitForCaseworker(
            authToken,
            authTokenGenerator.generate(),
            idamClient.getUserInfo(authToken).getUid(),
            PrlAppsConstants.JURISDICTION,
            PrlAppsConstants.CASE_TYPE,
            true,
            caseDataContent
        );
    }

    public void uploadDocument(String authorisation, MultipartFile document, String typeOfDocument, String caseId) {

        Map<String, Object> caseData = new HashMap<>();
        if (typeOfDocument.equals("fl401Doc1") || typeOfDocument.equals("fl401Doc2")) {
            UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
                authorisation,
                authTokenGenerator.generate(),
                PrlAppsConstants.CASE_TYPE,
                PrlAppsConstants.JURISDICTION,
                Arrays.asList(document)
            );
            log.info("Document uploaded successfully through caseDocumentClient");
            Document uploadedDocument = uploadResponse.getDocuments().get(0);
            GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
                .url(uploadedDocument.links.self.href)
                .mimeType(uploadedDocument.mimeType)
                .hashToken(uploadedDocument.hashToken)
                .binaryUrl(uploadedDocument.links.binary.href)
                .build();
            caseData.put(typeOfDocument, uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName(document.getOriginalFilename()).build());


            StartEventResponse startEventResponse =
                coreCaseDataApi.startEventForCaseWorker(
                    authorisation,
                    authTokenGenerator.generate(),
                    idamClient.getUserInfo(authorisation).getUid(),
                    PrlAppsConstants.JURISDICTION,
                    PrlAppsConstants.CASE_TYPE,
                    caseId,
                    COURTNAV_DOCUMENT_UPLOAD_EVENT_ID
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

            log.info("Document has been saved in caseData {}", caseDetails.getData().get(typeOfDocument));

        } else {
            log.error("Un acceptable type of document {}", typeOfDocument);
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
    }

}

