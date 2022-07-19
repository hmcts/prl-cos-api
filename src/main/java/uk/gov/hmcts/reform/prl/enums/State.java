package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum State {

    AWAITING_SUBMISSION_TO_HMCTS("AWAITING_SUBMISSION_TO_HMCTS", "Draft"),
    AWAITING_FL401_SUBMISSION_TO_HMCTS("AWAITING_FL401_SUBMISSION_TO_HMCTS", "Draft"),
    SUBMITTED_NOT_PAID("SUBMITTED_NOT_PAID", "Pending"),
    SUBMITTED_PAID("SUBMITTED_PAID", "Submitted"),
    AWAITING_RESUBMISSION_TO_HMCTS("AWAITING_RESUBMISSION_TO_HMCTS", "Returned"),
    CASE_ISSUE("CASE_ISSUE", "Case Issued"),
    CASE_WITHDRAWN("CASE_WITHDRAWN", "Withdrawn"),
    GATEKEEPING("GATE_KEEPING", "Gatekeeping"),
    PREPARE_FOR_HEARING_CONDUCT_HEARING("PREPARE_FOR_HEARING_CONDUCT_HEARING", "Hearing"),
    DECISION_OUTCOME("DECISION_OUTCOME"),
    ALL_FINAL_ORDERS_ISSUED("ALL_FINAL_ORDERS_ISSUED"),
    CASE_HEARING("CASE_HEARING","Prepare for hearing");
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

    public String getLabel() {
        return label;
    }


}
