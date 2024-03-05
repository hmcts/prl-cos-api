package uk.gov.hmcts.reform.prl.controllers.gatekeeping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.roleassignment.RoleAssignmentDto;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.List;
import java.util.Map;
import javax.ws.rs.NotFoundException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALLOCATE_JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLOCATED_JUDGE;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/allocateJudge")
public class AllocateJudgeController extends AbstractCallbackController {
    @Qualifier("caseSummaryTab")
    private final CaseSummaryTabService caseSummaryTabService;
    private final RefDataUserService refDataUserService;
    private final AllocatedJudgeService allocatedJudgeService;
    private final AuthorisationService authorisationService;

    private final RoleAssignmentService roleAssignmentService;

    @Autowired
    protected AllocateJudgeController(ObjectMapper objectMapper,
                                      EventService eventPublisher,
                                      CaseSummaryTabService caseSummaryTabService,
                                      RefDataUserService refDataUserService,
                                      AllocatedJudgeService allocatedJudgeService,
                                      AuthorisationService authorisationService,
                                      RoleAssignmentService roleAssignmentService) {
        super(objectMapper, eventPublisher);
        this.caseSummaryTabService = caseSummaryTabService;
        this.refDataUserService = refDataUserService;
        this.allocatedJudgeService = allocatedJudgeService;
        this.authorisationService = authorisationService;
        this.roleAssignmentService = roleAssignmentService;
    }

    @PostMapping(path = "/pre-populate-legalAdvisor-details", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to retrieve legal advisor details")
    public AboutToStartOrSubmitCallbackResponse prePopulateLegalAdvisorDetails(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            log.info("Allocate to judge Before calling ref data for LA users list {}", System.currentTimeMillis());
            List<DynamicListElement> legalAdviserList = refDataUserService.getLegalAdvisorList();
            log.info("Allocate to judge After calling ref data for LA users list {}", System.currentTimeMillis());
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            caseDataUpdated.put(
                "legalAdviserList",
                DynamicList.builder().value(DynamicListElement.EMPTY).listItems(legalAdviserList)
                    .build()
            );
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }

    }

    @PostMapping(path = "/allocatedJudgeDetails", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "allocatedJudgeDetails. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Allocated Judge Successfully ."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse allocateJudge(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws JsonProcessingException {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            AllocatedJudge allocatedJudge = allocatedJudgeService.getAllocatedJudgeDetails(
                caseDataUpdated,
                caseData.getLegalAdviserList(),
                refDataUserService
            );
            caseData = caseData.toBuilder().allocatedJudge(allocatedJudge).build();
            caseDataUpdated.putAll(caseSummaryTabService.updateTab(caseData));

            if (allocatedJudge.getIsSpecificJudgeOrLegalAdviserNeeded().equals(YesOrNo.Yes)) {
                RoleAssignmentDto roleAssignmentDto = RoleAssignmentDto.builder()
                    .judgeEmail(allocatedJudge.getJudgeEmail())
                    .legalAdviserList(allocatedJudge.getLegalAdviserList())
                    .build();
                roleAssignmentService.createRoleAssignment(
                    authorisation,
                    callbackRequest.getCaseDetails(),
                    roleAssignmentDto,
                    ALLOCATED_JUDGE.getName(),
                    false,
                    ALLOCATE_JUDGE_ROLE
                );
            }
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
