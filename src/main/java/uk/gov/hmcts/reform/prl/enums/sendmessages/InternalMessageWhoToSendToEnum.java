package uk.gov.hmcts.reform.prl.enums.sendmessages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum InternalMessageWhoToSendToEnum {
    @JsonProperty("COURT_ADMIN")
    COURT_ADMIN("COURT_ADMIN", "Court admin"),
    @JsonProperty("LEGAL_ADVISER")
    LEGAL_ADVISER("LEGAL_ADVISER", "Legal adviser"),
    @JsonProperty("JUDICIARY")
    JUDICIARY("JUDICIARY", "Judiciary"),
    @JsonProperty("OTHER")
    OTHER("OTHER", "Other");

    private final String code;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static InternalMessageWhoToSendToEnum getValue(String key) {
        return InternalMessageWhoToSendToEnum.valueOf(key);
    }
}
