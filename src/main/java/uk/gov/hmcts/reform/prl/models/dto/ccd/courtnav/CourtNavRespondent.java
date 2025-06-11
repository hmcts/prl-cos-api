package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtNavRespondent {

    @JsonProperty("respondentFirstName")
    private String firstName;

    @JsonProperty("respondentLastName")
    private String lastName;

    @JsonProperty("respondentOtherNames")
    private String previousName;

    @JsonProperty("respondentDateOfBirth")
    private CourtNavDate dateOfBirth;

    @JsonProperty("respondentPhoneNumber")
    private String phoneNumber;

    @JsonProperty("respondentEmailAddress")
    private String email;

    @JsonProperty("respondentAddress")
    private CourtNavAddress address;

    @JsonProperty("respondentLivesWithApplicant")
    private boolean respondentLivesWithApplicant;
}
