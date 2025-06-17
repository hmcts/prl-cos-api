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
import uk.gov.hmcts.reform.prl.enums.amroles.InternalCaseworkerAmRolesEnum;
import uk.gov.hmcts.reform.prl.events.CaseFlagsEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsWaService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseFlagsEventHandler {
    private final UserService userService;
    private final RoleAssignmentApi roleAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final ObjectMapper objectMapper;
    private final CaseFlagsWaService caseFlagsWaService;

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
        if (roles.stream().anyMatch(InternalCaseworkerAmRolesEnum.CTSC.getRoles()::contains)) {
            CaseData caseDataBefore = CaseUtils.getCaseData(event.callbackRequest().getCaseDetailsBefore(), objectMapper);
            CaseData caseData = CaseUtils.getCaseData(event.callbackRequest().getCaseDetails(), objectMapper);
            caseFlagsWaService.checkCaseFlagsToCreateTask(caseData, caseDataBefore);
        } else {
            CaseData caseDataBefore = CaseUtils.getCaseData(event.callbackRequest().getCaseDetailsBefore(), objectMapper);
            CaseData caseData = CaseUtils.getCaseData(event.callbackRequest().getCaseDetails(), objectMapper);
            caseFlagsWaService.checkCaseFlagsToCreateTask(caseData, caseDataBefore);
        }
    }
}
