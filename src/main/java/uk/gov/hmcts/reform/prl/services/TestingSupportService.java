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

import java.util.List;
import java.util.Map;

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

    public Map<String, Object> submitCaseCreation(String authorisation, CallbackRequest callbackRequest) throws Exception {
        log.info("/testing-support/submit-case-creation start ===>" + callbackRequest.getCaseDetails());
        String requestBody = null;
        CaseDetails initialCaseDetails = callbackRequest.getCaseDetails();
        CaseData initialCaseData = CaseUtils.getCaseData(initialCaseDetails, objectMapper);
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(initialCaseData.getCaseTypeOfApplication())) {
            requestBody = ResourceLoader.loadJson(VALID_C100_INPUT_JSON);
        }
        CaseDetails dummyCaseDetails = objectMapper.readValue(requestBody, CaseDetails.class);
        log.info("/testing-support/submit-case-creation dummyCaseDetails ===>" + dummyCaseDetails);
        CaseDetails updatedCaseDetails = dummyCaseDetails.toBuilder()
            .id(initialCaseDetails.getId())
            .createdDate(initialCaseDetails.getCreatedDate())
            .lastModified(initialCaseDetails.getLastModified()).build();
        log.info("/testing-support/submit-case-creation updatedCaseDetails ===>" + updatedCaseDetails);
        CaseData updatedCaseData = CaseUtils.getCaseData(updatedCaseDetails, objectMapper);
        Map<String, Object> caseDataUpdated = updatedCaseDetails.getData();
        log.info("/testing-support/submit-case-creation caseDataUpdated ===>" + caseDataUpdated);
        eventPublisher.publishEvent(new CaseDataChanged(updatedCaseData));
        UserDetails userDetails = userService.getUserDetails(authorisation);
        List<String> roles = userDetails.getRoles();
        boolean isCourtStaff = roles.stream().anyMatch(ROLES::contains);
        String state = updatedCaseDetails.getState();
        if (isCourtStaff && (SUBMITTED_STATE.equalsIgnoreCase(state) || ISSUED_STATE.equalsIgnoreCase(state))) {
            try {
                log.info("Generating documents for the amended details");
                caseDataUpdated.putAll(dgsService.generateDocuments(authorisation, updatedCaseData));
            } catch (Exception e) {
                log.error("Error regenerating the document", e);
            }
        }
        updatedCaseData = updatedCaseData.toBuilder()
            .c8Document((Document) caseDataUpdated.get("c8Document"))
            .c1ADocument((Document) caseDataUpdated.get("c1ADocument"))
            .c8WelshDocument((Document) caseDataUpdated.get("c8WelshDocument"))
            .finalDocument((Document) caseDataUpdated.get("finalDocument"))
            .finalWelshDocument((Document) caseDataUpdated.get("finalWelshDocument"))
            .c1AWelshDocument((Document) caseDataUpdated.get("c1AWelshDocument"))
            .build();
        tabService.updateAllTabsIncludingConfTab(updatedCaseData);
        log.info("/update-task-list end ===>" + caseDataUpdated);
        return caseDataUpdated;
    }

}
