package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonRelationshipToChild;

import java.time.LocalDate;
import java.util.List;

@Builder(toBuilder = true)
@Data
public class OtherPersonInTheCase {

    private final String firstName;
    private final String lastName;
    private final String previousName;
    private final YesOrNo isDateOfBirthKnown;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    private final String gender;
    private final String otherGender;
    private final YesOrNo isPlaceOfBirthKnown;
    private final String placeOfBirth;
    private final YesOrNo isCurrentAddressKnown;
    private final Address address;
    private final YesOrNo canYouProvideEmailAddress;
    private final String email;
    private final YesOrNo canYouProvidePhoneNumber;
    private final String phoneNumber;
    private final List<Element<OtherPersonRelationshipToChild>> relationshipToChild;


}
