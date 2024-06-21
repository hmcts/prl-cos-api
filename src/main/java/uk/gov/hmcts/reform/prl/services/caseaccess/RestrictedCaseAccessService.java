package uk.gov.hmcts.reform.prl.services.caseaccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.restrictedcaseaccessmanagement.CaseSecurityClassificationEnum;
import uk.gov.hmcts.reform.prl.models.ccd.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.services.extendedcasedataservice.ExtendedCaseDataService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_CASE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CHANGE_CASE_ACCESS_AS_SYSUSER;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.MARK_CASE_AS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.MARK_CASE_AS_PUBLIC;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.MARK_CASE_AS_RESTRICTED;
import static uk.gov.hmcts.reform.prl.enums.restrictedcaseaccessmanagement.CaseSecurityClassificationEnum.PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.restrictedcaseaccessmanagement.CaseSecurityClassificationEnum.RESTRICTED;


@Service
@Slf4j
@RequiredArgsConstructor
public class RestrictedCaseAccessService {

    public static final String RESTRICTED_CASE = " (Restricted case)";
    public static final String PRIVATE_CASE = " (Private case)";
    private final AllTabServiceImpl allTabService;
    private final CcdCoreCaseDataService coreCaseDataService;
    private final ExtendedCaseDataService caseDataService;
    private final ObjectMapper objectMapper;
    public static final String MARK_AS_PRIVATE_REASON = "markAsPrivateReason";
    public static final String MARK_AS_PUBLIC_REASON = "markAsPublicReason";
    public static final String MARK_AS_RESTRICTED_REASON = "markAsRestrictedReason";
    public static final String CASE_SECURITY_CLASSIFICATION = "caseSecurityClassification";
    public static final String SUMMARY_TAB = "#Summary";
    public static final String MY_WORK_URL = "/work/my-work/list";
    public static final String XUI_CASE_URL = "/cases/case-details/";
    public static final String ROLES_TAB = "/roles-and-access";
    public static final String RESTRICTED_CONFIRMATION_HEADER = "# Case marked as restricted";
    public static final String RESTRICTED_CONFIRMATION_SUBTEXT = "\n\n ## Only those with allocated roles on this case can access it";
    public static final String A_HREF = "<a href=\"";
    public static final String RESTRICTED_CONFIRMATION_BODY = "</br> You can return to " + A_HREF + MY_WORK_URL + "\">My Work</a>" + ".";
    public static final String PUBLIC_CONFIRMATION_HEADER = "# Case marked as public";
    public static final String PUBLIC_CONFIRMATION_SUBTEXT = """
        ## This case will now appear in search results
        and any previous access restrictions will be removed""";
    public static final String PRIVATE_CONFIRMATION_HEADER = "# Case marked as private";


    public Map<String, Object> initiateUpdateCaseAccess(CallbackRequest callbackRequest) {
        log.info("** restrictedCaseAccessAboutToSubmit event started");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseEvent caseEvent = CaseEvent.fromValue(callbackRequest.getEventId());
        if (MARK_CASE_AS_RESTRICTED.equals(caseEvent)) {
            caseDataUpdated.put(MARK_AS_PRIVATE_REASON, null);
            caseDataUpdated.put(MARK_AS_PUBLIC_REASON, null);
            caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, RESTRICTED.getValue());
        } else if (MARK_CASE_AS_PRIVATE.equals(caseEvent)) {
            caseDataUpdated.put(MARK_AS_RESTRICTED_REASON, null);
            caseDataUpdated.put(MARK_AS_PUBLIC_REASON, null);
            caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, PRIVATE.getValue());
        } else if (MARK_CASE_AS_PUBLIC.equals(caseEvent)) {
            caseDataUpdated.put(MARK_AS_RESTRICTED_REASON, null);
            caseDataUpdated.put(MARK_AS_PRIVATE_REASON, null);
            caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, CaseSecurityClassificationEnum.PUBLIC.getValue());
        }
        updateCaseName(caseDataUpdated, caseEvent);
        log.info("** restrictedCaseAccessAboutToSubmit abs done");
        return caseDataUpdated;
    }

    private static void updateCaseName(Map<String, Object> caseDataUpdated, CaseEvent caseEvent) {
        String applicantCaseName = caseDataUpdated.get(APPLICANT_CASE_NAME).toString();
        if (applicantCaseName.contains(PRIVATE_CASE)) {
            applicantCaseName = applicantCaseName.replace(PRIVATE_CASE, EMPTY_STRING);
        }
        if (applicantCaseName.contains(RESTRICTED_CASE)) {
            applicantCaseName = applicantCaseName.replace(RESTRICTED_CASE, EMPTY_STRING);
        }

        if (MARK_CASE_AS_RESTRICTED.equals(caseEvent)) {
            caseDataUpdated.put(APPLICANT_CASE_NAME, applicantCaseName + RESTRICTED_CASE);
        } else if (MARK_CASE_AS_PRIVATE.equals(caseEvent)) {
            caseDataUpdated.put(APPLICANT_CASE_NAME, applicantCaseName + PRIVATE_CASE);
        } else if (MARK_CASE_AS_PUBLIC.equals(caseEvent)) {
            caseDataUpdated.put(APPLICANT_CASE_NAME, applicantCaseName);
        }
    }

    public ResponseEntity<SubmittedCallbackResponse> changeCaseAccessRequestSubmitted(CallbackRequest callbackRequest) {
        log.info("** restrictedCaseAccess event started");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        log.info("** caseDataUpdated:: " + caseDataUpdated);
        CaseSecurityClassificationEnum caseSecurityClassification
            = CaseSecurityClassificationEnum.fromValue((String) caseDataUpdated.get(CASE_SECURITY_CLASSIFICATION));
        log.info("CaseSecurityClassificationEnum::" + caseSecurityClassification);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificEvent(
                String.valueOf(callbackRequest.getCaseDetails().getId()),
                CHANGE_CASE_ACCESS_AS_SYSUSER.getValue()
            );
        CaseDataContent caseDataContent = null;
        switch (caseSecurityClassification) {
            case RESTRICTED -> {
                log.info("** inside restriced:: ");
                caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
                    startAllTabsUpdateDataContent.startEventResponse(),
                    Classification.RESTRICTED
                );
            }
            case PRIVATE -> {
                log.info("** inside private:: ");
                caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
                    startAllTabsUpdateDataContent.startEventResponse(),
                    Classification.PRIVATE
                );
            }
            case PUBLIC -> {
                log.info("** inside public:: ");
                caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
                    startAllTabsUpdateDataContent.startEventResponse(),
                    Classification.PUBLIC
                );
            }
            default -> {
                log.info("** inside default:: ");
                coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
                    startAllTabsUpdateDataContent.startEventResponse(),
                    Classification.PUBLIC
                );
            }
        }
        coreCaseDataService.submitUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataContent,
            String.valueOf(callbackRequest.getCaseDetails().getId()),
            true
        );

        log.info("** restrictedCaseAccess submitUpdate done");
        return setConformationMessages(callbackRequest, caseSecurityClassification);
    }

    private static ResponseEntity<SubmittedCallbackResponse> setConformationMessages(CallbackRequest callbackRequest,
                                                                                     CaseSecurityClassificationEnum caseSecurityClassification) {
        if (RESTRICTED.equals(caseSecurityClassification)) {
            return ok(SubmittedCallbackResponse.builder().confirmationHeader(
                    RESTRICTED_CONFIRMATION_HEADER + RESTRICTED_CONFIRMATION_SUBTEXT)
                          .confirmationBody(RESTRICTED_CONFIRMATION_BODY)
                          .build());
        } else if (PRIVATE.equals(caseSecurityClassification)) {
            return ok(SubmittedCallbackResponse.builder().confirmationHeader(
                    PRIVATE_CONFIRMATION_HEADER + RESTRICTED_CONFIRMATION_SUBTEXT)
                          .confirmationBody(RESTRICTED_CONFIRMATION_BODY)
                          .build());
        } else {
            String summaryRequestUrl = XUI_CASE_URL + callbackRequest.getCaseDetails().getId() + SUMMARY_TAB;
            String roleRequestUrl = XUI_CASE_URL + callbackRequest.getCaseDetails().getId() + ROLES_TAB;
            String publicConfirmationBody = "</br>" + A_HREF + summaryRequestUrl + "\">Return to the case</a>" + " or "
                + A_HREF + roleRequestUrl + "\">see roles and access</a>" + ".";
            return ok(SubmittedCallbackResponse.builder().confirmationHeader(
                    PUBLIC_CONFIRMATION_HEADER + PUBLIC_CONFIRMATION_SUBTEXT)
                          .confirmationBody(publicConfirmationBody)
                          .build());

        }
    }

    public AboutToStartOrSubmitCallbackResponse changeCaseAccess(CallbackRequest callbackRequest) throws JsonProcessingException {
        log.info("Case details before for changeCaseAccess:: " + objectMapper.writeValueAsString(callbackRequest));
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        log.info("caseDataUpdated::" + caseDataUpdated);
        CaseSecurityClassificationEnum caseSecurityClassification
            = CaseSecurityClassificationEnum.fromValue((String) caseDataUpdated.get(CASE_SECURITY_CLASSIFICATION));
        log.info("CaseSecurityClassificationEnum::" + caseSecurityClassification);
        Map<String, Object> dataClassification
            = caseDataService.getDataClassification(String.valueOf(callbackRequest.getCaseDetails().getId()));
        log.info("dataClassification for changeCaseAccess::" + dataClassification);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .dataClassification(dataClassification)
            .securityClassification(String.valueOf(caseSecurityClassification))
            .build();
        log.info("Response after:: " + objectMapper.writeValueAsString(aboutToStartOrSubmitCallbackResponse));
        return aboutToStartOrSubmitCallbackResponse;
    }

}
