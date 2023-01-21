package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.RespondentTask;
import uk.gov.hmcts.reform.prl.models.tasklist.RespondentTaskSection;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.prl.models.tasklist.RespondentTaskSection.newSection;

@Slf4j
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


    public String render(List<RespondentTask> allTasks, CaseData caseData) {
        final List<String> lines = new LinkedList<>();
        lines.add(
            "<div class='width-50'>><h3>Respond to the application</h3><p>This online response combines forms C7 and C8."
                + " It also allows you to make your own allegations of harm and violence (C1A)"
                + " in the section of safety concerns.</p><div>");

        lines.add("<div class='width-50'>");

        (groupInSections(allTasks))
            .forEach(section -> lines.addAll(renderSection(section, caseData)));

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

        final RespondentTaskSection yourDetails = newSection("2. Your details")
            .withTask(tasks.get(RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE))
            .withTask(tasks.get(RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS))
            .withTask(tasks.get(RespondentSolicitorEvents.ATTENDING_THE_COURT));

        final RespondentTaskSection applicationDetails = newSection("3. Application details")
            .withTask(tasks.get(RespondentSolicitorEvents.MIAM))
            .withTask(tasks.get(RespondentSolicitorEvents.CURRENT_OR_PREVIOUS_PROCEEDINGS));

        final RespondentTaskSection safetyConcerns = newSection("4. Safety Concerns")
            .withTask(tasks.get(RespondentSolicitorEvents.ALLEGATION_OF_HARM));

        final RespondentTaskSection additionalInformation = newSection("5. Additional information")
            .withTask(tasks.get(RespondentSolicitorEvents.INTERNATIONAL_ELEMENT))
            .withTask(tasks.get(RespondentSolicitorEvents.ABILITY_TO_PARTICIPATE));

        final RespondentTaskSection viewResponse = newSection("6. View PDF response")
            .withTask(tasks.get(RespondentSolicitorEvents.VIEW_DRAFT_RESPONSE));

        final RespondentTaskSection submit = newSection("7. Submit")
            .withTask(tasks.get(RespondentSolicitorEvents.SUBMIT));

        return Stream.of(
            consent,
            yourDetails,
            applicationDetails,
            safetyConcerns,
            additionalInformation,
            viewResponse,
            submit
        )
            .filter(RespondentTaskSection::hasAnyTask)
            .collect(toList());
    }

    private List<String> renderSection(RespondentTaskSection sec, CaseData caseData) {
        final List<String> section = new LinkedList<>();


        section.add(NEW_LINE);
        section.add(taskListRenderElements.renderHeader(sec.getName()));

        sec.getHint().map(taskListRenderElements::renderHint).ifPresent(section::add);
        sec.getInfo().map(taskListRenderElements::renderInfo).ifPresent(section::add);

        section.add(HORIZONTAL_LINE);
        sec.getRespondentTasks().forEach(task -> {
            section.addAll(renderRespondentTask(task, caseData));
            section.add(HORIZONTAL_LINE);
        });

        return section;
    }

    private List<String> renderRespondentTask(RespondentTask respondentTask, CaseData caseData) {
        final List<String> lines = new LinkedList<>();
        List<PartyDetails> respondents = caseData
            .getRespondents()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<YesOrNo> activeRespondents = respondents.stream()
            .map(element -> element.getResponse().getActiveRespondent())
            .collect(Collectors.toList());
        for (YesOrNo activeResp : activeRespondents) {
            log.info("Active respodent is present::? {}", activeResp);
            if (activeResp.equals(YesOrNo.Yes)) {
                log.info("--------Entering if loop.... ");
                lines.add(taskListRenderElements.renderRespondentSolicitorLink(respondentTask));
                respondentTask.getHint().map(taskListRenderElements::renderHint).ifPresent(lines::add);
            } else {
                log.info("--------Entering else loop.... ");
                lines.add(taskListRenderElements.renderRespondentSolicitorDisabledLink(respondentTask));
                respondentTask.getHint().map(taskListRenderElements::renderHint).ifPresent(lines::add);
            }
        }
        return lines;
    }
}
