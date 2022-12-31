package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.models.tasklist.RespondentTask;
import uk.gov.hmcts.reform.prl.models.tasklist.RespondentTaskSection;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.prl.models.tasklist.RespondentTaskSection.newSection;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentSolicitorTaskListRenderer {

    private static final String HORIZONTAL_LINE = "<hr class='govuk-!-margin-top-3 govuk-!-margin-bottom-2'/>";
    private static final String NEW_LINE = "<br/>";
    private static final String NOT_STARTED = "not-started.png";
    private static final String CANNOT_START_YET = "cannot-start-yet.png";
    private static final String IN_PROGRESS = "in-progress.png";
    private static final String INFORMATION_ADDED = "information-added.png";
    private static final String FINISHED = "finished.png";

    private final TaskListRenderElements taskListRenderElements;


    public String render(List<RespondentTask> allTasks) {
        final List<String> lines = new LinkedList<>();

        lines.add("<div class='width-50'>");

        (groupInSections(allTasks))
            .forEach(section -> lines.addAll(renderSection(section)));

        lines.add("</div>");

        return String.join("\n\n", lines);
    }

    private List<RespondentTaskSection> groupInSections(List<RespondentTask> allTasks) {
        final Map<RespondentSolicitorEvents, RespondentTask> tasks = allTasks.stream().collect(toMap(
            RespondentTask::getEvent,
            identity()
        ));
        final RespondentTaskSection consent = newSection("1. Consent to the Application")
            .withTask(tasks.get(RespondentSolicitorEvents.CONSENT));

        final RespondentTaskSection details = newSection("2. Your details")
            .withTask(tasks.get(RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE))
            .withTask(tasks.get(RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS));

        return Stream.of(
            consent,
            details
        )
            .filter(RespondentTaskSection::hasAnyTask)
            .collect(toList());
    }

    private List<String> renderSection(RespondentTaskSection sec) {
        final List<String> section = new LinkedList<>();

        section.add(NEW_LINE);
        section.add(taskListRenderElements.renderHeader(sec.getName()));

        sec.getHint().map(taskListRenderElements::renderHint).ifPresent(section::add);
        sec.getInfo().map(taskListRenderElements::renderInfo).ifPresent(section::add);

        section.add(HORIZONTAL_LINE);
        sec.getRespondentTasks().forEach(task -> {
            section.addAll(renderTask(task));
            section.add(HORIZONTAL_LINE);
        });

        return section;
    }

    private List<String> renderTask(RespondentTask respondentTask) {
        final List<String> lines = new LinkedList<>();

        lines.add(taskListRenderElements.renderRespondentSolicitorLink(respondentTask));

        respondentTask.getHint().map(taskListRenderElements::renderHint).ifPresent(lines::add);
        return lines;
    }
}
