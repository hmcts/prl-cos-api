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
public class Appointment {

    @JsonProperty("base_location_id")
    private final String baseLocationId;

    @JsonProperty("cft_region_id")
    private final String courtRegionId;

    @JsonProperty("cft_region")
    private final String courtRegion;

    @JsonProperty("court_name")
    private final String courtName;

    //Tier of judge
    @JsonProperty("appointment")
    private final String appointment;

    @JsonProperty("roles")
    private final List<String> roles;

    //Add more fields later when required
}
