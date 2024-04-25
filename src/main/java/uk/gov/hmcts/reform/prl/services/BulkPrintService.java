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
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    private final DocumentGenService documentGenService;



    public UUID send(String caseId, String userToken, String letterType, List<Document> documents, String recipientName) {
        String s2sToken = authTokenGenerator.generate();
        List<Document> pdfDocuments = new ArrayList<>();

        try {
            for (Document doc:documents) {
                pdfDocuments.add(documentGenService.convertToPdf(userToken, doc));
            }
        } catch (NullPointerException e) {
            throw new NullPointerException("Null Pointer exception at bulk print send : " + e);
        } catch (Exception e) {
            log.info("The bulk print service has failed during convertToPdf", e);
        }
        long stringifiedDocStartTime = System.currentTimeMillis();
        final List<String> stringifiedDocuments = pdfDocuments.parallelStream()
            .map(docInfo -> getDocumentsAsBytes(docInfo.getDocumentBinaryUrl(), userToken, s2sToken))
            .map(getEncoder()::encodeToString)
            .toList();
        log.info("*** Time taken to convert docs to stringified array - {}s",
                 TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - stringifiedDocStartTime));

        log.info("Sending {} for case {}", letterType, caseId);
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

    private byte[] getDocumentsAsBytes(String docUrl, String authToken, String s2sToken) {
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
