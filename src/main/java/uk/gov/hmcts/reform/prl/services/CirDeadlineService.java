package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;
import static uk.gov.hmcts.reform.prl.constants.cafcass.CafcassAppConstants.CIR_DUE_DATE;
import static uk.gov.hmcts.reform.prl.constants.cafcass.CafcassAppConstants.CIR_RECEIVED_BY_DEADLINE;
import static uk.gov.hmcts.reform.prl.constants.cafcass.CafcassAppConstants.CIR_UPLOADED_DATE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CIR_OVERDUE_TASK_CREATION;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CirDeadlineService {

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AllTabServiceImpl allTabService;

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

    private static final int PAGE_SIZE = 100;

    public List<CaseDetails> retrieveOverdueCirCases() {
        String today = LocalDate.now(ZoneId.of(LONDON_TIME_ZONE)).format(DateTimeFormatter.ISO_LOCAL_DATE);

        try {
            String userToken = systemUserService.getSysUserToken();
            String s2sToken = authTokenGenerator.generate();

            SearchResult searchResult = coreCaseDataApi.searchCases(
                userToken, s2sToken, CASE_TYPE, buildOverdueCirQuery(today, 0));

            int totalCases = searchResult.getTotal();
            log.info("CIR overdue search found {} total case(s)", totalCases);

            if (totalCases == 0) {
                return Collections.emptyList();
            }

            List<CaseDetails> allCases = new ArrayList<>(searchResult.getCases());
            int pages = (int) Math.ceil((double) totalCases / PAGE_SIZE);

            for (int i = 1; i < pages; i++) {
                log.info("Processing page {} of {}", i + 1, pages);
                SearchResult page = coreCaseDataApi.searchCases(
                    userToken, s2sToken, CASE_TYPE, buildOverdueCirQuery(today, i * PAGE_SIZE));
                allCases.addAll(page.getCases());
            }

            log.info("Total CIR overdue cases to process: {}", allCases.size());
            return allCases;

        } catch (Exception e) {
            log.error("Exception in retrieveOverdueCirCases: {}", e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    private String buildOverdueCirQuery(String today, int from) {
        return "{"
            + "\"query\":{"
            + "  \"bool\":{"
            + "    \"must\":["
            + "      {\"exists\":{\"field\":\"data." + CIR_DUE_DATE + "\"}},"
            + "      {\"range\":{\"data." + CIR_DUE_DATE + "\":{\"lt\":\"" + today + "\"}}}"
            + "    ],"
            + "    \"must_not\":["
            + "      {\"match\":{\"data." + CIR_RECEIVED_BY_DEADLINE + "\":\"" + YES + "\"}},"
            + "      {\"exists\":{\"field\":\"data." + CIR_UPLOADED_DATE + "\"}}"
            + "    ]"
            + "  }"
            + "},"
            + "\"size\":" + PAGE_SIZE + ","
            + "\"from\":" + from
            + "}";
    }
}
