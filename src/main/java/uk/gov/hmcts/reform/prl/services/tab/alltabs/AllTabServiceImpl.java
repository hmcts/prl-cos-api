package uk.gov.hmcts.reform.prl.services.tab.alltabs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_APPLICANT_TABLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENT_TABLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ID_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANT_TABLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENT_TABLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Qualifier("allTabsService")
public class AllTabServiceImpl implements AllTabsService {
    @Autowired
    ApplicationsTabService applicationsTabService;

    @Autowired
    CoreCaseDataService coreCaseDataService;

    @Autowired
    @Qualifier("caseSummaryTab")
    CaseSummaryTabService caseSummaryTabService;

    @Autowired
    ConfidentialityTabService confidentialityTabService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CcdCoreCaseDataService coreCaseDataServiceCcdClient;

    @Override
    public void updateAllTabs(CaseData caseData) {
        Map<String, Object> combinedFieldsMap = getCombinedMap(caseData);
        if (caseData.getDateSubmitted() != null) {
            combinedFieldsMap.put(DATE_SUBMITTED_FIELD, caseData.getDateSubmitted());
        }
        if (caseData.getCourtName() != null) {
            combinedFieldsMap.put(COURT_NAME_FIELD, caseData.getCourtName());
        }
        if (caseData.getCourtId() != null) {
            combinedFieldsMap.put(COURT_ID_FIELD, caseData.getCourtId());
        }
        // Calling event to refresh the page.
        refreshCcdUsingEvent(caseData, combinedFieldsMap);
    }

    private void refreshCcdUsingEvent(CaseData caseData, Map<String, Object> combinedFieldsMap) {
        log.info("*** 1 Respondents *** {}", caseData.getRespondents());
        log.info("*** 2 Respondents *** {}", combinedFieldsMap.get("respondents"));
        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-all-tabs",
            combinedFieldsMap
        );
    }

    public void updateAllTabsIncludingConfTab(CaseData caseData) {
        Map<String, Object> confidentialDetails = confidentialityTabService.updateConfidentialityDetails(caseData);
        Map<String, Object> combinedFieldsMap = getCombinedMap(caseData);
        combinedFieldsMap.putAll(confidentialDetails);
        combinedFieldsMap.put("respondentConfidentialDetails", caseData.getRespondentConfidentialDetails());

        if (caseData.getDateSubmitted() != null) {
            combinedFieldsMap.put(DATE_SUBMITTED_FIELD, caseData.getDateSubmitted());
        }
        if (caseData.getCourtName() != null) {
            combinedFieldsMap.put(COURT_NAME_FIELD, caseData.getCourtName());
        }
        if (caseData.getCourtId() != null) {
            combinedFieldsMap.put(COURT_ID_FIELD, caseData.getCourtId());
        }
        getDocumentsMap(caseData, combinedFieldsMap);
        // Calling event to refresh the page.
        refreshCcdUsingEvent(caseData, combinedFieldsMap);
    }

    public void updateAllTabsIncludingConfTabRefactored(String authorisation,
                                                        String caseId,
                                                        StartEventResponse startEventResponse,
                                                        EventRequestData allTabsUpdateEventRequestData,
                                                        CaseData caseData) {
        Map<String, Object> confidentialDetails = confidentialityTabService.updateConfidentialityDetails(caseData);
        Map<String, Object> combinedFieldsMap = getCombinedMap(caseData);
        combinedFieldsMap.putAll(confidentialDetails);

        if (caseData.getDateSubmitted() != null) {
            combinedFieldsMap.put(DATE_SUBMITTED_FIELD, caseData.getDateSubmitted());
        }
        if (caseData.getCourtName() != null) {
            combinedFieldsMap.put(COURT_NAME_FIELD, caseData.getCourtName());
        }
        if (caseData.getCourtId() != null) {
            combinedFieldsMap.put(COURT_ID_FIELD, caseData.getCourtId());
        }
        getDocumentsMap(caseData, combinedFieldsMap);

        coreCaseDataServiceCcdClient.submitUpdate(
            authorisation,
            allTabsUpdateEventRequestData,
            coreCaseDataServiceCcdClient.createCaseDataContent(
                startEventResponse,
                combinedFieldsMap
            ),
            caseId,
            true
        );
    }

    private Map<String, Object> getDocumentsMap(CaseData caseData, Map<String, Object> documentMap) {

        documentMap.put("c1ADocument", caseData.getC1ADocument());
        documentMap.put("c1AWelshDocument", caseData.getC1AWelshDocument());
        documentMap.put("finalDocument", caseData.getFinalDocument());
        documentMap.put("finalWelshDocument", caseData.getFinalWelshDocument());
        documentMap.put("c8Document", caseData.getC8Document());
        documentMap.put("c8WelshDocument", caseData.getC8WelshDocument());
        documentMap.put("draftOrderDoc", caseData.getDraftOrderDoc());
        documentMap.put("draftOrderDocWelsh", caseData.getDraftOrderDocWelsh());

        return documentMap;
    }

    private Map<String, Object> getCombinedMap(CaseData caseData) {
        Map<String, Object> applicationTabFields = applicationsTabService.updateTab(
            caseData);

        Map<String, Object> summaryTabFields = caseSummaryTabService.updateTab(caseData);

        return Stream.concat(
            applicationTabFields.entrySet().stream(),
            summaryTabFields.entrySet().stream()
        ).collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);

    }

    @Override
    public Map<String, Object> getAllTabsFields(CaseData caseData) {
        return getCombinedMap(caseData);
    }

    public void updatePartyDetailsForNoc(CaseData caseData, Optional<SolicitorRole> solicitorRole) {
        Map<String, Object> caseDataUpdatedMap = new HashMap<>();
        if (caseData != null && solicitorRole.isPresent()) {
            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                if (CARESPONDENT.equals(solicitorRole.get().getRepresenting())) {
                    caseDataUpdatedMap.put(C100_RESPONDENTS, caseData.getRespondents());
                    caseDataUpdatedMap.put(C100_RESPONDENT_TABLE, applicationsTabService.getRespondentsTable(caseData));
                } else if (CAAPPLICANT.equals(solicitorRole.get().getRepresenting())) {
                    caseDataUpdatedMap.put(C100_APPLICANTS, caseData.getApplicants());
                    caseDataUpdatedMap.put(C100_APPLICANT_TABLE, applicationsTabService.getApplicantsTable(caseData));
                }
            } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                if (DAAPPLICANT.equals(solicitorRole.get().getRepresenting())) {
                    caseDataUpdatedMap.put(FL401_APPLICANTS, caseData.getApplicantsFL401());
                    caseDataUpdatedMap.put(
                        FL401_APPLICANT_TABLE,
                        applicationsTabService.getFl401ApplicantsTable(caseData)
                    );
                } else if (DARESPONDENT.equals(solicitorRole.get().getRepresenting())) {
                    caseDataUpdatedMap.put(FL401_RESPONDENTS, caseData.getApplicantsFL401());
                    caseDataUpdatedMap.put(
                        FL401_RESPONDENT_TABLE,
                        applicationsTabService.getFl401RespondentTable(caseData)
                    );
                }
            }

            refreshCcdUsingEvent(caseData, caseDataUpdatedMap);
        }
    }

}
