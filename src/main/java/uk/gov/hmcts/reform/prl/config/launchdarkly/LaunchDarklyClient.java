package uk.gov.hmcts.reform.prl.config.launchdarkly;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LaunchDarklyClient {

    private final LDClientInterface internalClient;
    private final LDContext privateLawProcessorContext;

    @Autowired
    public LaunchDarklyClient(
        LaunchDarkClientFactory ldClientFactory,
        @Value("${launchdarkly.sdk-key}") String sdkKey,
        @Value("${launchdarkly.offline-mode:false}") Boolean offlineMode
    ) {
        this.internalClient = ldClientFactory.create(sdkKey, offlineMode);
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        this.privateLawProcessorContext = LDContext.builder(sdkKey).build();
    }

    public boolean isFeatureEnabled(String feature) {
        return internalClient.boolVariation(feature, privateLawProcessorContext, false);
    }

    public boolean isFeatureEnabled(String feature, LDContext user) {
        return internalClient.boolVariation(feature, user, false);
    }

    private void close() {
        try {
            internalClient.close();
        } catch (IOException e) {
            // can't do anything clever here because things are being destroyed
            e.printStackTrace(System.err);
        }
    }
}
