package uk.gov.hmcts.reform.prl.enums.awaitinginformation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ExitAwaitingInformationReasonEnum {

    CASE_ISSUED("CASE_ISSUED", "Case Issued"),
    GATE_KEEPING("GATE_KEEPING", "Gate Keeping"),
    SUBMITTED_NOT_PAID("SUBMITTED_NOT_PAID", "Pending"),
    SUBMITTED_PAID("SUBMITTED_PAID", "Submitted");

    private final String value;
    private final String label;

    ExitAwaitingInformationReasonEnum(String value) {
        this.value = value;
        this.label = value;
    }

    public static ExitAwaitingInformationReasonEnum fromValue(final String value) {
        return tryFromValue(value)
            .orElseThrow(() -> new NoSuchElementException("Unable to map " + value + " to a case state"));
    }

    public static Optional<ExitAwaitingInformationReasonEnum> tryFromValue(final String value) {
        return Stream.of(values())
            .filter(state -> state.value.equalsIgnoreCase(value))
            .findFirst();
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static ExitAwaitingInformationReasonEnum getValue(String key) {
        return ExitAwaitingInformationReasonEnum.valueOf(key);
    }
}
