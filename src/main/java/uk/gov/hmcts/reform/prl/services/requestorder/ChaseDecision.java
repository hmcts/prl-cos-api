package uk.gov.hmcts.reform.prl.services.requestorder;

import java.time.LocalDate;

/**
 * Outcome of evaluating whether a hearing currently warrants a Request Order task fire.
 */
record ChaseDecision(boolean shouldFire, String description) {

    static ChaseDecision fire() {
        return new ChaseDecision(true, "cadence met - firing");
    }

    static ChaseDecision skipUnknownHearingId(String hmcStatus) {
        return skip("hearingId missing (status=" + hmcStatus + ")");
    }

    static ChaseDecision skipStatusNotInFilter(String hmcStatus) {
        return skip("status=" + hmcStatus + " not in filter");
    }

    static ChaseDecision skipHearingNotEnded(LocalDate hearingEndDate) {
        return skip("hearingEndDate=" + hearingEndDate + " not in past");
    }

    static ChaseDecision skipLinkedOrderExists() {
        return skip("linked order exists (cycle complete)");
    }

    static ChaseDecision skipInFlight() {
        return skip("previous fire awaiting completion");
    }

    static ChaseDecision skipBeforeCadence(int workingDaysSinceAnchor, LocalDate anchor, int cadence) {
        return skip(workingDaysSinceAnchor + " working day(s) since anchor " + anchor
            + " (need " + cadence + ")");
    }

    private static ChaseDecision skip(String reason) {
        return new ChaseDecision(false, "skipped - " + reason);
    }
}
