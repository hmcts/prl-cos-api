package uk.gov.hmcts.reform.prl.enums.sendmessages;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum InternalMessageWhoToSendToEnum {
    COURT_ADMIN("COURT_ADMIN", "Court admin"),
    LEGAL_ADVISER("LEGAL_ADVISER", "Legal adviser"),
    JUDICIARY("JUDICIARY", "Judiciary"),
    OTHER("OTHER", "Other");

    private final String code;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    public static InternalMessageWhoToSendToEnum getValue(String key) {
        return InternalMessageWhoToSendToEnum.valueOf(key);
    }
}
