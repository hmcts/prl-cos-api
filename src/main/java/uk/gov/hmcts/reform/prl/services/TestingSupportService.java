package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

import java.util.HashMap;
import java.util.Map;

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
    CaseRepository caseRepository;

    private static final String VALID_C100_INPUT_JSON = "C100_Dummy_CaseDetails.json";
    private static final String VALID_C100_CITIZEN_INPUT_JSON = "C100_citizen_Dummy_CaseDetails.json";
    private static final String VALID_FL401_INPUT_JSON = "FL401_Dummy_CaseDetails.json";

    public Map<String, Object> initiateCaseCreation(CallbackRequest callbackRequest) throws Exception {
        String requestBody;
        CaseDetails initialCaseDetails = callbackRequest.getCaseDetails();
        CaseData initialCaseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
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
        }
        return caseDataUpdated;
    }

    public CaseDetails initiateCaseCreation_citizen()throws Exception {
        String requestBody;
        Map<String, Object> caseDataUpdated = new HashMap<>();
        requestBody=ResourceLoader.loadJson(VALID_C100_CITIZEN_INPUT_JSON);
        CaseDetails dummyCaseDetails = objectMapper.readValue(requestBody, CaseDetails.class);
     return dummyCaseDetails;
    }
}
