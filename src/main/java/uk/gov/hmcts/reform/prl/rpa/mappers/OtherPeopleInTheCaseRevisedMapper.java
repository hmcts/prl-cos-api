package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
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
public class OtherPeopleInTheCaseRevisedMapper {

    private final AddressMapper addressMapper;

    public JsonArray map(List<Element<PartyDetails>> otherPeopleInTheCase) {
        Optional<List<Element<PartyDetails>>> otherPeopleInTheCaseElementCheck = ofNullable(otherPeopleInTheCase);
        if (otherPeopleInTheCaseElementCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<PartyDetails> otherPeopleInTheCaseList = otherPeopleInTheCase.stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        return otherPeopleInTheCaseList.stream().map(otherPeople -> new NullAwareJsonObjectBuilder()
            .add("firstName", otherPeople.getFirstName())
            .add("lastName", otherPeople.getLastName())
            .add("previousName", otherPeople.getPreviousName())
            .add("isDateOfBirthKnown", CommonUtils.getYesOrNoValue(otherPeople.getIsDateOfBirthKnown()))
            .add("dateOfBirth", String.valueOf(otherPeople.getDateOfBirth()))
            .add("gender", otherPeople.getGender().getDisplayedValue())
            .add("otherGender", otherPeople.getOtherGender())
            .add(
                "canYouProvideEmailAddress",
                CommonUtils.getYesOrNoValue(otherPeople.getCanYouProvideEmailAddress())
            )
            .add("email", otherPeople.getEmail())
            .add(
                "canYouProvidePhoneNumber",
                CommonUtils.getYesOrNoValue(otherPeople.getCanYouProvidePhoneNumber())
            )
            .add("phoneNumber", otherPeople.getPhoneNumber())
            .add("isPlaceOfBirthKnown", CommonUtils.getYesOrNoValue(otherPeople.getIsPlaceOfBirthKnown()))
            .add("placeOfBirth", otherPeople.getPlaceOfBirth())
            .add("isCurrentAddressKnown", CommonUtils.getYesOrNoValue(otherPeople.getIsCurrentAddressKnown()))
            .add("address", addressMapper.mapAddress(otherPeople.getAddress()))
            .add("isAddressConfidential", CommonUtils.getYesOrNoValue(otherPeople.getIsAddressConfidential()))
            .add("isPhoneNumberConfidential", CommonUtils.getYesOrNoValue(otherPeople.getIsPhoneNumberConfidential()))
            .add("isEmailAddressConfidential", CommonUtils.getYesOrNoValue(otherPeople.getIsEmailAddressConfidential()))

            .build()).collect(JsonCollectors.toJsonArray());
    }


}
