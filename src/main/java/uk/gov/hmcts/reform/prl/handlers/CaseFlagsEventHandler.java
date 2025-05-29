package uk.gov.hmcts.reform.prl.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.amroles.InternalCaseworkerAmRolesEnum;
import uk.gov.hmcts.reform.prl.events.CaseFlagsEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CREATE_WA_TASK_FOR_C100_CTSC_CASE_NOTES_AND_FLAGS;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CREATE_WA_TASK_FOR_CTSC_CASE_FLAGS;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CREATE_WA_TASK_FOR_FL401_CTSC_CASE_NOTES_AND_FLAGS;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseFlagsEventHandler {
    private final UserService userService;
    private final RoleAssignmentApi roleAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final AllTabServiceImpl allTabService;

    private static final String FLAG_STATUS_REQUESTED = "Requested";

    @Async
    @EventListener
    public void triggerDummyEventForCaseFlags(final CaseFlagsEvent event) {
        String caseId = String.valueOf(event.callbackRequest().getCaseDetails().getId());
        UserDetails userDetails = userService.getUserDetails(event.authorisation());
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentApi.getRoleAssignments(
            event.authorisation(),
            authTokenGenerator.generate(),
            null,
            userDetails.getId()
        );

        List<String> roles = roleAssignmentServiceResponse
            .getRoleAssignmentResponse()
            .stream()
            .map(RoleAssignmentResponse::getRoleName)
            .toList();

        CaseData caseData = objectMapper.convertValue(
            event.callbackRequest().getCaseDetails().getData(),
            CaseData.class
        );

        List<Element<FlagDetail>> allDetails = new ArrayList<>();

        if (caseData.getCaseFlags() != null && caseData.getCaseFlags().getDetails() != null) {
            allDetails.addAll(caseData.getCaseFlags().getDetails());
        }

        if (caseData.getAllPartyFlags() != null) {
            allDetails.addAll(extractAllFlagDetails(caseData.getAllPartyFlags()));
        }

        List<Element<FlagDetail>> sortedDetails = allDetails.stream()
            .filter(detail -> detail.getValue() != null
                && detail.getValue().getStatus() != null
                && detail.getValue().getDateTimeCreated() != null)
            .sorted(Comparator.comparing(detail -> detail.getValue().getDateTimeCreated()))
            .toList();

        String requestEventId = event.callbackRequest().getEventId();
        boolean eventCanBeTriggered =  (CREATE_WA_TASK_FOR_C100_CTSC_CASE_NOTES_AND_FLAGS.getValue().equals(requestEventId)
            || CREATE_WA_TASK_FOR_FL401_CTSC_CASE_NOTES_AND_FLAGS.getValue().equals(requestEventId)
            || CREATE_WA_TASK_FOR_CTSC_CASE_FLAGS.getValue().equals(requestEventId));

        boolean isLastFlagRequested = !sortedDetails.isEmpty()
            && FLAG_STATUS_REQUESTED.equalsIgnoreCase(sortedDetails.get(sortedDetails.size() - 1).getValue().getStatus());

        if (eventCanBeTriggered && isLastFlagRequested && roles.stream()
            .anyMatch(InternalCaseworkerAmRolesEnum.COURT_ADMIN_TEAM_LEADER.getRoles()::contains)) {
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificEvent(
                caseId,
                event.callbackRequest().getEventId()
            );

            allTabService.submitAllTabsUpdate(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                startAllTabsUpdateDataContent.caseDataMap()
            );
        }
    }

    private List<Element<FlagDetail>> extractAllFlagDetails(AllPartyFlags allPartyFlags) {
        if (allPartyFlags == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(allPartyFlags.getClass().getDeclaredFields())
            .filter(field -> field.getType().equals(Flags.class))
            .map(field -> {
                field.setAccessible(true);
                try {
                    Flags flags = (Flags) field.get(allPartyFlags);
                    return flags != null ? flags.getDetails() : null;
                } catch (IllegalAccessException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }
}
