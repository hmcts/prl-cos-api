package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertTrue;

public class TaskListRenderElementsTest {

    private static final String BASE_URL = "baseUrl/";

    private TaskListRenderElements underTest = new TaskListRenderElements(BASE_URL);

    @Test
    public void shouldRenderLink() {
        String actual = underTest.renderLink(Task.builder()
                                                 .event(Event.CASE_NAME)
                                                 .build());

        assert (actual).equalsIgnoreCase("<a href='/cases/case-details/${[CASE_REFERENCE]}/trigger/caseName/caseName1'>Case name"
                                         + "</a>");
    }

    @Test
    public void shouldRenderDisabledLink() {
        String actual = underTest.renderDisabledLink(Task.builder()
                                                         .event(Event.CASE_NAME)
                                                         .build());
        assert (actual).equalsIgnoreCase("<a>Case name</a>");
    }

    @Test
    public void shouldRenderHint() {
        String actual = underTest.renderHint("Hint");

        assert (actual).equalsIgnoreCase("<span class='govuk-hint govuk-!-font-size-14'>Hint</span>");
    }

    @Test
    public void shouldRenderInfo() {
        String actual = underTest.renderInfo("Info");

        assert (actual).equalsIgnoreCase("<div class='panel panel-border-wide govuk-!-font-size-16'>Info</div>");
    }

    @Test
    public void shouldRenderHeader() {
        String actual = underTest.renderHeader("Header");

        assert (actual).equalsIgnoreCase("## Header");
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

        List<String> actual = underTest.renderCollapsible("Header",emptyList());

        assertTrue(actual.containsAll(expected));

    }
}
