package uk.gov.hmcts.reform.prl.services;

import com.launchdarkly.shaded.com.google.gson.Gson;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "prl-dgs-api", name = "url")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DgsService {

    private final DgsApiClient dgsApiClient;
    private final AllegationOfHarmRevisedService allegationOfHarmService;
    private static final String CASE_DETAILS_STRING = "caseDetails";
    private static final String ERROR_MESSAGE = "Error generating and storing document for case {}";

    public GeneratedDocumentInfo generateDocument(String authorisation, String caseId, String templateName,
                                                  Map<String, Object> dataMap) throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo;
        try {
            generatedDocumentInfo =
                dgsApiClient.generateDocument(authorisation, GenerateDocumentRequest
                    .builder().template(templateName).values(dataMap).build()
                );

        } catch (Exception ex) {
            log.error(ERROR_MESSAGE, caseId);
            throw new DocumentGenerationException(ex.getMessage(), ex);
        }
        log.info("Template name : {} GeneratedDocumentInfo: {}", templateName, generatedDocumentInfo);
        return generatedDocumentInfo;
    }

    public GeneratedDocumentInfo generateDocument(String authorisation, CaseDetails caseDetails, String templateName) throws Exception {

        CaseData caseData = caseDetails.getCaseData();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseDetails.setCaseData(allegationOfHarmService.updateChildAbuses(caseData));
        }
        log.info("before case data : {}",new Gson().toJson(caseDetails));
        Map<String, Object> tempCaseDetails = new HashMap<>();
        tempCaseDetails.put(
            CASE_DETAILS_STRING,
            AppObjectMapper.getObjectMapper().convertValue(caseDetails, Map.class)
        );
        log.info("case data : {}",new Gson().toJson(tempCaseDetails));
        GeneratedDocumentInfo generatedDocumentInfo = null;
        try {
            generatedDocumentInfo =
                dgsApiClient.generateDocument(authorisation, GenerateDocumentRequest
                    .builder().template(templateName).values(tempCaseDetails).build()
                );

        } catch (Exception ex) {
            log.error(ERROR_MESSAGE, caseDetails.getCaseId());
            throw new DocumentGenerationException(ex.getMessage(), ex);
        }
        return generatedDocumentInfo;
    }

    public GeneratedDocumentInfo generateWelshDocument(String authorisation, String caseId, String caseTypeOfApplication, String templateName,
                                                       Map<String, Object> dataMap) throws Exception {

        dataMap.forEach((k, v) -> {
            if (v != null) {
                Object updatedWelshObj = WelshLangMapper.applyWelshTranslation(k, v,
                                                                               PrlAppsConstants.C100_CASE_TYPE
                                                                                   .equalsIgnoreCase(
                                                                                       caseTypeOfApplication)
                );
                dataMap.put(k, updatedWelshObj);
            }
        });

        return generateDocument(authorisation, caseId, templateName,
                                dataMap
        );
    }

    public GeneratedDocumentInfo generateWelshDocument(String authorisation, CaseDetails caseDetails, String templateName) throws Exception {


        CaseData caseData = caseDetails.getCaseData();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseDetails.setCaseData(allegationOfHarmService.updateChildAbuses(caseData));
        }
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
        Map<String, Object> tempCaseDetails = new HashMap<>();
        tempCaseDetails.put(CASE_DETAILS_STRING, caseDataMap);

        GeneratedDocumentInfo generatedDocumentInfo = null;
        try {
            generatedDocumentInfo =
                dgsApiClient.generateDocument(authorisation, GenerateDocumentRequest
                    .builder().template(templateName).values(tempCaseDetails).build()
                );

        } catch (Exception ex) {
            log.error(ERROR_MESSAGE, caseDetails.getCaseId());
            throw new DocumentGenerationException(ex.getMessage(), ex);
        }
        return generatedDocumentInfo;
    }

    public GeneratedDocumentInfo generateCitizenDocument(String authorisation,
                                                         GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest,
                                                         String templateName) throws Exception {

        Map<String, Object> tempCaseDetails = new HashMap<>();
        String freeTextUploadStatements = null;
        if (generateAndUploadDocumentRequest.getValues() != null
            && generateAndUploadDocumentRequest.getValues().containsKey("freeTextUploadStatements")) {
            freeTextUploadStatements = generateAndUploadDocumentRequest.getValues().get("freeTextUploadStatements");
        }
        String caseId = generateAndUploadDocumentRequest.getValues().get("caseId");
        CaseDetails caseDetails = CaseDetails.builder().caseId(caseId).state("ISSUE")
            .caseData(CaseData.builder().id(Long.valueOf(caseId))
                          .citizenUploadedStatement(freeTextUploadStatements).build()).build();
        tempCaseDetails.put(
            CASE_DETAILS_STRING,
            AppObjectMapper.getObjectMapper().convertValue(caseDetails, Map.class)
        );


        GeneratedDocumentInfo generatedDocumentInfo = null;
        try {
            generatedDocumentInfo =
                dgsApiClient.generateDocument(authorisation, GenerateDocumentRequest
                    .builder().template(templateName).values(tempCaseDetails).build()
                );

        } catch (Exception ex) {
            log.error(ERROR_MESSAGE, caseId);
            throw new DocumentGenerationException(ex.getMessage(), ex);
        }
        return generatedDocumentInfo;
    }


    public GeneratedDocumentInfo generateCoverLetterDocument(String authorisation, Map<String, Object> requestPayload,
                                                             String templateName, String caseId) throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo = null;
        try {
            generatedDocumentInfo =
                dgsApiClient.generateDocument(authorisation, GenerateDocumentRequest
                    .builder().template(templateName).values(requestPayload).build()
                );

        } catch (Exception ex) {
            log.error(ERROR_MESSAGE, caseId);
            throw new DocumentGenerationException(ex.getMessage(), ex);
        }
        return generatedDocumentInfo;
    }
}
