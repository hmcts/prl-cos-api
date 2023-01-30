package uk.gov.hmcts.reform.prl.models.dto.legalofficer;

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
public class StaffApiResponse {

    @JsonProperty(value = "staffProfile")
    private List<StaffProfile> staffProfile;



}
