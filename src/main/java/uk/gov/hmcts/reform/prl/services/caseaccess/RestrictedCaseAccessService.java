package uk.gov.hmcts.reform.prl.services.caseaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.restrictedcaseaccessmanagement.CaseSecurityClassificationEnum;
import uk.gov.hmcts.reform.prl.models.ccd.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.QueryAttributes;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentQueryRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.extendedcasedataservice.ExtendedCaseDataService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_CASE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_OR_RESPONDENT_CASE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;
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
    public static final String REASONS_TO_PRIVATE_TAB = "reasonsToPrivateTab";
    public static final String REASONS_TO_RESTRICT_TAB = "reasonsToRestrictTab";
    public static final String TD_START_CELL = "<td class=\"govuk-table__cell\">";
    public static final String TD_CLOSURE = "</td>";
    public static final String SPECIFIC_ACCESS_GRANT = "SPECIFIC";
    private final AllTabServiceImpl allTabService;
    private final CcdCoreCaseDataService coreCaseDataService;
    private final ExtendedCaseDataService caseDataService;
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

    private final RoleAssignmentApi roleAssignmentApi;

    private final SystemUserService systemUserService;

    private final AuthTokenGenerator authTokenGenerator;

    private final IdamApi idamApi;

    public static final List<String> ROLE_CATEGORIES = List.of("JUDICIAL",
                                                     "LEGAL_OPERATIONS",
                                                     "CTSC",
                                                     "ADMIN");

    public Map<String, Object> initiateUpdateCaseAccess(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseEvent caseEvent = CaseEvent.fromValue(callbackRequest.getEventId());
        log.info("event id {}",caseEvent);
        if (MARK_CASE_AS_RESTRICTED.equals(caseEvent)) {
            log.info("updating case fields ");
            caseDataUpdated.put(MARK_AS_PRIVATE_REASON, null);
            caseDataUpdated.put(MARK_AS_PUBLIC_REASON, null);
            caseDataUpdated.put(REASONS_TO_PRIVATE_TAB, null);
            caseDataUpdated.put(REASONS_TO_RESTRICT_TAB, caseDataUpdated.get(MARK_AS_RESTRICTED_REASON));
            caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, RESTRICTED.getValue());
        } else if (MARK_CASE_AS_PRIVATE.equals(caseEvent)) {
            caseDataUpdated.put(MARK_AS_RESTRICTED_REASON, null);
            caseDataUpdated.put(MARK_AS_PUBLIC_REASON, null);
            caseDataUpdated.put(REASONS_TO_RESTRICT_TAB, null);
            caseDataUpdated.put(REASONS_TO_PRIVATE_TAB, caseDataUpdated.get(MARK_AS_PRIVATE_REASON));
            caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, PRIVATE.getValue());
        } else if (MARK_CASE_AS_PUBLIC.equals(caseEvent)) {
            caseDataUpdated.put(MARK_AS_RESTRICTED_REASON, null);
            caseDataUpdated.put(MARK_AS_PRIVATE_REASON, null);
            caseDataUpdated.put(REASONS_TO_RESTRICT_TAB, null);
            caseDataUpdated.put(REASONS_TO_PRIVATE_TAB, null);
            caseDataUpdated.put(CASE_SECURITY_CLASSIFICATION, CaseSecurityClassificationEnum.PUBLIC.getValue());
        }
        updateCaseName(caseDataUpdated, caseEvent);
        log.info("updated staus {}",caseDataUpdated.get(CASE_SECURITY_CLASSIFICATION));
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
            applicantCaseName = applicantCaseName + RESTRICTED_CASE;
        } else if (MARK_CASE_AS_PRIVATE.equals(caseEvent)) {
            applicantCaseName = applicantCaseName + PRIVATE_CASE;
        }
        final String caseName = applicantCaseName;
        caseDataUpdated.put(APPLICANT_CASE_NAME, caseName);
        caseDataUpdated.put("caseNameHmctsInternal", caseName);
        caseDataUpdated.computeIfPresent(
            APPLICANT_OR_RESPONDENT_CASE_NAME,
            (k, v) -> caseDataUpdated.put(APPLICANT_OR_RESPONDENT_CASE_NAME, caseName)
        );
    }

    public ResponseEntity<SubmittedCallbackResponse> changeCaseAccessRequestSubmitted(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        log.info("security classification in submitted event {}",caseDataUpdated.get(CASE_SECURITY_CLASSIFICATION));
        CaseSecurityClassificationEnum caseSecurityClassification
            = CaseSecurityClassificationEnum.fromValue((String) caseDataUpdated.get(CASE_SECURITY_CLASSIFICATION));
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificEvent(
                String.valueOf(callbackRequest.getCaseDetails().getId()),
                CHANGE_CASE_ACCESS_AS_SYSUSER.getValue()
            );
        CaseDataContent caseDataContent;
        switch (caseSecurityClassification) {
            case RESTRICTED -> caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
                startAllTabsUpdateDataContent.startEventResponse(),
                Classification.RESTRICTED
            );
            case PRIVATE -> caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
                startAllTabsUpdateDataContent.startEventResponse(),
                Classification.PRIVATE
            );
            default -> caseDataContent = coreCaseDataService.createCaseDataContentOnlyWithSecurityClassification(
                startAllTabsUpdateDataContent.startEventResponse(),
                Classification.PUBLIC
            );
        }
        coreCaseDataService.submitUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataContent,
            String.valueOf(callbackRequest.getCaseDetails().getId()),
            true
        );

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

    public AboutToStartOrSubmitCallbackResponse changeCaseAccess(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseSecurityClassificationEnum caseSecurityClassification
            = CaseSecurityClassificationEnum.fromValue((String) caseDataUpdated.get(CASE_SECURITY_CLASSIFICATION));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated)
            .securityClassification(String.valueOf(caseSecurityClassification))
            .build();
    }

    public Map<String, Object> retrieveAssignedUserRoles(CallbackRequest callbackRequest) {

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseEvent caseEvent = CaseEvent.fromValue(callbackRequest.getEventId());
        if (MARK_CASE_AS_RESTRICTED.equals(caseEvent) || MARK_CASE_AS_PRIVATE.equals(caseEvent)) {
            List<String> assignedUserDetailsHtml = fetchAssignedUserDetails(callbackRequest);
            if (CollectionUtils.isNotEmpty(assignedUserDetailsHtml)) {
                caseDataUpdated.put("assignedUserDetailsText", String.join("\n\n", assignedUserDetailsHtml));
            } else if (MARK_CASE_AS_RESTRICTED.equals(caseEvent)) {
                caseDataUpdated.put("errors", "No user got access to this case right now, "
                    + "please provide access to the users with right permissions before proceeding.");
            }
        }
        return caseDataUpdated;
    }

    private List<String> fetchAssignedUserDetails(CallbackRequest callbackRequest) {
        List<String> assignedUserDetailsHtml = new ArrayList<>();
        Map<String, String> assignedUserDetails = new HashMap<>();
        RoleAssignmentQueryRequest roleAssignmentQueryRequest = RoleAssignmentQueryRequest.builder()
            .attributes(QueryAttributes.builder()
                            .caseId(List.of(callbackRequest.getCaseDetails().getId().toString()))
                            .build())
            .validAt(LocalDateTime.now())
            .build();
        String systemAuthorisation = systemUserService.getSysUserToken();

        RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentApi.queryRoleAssignments(
            systemAuthorisation,
            authTokenGenerator.generate(),
            null,
            roleAssignmentQueryRequest
        );

        if (ObjectUtils.isNotEmpty(roleAssignmentServiceResponse)
            && CollectionUtils.isNotEmpty(roleAssignmentServiceResponse.getRoleAssignmentResponse())) {
            roleAssignmentServiceResponse.getRoleAssignmentResponse()
                .stream().filter(roleAssignmentResponse -> ROLE_CATEGORIES.contains(roleAssignmentResponse.getRoleCategory())
                && SPECIFIC_ACCESS_GRANT.equalsIgnoreCase(roleAssignmentResponse.getGrantType()))
                .forEach(roleAssignmentResponse -> {
                    UserDetails userDetails = idamApi.getUserByUserId(
                        systemAuthorisation,
                        roleAssignmentResponse.getActorId()
                    );
                    if (ObjectUtils.isNotEmpty(userDetails)) {
                        assignedUserDetails.put(
                            userDetails.getFullName() + HYPHEN_SEPARATOR + userDetails.getEmail(),
                            roleAssignmentResponse.getRoleLabel()
                        );
                    }
                });
        }
        if (!assignedUserDetails.isEmpty()) {
            assignedUserDetailsHtml.add("<div class='width-100'>");
            assignedUserDetailsHtml.add(
                "<h2 class=\"govuk-heading-m\">Users with access</h2>");
            assignedUserDetailsHtml.add("<table class=\"govuk-table\">");
            assignedUserDetailsHtml.add("<thead class=\"govuk-table__head\">");
            assignedUserDetailsHtml.add(
                "<tr class=\"govuk-table__row\"><th scope=\"col\" class=\"govuk-table__header govuk-table-column-header\">User</th>"
                    + "<th scope=\"col\" class=\"govuk-table__header govuk-table-column-header\">Case role</th>"
                    + "<th scope=\"col\" class=\"govuk-table__header govuk-table-column-actions\">Email address</th></tr>");
            assignedUserDetailsHtml.add("</thead>");
            assignedUserDetailsHtml.add("<tbody class=\"govuk-table__body\">");
            for (Map.Entry<String, String> entry : assignedUserDetails.entrySet()) {
                assignedUserDetailsHtml.add("<tr class=\"govuk-table__row\">");
                String name = entry.getKey().split(HYPHEN_SEPARATOR)[0];
                String email = entry.getKey().split(HYPHEN_SEPARATOR)[1];
                assignedUserDetailsHtml.add(TD_START_CELL + name + TD_CLOSURE
                                                + TD_START_CELL + entry.getValue() + TD_CLOSURE
                                                + TD_START_CELL + email + TD_CLOSURE);
                assignedUserDetailsHtml.add("</tr>");
            }
            assignedUserDetailsHtml.add("</tbody>");
            assignedUserDetailsHtml.add("</table>");
            assignedUserDetailsHtml.add("</div>");
        }
        return assignedUserDetailsHtml;
    }

}
