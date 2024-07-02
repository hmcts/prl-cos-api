package uk.gov.hmcts.reform.prl.services.tab.alltabs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ID_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuDomesticAbuse;
import static uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum.mpuPreviousMiamAttendance;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Qualifier("allTabsService")
public class AllTabServiceImpl implements AllTabsService {

    private final ApplicationsTabService applicationsTabService;
    @Qualifier("caseSummaryTab")
    private final CaseSummaryTabService caseSummaryTabService;
    private final ConfidentialityTabService confidentialityTabService;
    private final ObjectMapper objectMapper;
    private final CcdCoreCaseDataService ccdCoreCaseDataService;
    private final SystemUserService systemUserService;
    private final IdamClient idamClient;

    /**
     * This method updates all tabs based on latest case data from DB.
     * If additional params needs to be stored, then use getStartAllTabsUpdate
     * followed by mapAndSubmitAllTabsUpdate.
     * @param caseId it will be used to start the transaction
     * @return CaseDetails will be returned
     **/
    @Override
    public CaseDetails updateAllTabsIncludingConfTab(String caseId) {
        if (StringUtils.isNotEmpty(caseId)) {
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = getStartAllTabsUpdate(caseId);
            log.info("all tab update triggered");
            return mapAndSubmitAllTabsUpdate(
                    startAllTabsUpdateDataContent.authorisation(),
                    caseId,
                    startAllTabsUpdateDataContent.startEventResponse(),
                    startAllTabsUpdateDataContent.eventRequestData(),
                    startAllTabsUpdateDataContent.caseData()
            );
        } else {
            log.error("All tabs update failed as no case found");
            return null;
        }
    }

    @Override
    public StartAllTabsUpdateDataContent getStartAllTabsUpdate(String caseId) {
        String systemAuthorisation = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(systemAuthorisation);
        EventRequestData allTabsUpdateEventRequestData = ccdCoreCaseDataService.eventRequest(
            CaseEvent.UPDATE_ALL_TABS,
            systemUpdateUserId
        );
        StartEventResponse allTabsUpdateStartEventResponse =
            ccdCoreCaseDataService.startUpdate(
                systemAuthorisation,
                allTabsUpdateEventRequestData,
                caseId,
                true
            );
        CaseData allTabsUpdateCaseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(
            allTabsUpdateStartEventResponse,
            objectMapper
        );
        return new StartAllTabsUpdateDataContent(
            systemAuthorisation,
            allTabsUpdateEventRequestData,
            allTabsUpdateStartEventResponse,
            allTabsUpdateStartEventResponse.getCaseDetails().getData(),
            allTabsUpdateCaseData,
            null
        );
    }

    @Override
    public StartAllTabsUpdateDataContent getStartUpdateForSpecificEvent(String caseId, String eventId) {
        String systemAuthorisation = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(systemAuthorisation);
        EventRequestData allTabsUpdateEventRequestData = ccdCoreCaseDataService.eventRequest(
            CaseEvent.fromValue(eventId),
            systemUpdateUserId
        );
        StartEventResponse allTabsUpdateStartEventResponse =
            ccdCoreCaseDataService.startUpdate(
                systemAuthorisation,
                allTabsUpdateEventRequestData,
                caseId,
                true
            );
        CaseData allTabsUpdateCaseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(
            allTabsUpdateStartEventResponse,
            objectMapper
        );
        return new StartAllTabsUpdateDataContent(
            systemAuthorisation,
            allTabsUpdateEventRequestData,
            allTabsUpdateStartEventResponse,
            allTabsUpdateStartEventResponse.getCaseDetails().getData(),
            allTabsUpdateCaseData,
            null
        );
    }

    public CaseDetails mapAndSubmitAllTabsUpdate(String systemAuthorisation,
                                                 String caseId,
                                                 StartEventResponse startEventResponse,
                                                 EventRequestData eventRequestData,
                                                 CaseData caseData) {
        Map<String, Object> combinedFieldsMap = findCaseDataMap(caseData);

        return submitAllTabsUpdate(systemAuthorisation, caseId, startEventResponse, eventRequestData, combinedFieldsMap);
    }

    public CaseDetails submitAllTabsUpdate(String systemAuthorisation,
                                           String caseId,
                                           StartEventResponse startEventResponse,
                                           EventRequestData eventRequestData,
                                           Map<String, Object> combinedFieldsMap) {
        return ccdCoreCaseDataService.submitUpdate(
                systemAuthorisation,
                eventRequestData,
                ccdCoreCaseDataService.createCaseDataContent(
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
        documentMap.put("submitAndPayDownloadApplicationLink", caseData.getSubmitAndPayDownloadApplicationLink());
        documentMap.put("submitAndPayDownloadApplicationWelshLink", caseData.getSubmitAndPayDownloadApplicationWelshLink());

        return documentMap;
    }

    public Map<String, Object> getNewMiamPolicyUpgradeDocumentMap(CaseData caseData, Map<String, Object> documentMap) {
        if (TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())
            && isNotEmpty(caseData.getMiamPolicyUpgradeDetails())
            && Yes.equals(caseData.getMiamPolicyUpgradeDetails().getMpuClaimingExemptionMiam())
            && CollectionUtils.isNotEmpty(caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons())
            && (caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(mpuDomesticAbuse)
            || caseData.getMiamPolicyUpgradeDetails().getMpuExemptionReasons().contains(mpuPreviousMiamAttendance))) {
            documentMap.put(
                "mpuDomesticAbuseEvidenceDocument",
                caseData.getMiamPolicyUpgradeDetails().getMpuDomesticAbuseEvidenceDocument()
            );
            documentMap.put(
                "mpuDocFromDisputeResolutionProvider",
                caseData.getMiamPolicyUpgradeDetails().getMpuDocFromDisputeResolutionProvider()
            );
            documentMap.put(
                "mpuCertificateByMediator",
                caseData.getMiamPolicyUpgradeDetails().getMpuCertificateByMediator()
            );
        }
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

    public void updatePartyDetailsForNoc(String authorisation,
                                         String caseId,
                                         StartEventResponse startEventResponse,
                                         EventRequestData eventRequestData,
                                         CaseData caseData) {
        Map<String, Object> dataMap = new HashMap<>();
        Map<String, Object> combinedFieldsMap = new HashMap<>();
        if (caseData != null) {
            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                dataMap.put(C100_RESPONDENTS, caseData.getRespondents());
                dataMap.put(C100_APPLICANTS, caseData.getApplicants());
            } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                dataMap.put(FL401_APPLICANTS, caseData.getApplicantsFL401());
                dataMap.put(FL401_RESPONDENTS, caseData.getRespondentsFL401());
            }
            //SHASHI NOT NEEDED ??? not sure ont on this if this ever happen without casedata having caseinvites
            /*setCaseInvitesIfNeeded(caseInvites, dataMap);*/
            combinedFieldsMap = findCaseDataMap(caseData);
            combinedFieldsMap.putAll(dataMap);
        }

        ccdCoreCaseDataService.submitUpdate(
            authorisation,
            eventRequestData,
            ccdCoreCaseDataService.createCaseDataContent(
                startEventResponse,
                combinedFieldsMap
            ),
            caseId,
            true
        );
    }

    private Map<String, Object> findCaseDataMap(CaseData caseData) {
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

        getNewMiamPolicyUpgradeDocumentMap(caseData, combinedFieldsMap);
        combinedFieldsMap.putAll(applicationsTabService.toMap(caseData.getAllPartyFlags()));
        return combinedFieldsMap;
    }

    //SHASHI NOT NEEDED ??? not sure ont on this if this ever happen without casedata having caseinvites
    /*private static void setCaseInvitesIfNeeded(List<Element<CaseInvite>> caseInvites, Map<String, Object> caseDataUpdatedMap) {
        if (CollectionUtils.isNotEmpty(caseInvites)) {
            caseDataUpdatedMap.put("caseInvites", caseInvites);
        }
    }*/

    @Override
    public StartAllTabsUpdateDataContent getStartUpdateForSpecificUserEvent(String caseId,
                                                                            String eventId,
                                                                            String authorisation) {
        log.info("event Id we got is:: {}", eventId);
        log.info("event is now:: {}", CaseEvent.fromValue(eventId));
        UserDetails userDetails = idamClient.getUserDetails(authorisation);
        EventRequestData allTabsUpdateEventRequestData = ccdCoreCaseDataService.eventRequest(
            CaseEvent.fromValue(eventId),
            userDetails.getId()
        );
        StartEventResponse allTabsUpdateStartEventResponse =
            ccdCoreCaseDataService.startUpdate(
                authorisation,
                allTabsUpdateEventRequestData,
                caseId,
                !userDetails.getRoles().contains(CITIZEN_ROLE)
            );
        CaseData allTabsUpdateCaseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(
            allTabsUpdateStartEventResponse,
            objectMapper
        );
        return new StartAllTabsUpdateDataContent(
            authorisation,
            allTabsUpdateEventRequestData,
            allTabsUpdateStartEventResponse,
            allTabsUpdateStartEventResponse.getCaseDetails().getData(),
            allTabsUpdateCaseData,
            userDetails
        );
    }

    @Override
    public CaseDetails submitUpdateForSpecificUserEvent(String authorisation,
                                                        String caseId,
                                                        StartEventResponse startEventResponse,
                                                        EventRequestData eventRequestData,
                                                        Map<String, Object> combinedFieldsMap,
                                                        UserDetails userDetails) {
        return ccdCoreCaseDataService.submitUpdate(
            authorisation,
            eventRequestData,
            ccdCoreCaseDataService.createCaseDataContent(
                startEventResponse,
                combinedFieldsMap
            ),
            caseId,
            !userDetails.getRoles().contains(CITIZEN_ROLE)
        );
    }

}
