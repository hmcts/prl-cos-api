package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonCollectors;

import static java.util.Optional.ofNullable;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentsMapper {

    private final AddressMapper addressMapper;

    public JsonArray map(List<Element<PartyDetails>> respondents, Map<String, PartyDetails> respondentSolicitorMap) {
        Optional<List<Element<PartyDetails>>> respondentElementsCheck = ofNullable(respondents);
        if (respondentElementsCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<PartyDetails> respondentList = respondents.stream()
            .map(Element::getValue)
            .toList();
        AtomicInteger counter = new AtomicInteger(1);
        return respondentList.stream().map(respondent -> getRespondent(counter, respondent,
                                                                       respondentSolicitorMap
        )).collect(JsonCollectors.toJsonArray());
    }

    private JsonObject getRespondent(AtomicInteger counter, PartyDetails respondent,
                                     Map<String, PartyDetails> respondentSolicitorMap) {
        if (respondent.getDoTheyHaveLegalRepresentation().equals(YesNoDontKnow.yes)) {
            respondentSolicitorMap.put("RES_SOL_" + counter, respondent);
        }
        return new NullAwareJsonObjectBuilder()
            .add("firstName", respondent.getFirstName())
            .add("lastName", respondent.getLastName())
            .add("previousName", respondent.getPreviousName())
            .add("isDateOfBirthKnown", CommonUtils.getYesOrNoValue(respondent.getIsDateOfBirthKnown()))
            .add("dateOfBirth", String.valueOf(respondent.getDateOfBirth()))
            .add("gender", respondent.getGender().getDisplayedValue())
            .add("otherGender", respondent.getOtherGender())
            .add("placeOfBirth", respondent.getPlaceOfBirth())
            .add("isPlaceOfBirthKnown", CommonUtils.getYesOrNoValue(respondent.getIsPlaceOfBirthKnown()))
            .add("isCurrentAddressKnown", CommonUtils.getYesOrNoValue(respondent.getIsCurrentAddressKnown()))
            .add("address", addressMapper.mapAddress(respondent.getAddress()))
            .add("canYouProvidePhoneNumber",
                 CommonUtils.getYesOrNoValue(respondent.getCanYouProvidePhoneNumber()))
            .add("phoneNumber", respondent.getPhoneNumber())
            .add("canYouProvideEmailAddress",
                 CommonUtils.getYesOrNoValue(respondent.getCanYouProvideEmailAddress()))
            .add("email", respondent.getEmail())
            .add(
                "doTheyHaveLegalRepresentation",
                CommonUtils.getYesOrNoDontKnowValue(respondent.getDoTheyHaveLegalRepresentation())
            )
            .add("solicitorOrganisationID", respondent.getSolicitorOrg().getOrganisationID())
            .add(
                "isAtAddressLessThan5YearsWithDontKnow",
                CommonUtils.getYesOrNoDontKnowValue(respondent.getIsAtAddressLessThan5YearsWithDontKnow())
            )
            .add("addressLivedLessThan5YearsDetails", respondent.getAddressLivedLessThan5YearsDetails())
            .add(
                "solicitorID",
                respondent.getDoTheyHaveLegalRepresentation().equals(YesNoDontKnow.yes)
                    ? "RES_SOL_" + counter.getAndIncrement() : null
            )
            .add("dxNumber", respondent.getDxNumber())
            .build();
    }

}
