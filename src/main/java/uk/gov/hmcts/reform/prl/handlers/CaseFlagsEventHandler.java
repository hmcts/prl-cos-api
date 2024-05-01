package uk.gov.hmcts.reform.prl.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.amroles.InternalCaseworkerAmRolesEnum;
import uk.gov.hmcts.reform.prl.events.CaseFlagsEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseFlagsEventHandler {
    private final UserService userService;
    private final RoleAssignmentApi roleAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final AllTabServiceImpl allTabService;

    @Async
    @EventListener
    public void triggerDummyEventForCaseFlags(final CaseFlagsEvent event) {
        log.info("Inside event handler");
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
        try {
            log.info("roles retrieved ===>" + objectMapper.writeValueAsString(roles));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        if (roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.CTSC.getRoles()::contains)) {
            Map<String, Object> caseDetailsBeforeMap
                = objectMapper.convertValue(event.callbackRequest().getCaseDetailsBefore(), Map.class);
            Map<String, Object> caseDetailsAfterMap
                = objectMapper.convertValue(event.callbackRequest().getCaseDetails(), Map.class);
            CaseData caseDataBefore = CaseUtils.getCaseData(
                event.callbackRequest().getCaseDetailsBefore(),
                objectMapper
            );
            CaseData caseDataAfter = CaseUtils.getCaseData(event.callbackRequest().getCaseDetails(), objectMapper);

            if (caseDataBefore.getCaseFlags().getDetails().size() != caseDataAfter.getCaseFlags().getDetails().size()) {
                Element<FlagDetail> flagDetailElement = caseDataAfter.getCaseFlags().getDetails()
                    .get(caseDataAfter.getCaseFlags().getDetails().size() - 1);

                if ("Requested".equalsIgnoreCase(flagDetailElement.getValue().status)) {
                    log.info("Status found as Requested for case flag item:: " + flagDetailElement.getValue().name);
                } else {
                    log.info("Status found as different for case flag item:: " + flagDetailElement.getValue().name);
                }
            }

            if (!caseDetailsBeforeMap.equals(caseDetailsAfterMap)) {
                MapDifference<String, Object> diff = Maps.difference(caseDetailsBeforeMap, caseDetailsAfterMap);
                try {
                    log.info("difference in the map is ===>" + objectMapper.writeValueAsString(diff.entriesDiffering()));
                } catch (JsonProcessingException e) {
                    log.info("error");
                }
            }

            log.info("triggered by user having CTSC role");
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificEvent(
                caseId,
                CaseEvent.CREATE_WA_TASK_FOR_CTSC_CASE_FLAGS.getValue()
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
}
