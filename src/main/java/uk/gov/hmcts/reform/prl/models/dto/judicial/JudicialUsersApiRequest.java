package uk.gov.hmcts.reform.prl.models.dto.judicial;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/* we can make use of this request class by passing only one request parameter
in the request at a time */

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JudicialUsersApiRequest {
    @JsonProperty("ccdServiceName")
    private final String ccdServiceName;

    @JsonProperty("personal_code")
    private final String[] personalCode;

    @JsonProperty("sidam_ids")
    private final String[] sidamIds;

}
