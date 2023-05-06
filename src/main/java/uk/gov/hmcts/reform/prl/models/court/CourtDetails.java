package uk.gov.hmcts.reform.prl.models.court;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CourtDetails {

    @JsonProperty("service_code")
    private String serviceCode;
    @JsonProperty("court_type_id")
    private String courtTypeId;
    @JsonProperty("court_type")
    private String courtType;
    @JsonProperty("welshCourtType")
    private String welshCourtType;
    @JsonProperty("court_venues")
    private List<CourtVenue> courtVenues;
}
