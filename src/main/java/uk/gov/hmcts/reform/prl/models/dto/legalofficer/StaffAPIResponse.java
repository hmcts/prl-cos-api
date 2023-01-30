package uk.gov.hmcts.reform.prl.models.dto.legalofficer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StaffAPIResponse {

    @JsonProperty(value = "staffProfile")
    private List<StaffProfile> staffProfile;



}
