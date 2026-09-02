package uk.gov.hmcts.reform.prl.services.requestorder;

import java.time.LocalDate;

/**
 * Outcome of evaluating whether a hearing currently warrants a Request Order task fire.
 */
record ChaseDecision(boolean shouldFire, String description) {

    static ChaseDecision fireCadenceMetDone() {
        return new ChaseDecision(true, "cadence met task Done action flow - firing");
    }

    static ChaseDecision fireCadenceMet() {
        return new ChaseDecision(true, "cadence met - firing");
    }

    static ChaseDecision skipUnknownHearingId(String hmcStatus) {
        return skip("hearingId missing (status=" + hmcStatus + ")");
    }

    static ChaseDecision skipStatusNotInFilter(String hmcStatus) {
        return skip("status=" + hmcStatus + " not in filter");
    }

    static ChaseDecision skipHearingNotAtCadence(LocalDate hearingEndDate, int cadence) {
        return skip("hearingEndDate=" + hearingEndDate + " not " + cadence + " days away");
    }

    static ChaseDecision skipLinkedOrderExists() {
        return skip("linked order exists (cycle complete)");
    }

    static ChaseDecision skipInFlight() {
        return skip("previous fire awaiting completion");
    }

    private static ChaseDecision skip(String reason) {
        return new ChaseDecision(false, "skipped - " + reason);
    }
}
