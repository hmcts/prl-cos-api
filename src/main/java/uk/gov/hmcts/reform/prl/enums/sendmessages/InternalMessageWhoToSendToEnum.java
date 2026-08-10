package uk.gov.hmcts.reform.prl.enums.sendmessages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum InternalMessageWhoToSendToEnum {
    @CCD(label = "Court admin")
    @JsonProperty("COURT_ADMIN")
    COURT_ADMIN("COURT_ADMIN", "Court admin"),
    @CCD(label = "Legal adviser")
    @JsonProperty("LEGAL_ADVISER")
    LEGAL_ADVISER("LEGAL_ADVISER", "Legal adviser"),
    @CCD(label = "Judiciary")
    @JsonProperty("JUDICIARY")
    JUDICIARY("JUDICIARY", "Judiciary"),
    @CCD(label = "Other")
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

    public static InternalMessageWhoToSendToEnum fromDisplayValue(String displayedValue) {
        if (InternalMessageWhoToSendToEnum.COURT_ADMIN.displayedValue.equalsIgnoreCase(displayedValue)) {
            return InternalMessageWhoToSendToEnum.COURT_ADMIN;
        } else if (InternalMessageWhoToSendToEnum.LEGAL_ADVISER.displayedValue.equalsIgnoreCase(displayedValue)) {
            return InternalMessageWhoToSendToEnum.LEGAL_ADVISER;
        } else if (InternalMessageWhoToSendToEnum.JUDICIARY.displayedValue.equalsIgnoreCase(displayedValue)) {
            return InternalMessageWhoToSendToEnum.JUDICIARY;
        } else if (InternalMessageWhoToSendToEnum.OTHER.displayedValue.equalsIgnoreCase(displayedValue)) {
            return InternalMessageWhoToSendToEnum.OTHER;
        }
        return null;
    }
}
