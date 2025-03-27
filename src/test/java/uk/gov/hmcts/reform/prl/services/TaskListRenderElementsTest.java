package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.models.tasklist.RespondentTask;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TaskListRenderElementsTest {

    private static final String BASE_URL = "baseUrl/";

    private TaskListRenderElements underTest = new TaskListRenderElements(BASE_URL);

    @Test
    public void shouldRenderLink() {
        String actual = underTest.renderLink(Task.builder()
                                                 .event(Event.CASE_NAME)
                                                 .build());

        assertThat(actual).isEqualToIgnoringCase(
            "<a href='/cases/case-details/${[CASE_REFERENCE]}/trigger/caseName/caseName1'>Case name"
                + "</a>");
    }

    @Test
    public void shouldRenderDisabledLink() {
        String actual = underTest.renderDisabledLink(Task.builder()
                                                         .event(Event.CASE_NAME)
                                                         .build());
        assertThat(actual).isEqualToIgnoringCase("Case name");
    }

    @Test
    public void shouldRenderHint() {
        String actual = underTest.renderHint("Hint");

        assertThat(actual).isEqualToIgnoringCase("<span class='govuk-hint govuk-!-font-size-14'>Hint</span>");
    }

    @Test
    public void shouldRenderInfo() {
        String actual = underTest.renderInfo("Info");

        assertThat(actual).isEqualToIgnoringCase("<div class='panel panel-border-wide govuk-!-font-size-16'>Info</div>");
    }

    @Test
    public void shouldRenderHeader() {
        String actual = underTest.renderHeader("Header");

        assertThat(actual).isEqualToIgnoringCase("## Header");
    }

    @Test
    public void shouldRenderCollapsable() {
        List<String> expected = new ArrayList<>();
        expected.add("<details class='govuk-details'>");
        expected.add("<summary class='govuk-details__summary'>");
        expected.add("<span class='govuk-details__summary-text'>");
        expected.add("Header");
        expected.add("</span>");
        expected.add("</summary>");
        expected.add("<div class='govuk-details__text'>");
        expected.add("</div>");
        expected.add("</details>");

        List<String> actual = underTest.renderCollapsible("Header", emptyList());

        assertTrue(actual.containsAll(expected));
    }

    @Test
    public void shouldRenderNestedCollapsable() {
        List<String> expected = new ArrayList<>();
        expected.add("<details class='govuk-details'>");
        expected.add("<summary class='govuk-details__summary'>");
        expected.add("<span class='govuk-details__summary-text'>");
        expected.add("Header");
        expected.add("</span>");
        expected.add("</summary>");
        expected.add("<div class='govuk-details__text'>");
        expected.add("</div>");
        expected.add("</details>");

        List<String> actual = underTest.renderNestedCollapsible("Header", emptyList());

        assertTrue(actual.containsAll(expected));
    }

    @Test
    public void testRenderRespondentSolicitorLink() {
        RespondentTask respondentTask = RespondentTask.builder()
            .event(RespondentSolicitorEvents.CONSENT)
            .build();
        String actual = underTest.renderRespondentSolicitorLink(respondentTask, "A");

        assertEquals("<a href='/cases/case-details/${[CASE_REFERENCE]}/trigger/c100ResSolConsentingToApplicationA/c100ResSolConsentingToApplicationA1'>Do you give your consent?</a>", actual);
    }
}
