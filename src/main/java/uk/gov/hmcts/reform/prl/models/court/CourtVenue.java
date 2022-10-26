package uk.gov.hmcts.reform.prl.models.court;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourtVenue {

    @JsonProperty("court_venue_id")
    private String courtVenueId;
    @JsonProperty("epimms_id")
    private String epimmsId;
    @JsonProperty("site_name")
    private String siteName;
    @JsonProperty("region_id")
    private String regionId;
    @JsonProperty("region")
    private String region;
    @JsonProperty("cluster_id")
    private String clusterId;
    @JsonProperty("cluster_name")
    private String clusterName;
    @JsonProperty("open_for_public")
    private String openForPublic;
    @JsonProperty("court_address")
    private String courtAddress;
    @JsonProperty("postcode")
    private String postcode;
    @JsonProperty("phone_number")
    private String phoneNumber;
    @JsonProperty("court_name")
    private String courtName;
    @JsonProperty("venue_name")
    private String venueName;
    @JsonProperty("is_case_management_location")
    private String isCaseManagementLocation;
    @JsonProperty("is_hearing_location")
    private String isHearingLocation;
    @JsonProperty("location_type")
    private String locationType;
    @JsonProperty("mrd_building_location_id")
    private String mrdBuildingLocationId;
    @JsonProperty("mrd_venue_id")
    private String mrdVenueId;
}
