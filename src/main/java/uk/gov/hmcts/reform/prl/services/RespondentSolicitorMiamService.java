package uk.gov.hmcts.reform.prl.services;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RespondentSolicitorMiamService {

    public String getCollapsableOfWhatIsMiamPlaceHolder() {
        final List<String> collapsible = new ArrayList<>();
        collapsible.add("<div class=\"govuk-details__text\">");
        collapsible.add("<p>A MIAM is a first meeting with a mediator.</p>");
        collapsible.add("<p>The MIAM will:");
        collapsible.add("</p><ul>");
        collapsible.add("<li>last about an hour</li>");
        collapsible.add("<li>give you the opportunity to tell the mediator about your situation, and the issues to be decided</li>");
        collapsible.add("<li>explain the mediation process, and other options for reaching agreements</li>");
        collapsible.add("</ul><p></p>");
        collapsible.add("<p>At the end of the meeting, the mediator will tell you if your case is suitable for mediation. "
                            + "You can then decide if you want to proceed, or explore other options.</p>");
        collapsible.add("<p>The benefits of a MIAM are:");
        collapsible.add("</p><ul>");
        collapsible.add("<li>less stress and a faster process than going to court</li>");
        collapsible.add("<li>help with making arrangements over parenting, property and money</li>");
        collapsible.add("<li>more control over your family's future</li>");
        collapsible.add("<li>help with putting your childrens' interests first</li>");
        collapsible.add("<li>help with moving on to the next stage of your lives</li>");
        collapsible.add("</ul><p></p>");
        collapsible.add("<p>The MIAM process works, with agreement reached in over 70% of cases.</p>");
        collapsible.add("</div>");
        return String.join("\n\n", collapsible);
    }

}
