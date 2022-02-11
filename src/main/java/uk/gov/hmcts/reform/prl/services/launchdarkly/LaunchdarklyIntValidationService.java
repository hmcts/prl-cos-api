package uk.gov.hmcts.reform.prl.services.launchdarkly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;

@Service
public class LaunchdarklyIntValidationService {

    LaunchDarklyClient launchDarklyClient;

    @Autowired
    public LaunchdarklyIntValidationService(LaunchDarklyClient launchDarklyClient) {
        this.launchDarklyClient = launchDarklyClient;
    }

    public  String checkFeatureFlag(String flag) {

        String result;
        if (launchDarklyClient.isFeatureEnabled(flag)) {
            result = "Feature flag is on";
        } else {
            result = "Feature flag is off";
        }
        return result;
    }

}
