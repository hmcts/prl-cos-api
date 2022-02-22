package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import javax.json.JsonArray;
import javax.json.stream.JsonCollectors;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RespondentsMapper {

    private final AddressMapper addressMapper;

    public JsonArray map(List<Element<PartyDetails>> respondents) {
        Optional<List<Element<PartyDetails>>> respondentElementsCheck = ofNullable(respondents);
        if(respondentElementsCheck.isEmpty()){
            return null;
        }
        List<PartyDetails> respondentList = respondents.stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        return respondentList.stream().map(respondent -> new NullAwareJsonObjectBuilder()
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
            .add("canYouProvidePhoneNumber", CommonUtils.getYesOrNoValue(respondent.getCanYouProvidePhoneNumber()))
            .add("phoneNumber", respondent.getPhoneNumber())
            .add("canYouProvideEmailAddress", CommonUtils.getYesOrNoValue(respondent.getCanYouProvideEmailAddress()))
            .add("email", respondent.getEmail())
            .add("doTheyHaveLegalRepresentation", CommonUtils.getYesOrNoDontKnowValue(respondent.getDoTheyHaveLegalRepresentation()))
            .add("solicitorOrganisationID", respondent.getSolicitorOrg().getOrganisationID())
            .add("isAtAddressLessThan5YearsWithDontKnow",
                 CommonUtils.getYesOrNoDontKnowValue(respondent.getIsAtAddressLessThan5YearsWithDontKnow()))
            .add("addressLivedLessThan5YearsDetails", respondent.getAddressLivedLessThan5YearsDetails())
            .add("solicitorID",CommonUtils.getSolicitorId(respondent))
            .add("dxNumber", respondent.getDxNumber())
            .build()).collect(JsonCollectors.toJsonArray());
    }

}
