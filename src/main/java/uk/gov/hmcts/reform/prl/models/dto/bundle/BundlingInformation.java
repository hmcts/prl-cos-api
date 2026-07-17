package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Getter
@Builder
public class BundlingInformation {
    @CCD(
            label = "Case Bundles",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Bundle"
    )
    @JsonProperty("caseBundles")
    private List<Bundle> caseBundles;
    @CCD(
            label = "Historical Bundles",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Bundle"
    )
    @JsonProperty("historicalBundles")
    private List<Bundle> historicalBundles;
    @CCD(
            label = "Bundle Configuration",
            showCondition = "bundleConfiguration=\"DUMMY_VALUE_TO_HIDE_FIELD\"",
            searchable = false
    )
    @JsonProperty("bundleConfiguration")
    private String bundleConfiguration;
    @CCD(
            label = "Bundle Configuration",
            showCondition = "multiBundleConfiguration=\"DUMMY_VALUE_TO_HIDE_FIELD\"",
            searchable = false,
            typeOverride = FieldType.Collection
    )
    @JsonProperty("multiBundleConfiguration")
    private List<MultiBundleConfig> multiBundleConfiguration;
    @CCD(label = "Bundle Creation Date and Time", searchable = false)
    @JsonProperty("bundleCreationDateAndTime")
    private String bundleCreationDateAndTime;
    @CCD(label = "Hearing Date and Time", searchable = false)
    @JsonProperty("bundleHearingDateAndTime")
    private String bundleHearingDateAndTime;
}