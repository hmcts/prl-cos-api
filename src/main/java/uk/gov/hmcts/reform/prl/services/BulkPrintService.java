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
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Base64.getEncoder;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BulkPrintService {

    private static final String XEROX_TYPE_PARAMETER = "PRL001";
    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String RECIPIENTS = "recipients";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";

    private final SendLetterApi sendLetterApi;

    private final CaseDocumentClient caseDocumentClient;

    private final AuthTokenGenerator authTokenGenerator;



    public UUID send(String caseId, String userToken, String letterType, List<Document> documents, String recipientName) {

        String s2sToken = authTokenGenerator.generate();
        final List<String> stringifiedDocuments = documents.stream()
            .map(docInfo -> {
                try {
                    return getDocumentsAsBytes(docInfo.getDocumentBinaryUrl(), userToken, s2sToken);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .map(getEncoder()::encodeToString)
            .toList();
        log.info("Sending {} for case {}", letterType, caseId);
        log.info("1111111111111111111222222222");
        log.info("stringifiedDocuments--> {}",stringifiedDocuments);
        Map<String, Object> map = getAdditionalData(caseId, letterType, recipientName);
        LetterWithPdfsRequest letter = new LetterWithPdfsRequest(
            stringifiedDocuments,
            XEROX_TYPE_PARAMETER,
            getAdditionalData(caseId, letterType, recipientName)
        );

        log.info("ddddd {}",letter);
        log.info("dfffff {}",s2sToken);
        log.info("mapppp {}",map);


        SendLetterResponse sendLetterResponse = sendLetterApi.sendLetter(
                s2sToken,
                new LetterWithPdfsRequest(
                    stringifiedDocuments,
                    XEROX_TYPE_PARAMETER,
                    getAdditionalData(caseId, letterType, recipientName)
                )
            );

        log.info(
            "Letter service produced the following letter Id {} for case {}",
            sendLetterResponse != null ? sendLetterResponse.letterId : "SOMETHING WRONG",
            caseId
        );
        return sendLetterResponse != null ? sendLetterResponse.letterId : null;
    }


    private Map<String, Object> getAdditionalData(String caseId, String letterType, String recipientName) {
        final Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(LETTER_TYPE_KEY, letterType);
        additionalData.put(CASE_IDENTIFIER_KEY, caseId);
        additionalData.put(CASE_REFERENCE_NUMBER_KEY, caseId);
        additionalData.put(RECIPIENTS, Arrays.asList(recipientName));
        return additionalData;
    }

    private byte[] getDocumentsAsBytes(String docUrl, String authToken, String s2sToken) throws IOException {
        return getDocumentBytes(docUrl, authToken, s2sToken);
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
