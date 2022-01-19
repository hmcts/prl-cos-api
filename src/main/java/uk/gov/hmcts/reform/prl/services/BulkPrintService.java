package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Base64.getEncoder;
import static java.util.stream.Collectors.toList;

import org.springframework.beans.factory.annotation.Autowired;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BulkPrintService {

    private static final String XEROX_TYPE_PARAMETER = "DIV001";
    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";

    private final AuthTokenGenerator authTokenGenerator;

    private final SendLetterApi sendLetterApi;

    /**
     * Note: the order of documents you send to this service is the order in which they will print.
     */
    public void send(final String caseId, final String letterType, final List<GeneratedDocumentInfo> documents) {
            final List<String> stringifiedDocuments = documents.stream()
                .map(GeneratedDocumentInfo::getBytes)
                .map(getEncoder()::encodeToString)
                .collect(toList());

            send(authTokenGenerator.generate(), caseId, letterType, stringifiedDocuments);
    }

    private void send(final String authToken, final String caseId, final String letterType, final List<String> documents) {
        log.info("Sending {} for case {}", letterType, caseId);
        SendLetterResponse sendLetterResponse = sendLetterApi.sendLetter(authToken,
                                                                         new LetterWithPdfsRequest(documents, XEROX_TYPE_PARAMETER, getAdditionalData(caseId, letterType)));

        log.info("Letter service produced the following letter Id {} for case {}", sendLetterResponse.letterId, caseId);
    }


    private Map<String, Object> getAdditionalData(final String caseId, final String letterType) {
        final Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(LETTER_TYPE_KEY, letterType);
        additionalData.put(CASE_IDENTIFIER_KEY, caseId);
        additionalData.put(CASE_REFERENCE_NUMBER_KEY, caseId);
        return additionalData;
    }
}
