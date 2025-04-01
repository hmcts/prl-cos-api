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
@Builder(builderClassName = "Builder", toBuilder = true)
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

    public static class Builder {
        private static String clean(String value) {
            return (value != null && !value.trim().isEmpty()) ? value : null;
        }

        public Builder firstName(String firstName) {
            this.firstName = clean(firstName);
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = clean(lastName);
            return this;
        }

        public Builder previousName(String previousName) {
            this.previousName = clean(previousName);
            return this;
        }

        public Builder gender(String gender) {
            this.gender = clean(gender);
            return this;
        }

        public Builder otherGender(String otherGender) {
            this.otherGender = clean(otherGender);
            return this;
        }

        public Builder placeOfBirth(String placeOfBirth) {
            this.placeOfBirth = clean(placeOfBirth);
            return this;
        }

        public Builder email(String email) {
            this.email = clean(email);
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = clean(phoneNumber);
            return this;
        }
    }
}
