package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.RespondentTask;
import uk.gov.hmcts.reform.prl.services.RespondentSolicitorTaskListRenderer;
import uk.gov.hmcts.reform.prl.services.TaskListRenderElements;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.ABILITY_TO_PARTICIPATE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.ATTENDING_THE_COURT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.CONSENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.CURRENT_OR_PREVIOUS_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.MIAM;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.START_RESPONSE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.VIEW_DRAFT_RESPONSE;

public class RespondentSolicitorTaskListRendererTest {
    private final RespondentSolicitorTaskListRenderer taskListRenderer = new RespondentSolicitorTaskListRenderer(
        new TaskListRenderElements(
            "NO IMAGE URL IN THIS BRANCH"
        )
    );

    private final List<RespondentTask> tasks = List.of(
        RespondentTask.builder().event(START_RESPONSE).build(),
        RespondentTask.builder().event(CONSENT).build(),
        RespondentTask.builder().event(KEEP_DETAILS_PRIVATE).build(),
        RespondentTask.builder().event(CONFIRM_EDIT_CONTACT_DETAILS).build(),
        RespondentTask.builder().event(ATTENDING_THE_COURT).build(),
        RespondentTask.builder().event(MIAM).build(),
        RespondentTask.builder().event(CURRENT_OR_PREVIOUS_PROCEEDINGS).build(),
        RespondentTask.builder().event(ALLEGATION_OF_HARM).build(),
        RespondentTask.builder().event(INTERNATIONAL_ELEMENT).build(),
        RespondentTask.builder().event(ABILITY_TO_PARTICIPATE).build(),
        RespondentTask.builder().event(VIEW_DRAFT_RESPONSE).build(),
        RespondentTask.builder().event(SUBMIT).build()
    );

    @Test
    public void renderTaskListTest() {

        Response response = Response.builder().c7ResponseSubmitted(YesOrNo.Yes).build();

        PartyDetails respondent = PartyDetails.builder().response(response).build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .respondents(respondentList)
            .build();



        String taskList = taskListRenderer.render(tasks, caseData);

        assertNotNull(taskList);
    }

}
