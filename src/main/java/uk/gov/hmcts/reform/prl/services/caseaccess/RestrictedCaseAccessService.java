package uk.gov.hmcts.reform.prl.services.caseaccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.restrictedcaseaccessmanagement.CaseSecurityClassificationEnum;
import uk.gov.hmcts.reform.prl.models.caseaccess.AssignCaseAccessRequest;
import uk.gov.hmcts.reform.prl.models.ccd.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.extendedcasedataservice.ExtendedCaseDataService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_CASE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CHANGE_CASE_ACCESS_AS_SYSUSER;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.MARK_CASE_AS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.MARK_CASE_AS_PUBLIC;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.MARK_CASE_AS_RESTRICTED;


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

    public Map<String, Object> initiateUpdateCaseAccess(CallbackRequest callbackRequest) {
        log.info("** restrictedCaseAccessAboutToSubmit event started");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseEvent caseEvent = CaseEvent.fromValue(callbackRequest.getEventId());
        if (MARK_CASE_AS_RESTRICTED.equals(caseEvent)) {
            caseDataUpdated.put(MARK_AS_PRIVATE_REASON, null);
            caseDataUpdated.put(MARK_AS_PUBLIC_REASON, null);
            caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, CaseSecurityClassificationEnum.RESTRICTED.getValue());
            caseDataUpdated.put(APPLICANT_CASE_NAME, caseDataUpdated.get(APPLICANT_CASE_NAME) + RESTRICTED_CASE);
        } else if (MARK_CASE_AS_PRIVATE.equals(caseEvent)) {
            caseDataUpdated.put(MARK_AS_RESTRICTED_REASON, null);
            caseDataUpdated.put(MARK_AS_PUBLIC_REASON, null);
            caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, CaseSecurityClassificationEnum.PRIVATE.getValue());
            caseDataUpdated.put(APPLICANT_CASE_NAME, caseDataUpdated.get(APPLICANT_CASE_NAME) + PRIVATE_CASE);
        } else if (MARK_CASE_AS_PUBLIC.equals(caseEvent)) {
            caseDataUpdated.put(MARK_AS_RESTRICTED_REASON, null);
            caseDataUpdated.put(MARK_AS_PRIVATE_REASON, null);
            caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, CaseSecurityClassificationEnum.PUBLIC.getValue());

            String applicantCaseName = caseDataUpdated.get(APPLICANT_CASE_NAME).toString();
            if (applicantCaseName.contains(PRIVATE_CASE)) {
                caseDataUpdated.put(APPLICANT_CASE_NAME, applicantCaseName.replace(PRIVATE_CASE, EMPTY_STRING));
            } else if (applicantCaseName.contains(RESTRICTED_CASE)) {
                caseDataUpdated.put(APPLICANT_CASE_NAME, applicantCaseName.replace(RESTRICTED_CASE, EMPTY_STRING));
            } else {
                caseDataUpdated.put(APPLICANT_CASE_NAME, applicantCaseName);
            }

        }
        log.info("** restrictedCaseAccessAboutToSubmit abs done");
        return caseDataUpdated;
    }

    public void changeCaseAccessRequestSubmitted(CallbackRequest callbackRequest) {
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
