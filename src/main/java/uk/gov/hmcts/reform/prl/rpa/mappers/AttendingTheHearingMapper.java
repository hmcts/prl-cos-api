package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.InterpreterNeed;
import uk.gov.hmcts.reform.prl.models.complextypes.WelshNeed;
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

@Component
public class AttendingTheHearingMapper {

    public JsonObject map(CaseData caseData) {

        return new NullAwareJsonObjectBuilder()
            .add("isWelshNeeded", CommonUtils.getYesOrNoValue(caseData.getIsWelshNeeded()))
            .add("welshNeeds", mapWelshNeeds(caseData.getWelshNeeds()))
            .add("isInterpreterNeeded", CommonUtils.getYesOrNoValue(caseData.getIsInterpreterNeeded()))
            .add("interpreterNeeds", mapInterpreterNeeds(caseData.getInterpreterNeeds()))
            .add("isDisabilityPresent", CommonUtils.getYesOrNoValue(caseData.getIsDisabilityPresent()))
            .add("adjustmentsRequired", caseData.getAdjustmentsRequired())
            .add(
                "isSpecialArrangementsRequired",
                CommonUtils.getYesOrNoValue(caseData.getIsSpecialArrangementsRequired())
            )
            .add("specialArrangementsRequired", caseData.getSpecialArrangementsRequired())
            .add("isIntermediaryNeeded", CommonUtils.getYesOrNoValue(caseData.getIsIntermediaryNeeded()))
            .build();
    }

    private JsonArray mapInterpreterNeeds(List<Element<InterpreterNeed>> interpreterNeeds) {

        Optional<List<Element<InterpreterNeed>>> interpreterNeedsElementCheck = Optional.ofNullable(interpreterNeeds);
        if (interpreterNeedsElementCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<InterpreterNeed> interpreterNeedsList = interpreterNeeds.stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        return interpreterNeedsList.stream().map(interpreterNeed -> new NullAwareJsonObjectBuilder()
            .add("name", interpreterNeed.getName())
            .add("party", interpreterNeed.getParty().isEmpty() ? null : interpreterNeed.getParty().stream()
                .map(PartyEnum::getDisplayedValue).collect(Collectors.joining(", ")))
            .add("language", interpreterNeed.getLanguage())
            .build()).collect(JsonCollectors.toJsonArray());

    }

    private JsonArray mapWelshNeeds(List<Element<WelshNeed>> welshNeeds) {
        Optional<List<Element<WelshNeed>>> welshNeedsElementCheck = Optional.ofNullable(welshNeeds);
        if (welshNeedsElementCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<WelshNeed> welshNeedsList = welshNeeds.stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        return welshNeedsList.stream().map(welshNeed -> new NullAwareJsonObjectBuilder()
            .add("whoNeedsWelsh", welshNeed.getWhoNeedsWelsh())
            .add(
                "spokenOrWritten",
                welshNeed.getSpokenOrWritten().isEmpty() ? null : welshNeed.getSpokenOrWritten().stream()
                    .map(SpokenOrWrittenWelshEnum::getDisplayedValue).collect(Collectors.joining(", "))
            )
            .build()).collect(JsonCollectors.toJsonArray());
    }
}
