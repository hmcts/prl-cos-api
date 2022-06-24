package uk.gov.hmcts.reform.prl.models.court;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Court {

    @JsonProperty("name")
    private String courtName;
    @JsonProperty("slug")
    private String courtSlug;
    private boolean open;
    @JsonProperty("county_location_code")
    private long countyLocationCode;
    @JsonProperty("types")
    private List<String> courtTypes;
    @JsonProperty("addresses")
    private List<CourtAddress> address;
    private String gbs;
    @JsonProperty("dx_number")
    private List<String> dxNumber;
    @JsonProperty("areas_of_law")
    private List<AreaOfLaw> areasOfLaw;
    @JsonProperty("in_person")
    private boolean inPerson;
    @JsonProperty("access_scheme")
    private boolean accessScheme;
    @JsonProperty("emails")
    private List<CourtEmailAddress> courtEmailAddresses;

}
