package uk.gov.hmcts.reform.prl.models.dto.judicial;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JudicialUsersAPIResponse {
    @JsonProperty("email_id")
    private final String email_id;

    @JsonProperty("full_name")
    private final String full_name;

    @JsonProperty("known_as")
    private final String known_as;

    @JsonProperty("personal_code")
    private final String personal_code;

    @JsonProperty("post_nominals")
    private final String post_nominals;

    @JsonProperty("sidam_id")
    private final String sidam_id;

    @JsonProperty("surname")
    private final String surname;
}
