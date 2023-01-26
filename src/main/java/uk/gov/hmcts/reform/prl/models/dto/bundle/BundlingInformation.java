package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
@Builder
public class BundlingInformation {
    @JsonProperty("caseBundles")
    private List<Bundle> caseBundles;
    @JsonProperty("historicalBundles")
    private List<Bundle> historicalBundles;
    @JsonProperty("bundleConfiguration")
    private String bundleConfiguration;
    @JsonProperty("multiBundleConfiguration")
    private List<MultiBundleConfig> multiBundleConfiguration;
    @JsonProperty("bundleCreationDateAndTime")
    private String bundleCreationDateAndTime;
    @JsonProperty("bundleHearingDateAndTime")
    private String bundleHearingDateAndTime;
}