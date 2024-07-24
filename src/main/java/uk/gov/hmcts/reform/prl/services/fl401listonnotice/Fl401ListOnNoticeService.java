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
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.AddCaseNoteService;
import uk.gov.hmcts.reform.prl.services.BulkPrintService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.ListOnNoticeService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.config.templates.Templates.PRL_LET_ENG_LIST_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
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
    public static final String LETTER_TYPE = "ListWithoutNoticeLetter";

    private final ObjectMapper objectMapper;

    private final AddCaseNoteService addCaseNoteService;

    private final UserService userService;

    private final EmailService emailService;

    private final ListOnNoticeService listOnNoticeService;

    private final DgsService dgsService;

    private final BulkPrintService bulkPrintService;

    private final AllTabServiceImpl allTabService;

    public static final String CONFIRMATION_BODY_WITHOUT_NOTICE = """
        ### What happens next

        Admin will be notified to list the hearing on notice.

        The reasons you have given to list the hearing on notice will be shared with the applicant.

        Your request details will be saved in case notes.""";

    public static final String CONFIRMATION_BODY_WITH_NOTICE = """
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
        PartyDetails applicantsInCase = caseData.getApplicantsFL401();
        String fl401ReasonsForListWithoutNoticeRequested = (String) caseDataUpdated.get(
            FL401_REASONS_FOR_LIST_WITHOUT_NOTICE_REQUESTED);
        String confirmationBody;
        if (!StringUtils.isEmpty(fl401ReasonsForListWithoutNoticeRequested)) {
            UUID bulkPrintId = null;
            StringBuilder finalReasonsForListWithoutNoticeRequested
                = new StringBuilder(fl401ReasonsForListWithoutNoticeRequested.replace("\n\n", "\n• "))
                .insert(0, "• ");
            if (StringUtils.isNotEmpty(applicantsInCase.getSolicitorEmail())) {
                log.info(
                    "Sending the email notification to applicant solicitor for List on Notice for caseId {}",
                    caseData.getId()
                );
                sendEmail(
                    caseData,
                    applicantsInCase.getSolicitorEmail(),
                    finalReasonsForListWithoutNoticeRequested,
                    applicantsInCase.getRepresentativeFirstName(),
                    applicantsInCase.getRepresentativeLastName()
                );
            } else {
                if (isNotEmpty(applicantsInCase.getUser())
                    && isNotEmpty(applicantsInCase.getPartyId())
                    && applicantsInCase.getPartyId().toString().equals(applicantsInCase.getUser().getIdamId())) {
                    log.info(
                        "Sending the email notification to applicant for List on Notice for caseId {}",
                        caseData.getId()
                    );
                    sendEmail(
                        caseData,
                        applicantsInCase.getEmail(),
                        finalReasonsForListWithoutNoticeRequested,
                        applicantsInCase.getFirstName(),
                        applicantsInCase.getLastName()
                    );
                } else {
                    log.info(
                        "Sending document via post to applicant for List on Notice for caseId {}",
                        caseData.getId()
                    );
                    bulkPrintId = sendViaPost(
                        authorisation,
                        caseData,
                        applicantsInCase,
                        bulkPrintId,
                        finalReasonsForListWithoutNoticeRequested
                    );
                }
                if (isNotEmpty(bulkPrintId)) {
                    fl401ReasonsForListWithoutNoticeRequested = fl401ReasonsForListWithoutNoticeRequested
                        + ", Bulk Print Id: " + bulkPrintId;
                }
            }
            CaseNoteDetails currentCaseNoteDetails = addCaseNoteService.getCurrentCaseNoteDetails(
                REJECT_WITHOUT_NOTICE_REASONS,
                fl401ReasonsForListWithoutNoticeRequested,
                userService.getUserDetails(authorisation)
            );
            caseDataUpdated.put(
                CASE_NOTES,
                addCaseNoteService.getCaseNoteDetails(caseData, currentCaseNoteDetails)
            );
            confirmationBody = CONFIRMATION_BODY_WITHOUT_NOTICE;
        } else {
            confirmationBody = CONFIRMATION_BODY_WITH_NOTICE;
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
                           confirmationBody).build());
    }

    private UUID sendViaPost(String authorisation,
                             CaseData caseData,
                             PartyDetails applicantsInCase,
                             UUID bulkPrintId,
                             StringBuilder finalReasonsForListWithoutNoticeRequested) {
        Map<String, Object> dataMap = populateDataMap(
            caseData,
            applicantsInCase,
            finalReasonsForListWithoutNoticeRequested
        );
        try {
            log.info(
                "generating letter : {} for case : {}",
                PRL_LET_ENG_LIST_WITHOUT_NOTICE,
                dataMap.get("id")
            );
            GeneratedDocumentInfo listOnNoticeLetter = dgsService.generateDocument(
                authorisation,
                String.valueOf(dataMap.get("id")),
                PRL_LET_ENG_LIST_WITHOUT_NOTICE,
                dataMap
            );
            Document listOnNoticeLetterDocs = Document.builder().documentUrl(listOnNoticeLetter.getUrl())
                .documentFileName(listOnNoticeLetter.getDocName()).documentBinaryUrl(listOnNoticeLetter.getBinaryUrl())
                .documentCreatedOn(new Date())
                .build();

            bulkPrintId = bulkPrintService.send(
                String.valueOf(caseData.getId()),
                authorisation,
                LETTER_TYPE,
                List.of(listOnNoticeLetterDocs),
                applicantsInCase.getLabelForDynamicList()
            );
            log.info("ID in the queue from bulk print service : {}", bulkPrintId);
        } catch (Exception e) {
            log.error("*** List without notice letter failed for {} :: because of {}", PRL_LET_ENG_LIST_WITHOUT_NOTICE, e.getMessage());
        }
        return bulkPrintId;
    }

    private static Map<String, Object> populateDataMap(CaseData caseData,
                                                       PartyDetails applicantsInCase,
                                                       StringBuilder finalReasonsForListWithoutNoticeRequested) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", caseData.getId());
        dataMap.put("address", applicantsInCase.getAddress());
        dataMap.put("recipientName", applicantsInCase.getFirstName() + " " + applicantsInCase.getLastName());
        dataMap.put("reasonsForListWithoutNoticeRequested", finalReasonsForListWithoutNoticeRequested);
        dataMap.put("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        return dataMap;
    }

    private void sendEmail(CaseData caseData,
                           String emailId,
                           StringBuilder finalSelectedAndAdditionalReasons,
                           String firstName,
                           String lastName) {
        emailService.send(
            emailId,
            EmailTemplateNames.LIST_ON_NOTICE_EMAIL_NOTIFICATION,
            listOnNoticeService.buildListOnNoticeEmail(
                caseData,
                firstName
                    + EMPTY_SPACE_STRING
                    + lastName,
                finalSelectedAndAdditionalReasons.toString()
            ),
            LanguagePreference.getPreferenceLanguage(caseData)
        );
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
