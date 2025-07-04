package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.CitizenUpdatePartyDataContent;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenPartyDetailsMapper;
import uk.gov.hmcts.reform.prl.mapper.citizen.awp.CitizenAwpMapper;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.caseflags.request.LanguageSupportCaseNotesRequest;
import uk.gov.hmcts.reform.prl.models.citizen.awp.CitizenAwpRequest;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.services.AddCaseNoteService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeFileUploadService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_HWF_REF_NUMBER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_WA_TASK_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.READY_FOR_DELETION_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_ADDITIONAL_APPLICATION_COLLECTION_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WA_CASE_NOTE_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WITHDRAWN_STATE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenCaseUpdateService {

    private final AllTabServiceImpl allTabService;
    private final CitizenPartyDetailsMapper citizenPartyDetailsMapper;
    private final ObjectMapper objectMapper;
    private final AddCaseNoteService addCaseNoteService;
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    private final MiamPolicyUpgradeService miamPolicyUpgradeService;
    private final MiamPolicyUpgradeFileUploadService miamPolicyUpgradeFileUploadService;
    private final SystemUserService systemUserService;
    private final NoticeOfChangePartiesService noticeOfChangePartiesService;
    private final CitizenAwpMapper citizenAwpMapper;
    private final CourtFinderService courtLocatorService;
    public static final String DA_APPLICANT_CONTACT_INSTRUCTIONS = "daApplicantContactInstructions";


    protected static final List<CaseEvent> EVENT_IDS_FOR_ALL_TAB_REFRESHED = Arrays.asList(
        CaseEvent.CONFIRM_YOUR_DETAILS,
        CaseEvent.KEEP_DETAILS_PRIVATE,
        CaseEvent.CITIZEN_CONTACT_PREFERENCE
    );

    public static final String WITHDRAW_APPLICATION_DATA = "withDrawApplicationData";
    public static final String CASE_STATUS = "caseStatus";
    public static final String LANG_SUPPORT_NEED_SUBJECT = "Support needs request";

    public CaseDetails updateCitizenPartyDetails(String authorisation,
                                                 String caseId,
                                                 String eventId,
                                                 CitizenUpdatedCaseData citizenUpdatedCaseData) {
        CaseDetails caseDetails = null;
        CaseEvent caseEvent = CaseEvent.fromValue(eventId);
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
            Map<String, Object> caseDataMapToBeUpdated = citizenUpdatePartyDataContent.get().updatedCaseDataMap();

            removeNullObjects(caseDataMapToBeUpdated, DA_APPLICANT_CONTACT_INSTRUCTIONS);
            caseDetails = allTabService.submitUpdateForSpecificUserEvent(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                caseDataMapToBeUpdated,
                startAllTabsUpdateDataContent.userDetails()
            );
            if (EVENT_IDS_FOR_ALL_TAB_REFRESHED.contains(caseEvent)) {
                return allTabService.updateAllTabsIncludingConfTab(caseId);
            }
        }

        return caseDetails;
    }

    public CaseDetails saveDraftCitizenApplication(String caseId, CaseData citizenUpdatedCaseData, String authToken)
        throws JsonProcessingException {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificUserEvent(
                caseId,
                CaseEvent.CITIZEN_SAVE_C100_DRAFT_INTERNAL.getValue(),
                authToken
            );
        Map<String, Object> caseDataMapToBeUpdated = citizenPartyDetailsMapper.getC100RebuildCaseDataMap(
            citizenUpdatedCaseData);

        return allTabService.submitUpdateForSpecificUserEvent(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataMapToBeUpdated,
            startAllTabsUpdateDataContent.userDetails()
        );
    }

    public CaseDetails submitCitizenC100Application(String authToken,
                                                    String caseId,
                                                    String eventId,
                                                    CaseData citizenUpdatedCaseData)
        throws JsonProcessingException, NotFoundException {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificUserEvent(
                caseId,
                CaseEvent.fromValue(eventId).getValue(),
                authToken
            );

        UserDetails userDetails = startAllTabsUpdateDataContent.userDetails();
        UserInfo userInfo = UserInfo
            .builder()
            .idamId(userDetails.getId())
            .firstName(userDetails.getForename())
            .lastName(userDetails.getSurname().orElse(null))
            .emailAddress(userDetails.getEmail())
            .build();
        // Find nearest court for the child post code
        Court nearestCourt = courtLocatorService.getNearestFamilyCourt(citizenUpdatedCaseData);
        CaseData dbCaseData = startAllTabsUpdateDataContent.caseData();
        dbCaseData = dbCaseData.toBuilder().userInfo(wrapElements(userInfo))
            .courtName((nearestCourt != null) ? nearestCourt.getCourtName() : "No Court Fetched")
            .taskListVersion(TASK_LIST_VERSION_V3)
            .build();

        CaseData caseDataToSubmit = citizenPartyDetailsMapper
            .buildUpdatedCaseData(dbCaseData, citizenUpdatedCaseData.getC100RebuildData());

        caseDataToSubmit = setPaymentDetails(citizenUpdatedCaseData, caseDataToSubmit);

        Map<String, Object> caseDataMapToBeUpdated = objectMapper.convertValue(caseDataToSubmit, Map.class);
        caseDataToSubmit = miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(
            caseDataToSubmit,
            caseDataMapToBeUpdated
        );

        caseDataToSubmit = miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(
            caseDataToSubmit,
            systemUserService.getSysUserToken()
        );
        allTabService.getNewMiamPolicyUpgradeDocumentMap(caseDataToSubmit, caseDataMapToBeUpdated);
        caseDataMapToBeUpdated.putAll(noticeOfChangePartiesService.generate(caseDataToSubmit, CARESPONDENT));
        caseDataMapToBeUpdated.putAll(noticeOfChangePartiesService.generate(caseDataToSubmit, CAAPPLICANT));
        OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
            "[APPLICANTSOLICITOR]").build();
        caseDataMapToBeUpdated.put("applicantOrganisationPolicy", applicantOrganisationPolicy);
        // Do not remove the next line as it will overwrite the case state change
        caseDataMapToBeUpdated.remove("state");
        Iterables.removeIf(caseDataMapToBeUpdated.values(), Objects::isNull);
        CaseDetails caseDetails = allTabService.submitUpdateForSpecificUserEvent(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataMapToBeUpdated,
            startAllTabsUpdateDataContent.userDetails()
        );

        return partyLevelCaseFlagsService.generateAndStoreCaseFlags(String.valueOf(caseDetails.getId()));
    }

    private static CaseData setPaymentDetails(CaseData citizenUpdatedCaseData, CaseData caseDataToSubmit) {
        caseDataToSubmit = caseDataToSubmit.toBuilder()
            .helpWithFeesNumber(YesOrNo.Yes.equals(caseDataToSubmit.getHelpWithFees())
                                    && isNotEmpty(citizenUpdatedCaseData.getHelpWithFeesNumber())
                                    ? citizenUpdatedCaseData.getHelpWithFeesNumber() : null)
            .build();
        return caseDataToSubmit;
    }

    public CaseDetails deleteApplication(String caseId, CaseData citizenUpdatedCaseData, String authToken)
        throws JsonProcessingException {
        Map<String, Object> caseDataMapToBeUpdated = citizenPartyDetailsMapper.getC100RebuildCaseDataMap(
            citizenUpdatedCaseData);
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

        return allTabService.updateAllTabsIncludingConfTab(caseId);
    }

    public ResponseEntity<Object> addLanguageSupportCaseNotes(
        String caseId,
        String authorisation,
        LanguageSupportCaseNotesRequest languageSupportCaseNotesRequest) {
        if (StringUtils.isEmpty(languageSupportCaseNotesRequest.getPartyIdamId())
            || StringUtils.isEmpty(languageSupportCaseNotesRequest.getLanguageSupportNotes())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("bad request");
        }

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = allTabService.getStartUpdateForSpecificUserEvent(
            caseId,
            CaseEvent.CITIZEN_LANG_SUPPORT_NOTES.getValue(),
            authorisation
        );
        CaseData dbCaseData = startAllTabsUpdateDataContent.caseData();

        CaseNoteDetails currentCaseNoteDetails = addCaseNoteService.getCurrentCaseNoteDetails(
            LANG_SUPPORT_NEED_SUBJECT,
            languageSupportCaseNotesRequest.getLanguageSupportNotes(),
            startAllTabsUpdateDataContent.userDetails()
        );
        Map<String, Object> caseNotesMap = new HashMap<>();

        List<Element<CaseNoteDetails>> caseNoteDetails = addCaseNoteService.getCaseNoteDetails(
            dbCaseData,
            currentCaseNoteDetails
        );

        caseNotesMap.put(
            CASE_NOTES,
            caseNoteDetails
        );

        caseNoteDetails.stream()
            .filter(caseNoteDetailsElement -> currentCaseNoteDetails.equals(caseNoteDetailsElement.getValue()))
            .findFirst()
            .map(Element::getId)
            .ifPresent(id ->
                           caseNotesMap.put(
                               WA_CASE_NOTE_ID,
                               id
                           ));

        allTabService.submitUpdateForSpecificUserEvent(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseNotesMap,
            startAllTabsUpdateDataContent.userDetails()
        );
        return ResponseEntity.status(HttpStatus.OK).body("Language support needs published in case notes");
    }

    public CaseDetails saveCitizenAwpApplication(String authorisation,
                                                 String caseId,
                                                 CitizenAwpRequest citizenAwpRequest) {

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = null;
        // PRL-4023, PRL-4024 WA - trigger events based on help with fees opted
        if (null != citizenAwpRequest
            && Yes.equals(citizenAwpRequest.getHaveHwfReference())
            && null != citizenAwpRequest.getHwfReferenceNumber()) {
            startAllTabsUpdateDataContent =
                allTabService.getStartUpdateForSpecificUserEvent(
                    caseId,
                    CaseEvent.CITIZEN_AWP_HWF_CREATE.getValue(),
                    authorisation
                );
        } else {
            startAllTabsUpdateDataContent =
                allTabService.getStartUpdateForSpecificUserEvent(
                    caseId,
                    CaseEvent.CITIZEN_AWP_CREATE.getValue(),
                    authorisation
                );
        }
        CaseData caseData = startAllTabsUpdateDataContent.caseData();

        // Map awp citizen data fields into solicitor fields
        CaseData updatedCaseData = citizenAwpMapper.map(caseData, citizenAwpRequest);

        Map<String, Object> caseDataMapToBeUpdated = startAllTabsUpdateDataContent.caseDataMap();
        // Update latest awp data after mapping into caseData
        caseDataMapToBeUpdated.put("additionalApplicationsBundle", updatedCaseData.getAdditionalApplicationsBundle());
        if (!CollectionUtils.isEmpty(updatedCaseData.getAdditionalApplicationsBundle())) {
            caseDataMapToBeUpdated.put(WA_ADDITIONAL_APPLICATION_COLLECTION_ID, updatedCaseData.getAdditionalApplicationsBundle()
                .getLast().getId());
        }
        caseDataMapToBeUpdated.put("citizenAwpPayments", updatedCaseData.getCitizenAwpPayments());
        caseDataMapToBeUpdated.put(
            "hwfRequestedForAdditionalApplicationsFlag",
            updatedCaseData.getHwfRequestedForAdditionalApplicationsFlag()
        );
        // WA fields
        caseDataMapToBeUpdated.put(AWP_WA_TASK_NAME, updatedCaseData.getAwpWaTaskName());
        caseDataMapToBeUpdated.put(AWP_HWF_REF_NUMBER, updatedCaseData.getAwpHwfRefNo());

        return allTabService.submitUpdateForSpecificUserEvent(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataMapToBeUpdated,
            startAllTabsUpdateDataContent.userDetails()
        );
    }

    private void removeNullObjects(Map<String, Object> caseDataMapToBeUpdated, String elementToNotBeDeleted) {
        caseDataMapToBeUpdated.entrySet()
            .removeIf(entry -> entry
                .getValue() == null && !elementToNotBeDeleted.equals(entry.getKey())
            );
    }
}
