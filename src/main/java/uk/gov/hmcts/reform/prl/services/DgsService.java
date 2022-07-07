package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.DgsApiClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.framework.exceptions.DocumentGenerationException;
import uk.gov.hmcts.reform.prl.mapper.AppObjectMapper;
import uk.gov.hmcts.reform.prl.mapper.welshlang.WelshLangMapper;
import uk.gov.hmcts.reform.prl.models.dto.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "prl-dgs-api", name = "url")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DgsService {

    private final DgsApiClient dgsApiClient;

    public GeneratedDocumentInfo generateDocument(String authorisation, CaseDetails caseDetails, String templateName) throws Exception {

        Map<String, Object> tempCaseDetails = new HashMap<>();
        tempCaseDetails.put("caseDetails", AppObjectMapper.getObjectMapper().convertValue(caseDetails, Map.class));
        log.info("in DGS Service tempCaseDetails, after appobjectMapper {}", tempCaseDetails);
        GeneratedDocumentInfo generatedDocumentInfo = null;
        try {
            generatedDocumentInfo =
                dgsApiClient.generateDocument(authorisation, GenerateDocumentRequest
                    .builder().template(templateName).values(tempCaseDetails).build()
                );

        } catch (Exception ex) {
            log.error("Error generating and storing document for case {}", caseDetails.getCaseId());
            throw new DocumentGenerationException(ex.getMessage(), ex);
        }
        return generatedDocumentInfo;
    }

    public GeneratedDocumentInfo generateWelshDocument(String authorisation, CaseDetails caseDetails, String templateName) throws Exception {

        Map<String, Object> tempCaseDetails = new HashMap<>();
        // Get the Welsh Value of each object using Welsh Mapper
        Map<String, Object> caseDataMap = AppObjectMapper.getObjectMapper().convertValue(caseDetails, Map.class);
        Map<String, Object> caseDataValues = (Map<String, Object>) caseDataMap.get("case_data");
        caseDataValues.forEach((k, v) -> {
            if (v != null) {
                Object updatedWelshObj = WelshLangMapper.applyWelshTranslation(k, v,
                                                                               PrlAppsConstants.C100_CASE_TYPE
                                                                                   .equalsIgnoreCase(
                                                                                       caseDetails.getCaseData()
                                                                                           .getCaseTypeOfApplication()
                                                                                   )
                );
                caseDataValues.put(k, updatedWelshObj);
            }
        });
        caseDataMap.put("case_data", caseDataValues);
        tempCaseDetails.put("caseDetails", caseDataMap);

        GeneratedDocumentInfo generatedDocumentInfo = null;
        try {
            generatedDocumentInfo =
                dgsApiClient.generateDocument(authorisation, GenerateDocumentRequest
                    .builder().template(templateName).values(tempCaseDetails).build()
                );

        } catch (Exception ex) {
            log.error("Error generating and storing document for case {}", caseDetails.getCaseId());
            throw new DocumentGenerationException(ex.getMessage(), ex);
        }
        return generatedDocumentInfo;
    }
}
