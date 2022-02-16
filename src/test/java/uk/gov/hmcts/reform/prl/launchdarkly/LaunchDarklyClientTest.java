package uk.gov.hmcts.reform.prl.documentgenerator.config.launchdarkly;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.documentgenerator.config.launchdarkly.LDClientFactory;
import uk.gov.hmcts.reform.prl.documentgenerator.config.launchdarkly.LaunchDarklyClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LaunchDarklyClientTest {
    private static final String SDK_KEY = "fake key";
    private static final String FAKE_FEATURE = "fake feature";

    @Mock
    private LDClientFactory ldClientFactory;

    @Mock
    private LDClientInterface ldClient;

    @Mock
    private LDUser ldUser;

    private LaunchDarklyClient launchDarklyClient;

    @Before
    public void setUp() {
        when(ldClientFactory.create(eq(SDK_KEY), anyBoolean())).thenReturn(ldClient);
        launchDarklyClient = new LaunchDarklyClient(ldClientFactory, SDK_KEY, true);
    }

    @Test
    public void testFeatureEnabled() {
        when(ldClient.boolVariation(eq(FAKE_FEATURE), any(LDUser.class), anyBoolean())).thenReturn(true);
        assertTrue(launchDarklyClient.isFeatureEnabled(FAKE_FEATURE, ldUser));
    }

    @Test
    public void testFeatureDisabled() {
        when(ldClient.boolVariation(eq(FAKE_FEATURE), any(LDUser.class), anyBoolean())).thenReturn(false);
        assertFalse(launchDarklyClient.isFeatureEnabled(FAKE_FEATURE, ldUser));
    }
}
