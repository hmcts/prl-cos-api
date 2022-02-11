package uk.gov.hmcts.reform.prl.launchdarkly;

import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarkClientFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LaunchDarkClientFactoryTest {
    private LaunchDarkClientFactory factory;

    @Before
    public void setUp() {
        factory = new LaunchDarkClientFactory();
    }

    @Test
    public void testCreate() {
        LDClientInterface client = factory.create("test key", true);
        assertNotNull(client);
    }
}
