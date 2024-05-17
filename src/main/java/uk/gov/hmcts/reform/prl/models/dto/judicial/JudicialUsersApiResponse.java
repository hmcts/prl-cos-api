package uk.gov.hmcts.reform.prl.models.dto.judicial;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JudicialUsersApiResponse {
    @JsonProperty("email_id")
    private final String emailId;

    @JsonProperty("full_name")
    private final String fullName;

    @JsonProperty("known_as")
    private final String knownAs;

    @JsonProperty("personal_code")
    private final String personalCode;

    @JsonProperty("post_nominals")
    private final String postNominals;

    @JsonProperty("sidam_id")
    private final String sidamId;

    @JsonProperty("surname")
    private final String surname;

    //PRL-4403 - populate tier of judge
    @JsonProperty("appointments")
    private final List<Appointment> appointments;
}
