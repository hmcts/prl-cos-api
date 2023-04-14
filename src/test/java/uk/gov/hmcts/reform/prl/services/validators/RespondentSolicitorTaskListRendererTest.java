package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.models.c100respondentsolicitor.RespondentEventValidationErrors;
import uk.gov.hmcts.reform.prl.models.tasklist.RespondentTask;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskListRenderElements;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentSolicitorTaskListRenderer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ABILITY_TO_PARTICIPATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ATTENDING_THE_COURT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONSENT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CURRENT_OR_PREVIOUS_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.MIAM;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.VIEW_DRAFT_RESPONSE;

public class RespondentSolicitorTaskListRendererTest {
    private final RespondentSolicitorTaskListRenderer taskListRenderer = new RespondentSolicitorTaskListRenderer(
        new TaskListRenderElements(
            "NO IMAGE URL IN THIS BRANCH"
        )
    );

    private final List<RespondentTask> tasks = List.of(
        RespondentTask.builder().event(CONSENT).state(TaskState.NOT_STARTED).build(),
        RespondentTask.builder().event(KEEP_DETAILS_PRIVATE).state(TaskState.NOT_STARTED).build(),
        RespondentTask.builder().event(CONFIRM_EDIT_CONTACT_DETAILS).state(TaskState.NOT_STARTED).build(),
        RespondentTask.builder().event(ATTENDING_THE_COURT).state(TaskState.NOT_STARTED).build(),
        RespondentTask.builder().event(MIAM).state(TaskState.NOT_STARTED).build(),
        RespondentTask.builder().event(CURRENT_OR_PREVIOUS_PROCEEDINGS).state(TaskState.NOT_STARTED).build(),
        RespondentTask.builder().event(ALLEGATION_OF_HARM).state(TaskState.NOT_STARTED).build(),
        RespondentTask.builder().event(INTERNATIONAL_ELEMENT).state(TaskState.NOT_STARTED).build(),
        RespondentTask.builder().event(ABILITY_TO_PARTICIPATE).state(TaskState.NOT_STARTED).build(),
        RespondentTask.builder().event(VIEW_DRAFT_RESPONSE).state(TaskState.NOT_STARTED).build(),
        RespondentTask.builder().event(SUBMIT).state(TaskState.NOT_STARTED).build()
    );

    @Test
    public void renderTaskListTest() {
        List<RespondentEventValidationErrors> resErrors = new ArrayList<>();
        String taskList = taskListRenderer.render(tasks, resErrors, "A", "test test", true, 12345L);

        assertNotNull(taskList);
    }

}
