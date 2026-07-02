package uk.gov.hmcts.reform.prl.services.requestorder;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChaseDecisionTest {

    @Test
    void fireFactoryProducesShouldFireWithFiringDescription() {
        String reason = "cadence met - firing";
        ChaseDecision decision = ChaseDecision.fire(reason);

        assertThat(decision.shouldFire()).isTrue();
        assertThat(decision.description()).isEqualTo(reason);
    }

    @Test
    void everySkipFactoryHasShouldFireFalseAndPrefixesDescriptionWithSkipped() {
        ChaseDecision[] decisions = {
            ChaseDecision.skipUnknownHearingId("LISTED"),
            ChaseDecision.skipStatusNotInFilter("LISTED"),
            ChaseDecision.skipLinkedOrderExists(),
            ChaseDecision.skipInFlight()
        };

        assertThat(decisions).allSatisfy(d -> {
            assertThat(d.shouldFire()).isFalse();
            assertThat(d.description()).startsWith("skipped - ");
        });
    }

    @Test
    void skipUnknownHearingIdEmbedsHmcStatus() {
        assertThat(ChaseDecision.skipUnknownHearingId("LISTED").description())
            .isEqualTo("skipped - hearingId missing (status=LISTED)");
    }

    @Test
    void skipStatusNotInFilterEmbedsHmcStatus() {
        assertThat(ChaseDecision.skipStatusNotInFilter("LISTED").description())
            .isEqualTo("skipped - status=LISTED not in filter");
    }

    @Test
    void skipLinkedOrderExistsHasFixedDescription() {
        assertThat(ChaseDecision.skipLinkedOrderExists().description())
            .isEqualTo("skipped - linked order exists (cycle complete)");
    }

    @Test
    void skipInFlightHasFixedDescription() {
        assertThat(ChaseDecision.skipInFlight().description())
            .isEqualTo("skipped - previous fire awaiting completion");
    }

}
