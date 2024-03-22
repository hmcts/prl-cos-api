package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherChildrenNotInTheCase;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;

import java.util.List;
import java.util.Optional;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.stream.JsonCollectors;

import static java.util.Optional.ofNullable;

@Ignore
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OtherChildrenNotInTheCaseMapper {

    public JsonArray map(List<Element<OtherChildrenNotInTheCase>> elementList) {
        Optional<List<Element<OtherChildrenNotInTheCase>>> childElementsCheck = ofNullable(elementList);
        if (childElementsCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<OtherChildrenNotInTheCase> childList = elementList.stream()
            .map(Element::getValue)
            .toList();
        return childList.stream().map(child -> new NullAwareJsonObjectBuilder()
            .add("firstName", child.getFirstName())
            .add("lastName", child.getLastName())
            .add("isDateOfBirtKnown", String.valueOf(child.getIsDateOfBirthKnown()))
            .add("dateOfBirth", String.valueOf(child.getDateOfBirth()))
            .add("gender", child.getGender().getDisplayedValue())
            .add("otherGender", child.getOtherGender())
            .build()).collect(JsonCollectors.toJsonArray());
    }

}
