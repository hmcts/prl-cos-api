package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = { FeatureToggleServiceTest.TestConfig.class },
    initializers = ConfigDataApplicationContextInitializer.class
)
@TestPropertySource(properties = {
    "feature.toggle.barristerFeatureEnabled=true"
})
class FeatureToggleServiceTest {
    @EnableConfigurationProperties(FeatureToggleService.class)
    public static class TestConfig {}

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        void isAddBarristerFlagReturnTrue() {
            assertThat(featureToggleService.isBarristerFeatureEnabled(), is(true));
        }
}

