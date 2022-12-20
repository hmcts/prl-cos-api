package uk.gov.hmcts.reform.prl.enums.serveorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WhatToDoWithOrderEnum {

    @JsonProperty("saveAsDraft")
    SAVE_AS_DRAFT("saveAsDraft", "Save the order as a draft"),
    @JsonProperty("finalizeSaveToServeLater")
    FINALIZE_SAVE_TO_SERVE_LATER("finalizeSaveToServeLater", "Finalise the order, and save to serve later");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static WhatToDoWithOrderEnum getValue(String key) {
        return WhatToDoWithOrderEnum.valueOf(key);
    }
}
