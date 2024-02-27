package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherPersonInTheCase {

    private String firstName;
    private String lastName;
    private String previousName;
    private YesOrNo isDateOfBirthKnown;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private String gender;
    private String otherGender;
    private YesOrNo isPlaceOfBirthKnown;
    private String placeOfBirth;
    private YesOrNo isCurrentAddressKnown;
    private Address address;
    private YesOrNo canYouProvideEmailAddress;
    private String email;
    private YesOrNo canYouProvidePhoneNumber;
    private String phoneNumber;

}
