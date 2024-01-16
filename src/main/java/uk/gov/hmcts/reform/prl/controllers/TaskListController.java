package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDICIAL_REVIEW_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBMITTED_STATE;


@Tag(name = "task-list-controller")
@Slf4j
@RestController
@RequestMapping("/update-task-list")
@SecurityRequirement(name = "Bearer Authentication")
public class TaskListController extends AbstractCallbackController {
    @Qualifier("allTabsService")
    private final AllTabServiceImpl tabService;
    private final UserService userService;
    private final DocumentGenService dgsService;

    @Autowired
    public TaskListController(ObjectMapper objectMapper,
                              EventService eventPublisher,
                              AllTabServiceImpl tabService,
                              UserService userService,
                              DocumentGenService dgsService) {
        super(objectMapper, eventPublisher);
        this.tabService = tabService;
        this.userService = userService;
        this.dgsService = dgsService;
    }

    @PostMapping("/submitted")
    public AboutToStartOrSubmitCallbackResponse handleSubmitted(@RequestBody CallbackRequest callbackRequest,
                                                                @RequestHeader(HttpHeaders.AUTHORIZATION)
                                                                @Parameter(hidden = true) String authorisation) throws JsonProcessingException {
        log.info("Private law monitoring: TaskListController - handleSubmitted event started for case id {} at {} ",
                 callbackRequest.getCaseDetails().getId(), LocalDate.now()
        );
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        publishEvent(new CaseDataChanged(caseData));
        UserDetails userDetails = userService.getUserDetails(authorisation);
        List<String> roles = userDetails.getRoles();
        boolean isCourtStaff = roles.stream().anyMatch(ROLES::contains);
        String state = callbackRequest.getCaseDetails().getState();
        if (isCourtStaff && (SUBMITTED_STATE.equalsIgnoreCase(state) || ISSUED_STATE.equalsIgnoreCase(state)
        || JUDICIAL_REVIEW_STATE.equalsIgnoreCase(state))) {
            try {
                caseDataUpdated.putAll(dgsService.generateDocuments(authorisation, caseData));
                CaseData updatedCaseData = objectMapper.convertValue(caseDataUpdated, CaseData.class);
                caseData = caseData.toBuilder()
                    .c8Document(updatedCaseData.getC8Document())
                    .c1ADocument(updatedCaseData.getC1ADocument())
                    .c8WelshDocument(updatedCaseData.getC8WelshDocument())
                    .finalDocument(updatedCaseData.getFinalDocument())
                    .finalWelshDocument(updatedCaseData.getFinalWelshDocument())
                    .c1AWelshDocument(updatedCaseData.getC1AWelshDocument())
                    .build();
            } catch (Exception e) {
                log.error("Error regenerating the document", e);
            }
        }

        tabService.updateAllTabsIncludingConfTab(caseData);

        if (!isCourtStaff) {
            log.info("Private law monitoring: TaskListController - case data changed started for case id {} at {} ",
                     callbackRequest.getCaseDetails().getId(), LocalDate.now()
            );
            publishEvent(new CaseDataChanged(caseData));
            log.info("Private law monitoring: TaskListController - case data changed completed for case id {} at {} ",
                     callbackRequest.getCaseDetails().getId(), LocalDate.now()
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    @PostMapping("/updateTaskListOnly")
    public void updateTaskListWhenSubmitted(@RequestBody CallbackRequest callbackRequest,
                                            @RequestHeader(HttpHeaders.AUTHORIZATION)
                                            @Parameter(hidden = true) String authorisation) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        publishEvent(new CaseDataChanged(caseData));
    }
}
