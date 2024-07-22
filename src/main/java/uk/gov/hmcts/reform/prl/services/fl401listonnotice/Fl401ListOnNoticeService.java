package uk.gov.hmcts.reform.prl.services.fl401listonnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AddCaseNoteService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_LIST_ON_NOTICE_HEARING_INSTRUCTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_REASONS_FOR_LIST_WITHOUT_NOTICE_REQUESTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LIST_ON_NOTICE_HEARING_INSTRUCTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REJECT_WITHOUT_NOTICE_REASONS;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Fl401ListOnNoticeService {

    private final ObjectMapper objectMapper;

    private final AddCaseNoteService addCaseNoteService;

    private final UserService userService;

    private final AllTabServiceImpl allTabService;

    public static final String CONFIRMATION_BODY = """
        ### What happens next

        Admin will be notified to list the hearing on notice.

        Your request details will be saved in case notes.""";

    public static final String CONFIRMATION_HEADER = "# Listing instructions sent to admin";

    public Map<String, Object> prePopulateHearingPageDataForFl401ListOnNotice(CaseData caseData) {

        Map<String, Object> caseDataUpdated = new HashMap<>();
        String isCaseWithOutNotice = String.valueOf(isNotEmpty(caseData.getOrderWithoutGivingNoticeToRespondent())
                                                    && Yes.equals(caseData.getOrderWithoutGivingNoticeToRespondent()
                                                                   .getOrderWithoutGivingNotice())
                                                        ? Yes : No);
        caseDataUpdated.put(FL401_CASE_WITHOUT_NOTICE, isCaseWithOutNotice);
        return caseDataUpdated;
    }

    public Map<String, Object> fl401ListOnNoticeSubmission(CaseDetails caseDetails, String authorisation) {
        CaseData caseData = objectMapper.convertValue(
            caseDetails.getData(),
            CaseData.class
        );
        Map<String, Object> caseDataUpdated = caseDetails.getData();

        String fl401listOnNoticeHearingInstruction = (String) caseDataUpdated.get(
            FL401_LIST_ON_NOTICE_HEARING_INSTRUCTION);
        if (!StringUtils.isEmpty(fl401listOnNoticeHearingInstruction)) {
            CaseNoteDetails currentCaseNoteDetails = addCaseNoteService.getCurrentCaseNoteDetails(
                LIST_ON_NOTICE_HEARING_INSTRUCTIONS,
                fl401listOnNoticeHearingInstruction,
                userService.getUserDetails(authorisation)
            );
            caseDataUpdated.put(
                CASE_NOTES,
                addCaseNoteService.getCaseNoteDetails(caseData, currentCaseNoteDetails)
            );
        }
        return caseDataUpdated;
    }

    public ResponseEntity<SubmittedCallbackResponse> sendNotification(Map<String, Object> caseDataUpdated, String authorisation) {
        CaseData caseData = objectMapper.convertValue(
            caseDataUpdated,
            CaseData.class
        );
        String fl401ReasonsForListWithoutNoticeRequested = (String) caseDataUpdated.get(
            FL401_REASONS_FOR_LIST_WITHOUT_NOTICE_REQUESTED);
        if (!StringUtils.isEmpty(fl401ReasonsForListWithoutNoticeRequested)) {
            CaseNoteDetails currentCaseNoteDetails = addCaseNoteService.getCurrentCaseNoteDetails(
                REJECT_WITHOUT_NOTICE_REASONS,
                fl401ReasonsForListWithoutNoticeRequested,
                userService.getUserDetails(authorisation)
            );
            caseDataUpdated.put(
                CASE_NOTES,
                addCaseNoteService.getCaseNoteDetails(caseData, currentCaseNoteDetails)
            );
        }

        cleanUpListOnNoticeFields(caseDataUpdated);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = allTabService.getStartAllTabsUpdate(String.valueOf(caseData.getId()));

        //update all tabs
        allTabService.submitAllTabsUpdate(startAllTabsUpdateDataContent.authorisation(),
                                          String.valueOf(caseData.getId()),
                                          startAllTabsUpdateDataContent.startEventResponse(),
                                          startAllTabsUpdateDataContent.eventRequestData(),
                                          caseDataUpdated);

        return ok(SubmittedCallbackResponse.builder()
                       .confirmationHeader(CONFIRMATION_HEADER)
                       .confirmationBody(
                               CONFIRMATION_BODY).build());
    }

    public void cleanUpListOnNoticeFields(Map<String, Object> caseDataUpdated) {
        String[] listOnNoticeFields = {FL401_LIST_ON_NOTICE_HEARING_INSTRUCTION, FL401_REASONS_FOR_LIST_WITHOUT_NOTICE_REQUESTED};
        for (String field : listOnNoticeFields) {
            if (caseDataUpdated.containsKey(field)) {
                caseDataUpdated.put(field, null);
            }
        }
    }
}
