package uk.gov.hmcts.reform.prl.config.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class LaunchDarklyClient {
    public static final LDUser PRL_COS_USER = new LDUser.Builder("prl-cos-api")
        .anonymous(true)
        .build();

    private final LDClientInterface internalClient;

    @Value("${launchdarkly.sdk.environment}")
    private String environment;


    @Autowired
    public LaunchDarklyClient(
        LaunchDarkClientFactory ldClientFactory,
        @Value("${launchdarkly.sdk-key}") String sdkKey,
        @Value("${launchdarkly.offline-mode:false}") Boolean offlineMode
    ) {
        this.internalClient = ldClientFactory.create(sdkKey, offlineMode);
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    public boolean isFeatureEnabled(String feature) {
        return internalClient.boolVariation(feature, LaunchDarklyClient.PRL_COS_USER, false);
    }

    public boolean isFeatureEnabled(String feature, LDUser user) {
        return internalClient.boolVariation(feature, user, false);
    }
    public boolean isFeatureEnabledForEnv(String feature) {

        LDUser user = new LDUser.Builder("prl-cos-api")
            .firstName("PRIVATE LAW")
            .custom("environment", environment)
            .build();
        log.info("Display Environment accordingly" + environment);
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
