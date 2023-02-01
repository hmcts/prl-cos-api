package uk.gov.hmcts.reform.prl.enums.serveorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum WhatToDoWithOrderEnum {

    @JsonProperty("saveAsDraft")
    saveAsDraft("saveAsDraft", "Save the order as a draft"),
    @JsonProperty("finalizeSaveToServeLater")
    finalizeSaveToServeLater("finalizeSaveToServeLater", "Finalise the order, and save to serve later");

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
