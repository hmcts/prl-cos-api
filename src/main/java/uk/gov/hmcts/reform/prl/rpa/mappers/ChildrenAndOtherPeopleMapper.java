package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.List;
import java.util.Optional;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.stream.JsonCollectors;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChildrenAndOtherPeopleMapper {


    public JsonArray map(List<Element<ChildrenAndOtherPeopleRelation>> childrenAndOtherPeopleRelation) {
        Optional<List<Element<ChildrenAndOtherPeopleRelation>>> childrenAndOtherPeopleRelationChecker = ofNullable(childrenAndOtherPeopleRelation);
        if (childrenAndOtherPeopleRelationChecker.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<ChildrenAndOtherPeopleRelation> childrenAndApplicantList = childrenAndOtherPeopleRelation.stream()
            .map(Element::getValue)
            .toList();
        return childrenAndApplicantList.stream().map(relation -> new NullAwareJsonObjectBuilder()
            .add("otherPeopleFullName", relation.getOtherPeopleFullName())
            .add("childFullName", relation.getChildFullName())
            .add("childAndOtherPeopleRelation", relation.getChildAndOtherPeopleRelation().getDisplayedValue())
            .add("childAndOtherPeopleRelationOtherDetails", relation.getChildAndOtherPeopleRelationOtherDetails())
            .add("childLivesWith", CommonUtils.getYesOrNoValue(relation.getChildLivesWith()))
            .add("isChildLivesWithPersonConfidential", CommonUtils.getYesOrNoValue(relation.getIsChildLivesWithPersonConfidential()))
            .build()).collect(JsonCollectors.toJsonArray());
    }

}
