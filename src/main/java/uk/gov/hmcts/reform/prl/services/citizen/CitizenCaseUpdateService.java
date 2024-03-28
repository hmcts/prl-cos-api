package uk.gov.hmcts.reform.prl.services.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.CitizenUpdatePartyDataContent;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenPartyDetailsMapper;
import uk.gov.hmcts.reform.prl.models.UpdateCaseData;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.READY_FOR_DELETION_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WITHDRAWN_STATE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenCaseUpdateService {

    private final AllTabServiceImpl allTabService;
    private final CitizenPartyDetailsMapper citizenPartyDetailsMapper;

    protected static final List<CaseEvent> EVENT_IDS_FOR_ALL_TAB_REFRESHED = Arrays.asList(
        CaseEvent.CONFIRM_YOUR_DETAILS,
        CaseEvent.KEEP_DETAILS_PRIVATE
    );

    public static final String WITHDRAW_APPLICATION_DATA = "withDrawApplicationData";
    public static final String CASE_STATUS = "caseStatus";

    public CaseDetails updateCitizenPartyDetails(String authorisation,
                                                 String caseId,
                                                 String eventId,
                                                 UpdateCaseData citizenUpdatedCaseData) {
        CaseDetails caseDetails = null;
        CaseEvent caseEvent = CaseEvent.fromValue(eventId);
        log.info("*************** eventId received from " + caseEvent.getValue());

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = allTabService.getStartUpdateForSpecificUserEvent(caseId, eventId, authorisation);
        CaseData dbCaseData = startAllTabsUpdateDataContent.caseData();

        Optional<CitizenUpdatePartyDataContent> citizenUpdatePartyDataContent = Optional.ofNullable(
            citizenPartyDetailsMapper.mapUpdatedPartyDetails(
                dbCaseData, citizenUpdatedCaseData,
                caseEvent,
                startAllTabsUpdateDataContent.authorisation()
            ));

        if (citizenUpdatePartyDataContent.isPresent()) {
            log.info("*************** Going to update party details received from Citizen");
            caseDetails = allTabService.submitUpdateForSpecificUserEvent(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                citizenUpdatePartyDataContent.get().updatedCaseDataMap(),
                startAllTabsUpdateDataContent.userDetails()
            );

            if (EVENT_IDS_FOR_ALL_TAB_REFRESHED.contains(caseEvent)) {
                log.info("*************** Going to refresh all tabs after updating citizen party details");
                return allTabService.updateAllTabsIncludingConfTab(caseId);
            }
        }

        return caseDetails;
    }

    public CaseDetails saveDraftCitizenApplication(String caseId, CaseData citizenUpdatedCaseData, String authToken) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificUserEvent(
                caseId,
                CaseEvent.CITIZEN_SAVE_C100_DRAFT_INTERNAL.getValue(),
                authToken
            );
        Map<String, Object> caseDataMapToBeUpdated = getC100RebuildCaseDataMap(citizenUpdatedCaseData);

        return allTabService.submitUpdateForSpecificUserEvent(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataMapToBeUpdated,
            startAllTabsUpdateDataContent.userDetails()
        );
    }

    public CaseDetails submitCitizenC100Application(String caseId, CaseData citizenUpdatedCaseData, String authToken) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificUserEvent(
                caseId,
                CaseEvent.CITIZEN_SAVE_C100_DRAFT_INTERNAL.getValue(),
                authToken
            );
        Map<String, Object> caseDataMapToBeUpdated = getC100RebuildCaseDataMap(citizenUpdatedCaseData);
        //TODO: Add case state update - now shall we run all tabs update - is it needed?

        return allTabService.submitUpdateForSpecificUserEvent(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataMapToBeUpdated,
            startAllTabsUpdateDataContent.userDetails()
        );
    }

    public CaseDetails deleteApplication(String caseId, CaseData citizenUpdatedCaseData, String authToken) {
        Map<String, Object> caseDataMapToBeUpdated = getC100RebuildCaseDataMap(citizenUpdatedCaseData);
        caseDataMapToBeUpdated.put(STATE, READY_FOR_DELETION_STATE);
        caseDataMapToBeUpdated.put(
            CASE_STATUS,
            CaseStatus.builder().state(State.READY_FOR_DELETION.getLabel()).build()
        );

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificUserEvent(
                caseId,
                CaseEvent.DELETE_APPLICATION.getValue(),
                authToken
            );

        return allTabService.submitUpdateForSpecificUserEvent(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataMapToBeUpdated,
            startAllTabsUpdateDataContent.userDetails()
        );
    }

    private static Map<String, Object> getC100RebuildCaseDataMap(CaseData citizenUpdatedCaseData) {
        Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();
        if (citizenUpdatedCaseData != null) {
            caseDataMapToBeUpdated.put(
                "c100RebuildInternationalElements",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildInternationalElements()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildReasonableAdjustments",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildReasonableAdjustments()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildTypeOfOrder",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildTypeOfOrder()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildHearingWithoutNotice",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildHearingWithoutNotice()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildHearingUrgency",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildHearingUrgency()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildOtherProceedings",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildOtherProceedings()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildReturnUrl",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildReturnUrl()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildMaim",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildMaim()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildChildDetails",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildChildDetails()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildApplicantDetails",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildApplicantDetails()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildOtherChildrenDetails",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildOtherChildrenDetails()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildRespondentDetails",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildRespondentDetails()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildOtherPersonsDetails",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildOtherPersonsDetails()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildSafetyConcerns",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildSafetyConcerns()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildScreeningQuestions",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildScreeningQuestions()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildHelpWithFeesDetails",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildHelpWithFeesDetails()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildStatementOfTruth",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildStatementOfTruth()
            );
            caseDataMapToBeUpdated.put(
                "helpWithFeesReferenceNumber",
                citizenUpdatedCaseData.getC100RebuildData().getHelpWithFeesReferenceNumber()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildChildPostCode",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildChildPostCode()
            );
            caseDataMapToBeUpdated.put(
                "c100RebuildConsentOrderDetails",
                citizenUpdatedCaseData.getC100RebuildData().getC100RebuildConsentOrderDetails()
            );
        }
        return caseDataMapToBeUpdated;
    }

    public CaseDetails withdrawCase(CaseData oldCaseData, String caseId, String authToken) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificUserEvent(
                caseId,
                CaseEvent.CITIZEN_CASE_WITHDRAW.getValue(),
                authToken
            );
        Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();

        WithdrawApplication withDrawApplicationData = oldCaseData.getWithDrawApplicationData();
        Optional<YesOrNo> withdrawApplication = ofNullable(withDrawApplicationData.getWithDrawApplication());
        if ((withdrawApplication.isPresent() && Yes.equals(withdrawApplication.get()))) {
            caseDataMapToBeUpdated.put(WITHDRAW_APPLICATION_DATA, withDrawApplicationData);
            caseDataMapToBeUpdated.put(STATE, WITHDRAWN_STATE);
            caseDataMapToBeUpdated.put(
                CASE_STATUS,
                CaseStatus.builder().state(State.CASE_WITHDRAWN.getLabel()).build()
            );
        }

        allTabService.submitUpdateForSpecificUserEvent(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataMapToBeUpdated,
            startAllTabsUpdateDataContent.userDetails()
        );

        //TODO: Do we need all tabs service update
        return allTabService.updateAllTabsIncludingConfTab(caseId);
    }
}
