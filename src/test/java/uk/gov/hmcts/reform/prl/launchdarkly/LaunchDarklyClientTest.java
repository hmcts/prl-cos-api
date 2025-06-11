package uk.gov.hmcts.reform.prl.launchdarkly;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarkClientFactory;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LaunchDarklyClientTest {
    private static final String SDK_KEY = "fake key";
    private static final String FAKE_FEATURE = "fake feature";

    @Mock
    private LaunchDarkClientFactory ldClientFactory;

    @Mock
    private LDClientInterface ldClient;

    @Mock
    private LDContext ldContext;

    private LaunchDarklyClient launchDarklyClient;

    @BeforeEach
    public void setUp() {
        when(ldClientFactory.create(eq(SDK_KEY), anyBoolean())).thenReturn(ldClient);
        launchDarklyClient = new LaunchDarklyClient(ldClientFactory, SDK_KEY, true);
    }

    @Test
    public void testFeatureEnabled() {
        when(ldClient.boolVariation(eq(FAKE_FEATURE), any(LDContext.class), anyBoolean())).thenReturn(true);
        assertTrue(launchDarklyClient.isFeatureEnabled(FAKE_FEATURE, ldContext));
    }

    @Test
    public void testFeatureDisabled() {
        when(ldClient.boolVariation(eq(FAKE_FEATURE), any(LDContext.class), anyBoolean())).thenReturn(false);
        assertFalse(launchDarklyClient.isFeatureEnabled(FAKE_FEATURE, ldContext));
    }
}
