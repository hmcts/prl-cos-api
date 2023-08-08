package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBMITTED_STATE;


@Tag(name = "task-list-controller")
@Slf4j
@RestController
@RequestMapping("/update-task-list")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SecurityRequirement(name = "Bearer Authentication")

public class TaskListController extends AbstractCallbackController {

    @Autowired
    @Qualifier("allTabsService")
    AllTabServiceImpl tabService;

    @Autowired
    UserService userService;

    @Autowired
    DocumentGenService dgsService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/submitted")
    public AboutToStartOrSubmitCallbackResponse handleSubmitted(@RequestBody CallbackRequest callbackRequest,
                                                                @RequestHeader(HttpHeaders.AUTHORIZATION)
                                                                @Parameter(hidden = true) String authorisation) throws JsonProcessingException {
        log.info("Private law monitoring: TaskListController - handleSubmitted event started for case id {} at {} ",
                 callbackRequest.getCaseDetails().getId(), LocalDate.now()
        );
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        if ("allegationsOfHarmRevised".equalsIgnoreCase(callbackRequest.getEventId())
            && YesOrNo.Yes.equals(caseData.getAllegationOfHarmRevised().getNewAllegationsOfHarmYesNo())) {
            log.info("caseData : {}", objectMapper.writeValueAsString(callbackRequest));
            Optional<ChildAbuse> childPhysicalAbuse =
                ofNullable(caseData.getAllegationOfHarmRevised().getChildPhysicalAbuse());
            if (childPhysicalAbuse.isPresent()) {
                childPhysicalAbuse.get().setTypeOfAbuse(ChildAbuseEnum.physicalAbuse);
            }

            Optional<ChildAbuse> childPsychologicalAbuse =
                ofNullable(caseData.getAllegationOfHarmRevised().getChildPsychologicalAbuse());
            if (childPsychologicalAbuse.isPresent()) {
                childPsychologicalAbuse.get().setTypeOfAbuse(ChildAbuseEnum.psychologicalAbuse);
            }

            Optional<ChildAbuse> childEmotionalAbuse =
                ofNullable(caseData.getAllegationOfHarmRevised().getChildEmotionalAbuse());
            if (childEmotionalAbuse.isPresent()) {
                childEmotionalAbuse.get().setTypeOfAbuse(ChildAbuseEnum.emotionalAbuse);
            }

            Optional<ChildAbuse> childSexualAbuse =
                ofNullable(caseData.getAllegationOfHarmRevised().getChildSexualAbuse());
            if (childSexualAbuse.isPresent()) {
                childSexualAbuse.get().setTypeOfAbuse(ChildAbuseEnum.sexualAbuse);
            }

            Optional<ChildAbuse> childFinancialAbuse =
                ofNullable(caseData.getAllegationOfHarmRevised().getChildFinancialAbuse());
            if (childFinancialAbuse.isPresent()) {
                childFinancialAbuse.get().setTypeOfAbuse(ChildAbuseEnum.financialAbuse);
            }

            log.info("updated allegation of harm");
        }
        publishEvent(new CaseDataChanged(caseData));
        UserDetails userDetails = userService.getUserDetails(authorisation);
        List<String> roles = userDetails.getRoles();
        boolean isCourtStaff = roles.stream().anyMatch(ROLES::contains);
        String state = callbackRequest.getCaseDetails().getState();
        if (isCourtStaff && (SUBMITTED_STATE.equalsIgnoreCase(state) || ISSUED_STATE.equalsIgnoreCase(state))) {
            try {
                log.info("Private law monitoring: TaskListController - handleSubmitted Generating documents for case id {} at {} ",
                         callbackRequest.getCaseDetails().getId(), LocalDate.now()
                );
                log.info("Generating documents for the amended details");
                caseDataUpdated.putAll(dgsService.generateDocuments(authorisation, caseData));
                log.info("Private law monitoring: TaskListController - handleSubmitted Generating documents completed for case id {} at {} ",
                         callbackRequest.getCaseDetails().getId(), LocalDate.now()
                );
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

        log.info("Private law monitoring: TaskListController - updateAllTabsIncludingConfTab started for case id {} at {} ",
                 callbackRequest.getCaseDetails().getId(), LocalDate.now()
        );
        tabService.updateAllTabsIncludingConfTab(caseData);
        log.info("Private law monitoring: TaskListController - updateAllTabsIncludingConfTab completed for case id {} at {} ",
                 callbackRequest.getCaseDetails().getId(), LocalDate.now()
        );

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
}
