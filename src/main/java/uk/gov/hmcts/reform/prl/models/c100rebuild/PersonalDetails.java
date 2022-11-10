package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonalDetails {

    private DateofBirth dateOfBirth;
    private String isDateOfBirthUnknown;
    private DateofBirth approxDateOfBirth;
    private String gender;
    private String otherGenderDetails;
    private String hasNameChanged;
    private String resPreviousName;
    private String respondentPlaceOfBirth;
    private String respondentPlaceOfBirthUnknown;
    private String isNameChanged;
    private String previousFullName;
    private String applicantPlaceOfBirth;
}
