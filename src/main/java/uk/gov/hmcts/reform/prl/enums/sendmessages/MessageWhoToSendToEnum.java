package uk.gov.hmcts.reform.prl.enums.sendmessages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MessageWhoToSendToEnum {
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
    public static MessageWhoToSendToEnum getValue(String key) {
        return MessageWhoToSendToEnum.valueOf(key);
    }

    public static MessageWhoToSendToEnum fromDisplayValue(String displayedValue) {
        if (MessageWhoToSendToEnum.COURT_ADMIN.displayedValue.equalsIgnoreCase(displayedValue)) {
            return MessageWhoToSendToEnum.COURT_ADMIN;
        } else if (MessageWhoToSendToEnum.LEGAL_ADVISER.displayedValue.equalsIgnoreCase(displayedValue)) {
            return MessageWhoToSendToEnum.LEGAL_ADVISER;
        } else if (MessageWhoToSendToEnum.JUDICIARY.displayedValue.equalsIgnoreCase(displayedValue)) {
            return MessageWhoToSendToEnum.JUDICIARY;
        } else if (MessageWhoToSendToEnum.OTHER.displayedValue.equalsIgnoreCase(displayedValue)) {
            return MessageWhoToSendToEnum.OTHER;
        }
        return null;
    }
}
