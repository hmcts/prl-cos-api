package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RespondentSolicitorMiamService {

    private static final String PULTAG = "</p><ul>";
    private static final String ULPPTAG = "</ul><p></p>";
    private static final String SUMMARYCLASS = "<summary class=\"govuk-details__summary\">";
    private static final String SPANCLASS = "<span class=\"govuk-details__summary-text\">>";
    private static final String SPAN = "</span>";
    private static final String DIV = "</div>";
    private static final String DETAILS = "</details>";
    private static final String SUMMARY = "</summary>";
    private static final String DIVCLASS = "<div class=\"govuk-details__text\">";

    public String getCollapsableOfWhatIsMiamPlaceHolder() {
        final List<String> collapsible = new ArrayList<>();

        collapsible.add("<details class=\"govuk-details\">");

        collapsible.add(SUMMARYCLASS);
        collapsible.add(SPANCLASS);
        collapsible.add("What is a Mediation Information and Assessment Meeting (MIAM)?");
        collapsible.add(SPAN);
        collapsible.add(SUMMARY);

        collapsible.add(DIVCLASS);
        collapsible.add("<p>A MIAM is a first meeting with a mediator.</p>");
        collapsible.add("<p>The MIAM will:");
        collapsible.add(PULTAG);
        collapsible.add("<li>last about an hour</li>");
        collapsible.add("<li>give you the opportunity to tell the mediator about your situation, and the issues to be decided</li>");
        collapsible.add("<li>explain the mediation process, and other options for reaching agreements</li>");
        collapsible.add(ULPPTAG);
        collapsible.add("<p>At the end of the meeting, the mediator will tell you if your case is suitable for mediation. "
                            + "You can then decide if you want to proceed, or explore other options.</p>");
        collapsible.add("<p>The benefits of a MIAM are:");
        collapsible.add(PULTAG);
        collapsible.add("<li>less stress and a faster process than going to court</li>");
        collapsible.add("<li>help with making arrangements over parenting, property and money</li>");
        collapsible.add("<li>more control over your family's future</li>");
        collapsible.add("<li>help with putting your childrens' interests first</li>");
        collapsible.add("<li>help with moving on to the next stage of your lives</li>");
        collapsible.add(ULPPTAG);
        collapsible.add("<p>The MIAM process works, with agreement reached in over 70% of cases.</p>");
        collapsible.add(DIV);

        collapsible.add(DETAILS);

        return String.join("\n\n", collapsible);
    }

    public String getCollapsableOfWhatIsMiamPlaceHolderWelsh() {
        return String.join("\n\n", List.of(
            "<details class=\"govuk-details\">",
            SUMMARYCLASS, SPANCLASS,
            "What is a Mediation Information and Assessment Meeting (MIAM)? - welsh",
            SPAN, SUMMARY, DIVCLASS,
            "<p>A MIAM is a first meeting with a mediator. - welsh</p>",
            "<p>The MIAM will: - welsh",
            PULTAG,
            "<li>last about an hour - welsh</li>",
            "<li>give you the opportunity to tell the mediator about your situation, and the issues to be decided - welsh</li>",
            "<li>explain the mediation process, and other options for reaching agreements - welsh</li>",
            ULPPTAG,
            "<p>At the end of the meeting, the mediator will tell you if your case is suitable for mediation."
                + "You can then decide if you want to proceed, or explore other options. - welsh</p>",
            "<p>The benefits of a MIAM are: - welsh",
            PULTAG,
            "<li>less stress and a faster process than going to court - welsh</li>",
            "<li>help with making arrangements over parenting, property and money - welsh</li>",
            "<li>more control over your family's future - welsh</li>",
            "<li>help with putting your childrens' interests first - welsh</li>",
            "<li>help with moving on to the next stage of your lives - welsh</li>",
            ULPPTAG,
            "<p>The MIAM process works, with agreement reached in over 70% of cases. - welsh</p>",
            DIV, DETAILS
        ));
    }

    public String getCollapsableOfHelpMiamCostsExemptionsPlaceHolder() {
        final List<String> collapsibleWillingnessToAttendMiam = new ArrayList<>();

        collapsibleWillingnessToAttendMiam.add("<details class=\"govuk-details\" data-module=\"govuk-details\">");
        collapsibleWillingnessToAttendMiam.add(SUMMARYCLASS);
        collapsibleWillingnessToAttendMiam.add(SPANCLASS);
        collapsibleWillingnessToAttendMiam.add("Help with MIAM costs and exemptions");
        collapsibleWillingnessToAttendMiam.add(SPAN);
        collapsibleWillingnessToAttendMiam.add(SUMMARY);
        collapsibleWillingnessToAttendMiam.add(DIVCLASS);
        collapsibleWillingnessToAttendMiam.add("<p>The price of mediation will vary depending on:");
        collapsibleWillingnessToAttendMiam.add(PULTAG);
        collapsibleWillingnessToAttendMiam.add("<li>where you live</li>");
        collapsibleWillingnessToAttendMiam.add("<li>how many sessions you attend</li>");
        collapsibleWillingnessToAttendMiam.add("<li>the type of issues you need to discuss</li>");
        collapsibleWillingnessToAttendMiam.add(ULPPTAG);
        collapsibleWillingnessToAttendMiam.add("<p>You may be able to get legal aid if you are on a low income.</p>");
        collapsibleWillingnessToAttendMiam.add("<h3 class=\"govuk-heading-s\">Family Mediation Voucher Scheme</h3>");
        collapsibleWillingnessToAttendMiam.add("<p>You may be eligible for the Family Mediation Voucher Scheme. This is a government scheme "
            + "that provides up to £500 towards the cost of mediation for eligible cases.</p>");
        collapsibleWillingnessToAttendMiam.add("<p>At your MIAM, your mediator will explain the voucher scheme and you may be offered a "
            + "voucher if your case meets the scheme's criteria. Not all cases qualify for a voucher.</p>");
        collapsibleWillingnessToAttendMiam.add("<p>See the <a href=\"https://www.gov.uk\" class=\"govuk-link\""
            + "target=\"_blank\">GOV.UK</a> guidance on the <a href=\"https://www.gov.uk/guidance/family-mediation-voucher-scheme\" "
            + "class=\"govuk-link\" target=\"_blank\">Family Mediation Voucher Scheme</a> for more information.</p>");
        collapsibleWillingnessToAttendMiam.add("<p>The MIAM process works, with agreement reached in over 70% of cases.</p>");
        collapsibleWillingnessToAttendMiam.add("<h3 class=\"govuk-heading-s\">Exemptions</h3>");
        collapsibleWillingnessToAttendMiam.add("<p>You don't need to attend a MIAM if you have a valid reason. For example, "
            + "you or the children are at risk of harm.</p>");
        collapsibleWillingnessToAttendMiam.add("<p>Find out more about <a href=\"https://apply-to-court-about-child-arrangements.service.justice.gov.uk/about/miam_exemptions\" "
            + "class=\"govuk-link\" target=\"_blank\">the reasons for a MIAM exemption</a>.</p>");
        collapsibleWillingnessToAttendMiam.add(DIV);
        collapsibleWillingnessToAttendMiam.add(DETAILS);

        return String.join("\n\n", collapsibleWillingnessToAttendMiam);
    }

    public String getCollapsableOfHelpMiamCostsExemptionsPlaceHolderWelsh() {
        return String.join("\n\n", List.of(
            "<details class=\"govuk-details\" data-module=\"govuk-details\">",
            SUMMARYCLASS, SPANCLASS,
            "Help with MIAM costs and exemptions - welsh",
            SPAN, SUMMARY, DIVCLASS,
            "<p>The price of mediation will vary depending on: - welsh</p>",
            PULTAG,
            "<li>where you live - welsh</li>",
            "<li>how many sessions you attend - welsh</li>",
            "<li>the type of issues you need to discuss - welsh</li>",
            ULPPTAG,
            "<p>You may be able to get legal aid if you are on a low income. - welsh</p>",
            "<h3 class=\"govuk-heading-s\">Family Mediation Voucher Scheme - welsh</h3>",
            "<p>You may be eligible for the Family Mediation Voucher Scheme. This is a government scheme "
                + "that provides up to £500 towards the cost of mediation for eligible cases. - welsh</p>",
            "<p>At your MIAM, your mediator will explain the voucher scheme and you may be offered a "
                + "voucher if your case meets the scheme's criteria. Not all cases qualify for a voucher. - welsh</p>",
            "<p>See the <a href=\"https://www.gov.uk\" class=\"govuk-link\""
                + "target=\"_blank\">GOV.UK</a> guidance on the <a href=\"https://www.gov.uk/guidance/family-mediation-voucher-scheme\" "
                + "class=\"govuk-link\" target=\"_blank\">Family Mediation Voucher Scheme</a> for more information. - welsh</p>",
            "<p>The MIAM process works, with agreement reached in over 70% of cases. - welsh</p>",
            "<h3 class=\"govuk-heading-s\">Exemptions - welsh</h3>",
            "<p>You don't need to attend a MIAM if you have a valid reason. For example, "
                + "you or the children are at risk of harm. - welsh</p>",
            "<p>Find out more about <a href=\"https://apply-to-court-about-child-arrangements.service.justice.gov.uk/about/miam_exemptions\" "
                + "class=\"govuk-link\" target=\"_blank\">the reasons for a MIAM exemption</a>. - welsh</p>",
            DIV, DETAILS
        ));
    }

}
