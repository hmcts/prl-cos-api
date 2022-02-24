package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.models.dto.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.utils.DgsSerializer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "prl-dgs-api", name = "url")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DgsService {

    private final DgsApiClient dgsApiClient;

    public GeneratedDocumentInfo generateDocument(String authorisation, CaseDetails caseDetails, String templateName) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule());

        SimpleModule module = new SimpleModule();
        module.addSerializer(CaseData.class, new DgsSerializer());
        objectMapper.registerModule(module);

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> tempCaseDetails = new HashMap<String, Object>();
        tempCaseDetails.put("caseDetails", caseDetails);
        JSONObject json1 = new JSONObject(caseDetails);
        log.info(json1.toString(4));

        Map<String, Object> caseDataMap = mapper.convertValue(caseDetails.getCaseData(), Map.class);
        JSONObject json = new JSONObject(caseDataMap);
        log.info(json.toString(4));




        Map<String, Object> caseData = mapper.convertValue(caseDetails.getCaseData(), Map.class);

        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("caseId", caseDetails.getCaseId());
        caseDetailsMap.put("state", caseDetails.getState());
        caseDetailsMap.put("caseData",caseData);

        log.info(caseDetailsMap.toString());

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
