package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.stream.JsonCollectors;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChildDetailsRevisedMapper {


    public JsonArray map(List<Element<ChildDetailsRevised>> children) {
        Optional<List<Element<ChildDetailsRevised>>> childElementsCheck = ofNullable(children);
        if (childElementsCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<ChildDetailsRevised> childList = children.stream()
            .map(Element::getValue)
            .toList();
        return childList.stream().map(child -> new NullAwareJsonObjectBuilder()
            .add("firstName", child.getFirstName())
            .add("lastName", child.getLastName())
            .add("dateOfBirth", String.valueOf(child.getDateOfBirth()))
            .add("gender", child.getGender().getDisplayedValue())
            .add("otherGender", child.getOtherGender())
            .add("orderAppliedFor", child.getOrderAppliedFor().isEmpty()
                ? null : child.getOrderAppliedFor().stream()
                .map(OrderTypeEnum::getDisplayedValue).collect(Collectors.joining(", ")))
            .add("parentalResponsibilityDetails", child.getParentalResponsibilityDetails())
            .build()).collect(JsonCollectors.toJsonArray());
    }

}
