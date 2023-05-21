package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.exception.InvalidResourceException;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Base64.getEncoder;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BulkPrintService {

    private static final String XEROX_TYPE_PARAMETER = "PRL001";
    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";

    private final SendLetterApi sendLetterApi;

    private final CaseDocumentClient caseDocumentClient;

    private final AuthTokenGenerator authTokenGenerator;


    public UUID send(GeneratedDocumentInfo coverDoc, String caseId, String userToken, String letterType, List<GeneratedDocumentInfo> documents) {


        String s2sToken = authTokenGenerator.generate();
        String coverDocument = null;
        List<String> docsToSendToBulkPrint = new ArrayList<>();
        if (null != coverDoc) {
            coverDocument = getEncoder().encodeToString(getDocumentBytes(coverDoc.getUrl(), userToken, s2sToken));
            docsToSendToBulkPrint.add(coverDocument);
        }
        final String stringifiedDocuments = documents.stream()
            .map(docInfo -> getDocumentBytes(docInfo.getUrl(), userToken, s2sToken))
            .map(getEncoder()::encodeToString)
            .collect(toList()).toString();
        docsToSendToBulkPrint.add(stringifiedDocuments);
        log.info("*** Documents from bulk print service after stringify ***" + docsToSendToBulkPrint);
        log.info("Sending {} for case {}", letterType, caseId);
        SendLetterResponse sendLetterResponse = sendLetterApi.sendLetter(
            s2sToken,
            new LetterWithPdfsRequest(
                List.of(coverDocument, stringifiedDocuments),
                XEROX_TYPE_PARAMETER,
                getAdditionalData(caseId, letterType)
            )
        );

        log.info("Letter service produced the following letter Id {} for case {}", sendLetterResponse.letterId, caseId);
        return sendLetterResponse.letterId;
    }


    private Map<String, Object> getAdditionalData(String caseId, String letterType) {
        final Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(LETTER_TYPE_KEY, letterType);
        additionalData.put(CASE_IDENTIFIER_KEY, caseId);
        additionalData.put(CASE_REFERENCE_NUMBER_KEY, caseId);
        return additionalData;
    }

    private byte[] getDocumentBytes(String docUrl, String authToken, String s2sToken) {
        String fileName = FilenameUtils.getName(docUrl);
        ResponseEntity<Resource> resourceResponseEntity = caseDocumentClient.getDocumentBinary(
            authToken,
            s2sToken,
            docUrl
        );

        return Optional.ofNullable(resourceResponseEntity)
            .map(ResponseEntity::getBody)
            .map(resource -> {
                try {
                    return resource.getInputStream().readAllBytes();
                } catch (IOException e) {
                    throw new InvalidResourceException("Doc name " + fileName, e);
                }
            })
            .orElseThrow(() -> new InvalidResourceException("Resource is invalid " + fileName));
    }
}
