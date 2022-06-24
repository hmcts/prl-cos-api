package uk.gov.hmcts.reform.prl.config.launchdarkly;

import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.LDConfig;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.stereotype.Service;

@Service
public class LaunchDarkClientFactory {
    public LDClientInterface create(String sdkKey, boolean offlineMode) {
        LDConfig config = new LDConfig.Builder()
            .offline(offlineMode)
            .build();

        return new LDClient(sdkKey, config);
    }
}
