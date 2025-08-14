package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
@JsonSerialize(using = CustomEnumSerializer.class)
public enum State {

    AWAITING_SUBMISSION_TO_HMCTS("AWAITING_SUBMISSION_TO_HMCTS", "Draft"),
    AWAITING_FL401_SUBMISSION_TO_HMCTS("AWAITING_FL401_SUBMISSION_TO_HMCTS", "Draft"),
    SUBMITTED_NOT_PAID("SUBMITTED_NOT_PAID", "Pending"),
    SUBMITTED_PAID("SUBMITTED_PAID", "Submitted"),
    AWAITING_RESUBMISSION_TO_HMCTS("AWAITING_RESUBMISSION_TO_HMCTS", "Returned"),
    CASE_ISSUED("CASE_ISSUED", "Case Issued"),
    CASE_WITHDRAWN("CASE_WITHDRAWN", "Withdrawn"),
    JUDICIAL_REVIEW("JUDICIAL_REVIEW", "Gatekeeping"),
    ALL_FINAL_ORDERS_ISSUED("ALL_FINAL_ORDERS_ISSUED", "Closed"),
    PREPARE_FOR_HEARING_CONDUCT_HEARING("PREPARE_FOR_HEARING_CONDUCT_HEARING","Hearing"),
    DELETED("DELETED", "Deleted"),
    REQUESTED_FOR_DELETION("REQUESTED_FOR_DELETION", "Requested for deletion"),
    READY_FOR_DELETION("READY_FOR_DELETION", "Ready for deletion"),
    DECISION_OUTCOME("DECISION_OUTCOME","Hearing Outcome"),
    PROCEEDS_IN_HERITAGE_SYSTEM("PROCEEDS_IN_HERITAGE_SYSTEM",
                                "Proceeding in offline mode in familyman system");

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

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static State getValue(String key) {
        return State.valueOf(key);
    }

}
