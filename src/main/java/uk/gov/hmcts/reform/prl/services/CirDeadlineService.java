package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CIR_OVERDUE_TASK_CREATION;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CirDeadlineService {

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AllTabServiceImpl allTabService;
    private final ObjectMapper objectMapper;

    public void checkAndCreateCirOverdueTasks() {
        final long startTime = System.currentTimeMillis();
        log.info("*** CIR overdue task creation started ***");

        List<CaseDetails> overdueCases = retrieveOverdueCirCases();
        log.info("Found {} case(s) with overdue CIR", overdueCases.size());

        for (CaseDetails caseDetails : overdueCases) {
            String caseId = String.valueOf(caseDetails.getId());
            try {
                log.info("Firing cirOverdueTaskCreation event for case {}", caseId);
                StartAllTabsUpdateDataContent startData =
                    allTabService.getStartUpdateForSpecificEvent(caseId, CIR_OVERDUE_TASK_CREATION.getValue());
                Map<String, Object> caseDataUpdated = new HashMap<>();
                allTabService.submitAllTabsUpdate(
                    startData.authorisation(),
                    caseId,
                    startData.startEventResponse(),
                    startData.eventRequestData(),
                    caseDataUpdated
                );
                log.info("cirOverdueTaskCreation event fired successfully for case {}", caseId);
            } catch (Exception e) {
                log.error("Failed to fire cirOverdueTaskCreation event for case {}: {}", caseId, e.getMessage(), e);
            }
        }

        log.info("*** CIR overdue task creation completed in {}s ***",
            TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime));
    }

    public List<CaseDetails> retrieveOverdueCirCases() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Find cases where whenReportsMustBeFiledByLocalAuthority exists, is in the past, and CIR was not received by deadline
        String searchQuery = "{"
            + "\"query\":{"
            + "  \"bool\":{"
            + "    \"must\":["
            + "      {\"exists\":{\"field\":\"data.whenReportsMustBeFiledByLocalAuthority\"}},"
            + "      {\"range\":{\"data.whenReportsMustBeFiledByLocalAuthority\":{\"lt\":\"" + today + "\"}}}"
            + "    ],"
            + "    \"must_not\":["
            + "      {\"match\":{\"data.cirReceivedByDeadline\":\"Yes\"}},"
            + "      {\"exists\":{\"field\":\"data.cirUploadedDate\"}}"
            + "    ]"
            + "  }"
            + "},"
            + "\"size\":\"100\""
            + "}";

        try {
            String userToken = systemUserService.getSysUserToken();
            String s2sToken = authTokenGenerator.generate();

            SearchResult searchResult = coreCaseDataApi.searchCases(userToken, s2sToken, CASE_TYPE, searchQuery);

            SearchResultResponse response = objectMapper.convertValue(searchResult, SearchResultResponse.class);
            if (response != null) {
                log.info("CIR overdue search returned {} case(s)", response.getTotal());
                return response.getCases();
            }
        } catch (Exception e) {
            log.error("Exception in retrieveOverdueCirCases: {}", e.getMessage(), e);
        }

        return Collections.emptyList();
    }
}
