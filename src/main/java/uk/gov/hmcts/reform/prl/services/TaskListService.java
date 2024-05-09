package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.models.tasklist.RespondentTask;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators.RespondentEventsChecker;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDICIAL_REVIEW_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ROLES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBMITTED_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM_REVISED;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_APPLICANTS;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS_REVISED;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_APPLICANT_FAMILY_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_HOME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_RESUBMIT;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_SOT_AND_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_UPLOAD_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.prl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.LITIGATION_CAPACITY;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM_POLICY_UPGRADE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE_REVISED;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.RELATIONSHIP_TO_RESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_BEHAVIOUR;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.Event.SUBMIT_AND_PAY;
import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.VIEW_PDF_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.WELSH_LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.WITHOUT_NOTICE_ORDER;
import static uk.gov.hmcts.reform.prl.enums.State.AWAITING_FL401_SUBMISSION_TO_HMCTS;
import static uk.gov.hmcts.reform.prl.enums.State.AWAITING_SUBMISSION_TO_HMCTS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ABILITY_TO_PARTICIPATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ATTENDING_THE_COURT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONSENT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.VIEW_DRAFT_RESPONSE;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListService {

    private final EventsChecker eventsChecker;
    private final RespondentEventsChecker respondentEventsChecker;
    @Qualifier("allTabsService")
    private final AllTabServiceImpl tabService;
    private final UserService userService;
    private final DocumentGenService dgsService;
    private final ObjectMapper objectMapper;
    private final EventService eventPublisher;
    private final LaunchDarklyClient launchDarklyClient;
    private final RoleAssignmentApi roleAssignmentApi;
    private final AuthTokenGenerator authTokenGenerator;

    private final MiamPolicyUpgradeFileUploadService miamPolicyUpgradeFileUploadService;

    public List<Task> getTasksForOpenCase(CaseData caseData) {
        return getEvents(caseData).stream()
                .map(event -> Task.builder()
                        .event(event)
                        .state(getTaskState(caseData, event))
                        .build())
                .toList();
    }

    public List<RespondentTask> getRespondentSolicitorTasks(PartyDetails respondingParty, CaseData caseData) {
        return getRespondentsEvents(caseData).stream()
                .map(event -> RespondentTask.builder()
                        .event(event)
                        .state(getRespondentTaskState(event, respondingParty))
                        .build())
                .toList();
    }

    private TaskState getTaskState(CaseData caseData, Event event) {
        if (eventsChecker.isFinished(event, caseData)) {
            return TaskState.FINISHED;
        }
        if (eventsChecker.hasMandatoryCompleted(event, caseData)) {
            return TaskState.MANDATORY_COMPLETED;
        }
        if (eventsChecker.isStarted(event, caseData)) {
            return TaskState.IN_PROGRESS;
        }
        return eventsChecker.getDefaultState(event, caseData);
    }

    private TaskState getRespondentTaskState(RespondentSolicitorEvents event, PartyDetails respondingParty) {
        if (respondentEventsChecker.isFinished(event, respondingParty)) {
            return TaskState.FINISHED;
        }
        if (respondentEventsChecker.isStarted(event, respondingParty)) {
            return TaskState.IN_PROGRESS;
        }
        return TaskState.NOT_STARTED;
    }

    private List<Event> getEvents(CaseData caseData) {
        return (PrlAppsConstants.FL401_CASE_TYPE).equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                ? getFL401Events(caseData) : getC100Events(caseData);
    }

    public List<Event> getC100Events(CaseData caseData) {

        if (TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())) {
            return new ArrayList<>(List.of(
                CASE_NAME,
                TYPE_OF_APPLICATION,
                HEARING_URGENCY,
                CHILD_DETAILS_REVISED,
                APPLICANT_DETAILS,
                RESPONDENT_DETAILS,
                OTHER_PEOPLE_IN_THE_CASE_REVISED,
                OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION,
                CHILDREN_AND_APPLICANTS,
                CHILDREN_AND_RESPONDENTS,
                CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION,
                ALLEGATIONS_OF_HARM_REVISED,
                MIAM_POLICY_UPGRADE,
                OTHER_PROCEEDINGS,
                ATTENDING_THE_HEARING,
                INTERNATIONAL_ELEMENT,
                LITIGATION_CAPACITY,
                WELSH_LANGUAGE_REQUIREMENTS,
                VIEW_PDF_DOCUMENT,
                SUBMIT_AND_PAY,
                SUBMIT
            ));
        } else if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())) {
            return new ArrayList<>(List.of(
                CASE_NAME,
                TYPE_OF_APPLICATION,
                HEARING_URGENCY,
                CHILD_DETAILS_REVISED,
                APPLICANT_DETAILS,
                RESPONDENT_DETAILS,
                OTHER_PEOPLE_IN_THE_CASE_REVISED,
                OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION,
                CHILDREN_AND_APPLICANTS,
                CHILDREN_AND_RESPONDENTS,
                CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION,
                ALLEGATIONS_OF_HARM_REVISED,
                MIAM,
                OTHER_PROCEEDINGS,
                ATTENDING_THE_HEARING,
                INTERNATIONAL_ELEMENT,
                LITIGATION_CAPACITY,
                WELSH_LANGUAGE_REQUIREMENTS,
                VIEW_PDF_DOCUMENT,
                SUBMIT_AND_PAY,
                SUBMIT
            ));
        }

        return new ArrayList<>(List.of(
            CASE_NAME,
            TYPE_OF_APPLICATION,
            HEARING_URGENCY,
            APPLICANT_DETAILS,
            CHILD_DETAILS,
            RESPONDENT_DETAILS,
            MIAM,
            ALLEGATIONS_OF_HARM,
            OTHER_PEOPLE_IN_THE_CASE,
            OTHER_PROCEEDINGS,
            ATTENDING_THE_HEARING,
            INTERNATIONAL_ELEMENT,
            LITIGATION_CAPACITY,
            WELSH_LANGUAGE_REQUIREMENTS,
            VIEW_PDF_DOCUMENT,
            SUBMIT_AND_PAY,
            SUBMIT
        ));
    }

    public List<Event> getFL401Events(CaseData caseData) {

        Optional<TypeOfApplicationOrders> ordersOptional = ofNullable(caseData.getTypeOfApplicationOrders());

        List<Event> eventsList = new ArrayList<>(List.of(
            FL401_CASE_NAME,
            FL401_TYPE_OF_APPLICATION,
            WITHOUT_NOTICE_ORDER,
            APPLICANT_DETAILS,
            RESPONDENT_DETAILS,
            FL401_APPLICANT_FAMILY_DETAILS,
            RELATIONSHIP_TO_RESPONDENT,
            FL401_OTHER_PROCEEDINGS,
            ATTENDING_THE_HEARING,
            WELSH_LANGUAGE_REQUIREMENTS,
            FL401_UPLOAD_DOCUMENTS,
            VIEW_PDF_DOCUMENT,
            FL401_SOT_AND_SUBMIT,
            FL401_RESUBMIT
        ));

        if (ordersOptional.isEmpty() || (ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)
            &&
            ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder))) {
            eventsList.add(RESPONDENT_BEHAVIOUR);
            eventsList.add(FL401_HOME);
        } else if (ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)) {
            eventsList.add(FL401_HOME);
        } else if (ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder)) {
            eventsList.add(RESPONDENT_BEHAVIOUR);
        }
        return eventsList;
    }

    public List<RespondentSolicitorEvents> getRespondentsEvents(CaseData caseData) {
        if (null != caseData.getC1ADocument()) {
            return new ArrayList<>(List.of(
                CONSENT,
                KEEP_DETAILS_PRIVATE,
                CONFIRM_EDIT_CONTACT_DETAILS,
                ATTENDING_THE_COURT,
                RespondentSolicitorEvents.MIAM,
                RespondentSolicitorEvents.OTHER_PROCEEDINGS,
                RespondentSolicitorEvents.ALLEGATION_OF_HARM,
                RespondentSolicitorEvents.RESPOND_ALLEGATION_OF_HARM,
                RespondentSolicitorEvents.INTERNATIONAL_ELEMENT,
                ABILITY_TO_PARTICIPATE,
                VIEW_DRAFT_RESPONSE,
                RespondentSolicitorEvents.SUBMIT
            ));
        }
        return new ArrayList<>(List.of(
            CONSENT,
            KEEP_DETAILS_PRIVATE,
            CONFIRM_EDIT_CONTACT_DETAILS,
            ATTENDING_THE_COURT,
            RespondentSolicitorEvents.MIAM,
            RespondentSolicitorEvents.OTHER_PROCEEDINGS,
            RespondentSolicitorEvents.ALLEGATION_OF_HARM,
            RespondentSolicitorEvents.INTERNATIONAL_ELEMENT,
            ABILITY_TO_PARTICIPATE,
            VIEW_DRAFT_RESPONSE,
            RespondentSolicitorEvents.SUBMIT
        ));
    }

    public AboutToStartOrSubmitCallbackResponse updateTaskList(CallbackRequest callbackRequest, String authorisation) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = tabService.getStartAllTabsUpdate(String.valueOf(callbackRequest.getCaseDetails().getId()));
        CaseData caseData = startAllTabsUpdateDataContent.caseData();
        Map<String, Object> caseDataUpdated = startAllTabsUpdateDataContent.caseDataMap();
        UserDetails userDetails = userService.getUserDetails(authorisation);
        List<String> roles = userDetails.getRoles();
        if (launchDarklyClient.isFeatureEnabled(ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY)) {
            RoleAssignmentServiceResponse roleAssignmentServiceResponse = roleAssignmentApi.getRoleAssignments(
                authorisation,
                authTokenGenerator.generate(),
                null,
                userDetails.getId()
            );
            roles = CaseUtils.mapAmUserRolesToIdamRoles(roleAssignmentServiceResponse, authorisation, userDetails);
        }
        boolean isCourtStaff = roles.stream().anyMatch(ROLES::contains);
        String state = callbackRequest.getCaseDetails().getState();
        if (isCourtStaff && (SUBMITTED_STATE.equalsIgnoreCase(state) || ISSUED_STATE.equalsIgnoreCase(state))
            || JUDICIAL_REVIEW_STATE.equalsIgnoreCase(state)) {
            try {
                if (Event.AMEND_MIAM_POLICY_UPGRADE.getId().equals(callbackRequest.getEventId())) {
                    caseData = miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithConfidential(
                        caseData,
                        startAllTabsUpdateDataContent.authorisation()
                    );
                }
                caseDataUpdated.putAll(dgsService.generateDocuments(authorisation, caseData));
                CaseData updatedCaseData = objectMapper.convertValue(caseDataUpdated, CaseData.class);
                caseData = caseData.toBuilder()
                        .c8Document(updatedCaseData.getC8Document())
                        .c1ADocument(updatedCaseData.getC1ADocument())
                        .c8WelshDocument(updatedCaseData.getC8WelshDocument())
                        .finalDocument(!JUDICIAL_REVIEW_STATE.equalsIgnoreCase(state)
                                           ? updatedCaseData.getFinalDocument() : caseData.getFinalDocument())
                        .finalWelshDocument(!JUDICIAL_REVIEW_STATE.equalsIgnoreCase(state)
                                                ? updatedCaseData.getFinalWelshDocument() : caseData.getFinalWelshDocument())
                        .c1AWelshDocument(updatedCaseData.getC1AWelshDocument())
                        .build();
            } catch (Exception e) {
                log.error("Error regenerating the document", e);
            }
        }

        tabService.mapAndSubmitAllTabsUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            String.valueOf(callbackRequest.getCaseDetails().getId()),
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseData
        );

        if (!isCourtStaff
            || (isCourtStaff && (AWAITING_SUBMISSION_TO_HMCTS.getValue().equalsIgnoreCase(state)
            || AWAITING_FL401_SUBMISSION_TO_HMCTS.getValue().equalsIgnoreCase(state)))) {
            eventPublisher.publishEvent(new CaseDataChanged(caseData));
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }
}
