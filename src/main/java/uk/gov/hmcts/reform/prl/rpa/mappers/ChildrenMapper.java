package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.stream.JsonCollectors;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChildrenMapper {

    public JsonArray map(List<Element<Child>> children) {
        Optional<List<Element<Child>>> childElementsCheck = ofNullable(children);
        List<Child> childList = children.stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        return childList.stream().map(child -> new NullAwareJsonObjectBuilder()
            .add("id", child.getId())
            .add("firstName", child.getFirstName())
            .add("lastName", child.getLastName())
            .add("dateOfBirth", String.valueOf(child.getDateOfBirth()))
            .add("gender", child.getGender().getDisplayedValue())
            .add("otherGender", child.getOtherGender())
            .add("childLiveWith", String.valueOf(child.getChildLiveWith()))
            .add("orderAppliedFor", String.valueOf(child.getOrderAppliedFor()))
            .add("applicantsRelationshipToChild",child.getApplicantsRelationshipToChild().getDisplayedValue())
            .add("parentalResponsibilityDetails",child.getParentalResponsibilityDetails())
            .add("respondentsRelationshipToChild",child.getRespondentsRelationshipToChild().getDisplayedValue())
            .add("otherApplicantsRelationshipToChild",child.getOtherApplicantsRelationshipToChild())
            .add("otherRespondentsRelationshipToChild",child.getOtherRespondentsRelationshipToChild())
            //.add("personWhoLivesWithChild",String.valueOf(child.getPersonWhoLivesWithChild()))
            .build()).collect(JsonCollectors.toJsonArray());
    }

}
