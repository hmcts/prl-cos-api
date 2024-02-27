package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndApplicantRelation;
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
public class ChildrenAndApplicantsMapper {


    public JsonArray map(List<Element<ChildrenAndApplicantRelation>> childrenAndApplicantRelations) {
        Optional<List<Element<ChildrenAndApplicantRelation>>> childrenAndApplicantRelationCheck = ofNullable(childrenAndApplicantRelations);
        if (childrenAndApplicantRelationCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<ChildrenAndApplicantRelation> childrenAndApplicantList = childrenAndApplicantRelations.stream()
            .map(Element::getValue)
            .toList();
        return childrenAndApplicantList.stream().map(relation -> new NullAwareJsonObjectBuilder()
            .add("applicantFullName", relation.getApplicantFullName())
            .add("childFullName", relation.getChildFullName())
            .add("childAndApplicantRelation", relation.getChildAndApplicantRelation().getDisplayedValue())
            .add("childAndApplicantRelationOtherDetails", relation.getChildAndApplicantRelationOtherDetails())
            .add("childLivesWith", CommonUtils.getYesOrNoValue(relation.getChildLivesWith()))
            .build()).collect(JsonCollectors.toJsonArray());
    }

}
