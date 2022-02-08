package uk.gov.hmcts.reform.prl.enums;

import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public enum State {

    AWAITING_SUBMISSION_TO_HMCTS("AWAITING_SUBMISSION_TO_HMCTS", "Draft"),
    SUBMITTED_NOT_PAID("PENDING_CASE_ISSUED", "Pending"),
    SUBMITTED("SUBMITTED", "Submitted"),
    AWAITING_RESUBMISSION_TO_HMCTS("AWAITING_RESUBMISSION_TO_HMCTS", "Returned"),
    CASE_ISSUED("CASE_ISSUED", "Case Issued"),
    CASE_WITHDRAWN("CASE_WITHDRAWN", "Withdrawn"),
    GATEKEEPING("GATEKEEPING", "Gatekeeping"),
    PREPARE_FOR_HEARING_CONDUCT_HEARING("PREPARE_FOR_HEARING_CONDUCT_HEARING", "Hearing"),
    DECISION_OUTCOME("DECISION_OUTCOME"),
    ALL_FINAL_ORDERS_ISSUED("ALL_FINAL_ORDERS_ISSUED");

    private final String value;
    private final String label;

    State(String value) {
        this.value = value;
        this.label = value;
    }

    public static State fromValue(final String value) {
        return tryFromValue(value)
            .orElseThrow(() -> new NoSuchElementException("Unable to map " + value + " to a case state"));
    }

    public static Optional<State> tryFromValue(final String value) {
        return Stream.of(values())
            .filter(state -> state.value.equalsIgnoreCase(value))
            .findFirst();
    }

}
