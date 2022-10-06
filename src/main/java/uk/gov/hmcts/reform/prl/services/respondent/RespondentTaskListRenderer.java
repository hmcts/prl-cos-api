package uk.gov.hmcts.reform.prl.services.respondent;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskSection;
import uk.gov.hmcts.reform.prl.services.TaskListRenderElements;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.enums.Event.ABILITY_TO_PARICIPATE_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_COURT;
import static uk.gov.hmcts.reform.prl.enums.Event.CONFIRM_EDIT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.CONSENT_TO_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.CURRENT_OR_PREVIOUS_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_SUBMIT;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskSection.newSection;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentTaskListRenderer {

    private static final String HORIZONTAL_LINE = "<hr class='govuk-!-margin-top-3 govuk-!-margin-bottom-2'/>";
    private static final String NEW_LINE = "<br/>";
    private static final String NOT_STARTED = "not-started.png";
    private static final String CANNOT_START_YET = "cannot-start-yet.png";
    private static final String IN_PROGRESS = "in-progress.png";
    private static final String INFORMATION_ADDED = "information-added.png";
    private static final String FINISHED = "finished.png";

    private final TaskListRenderElements taskListRenderElements;


    public String render(List<Task> allTasks, List<EventValidationErrors> tasksErrors, CaseData caseData) {
        final List<String> lines = new LinkedList<>();

        lines.add("<div class='width-50'>");
        groupInSections(allTasks, caseData)
            .forEach(section -> lines.addAll(renderSection(section)));
        lines.add("</div>");
        lines.addAll(renderTasksErrors(tasksErrors));

        return String.join("\n\n", lines);
    }

    private List<TaskSection> groupInSections(List<Task> allTasks, CaseData caseData) {
        final Map<Event, Task> tasks = allTasks.stream().collect(toMap(Task::getEvent, identity()));
        final TaskSection consentDetails = newSection("1. Consent to the application")
            .withTask(tasks.get(CONSENT_TO_APPLICATION));

        final TaskSection yourDetails = newSection("2. Your details")
            .withTask(tasks.get(KEEP_DETAILS_PRIVATE))
            .withTask(tasks.get(CONFIRM_EDIT_CONTACT_DETAILS))
            .withTask(tasks.get(ATTENDING_THE_COURT));

        final TaskSection applicationDetails = newSection("3. Application details")
            .withTask(tasks.get(MIAM))
            .withTask(tasks.get(CURRENT_OR_PREVIOUS_PROCEEDINGS));

        final TaskSection safetyDetails = newSection("4. Safety concerns")
            .withTask(tasks.get(RESPONDENT_ALLEGATIONS_OF_HARM));

        final TaskSection additionalInformation = newSection("5. Additional information")
            .withTask(tasks.get(RESPONDENT_INTERNATIONAL_ELEMENT))
            .withTask(tasks.get(ABILITY_TO_PARICIPATE_PROCEEDINGS));

        final TaskSection pdfApplication = newSection("6. View PDF response")
            .withTask(tasks.get(RESPONDENT_DRAFT_DOCUMENT));

        final TaskSection submit = newSection("7. Submit ")
                .withTask(tasks.get(RESPONDENT_SUBMIT));

        return Stream.of(applicationDetails,
                         consentDetails,
                         yourDetails,
                         applicationDetails,
                         safetyDetails,
                         additionalInformation,
                         pdfApplication,
                         submit)
            .filter(TaskSection::hasAnyTask)
            .collect(toList());
    }

    private List<String> renderSection(TaskSection sec) {
        final List<String> section = new LinkedList<>();

        section.add(NEW_LINE);
        section.add(taskListRenderElements.renderHeader(sec.getName()));

        sec.getHint().map(taskListRenderElements::renderHint).ifPresent(section::add);
        sec.getInfo().map(taskListRenderElements::renderInfo).ifPresent(section::add);

        section.add(HORIZONTAL_LINE);
        sec.getTasks().forEach(task -> {
            section.addAll(renderTask(task));
            section.add(HORIZONTAL_LINE);
        });

        return section;
    }

    private List<String> renderTask(Task task) {
        final List<String> lines = new LinkedList<>();

        switch (task.getState()) {

            case NOT_STARTED:
                if (task.getEvent().equals(RESPONDENT_DRAFT_DOCUMENT)) {
                    lines.add(taskListRenderElements.renderLink(task));
                } else if (task.getEvent().equals(RESPONDENT_SUBMIT)) {
                    lines.add(taskListRenderElements.renderDisabledLink(task)
                                  + taskListRenderElements.renderImage(CANNOT_START_YET, "Cannot start yet"));
                } else {
                    lines.add(taskListRenderElements.renderLink(task)
                                  + taskListRenderElements.renderImage(NOT_STARTED, "Not started"));
                }
                break;
            case IN_PROGRESS:
                lines.add(taskListRenderElements.renderLink(task)
                              + taskListRenderElements.renderImage(IN_PROGRESS, "In progress"));
                break;
            case MANDATORY_COMPLETED:
                lines.add(taskListRenderElements.renderLink(task)
                              + taskListRenderElements.renderImage(INFORMATION_ADDED, "Information added"));
                break;
            case FINISHED:
                if (task.getEvent().equals(RESPONDENT_SUBMIT)) {
                    lines.add(taskListRenderElements.renderLink(task)
                                  + taskListRenderElements.renderImage(NOT_STARTED, "Not started yet"));
                } else {
                    lines.add(taskListRenderElements.renderLink(task)
                                  + taskListRenderElements.renderImage(FINISHED, "Finished"));
                }
                break;
            default:
                lines.add(taskListRenderElements.renderLink(task));
        }

        task.getHint().map(taskListRenderElements::renderHint).ifPresent(lines::add);
        return lines;
    }

    private List<String> renderTasksErrors(List<EventValidationErrors> taskErrors) {
        if (isEmpty(taskErrors)) {
            return emptyList();
        }
        final List<String> errors = taskErrors.stream()
            .flatMap(task -> task.getErrors()
                .stream()
                .map(error -> format("%s in %s", error, taskListRenderElements.renderLink(task.getEvent()))))
            .collect(toList());

        return taskListRenderElements.renderCollapsible("Why can't I submit my application?", errors);
    }

}
