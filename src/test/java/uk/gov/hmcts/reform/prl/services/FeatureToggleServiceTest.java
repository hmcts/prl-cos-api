package uk.gov.hmcts.reform.prl.services;

import org.junit.experimental.runners.Enclosed;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.models.Features;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(Enclosed.class)
class FeatureToggleServiceTest {

    @Nested
    @RunWith(SpringRunner.class)
    @SpringBootTest(properties = {
        "feature.toggle.add_barrister=true"
    })
    class FeatureFlagSwitchOn {

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        void isAddBarristerFlagReturnTrue() {
            assertThat(featureToggleService.isAddBarristerIsEnabled(Features.ADD_BARRISTER), is(true));
        }
    }
}
