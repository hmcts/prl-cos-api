package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
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
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OtherProceedingsMapper {

    private final AddressMapper addressMapper;

    public JsonObject map(CaseData caseData) {

        return new NullAwareJsonObjectBuilder()
            .add(
                "previousOrOngoingProceedingsForChildren",
                CommonUtils.getYesOrNoDontKnowValue(caseData.getPreviousOrOngoingProceedingsForChildren())
            )
            .add("existingProceedings", mapExistingProceedings(caseData.getExistingProceedings()))
            .build();
    }

    public JsonArray mapExistingProceedings(List<Element<ProceedingDetails>> existingProceedings) {
        Optional<List<Element<ProceedingDetails>>> existingProceedingsElementCheck = ofNullable(existingProceedings);
        if (existingProceedingsElementCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<ProceedingDetails> otherPeopleInTheCaseList = existingProceedings.stream()
            .map(Element::getValue)
            .toList();
        return otherPeopleInTheCaseList.stream().map(proceedings -> new NullAwareJsonObjectBuilder()
            .add("dateEnded", String.valueOf(proceedings.getDateEnded()))
            .add("caseNumber", proceedings.getCaseNumber())
            .add("dateStarted", String.valueOf(proceedings.getDateStarted()))
            .add("nameOfCourt", proceedings.getNameOfCourt())
            .add("nameOfJudge", proceedings.getNameOfJudge())
            .add("typeOfOrder", proceedings.getTypeOfOrder().isEmpty() ? null :
                proceedings.getTypeOfOrder().stream()
                .map(TypeOfOrderEnum::getDisplayedValue).collect(Collectors.joining(", ")))
            .add("nameAndOffice", proceedings.getNameAndOffice())
            .add("nameOfGuardian", proceedings.getNameOfGuardian())
            .add("otherTypeOfOrder", proceedings.getOtherTypeOfOrder())
            .add("nameOfChildrenInvolved", proceedings.getNameOfChildrenInvolved())
            .add(
                "previousOrOngoingProceedings",
                proceedings.getPreviousOrOngoingProceedings() != null
                    ? proceedings.getPreviousOrOngoingProceedings().getDisplayedValue() : null
            )
            .build()).collect(JsonCollectors.toJsonArray());
    }

}
