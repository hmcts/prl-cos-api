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

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_CASE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_OR_RESPONDENT_CASE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NAME_HMCTS_INTERNAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBMITTED_STATE;

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

    private static final String VALID_C100_INPUT_JSON = "C100_Dummy_CaseDetails.json";

    private static final String VALID_FL401_INPUT_JSON = "FL401_Dummy_CaseDetails.json";

    public Map<String, Object> initiateCaseCreation(CallbackRequest callbackRequest) throws Exception {
        String requestBody;
        CaseDetails initialCaseDetails = callbackRequest.getCaseDetails();
        CaseData initialCaseData = CaseUtils.getCaseData(initialCaseDetails, objectMapper);
        Map<String, Object> caseDataUpdated = new HashMap<>();
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(initialCaseData.getCaseTypeOfApplication())) {
            requestBody = ResourceLoader.loadJson(VALID_C100_INPUT_JSON);
        } else {
            requestBody = ResourceLoader.loadJson(VALID_FL401_INPUT_JSON);
        }
        CaseDetails dummyCaseDetails = objectMapper.readValue(requestBody, CaseDetails.class);
        if (dummyCaseDetails != null) {
            CaseDetails updatedCaseDetails = dummyCaseDetails.toBuilder()
                .id(initialCaseDetails.getId())
                .createdDate(initialCaseDetails.getCreatedDate())
                .lastModified(initialCaseDetails.getLastModified()).build();
            caseDataUpdated = updatedCaseDetails.getData();
            updateCaseName(initialCaseData, caseDataUpdated);
        }
        log.info("/testing-support/initiateCaseCreation caseDataUpdated ===>" + caseDataUpdated);
        return caseDataUpdated;
    }

    public Map<String, Object> submittedCaseCreation(String authorisation, CallbackRequest callbackRequest) {
        CaseData data = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        eventPublisher.publishEvent(new CaseDataChanged(data));
        UserDetails userDetails = userService.getUserDetails(authorisation);
        List<String> roles = userDetails.getRoles();
        boolean isCourtStaff = roles.stream().anyMatch(ROLES::contains);
        String state = callbackRequest.getCaseDetails().getState();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if (isCourtStaff && (SUBMITTED_STATE.equalsIgnoreCase(state) || ISSUED_STATE.equalsIgnoreCase(state))) {
            try {
                log.info("Generating documents for the amended details");
                caseDataUpdated.putAll(dgsService.generateDocuments(authorisation, data));
            } catch (Exception e) {
                log.error("Error regenerating the document", e);
            }
        }
        data = data.toBuilder()
            .c8Document((Document) caseDataUpdated.get("c8Document"))
            .c1ADocument((Document) caseDataUpdated.get("c1ADocument"))
            .c8WelshDocument((Document) caseDataUpdated.get("c8WelshDocument"))
            .finalDocument((Document) caseDataUpdated.get("finalDocument"))
            .finalWelshDocument((Document) caseDataUpdated.get("finalWelshDocument"))
            .c1AWelshDocument((Document) caseDataUpdated.get("c1AWelshDocument"))
            .build();
        tabService.updateAllTabsIncludingConfTab(data);
        log.info("/testing-support/submittedCaseCreation end ===>" + caseDataUpdated);
        return caseDataUpdated;
    }

    private static void updateCaseName(CaseData initialCaseData, Map<String, Object> caseDataUpdated) {
        caseDataUpdated.put(APPLICANT_CASE_NAME, initialCaseData.getApplicantCaseName());
        caseDataUpdated.put(CASE_NAME_HMCTS_INTERNAL, initialCaseData.getApplicantCaseName());
        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(initialCaseData.getCaseTypeOfApplication())) {
            caseDataUpdated.put(APPLICANT_OR_RESPONDENT_CASE_NAME, initialCaseData.getApplicantCaseName());
        }
    }

}
