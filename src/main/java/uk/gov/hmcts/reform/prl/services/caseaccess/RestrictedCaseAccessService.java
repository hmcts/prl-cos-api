package uk.gov.hmcts.reform.prl.services.caseaccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final RoleAssignmentApi roleAssignmentApi;

    private final SystemUserService systemUserService;

    private final AuthTokenGenerator authTokenGenerator;

    private final IdamApi idamApi;

    public static final List<String> ROLE_CATEGORIES = List.of("JUDICIAL",
                                                     "LEGAL_OPERATIONS",
                                                     "CTSC",
                                                     "ADMIN");

    public Map<String, Object> initiateUpdateCaseAccess(CallbackRequest callbackRequest) {
        log.info("** restrictedCaseAccessAboutToSubmit event started");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseEvent caseEvent = CaseEvent.fromValue(callbackRequest.getEventId());
        if (MARK_CASE_AS_RESTRICTED.equals(caseEvent)) {
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
            applicantCaseName = applicantCaseName + RESTRICTED_CASE;
        } else if (MARK_CASE_AS_PRIVATE.equals(caseEvent)) {
            applicantCaseName = applicantCaseName + PRIVATE_CASE;
        }

        caseDataUpdated.put(APPLICANT_CASE_NAME, applicantCaseName);
        caseDataUpdated.put("caseNameHmctsInternal", applicantCaseName);
        if (caseDataUpdated.containsKey(APPLICANT_OR_RESPONDENT_CASE_NAME)) {
            caseDataUpdated.put(APPLICANT_OR_RESPONDENT_CASE_NAME, applicantCaseName);
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

    public Map<String, Object> retrieveAssignedUserRoles(CallbackRequest callbackRequest) {
        log.info("** retrieveAssignedUserRoles event started");

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseEvent caseEvent = CaseEvent.fromValue(callbackRequest.getEventId());
        if (MARK_CASE_AS_RESTRICTED.equals(caseEvent) || MARK_CASE_AS_PRIVATE.equals(caseEvent)) {
            List<String> assignedUserDetailsHtml = fetchAssignedUserDetails(callbackRequest);
            if (CollectionUtils.isNotEmpty(assignedUserDetailsHtml)) {
                caseDataUpdated.put("assignedUserDetailsText", String.join("\n\n", assignedUserDetailsHtml));
            } else if (MARK_CASE_AS_RESTRICTED.equals(caseEvent)) {
                // caseDataUpdated.put("errors", "No one have access to this case right now, "
                //    + "Please provide access to the people with right permissions");
            }
        }
        log.info("** retrieveAssignedUserRoles done");
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
        log.info("** RoleAssignmentQueryRequest " + roleAssignmentQueryRequest);
        String systemAuthorisation = systemUserService.getSysUserToken();

        RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentApi.queryRoleAssignments(
            systemAuthorisation,
            authTokenGenerator.generate(),
            null,
            roleAssignmentQueryRequest
        );
        log.info("** RoleAssignmentServiceResponse " + roleAssignmentServiceResponse);

        if (ObjectUtils.isNotEmpty(roleAssignmentServiceResponse)
            && CollectionUtils.isNotEmpty(roleAssignmentServiceResponse.getRoleAssignmentResponse())) {
            roleAssignmentServiceResponse.getRoleAssignmentResponse()
                .stream().filter(roleAssignmentResponse -> ROLE_CATEGORIES.contains(roleAssignmentResponse.getRoleCategory()))
                .forEach(roleAssignmentResponse -> {
                    log.info("** Fetching user details from idam for actorId {} " + roleAssignmentResponse.getActorId());
                    UserDetails userDetails = idamApi.getUserByUserId(
                        systemAuthorisation,
                        roleAssignmentResponse.getActorId()
                    );
                    if (ObjectUtils.isNotEmpty(userDetails)) {
                        assignedUserDetails.put(
                            userDetails.getFullName() + HYPHEN_SEPARATOR + userDetails.getEmail(),
                            roleAssignmentResponse.getRoleCategory()
                        );
                    }
                });
        }
        log.info("** AssignedUserDetails " + assignedUserDetails);
        if (!assignedUserDetails.isEmpty()) {
            assignedUserDetailsHtml.add("<table class=\"govuk-table\">");
            assignedUserDetailsHtml.add(
                "<caption class=\"govuk-table__caption govuk-table__caption--m\">Users with access</caption>");
            assignedUserDetailsHtml.add("<thead class=\"govuk-table__head\">");
            assignedUserDetailsHtml.add(
                "<tr class=\"govuk-table__row\"><th scope=\"col\" class=\"govuk-table__header govuk-!-width-one-half\">Name</th>"
                    + "<th scope=\"col\" class=\"govuk-table__header govuk-!-width-one-quarter\">Case role</th>"
                    + "<th scope=\"col\" class=\"govuk-table__header govuk-!-width-one-quarter\">Email address</th></tr>");
            assignedUserDetailsHtml.add("<thead class=\"govuk-table__head\">");
            assignedUserDetailsHtml.add("<tbody class=\"govuk-table__body\">");
            for (Map.Entry<String, String> entry : assignedUserDetails.entrySet()) {
                assignedUserDetailsHtml.add("<tr class=\"govuk-table__row\">");
                String name = entry.getKey().split(HYPHEN_SEPARATOR)[0];
                String email = entry.getKey().split(HYPHEN_SEPARATOR)[1];
                assignedUserDetailsHtml.add("<td class=\"govuk-table__cell\">" + name + "</td>"
                                                + "<td class=\"govuk-table__cell\">" + entry.getValue()
                                                + "</td><td class=\"govuk-table__cell\">" + email + "</td>");
                assignedUserDetailsHtml.add("</tr>");
            }
            assignedUserDetailsHtml.add("</tbody>");
            assignedUserDetailsHtml.add("</table>");
            assignedUserDetailsHtml.add("</div>");
        }
        log.info("** assignedUserDetailsText " + assignedUserDetailsHtml);
        return assignedUserDetailsHtml;
    }

}
