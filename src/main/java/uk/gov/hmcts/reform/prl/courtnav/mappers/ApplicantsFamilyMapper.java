package uk.gov.hmcts.reform.prl.courtnav.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ProtectedChild;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonCollectors;

import static java.util.Optional.ofNullable;

@Component
public class ApplicantsFamilyMapper {

    private JsonObject map(CourtNavCaseData courtNavCaseData) {

        return new NullAwareJsonObjectBuilder()
            .add("doesApplicantHaveChildren", CommonUtils.getYesOrNoValue(courtNavCaseData.getWhoApplicationIsFor()))
            .add("applicantChildDetails", courtNavCaseData.getWhoApplicationIsFor().getDisplayedValue().equals("Yes")
                ? mapChildren(courtNavCaseData.getProtectedChildren()) : null)
            .build();
    }

    private JsonArray mapChildren(List<Element<ProtectedChild>> protectedChild) {
        Optional<List<Element<ProtectedChild>>> childElementsCheck = ofNullable(protectedChild);
        if (childElementsCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<ProtectedChild> childList = protectedChild.stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        return childList.stream().map(child -> new NullAwareJsonObjectBuilder()
            .add("fullName",  child.getFullName())
            .add("dateOfBirth", String.valueOf(child.getDateOfBirth()))
            .add("applicantChildRelationship", child.getRelationship())
            .add("applicantRespondentShareParental", CommonUtils.getYesOrNoValue(child.getParentalResponsibility()))
            .add("respondentChildRelationship", child.getRespondentRelationship())
            .build()).collect(JsonCollectors.toJsonArray());
    }

}
