package uk.gov.hmcts.reform.prl.models.dto.bundle;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BundlingInformation {
    private List<Bundle> caseBundles;
    private List<Bundle> historicalBundles;
    private String bundleConfiguration;
    private List<MultiBundleConfig> multiBundleConfiguration;
}
