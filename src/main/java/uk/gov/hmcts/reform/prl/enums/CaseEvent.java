package uk.gov.hmcts.reform.prl.enums;

import java.util.Arrays;

public enum CaseEvent {
    LINK_CITIZEN("linkCitizenAccount"),
    CITIZEN_INTERNAL_CASE_UPDATE("citizen-internal-case-update");

    private final String value;

    CaseEvent(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static uk.gov.hmcts.reform.prl.enums.CaseEvent fromValue(String value) {
        return Arrays.stream(values())
            .filter(event -> event.value.equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown event name: " + value));
    }
}
