package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class CourtNavRespondent {

    @JsonProperty("respondentFirstName")
    private final String firstName;

    @JsonProperty("respondentLastName")
    private final String lastName;

    @JsonProperty("respondentOtherNames")
    private final String previousName;

    @JsonProperty("respondentDateOfBirth")
    private final CourtNavDate dateOfBirth;

    @JsonProperty("respondentPhoneNumber")
    private String phoneNumber;

    @JsonProperty("respondentEmailAddress")
    private String email;

    @JsonProperty("respondentAddress")
    private CourtNavAddress address;

    @JsonProperty("respondentLivesWithApplicant")
    private final boolean respondentLivesWithApplicant;
}
