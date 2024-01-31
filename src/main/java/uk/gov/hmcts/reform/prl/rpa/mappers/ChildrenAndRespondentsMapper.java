package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;
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
public class ChildrenAndRespondentsMapper {


    public JsonArray map(List<Element<ChildrenAndRespondentRelation>> childrenAndRespondentRelations) {
        Optional<List<Element<ChildrenAndRespondentRelation>>> childrenAndRespondentRelationCheck = ofNullable(childrenAndRespondentRelations);
        if (childrenAndRespondentRelationCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<ChildrenAndRespondentRelation> childrenAndRespondentList = childrenAndRespondentRelations.stream()
                .map(Element::getValue)
                .toList();
        return childrenAndRespondentList.stream().map(relation -> new NullAwareJsonObjectBuilder()
                .add("respondentFullName", relation.getRespondentFullName())
                .add("childFullName", relation.getChildFullName())
                .add("childAndRespondentRelation", relation.getChildAndRespondentRelation().getDisplayedValue())
                .add("childAndRespondentRelationOtherDetails", relation.getChildAndRespondentRelationOtherDetails())
                .add("childLivesWith", CommonUtils.getYesOrNoValue(relation.getChildLivesWith()))
                .build()).collect(JsonCollectors.toJsonArray());
    }

}
