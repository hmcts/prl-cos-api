package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.models.dto.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.utils.DgsSerializer;

import java.util.Map;

import static java.util.Optional.ofNullable;

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

        Map<String, Object> caseDetailsMap = mapper.convertValue(caseDetails, Map.class);
        log.info(caseDetailsMap.toString());

        GeneratedDocumentInfo generatedDocumentInfo = null;
        try {
            generatedDocumentInfo =
                dgsApiClient.generateDocument(authorisation, GenerateDocumentRequest
                    .builder().template(templateName).values(caseDetailsMap).build()
                );

        } catch (Exception ex) {
            log.error("Error generating and storing document for case {}", caseDetails.getCaseId());
            throw new Exception(ex.getMessage());
        }
        return generatedDocumentInfo;
    }
}
