package uk.gov.hmcts.reform.prl.models.court;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder
public class Court {

    @CCD(ignore = true)
    @JsonProperty("name")
    private String courtName;
    @CCD(ignore = true)
    @JsonProperty("slug")
    private String courtSlug;
    @CCD(ignore = true)
    private boolean open;
    @CCD(ignore = true)
    @JsonProperty("county_location_code")
    private long countyLocationCode;
    @CCD(ignore = true)
    @JsonProperty("types")
    private List<String> courtTypes;
    @CCD(ignore = true)
    @JsonProperty("addresses")
    private List<CourtAddress> address;
    @CCD(ignore = true)
    private String gbs;
    @CCD(ignore = true)
    @JsonProperty("dx_number")
    private List<String> dxNumber;
    @CCD(ignore = true)
    @JsonProperty("areas_of_law")
    private List<AreaOfLaw> areasOfLaw;
    @CCD(ignore = true)
    @JsonProperty("in_person")
    private boolean inPerson;
    @CCD(ignore = true)
    @JsonProperty("access_scheme")
    private boolean accessScheme;
    @CCD(ignore = true)
    @JsonProperty("emails")
    private List<CourtEmailAddress> courtEmailAddresses;

}
