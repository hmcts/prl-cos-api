package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;
import static uk.gov.hmcts.reform.prl.enums.Event.TS_ADMIN_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.TS_SOLICITOR_APPLICATION;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestingSupportService {

    @Autowired
    private final ObjectMapper objectMapper;
    @Autowired
    private final EventService eventPublisher;
    @Autowired
    @Qualifier("allTabsService")
    private final AllTabServiceImpl tabService;
    @Autowired
    private final UserService userService;
    @Autowired
    private final DocumentGenService dgsService;
    @Autowired
    private final CaseWorkerEmailService caseWorkerEmailService;
    @Autowired
    private final AllTabServiceImpl allTabsService;

    private static final String VALID_C100_DRAFT_INPUT_JSON = "C100_Dummy_Draft_CaseDetails.json";

    private static final String VALID_FL401_DRAFT_INPUT_JSON = "FL401_Dummy_Draft_CaseDetails.json";

    private static final String VALID_C100_GATEKEEPING_INPUT_JSON = "C100_Dummy_Gatekeeping_CaseDetails.json";

    private static final String VALID_FL401_GATEKEEPING_INPUT_JSON = "FL401_Dummy_Draft_CaseDetails.json";

    public Map<String, Object> initiateCaseCreation(CallbackRequest callbackRequest) throws Exception {
        String requestBody;
        CaseDetails initialCaseDetails = callbackRequest.getCaseDetails();
        CaseData initialCaseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        if (TS_SOLICITOR_APPLICATION.getId().equalsIgnoreCase(callbackRequest.getEventId())) {
            if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(initialCaseData.getCaseTypeOfApplication())) {
                requestBody = ResourceLoader.loadJson(VALID_C100_DRAFT_INPUT_JSON);
            } else {
                requestBody = ResourceLoader.loadJson(VALID_FL401_DRAFT_INPUT_JSON);
            }
        } else {
            if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(initialCaseData.getCaseTypeOfApplication())) {
                requestBody = ResourceLoader.loadJson(VALID_C100_GATEKEEPING_INPUT_JSON);
            } else {
                requestBody = ResourceLoader.loadJson(VALID_FL401_GATEKEEPING_INPUT_JSON);
            }
        }
        CaseDetails dummyCaseDetails = objectMapper.readValue(requestBody, CaseDetails.class);
        if (dummyCaseDetails != null) {
            CaseDetails updatedCaseDetails = dummyCaseDetails.toBuilder()
                .id(initialCaseDetails.getId())
                .createdDate(initialCaseDetails.getCreatedDate())
                .lastModified(initialCaseDetails.getLastModified()).build();
            caseDataUpdated = updatedCaseDetails.getData();
        }
        return caseDataUpdated;
    }

    public Map<String, Object> submittedCaseCreation(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        eventPublisher.publishEvent(new CaseDataChanged(caseData));
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        try {
            caseDataUpdated.putAll(dgsService.generateDocumentsForTestingSupport(authorisation, caseData));
        } catch (Exception e) {
            log.error("Error regenerating the document", e);
        }
        caseData = caseData.toBuilder()
            .c8Document(objectMapper.convertValue(caseDataUpdated.get("c8Document"), Document.class))
            .c1ADocument(objectMapper.convertValue(caseDataUpdated.get("c1ADocument"), Document.class))
            .c8WelshDocument(objectMapper.convertValue(caseDataUpdated.get("c8WelshDocument"), Document.class))
            .finalDocument(objectMapper.convertValue(caseDataUpdated.get("finalDocument"), Document.class))
            .finalWelshDocument(objectMapper.convertValue(caseDataUpdated.get("finalWelshDocument"), Document.class))
            .c1AWelshDocument(objectMapper.convertValue(caseDataUpdated.get("c1AWelshDocument"), Document.class))
            .build();
        tabService.updateAllTabsIncludingConfTab(caseData);
        caseWorkerEmailService.sendEmailToGateKeeper(callbackRequest.getCaseDetails());
        Map<String, Object> allTabsFields = allTabsService.getAllTabsFields(caseData);
        caseDataUpdated.putAll(allTabsFields);

        return caseDataUpdated;
    }
}
