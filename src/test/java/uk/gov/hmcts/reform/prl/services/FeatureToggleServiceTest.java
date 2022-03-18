package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.config.Features;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "FEATURE_EXAMPLE=true", "FEATURE_EXAMPLE_OFF=false" })
public class FeatureToggleServiceTest {

    @Autowired
    private FeatureToggleService classUnderTest;

    @Test
    public void shouldReturnTrueWhenEnvVarIsSetTrue() {
        assertThat(classUnderTest.isFeatureEnabled(Features.EXAMPLE), is(true));
    }

    @Test
    public void shouldReturnFalseWhenEnvVarIsSetFalse() {
        assertThat(classUnderTest.isFeatureEnabled(Features.EXAMPLE_OFF), is(false));
    }

    @Test
    public void shouldReturnFalseWhenEnumIsNotDefinedInConfig() {
        assertThat(classUnderTest.isFeatureEnabled(Features.EXAMPLE_NOT_DEFINED), is(false));
    }
}
