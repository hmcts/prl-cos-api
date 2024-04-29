

package uk.gov.hmcts.reform.prl.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.models.tasklist.RespondentTask;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Component
public class TaskListRenderElements {

    public TaskListRenderElements(@Value("${resources.images.baseUrl}") String imagesBaseUrl) {
        this.imagesBaseUrl = imagesBaseUrl;
    }

    private final String imagesBaseUrl;

    public String renderLink(Task task) {
        return renderLink(task.getEvent());
    }

    public String renderLink(Event event) {
        return format("<a href='/cases/case-details/${[CASE_REFERENCE]}/trigger/%s/%s1'>%s</a>",
                      event.getId(), event.getId(), event.getName());
    }

    public String renderRespondentSolicitorLink(RespondentTask respondentTask, String respondent) {
        return renderRespondentSolicitorLink(respondentTask.getEvent(), respondent);
    }

    public String renderRespondentSolicitorLink(RespondentSolicitorEvents respondentSolicitorEvents, String respondent) {
        return format("<a href='/cases/case-details/${[CASE_REFERENCE]}/trigger/%s/%s1'>%s</a>",
                      respondentSolicitorEvents.getEventId() + respondent,
                      respondentSolicitorEvents.getEventId() + respondent,
                      respondentSolicitorEvents.getEventName());
    }

    public String renderImage(String imageName, String title) {
        return format("<img align='right' height='25px' src='%s%s' title='%s'/>", imagesBaseUrl, imageName, title);
    }

    public String renderDisabledLink(Task event) {
        return format("%s", event.getEvent().getName());
    }

    public String renderRespondentDisabledLink(RespondentTask respondentTask) {
        return format("%s", respondentTask.getEvent().getEventName());
    }

    public String renderHint(String text) {
        return format("<span class='govuk-hint govuk-!-font-size-14'>%s</span>", text);
    }

    public String renderInfo(String text) {
        return format("<div class='panel panel-border-wide govuk-!-font-size-16'>%s</div>", text);
    }

    public String renderHeader(String text) {
        return format("## %s", text);
    }

    public List<String> renderCollapsible(String header, List<String> lines) {
        final List<String> collapsible = new ArrayList<>();
        collapsible.add("<details class='govuk-details'>");
        collapsible.add("<summary class='govuk-details__summary'>");
        collapsible.add("<span class='govuk-details__summary-text'>");
        collapsible.add(header);
        collapsible.add("</span>");
        collapsible.add("</summary>");
        collapsible.add("<div class='govuk-details__text'>");
        collapsible.addAll(lines);
        collapsible.add("</div>");
        collapsible.add("</details>");

        return collapsible;
    }

    public List<String> renderNestedCollapsible(String header, List<String> lines) {
        final List<String> nestedCollapsible = new ArrayList<>();
        nestedCollapsible.add("<details class='govuk-details'>");
        nestedCollapsible.add("<summary class='govuk-details__summary'>");
        nestedCollapsible.add("<span class='govuk-details__summary-text'>");
        nestedCollapsible.add(header);
        nestedCollapsible.add("</span>");
        nestedCollapsible.add("</summary>");
        nestedCollapsible.add("<div class='govuk-details__text'>");
        nestedCollapsible.addAll(lines);
        nestedCollapsible.add("</div>");
        nestedCollapsible.add("</details>");

        return nestedCollapsible;
    }

}
