package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
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
public class ApplicantsMapper {

    private final AddressMapper addressMapper;

    public JsonArray map(List<Element<PartyDetails>> applicants, Map<String, PartyDetails> applicantSolicitorMap) {
        Optional<List<Element<PartyDetails>>> applicantElementsCheck = ofNullable(applicants);
        if (applicantElementsCheck.isEmpty()) {
            return JsonValue.EMPTY_JSON_ARRAY;
        }
        List<PartyDetails> applicantList = applicants.stream()
            .map(Element::getValue)
            .toList();
        AtomicInteger counter = new AtomicInteger(1);
        return applicantList.stream().map(applicant -> getApplicant(counter, applicant, applicantSolicitorMap)).collect(
            JsonCollectors.toJsonArray());
    }

    private JsonObject getApplicant(AtomicInteger counter, PartyDetails applicant, Map<String, PartyDetails> applicantSolicitorMap) {
        applicantSolicitorMap.put("APP_SOL_" + counter, applicant);
        return new NullAwareJsonObjectBuilder()
            .add("firstName", applicant.getFirstName())
            .add("lastName", applicant.getLastName())
            .add("previousName", applicant.getPreviousName())
            .add("dateOfBirth", String.valueOf(applicant.getDateOfBirth()))
            .add("gender", applicant.getGender().getDisplayedValue())
            .add("otherGender", applicant.getOtherGender())
            .add("email", applicant.getEmail())
            .add("phoneNumber", applicant.getPhoneNumber())
            .add("placeOfBirth", applicant.getPlaceOfBirth())
            .add("address", addressMapper.mapAddress(applicant.getAddress()))
            .add("isAtAddressLessThan5Years", CommonUtils.getYesOrNoValue(applicant.getIsAtAddressLessThan5Years()))
            .add("addressLivedLessThan5YearsDetails", applicant.getAddressLivedLessThan5YearsDetails())
            .add("isAddressConfidential", CommonUtils.getYesOrNoValue(applicant.getIsAddressConfidential()))
            .add("isPhoneNumberConfidential", CommonUtils.getYesOrNoValue(applicant.getIsPhoneNumberConfidential()))
            .add("isEmailAddressConfidential", CommonUtils.getYesOrNoValue(applicant.getIsEmailAddressConfidential()))
            .add("solicitorOrganisationID", applicant.getSolicitorOrg().getOrganisationID())
            .add("solicitorID", "APP_SOL_" + counter.getAndIncrement())
            .add("dxNumber", applicant.getDxNumber())
            .build();
    }


}
