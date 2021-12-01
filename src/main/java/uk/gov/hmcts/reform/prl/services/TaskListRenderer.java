package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
//import uk.gov.hmcts.reform.prl.models.submission.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskSection;

import java.util.ArrayList;
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
import static uk.gov.hmcts.reform.prl.enums.Event.*;
import static uk.gov.hmcts.reform.prl.models.tasklist.TaskSection.newSection;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TaskListRenderer {

    private static final String HORIZONTAL_LINE = "<hr class='govuk-!-margin-top-3 govuk-!-margin-bottom-2'/>";
    private static final String NEW_LINE = "<br/>";

    private final TaskListRenderElements taskListRenderElements;


//    public String render(List<Task> allTasks, List<EventValidationErrors> tasksErrors) {
//        final List<String> lines = new LinkedList<>();
//
//        lines.add("<div class='width-50'>");
//
//        groupInSections(allTasks).forEach(section -> lines.addAll(renderSection(section)));
//
//        lines.add("</div>");
//
//        lines.addAll(renderTasksErrors(tasksErrors));
//
//        return String.join("\n\n", lines);
//    }

    public String render(List<Task> allTasks) {
        final List<String> lines = new LinkedList<>();

        lines.add("<div class='width-50'>");

        groupInSections(allTasks).forEach(section -> lines.addAll(renderSection(section)));

        lines.add("</div>");

        //lines.addAll(renderTasksErrors(tasksErrors));

        List<String> testLines = new ArrayList<>();

        lines.addAll(taskListRenderElements.renderCollapsible("Why can’t I submit my application?", testLines));

        return String.join("\n\n", lines);
    }

    private List<TaskSection> groupInSections(List<Task> allTasks) {
        final Map<Event, Task> tasks = allTasks.stream().collect(toMap(Task::getEvent, identity()));

        final TaskSection applicationDetails = newSection("Add application details")
            .withTask(tasks.get(CASE_NAME))
            .withTask(tasks.get(TYPE_OF_APPLICATION))
            .withTask(tasks.get(HEARING_URGENCY));

        final TaskSection peopleInTheCase = newSection("Add people to the case")
            .withTask(tasks.get(APPLICANT_DETAILS))
            .withTask(tasks.get(CHILD_DETAILS))
            .withTask(tasks.get(RESPONDENT_DETAILS));

        final TaskSection requiredDetails = newSection("Add required details")
            .withTask(tasks.get(MIAM))
            .withTask(tasks.get(ALLEGATIONS_OF_HARM));

        final TaskSection additionalInformation = newSection("Add additional information")
            .withInfo("Only complete if relevant")
            .withTask(tasks.get(OTHER_PEOPLE_IN_THE_CASE))
            .withTask(tasks.get(OTHER_PROCEEDINGS))
            .withTask(tasks.get(ATTENDING_THE_HEARING))
            .withTask(tasks.get(INTERNATIONAL_ELEMENT))
            .withTask(tasks.get(LITIGATION_CAPACITY))
            .withTask(tasks.get(WELSH_LANGUAGE_REQUIREMENTS));

        final TaskSection pdfApplication = newSection("View PDF application")
            .withTask(tasks.get(VIEW_PDF_APPLICATION));

        final TaskSection submitAndPay = newSection("Submit and pay")
            .withTask(tasks.get(SUBMIT_AND_PAY));

        return Stream.of(applicationDetails,
                         peopleInTheCase,
                         requiredDetails,
                         additionalInformation,
                         pdfApplication,
                         submitAndPay)
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

        lines.add(taskListRenderElements.renderLink(task));

//        switch (task.getState()) {
//            case NOT_AVAILABLE:
//                lines.add(taskListRenderElements.renderDisabledLink(task)
//                              + taskListRenderElements.renderImage("cannot-send-yet.png", "Cannot send yet"));
//                break;
//            case IN_PROGRESS:
//                lines.add(taskListRenderElements.renderLink(task)
//                              + taskListRenderElements.renderImage("in-progress.png", "In progress"));
//                break;
//            case COMPLETED:
//                lines.add(taskListRenderElements.renderLink(task)
//                              + taskListRenderElements.renderImage("information-added.png", "Information added"));
//                break;
//            case COMPLETED_FINISHED:
//                lines.add(taskListRenderElements.renderLink(task)
//                              + taskListRenderElements.renderImage("finished.png", "Finished"));
//                break;
//            default:
//                lines.add(taskListRenderElements.renderLink(task));
//        }

        task.getHint().map(taskListRenderElements::renderHint).ifPresent(lines::add);
        return lines;
    }

//    private List<String> renderTasksErrors(List<EventValidationErrors> taskErrors) {
//        if (isEmpty(taskErrors)) {
//            return emptyList();
//        }
//        final List<String> errors = taskErrors.stream()
//            .flatMap(task -> task.getErrors()
//                .stream()
//                .map(error -> format("%s in the %s", error, taskListRenderElements.renderLink(task.getEvent()))))
//            .collect(toList());
//
//        return taskListRenderElements.renderCollapsible("Why can't I submit my application?", errors);
//    }



}
