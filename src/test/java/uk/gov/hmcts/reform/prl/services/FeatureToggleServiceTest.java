package uk.gov.hmcts.reform.prl.services;

import org.junit.experimental.runners.Enclosed;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(Enclosed.class)
class FeatureToggleServiceTest {

    @Nested
    @RunWith(SpringRunner.class)
    @SpringBootTest(properties = {
        "feature.toggle.barristerFeatureEnabled=true",
        "feature.toggle.cafcassDateTimeFeatureEnabled=true",
        "feature.toggle.osCourtLookupEnabled=true",
        "feature.toggle.createRequestCirUpdateTaskEnabled=true",
        "feature.toggle.caseNameWithPrefixReleaseDate=2026-01-31",
        "feature.toggle.exitAwaitingInformationEnabled=true",
        "feature.toggle.awaitingInformationEnabled=true"
    })
    class FeatureFlagSwitchOn {

        @Autowired
        private FeatureToggleService featureToggleService;

        @Test
        void isAddBarristerFlagReturnTrue() {
            assertThat(featureToggleService.isBarristerFeatureEnabled(), is(true));
        }

        @Test
        void isCafcassDateTimeFeatureEnabledReturnTrue() {
            assertThat(featureToggleService.isCafcassDateTimeFeatureEnabled(), is(true));
        }

        @Test
        void isOsCourtLookupFeatureEnabledReturnTrue() {
            assertThat(featureToggleService.isOsCourtLookupFeatureEnabled(), is(true));
        }

        @Test
        void isCreateRequestCirUpdateTaskEnabledReturnTrue() {
            assertThat(featureToggleService.isCreateRequestCirUpdateTaskEnabled(), is(true));
        }

        @Test
        void isAwaitingInformationEnabledReturnTrue() {
            assertThat(featureToggleService.isAwaitingInformationEnabled(), is(true));
        }

        @Test
        void isExitAwaitingInformationEnabledReturnTrue() {
            assertThat(featureToggleService.isExitAwaitingInformationEnabled(), is(true));
        }

        @Test
        void isFullCaseNameEnabledReturnTrue() {
            assertThat(featureToggleService.isFullCaseNameEnabled(), is(true));
        }

        @Test
        void getCaseNameWithPrefixReleaseDateReturnsDate() {
            LocalDate releaseDate = LocalDate.of(2026, 1, 31);
            assertThat(featureToggleService.getCaseNameWithPrefixReleaseDate(), is(releaseDate));
        }

        @Test
        void isCaseIssuedAfterReleaseDateReturnsTrue() {
            LocalDate caseIssueDate = LocalDate.of(2026, 2, 1);
            assertThat(featureToggleService.isCaseIssuedAfterReleaseDate(caseIssueDate), is(true));
        }

        @Test
        void isCaseIssuedAfterReleaseDateReturnsFalsefromNull() {
            assertThat(featureToggleService.isCaseIssuedAfterReleaseDate(null), is(false));
        }
    }
}
