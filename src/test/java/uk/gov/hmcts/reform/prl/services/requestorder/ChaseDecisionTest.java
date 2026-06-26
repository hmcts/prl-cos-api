package uk.gov.hmcts.reform.prl.services.requestorder;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ChaseDecisionTest {

    @Test
    void fireFactoryProducesShouldFireWithFiringDescription() {
        ChaseDecision decision = ChaseDecision.fire();

        assertThat(decision.shouldFire()).isTrue();
        assertThat(decision.description()).isEqualTo("cadence met - firing");
    }

    @Test
    void everySkipFactoryHasShouldFireFalseAndPrefixesDescriptionWithSkipped() {
        ChaseDecision[] decisions = {
            ChaseDecision.skipUnknownHearingId("LISTED"),
            ChaseDecision.skipStatusNotInFilter("LISTED"),
            ChaseDecision.skipHearingNotEnded(LocalDate.of(2026, 5, 1)),
            ChaseDecision.skipLinkedOrderExists(),
            ChaseDecision.skipInFlight(),
            ChaseDecision.skipBeforeCadence(2, LocalDate.of(2026, 4, 22), 3),
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
    void skipHearingNotEndedEmbedsDate() {
        assertThat(ChaseDecision.skipHearingNotEnded(LocalDate.of(2026, 5, 1)).description())
            .isEqualTo("skipped - hearingEndDate=2026-05-01 not in past");
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

    @Test
    void skipBeforeCadenceEmbedsWorkingDaysAnchorAndCadence() {
        assertThat(ChaseDecision.skipBeforeCadence(2, LocalDate.of(2026, 4, 22), 3).description())
            .isEqualTo("skipped - 2 working day(s) since anchor 2026-04-22 (need 3)");
    }
}
