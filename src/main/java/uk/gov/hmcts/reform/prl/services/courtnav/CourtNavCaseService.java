package uk.gov.hmcts.reform.prl.services.courtnav;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
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
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CourtNavCaseService {

    public static final String COURTNAV_DOCUMENT_UPLOAD_EVENT_ID = "courtnav-document-upload";
    public static final String[] ALLOWED_FILE_TYPES = {"pdf", "jpeg", "jpg", "doc", "docx", "bmp", "png", "tiff", "txt"};
    public static final String[] ALLOWED_TYPE_OF_DOCS = {"FL401", "C8", "WITNESS_STATEMENT", "EXHIBITS_EVIDENCE", "EXHIBITS_COVERSHEET"};
    private final CoreCaseDataApi coreCaseDataApi;
    private final IdamClient idamClient;
    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final DocumentGenService documentGenService;
    private final AllTabServiceImpl allTabService;

    public CaseDetails createCourtNavCase(String authToken, CaseData caseData) throws Exception {
        log.info("Roles of the calling user {}", idamClient.getUserInfo(authToken).getRoles());
        log.info("Name of the calling user {}", idamClient.getUserInfo(authToken).getName());
        log.info("ApplicantCaseName::::: {}", caseData.getApplicantCaseName());
        Map<String, Object> caseDataMap = caseData.toMap(CcdObjectMapper.getObjectMapper());
        log.info("****************executing caseworker flow***************");
        log.info("before case creation", caseDataMap);
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
            .data(caseDataMap).build();

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
        if (checkFileFormat(document.getOriginalFilename()) && checkTypeOfDocument(typeOfDocument)) {
            CaseDetails tempCaseDetails = coreCaseDataApi.getCase(
                authorisation,
                authTokenGenerator.generate(),
                caseId
            );

            log.info("tempCaseDetails CaseData ****{}**** ", tempCaseDetails.getData());
            Iterables.removeIf(tempCaseDetails.getData().values(), Objects::isNull);
            log.info("After removing null tempCaseDetails CaseData ****{}**** ", tempCaseDetails.getData());
            CaseData tempCaseData = CaseUtils.getCaseData(tempCaseDetails, objectMapper);
            UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
                authorisation,
                authTokenGenerator.generate(),
                PrlAppsConstants.CASE_TYPE,
                PrlAppsConstants.JURISDICTION,
                Arrays.asList(document)
            );
            log.info("Document uploaded successfully through caseDocumentClient");
            CaseData updatedCaseData = addDocumentAndGetCaseData(
                document.getOriginalFilename(),
                typeOfDocument,
                tempCaseData.getApplicantCaseName(),
                tempCaseData,
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
                    COURTNAV_DOCUMENT_UPLOAD_EVENT_ID
                );

            CaseDataContent caseDataContent = CaseDataContent.builder()
                .eventToken(startEventResponse.getToken())
                .event(Event.builder()
                           .id(startEventResponse.getEventId())
                           .build())
                .data(updatedCaseData.toMap(CcdObjectMapper.getObjectMapper())).build();

            log.info("Updated CaseData ****{}**** ", updatedCaseData);
            log.info("Updated CaseData map -----{}--- ", updatedCaseData.toMap(CcdObjectMapper.getObjectMapper()));
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

    private CaseData addDocumentAndGetCaseData(String fileName, String typeOfDocument, String partyName, CaseData tempCaseData, Document document) {

        List<Element<UploadedDocuments>> uploadedDocumentsList;
        Element<UploadedDocuments> uploadedDocsElement =
            element(UploadedDocuments.builder().dateCreated(new Date())
                        .documentType(typeOfDocument)
                        .uploadedBy("COURNAV")
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

        tempCaseData = tempCaseData.toBuilder().courtNavUploadedDocs(uploadedDocumentsList).build();
        return tempCaseData;
    }

    private boolean checkTypeOfDocument(String typeOfDocument) {
        if (typeOfDocument != null) {
            return Arrays.stream(ALLOWED_TYPE_OF_DOCS).anyMatch(s -> s.equalsIgnoreCase(typeOfDocument));
        }
        return false;
    }

    private boolean checkFileFormat(String fileName) {
        String format = "";
        int i = fileName.lastIndexOf('.');
        if (i >= 0) {
            format = fileName.substring(i + 1);
        }
        String finalFormat = format;
        return Arrays.stream(ALLOWED_FILE_TYPES).anyMatch(s -> s.equalsIgnoreCase(finalFormat));
    }

    public void refreshTabs(String authToken, Map<String, Object> data, Long id) throws Exception {
        log.info("Before document generation {}", data);
        data.put("id", String.valueOf(id));
        data.putAll(documentGenService.generateDocuments(authToken, objectMapper.convertValue(data, CaseData.class)));
        CaseData caseData = objectMapper.convertValue(data, CaseData.class);
        log.info("After tab refresh {}", caseData);
        allTabService.updateAllTabsIncludingConfTab(caseData);
        log.info("**********************Tab refresh and Courtnav case creation complete**************************");
    }
}

