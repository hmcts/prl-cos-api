package uk.gov.hmcts.reform.prl.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.models.dto.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "prl-dgs-api", name = "url")
public class DgsService {

    @Autowired
    private DgsApiClient dgsApiClient;

    public GeneratedDocumentInfo generateDocument(String authorisation, CaseDetails caseDetails, String templateName) throws Exception {

        Map<String, Object> tempCaseDetails = new HashMap<String, Object>();
        tempCaseDetails.put("caseDetails", caseDetails);
        GeneratedDocumentInfo generatedDocumentInfo = null;
        try {
            generatedDocumentInfo =
                dgsApiClient.generateDocument(authorisation, GenerateDocumentRequest
                    .builder().template(templateName).values(tempCaseDetails).build()
                );

        } catch (Exception ex) {
            log.error("Error generating and storing document for case {}", caseDetails.getCaseId());
            throw new Exception(ex.getMessage());
        }
        return generatedDocumentInfo;
    }
}
