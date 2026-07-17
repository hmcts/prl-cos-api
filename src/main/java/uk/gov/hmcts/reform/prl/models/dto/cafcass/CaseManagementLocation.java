package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(builderMethodName = "caseManagementLocationWith")
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CaseManagementLocation {
    @CCD(label = " ", searchable = false)
    private  String regionId;
    @CCD(label = " ", searchable = false)
    private  String baseLocationId;
    @CCD(label = " ", searchable = false)
    private  String regionName;
    @CCD(label = " ", searchable = false)
    private  String baseLocationName;
    @CCD(label = " ", searchable = false)
    private  String region;
    @CCD(label = " ", searchable = false)
    private String baseLocation;
}
