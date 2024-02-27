package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.stream.JsonCollectors;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChildrenMapper {

    private final AddressMapper addressMapper;

    public JsonArray map(List<Element<Child>> children) {
        Optional<List<Element<Child>>> childElementsCheck = ofNullable(children);
        if (childElementsCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<Child> childList = children.stream()
            .map(Element::getValue)
            .toList();
        return childList.stream().map(child -> new NullAwareJsonObjectBuilder()
            .add("firstName", child.getFirstName())
            .add("lastName", child.getLastName())
            .add("dateOfBirth", String.valueOf(child.getDateOfBirth()))
            .add("gender", child.getGender().getDisplayedValue())
            .add("otherGender", child.getOtherGender())
            .add("childLiveWith", child.getChildLiveWith().isEmpty() ? null : child.getChildLiveWith().stream()
                .map(LiveWithEnum::getDisplayedValue).collect(Collectors.joining(", ")))
            .add("orderAppliedFor", child.getOrderAppliedFor().isEmpty()
                ? null : child.getOrderAppliedFor().stream()
                .map(OrderTypeEnum::getDisplayedValue).collect(Collectors.joining(", ")))
            .add(
                "applicantsRelationshipToChild",
                child.getApplicantsRelationshipToChild() != null
                    ? child.getApplicantsRelationshipToChild().getDisplayedValue() : null
            )
            .add("parentalResponsibilityDetails", child.getParentalResponsibilityDetails())
            .add(
                "respondentsRelationshipToChild",
                child.getRespondentsRelationshipToChild() != null
                    ? child.getRespondentsRelationshipToChild().getDisplayedValue() : null
            )
            .add("otherApplicantsRelationshipToChild", child.getOtherApplicantsRelationshipToChild())
            .add("otherRespondentsRelationshipToChild", child.getOtherRespondentsRelationshipToChild())
            .add("personWhoLivesWithChild", mapOtherPerson(child.getPersonWhoLivesWithChild()))
            .build()).collect(JsonCollectors.toJsonArray());
    }

    private JsonArray mapOtherPerson(List<Element<OtherPersonWhoLivesWithChild>> personWhoLivesWithChild) {
        Optional<List<Element<OtherPersonWhoLivesWithChild>>> childElementsCheck = ofNullable(personWhoLivesWithChild);
        if (childElementsCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<OtherPersonWhoLivesWithChild> otherPersonList = personWhoLivesWithChild.stream()
            .map(Element::getValue)
            .toList();
        return otherPersonList.stream().map(otherPerson -> new NullAwareJsonObjectBuilder()
            .add("firstName", otherPerson.getFirstName())
            .add("lastName", otherPerson.getLastName())
            .add("relationshipToChildDetails", otherPerson.getRelationshipToChildDetails())
            .add(
                "isPersonIdentityConfidential",
                CommonUtils.getYesOrNoValue(otherPerson.getIsPersonIdentityConfidential())
            )
            .add(
                "address",
                addressMapper.mapAddress(otherPerson.getAddress())
            ).build()).collect(JsonCollectors.toJsonArray());
    }

}
