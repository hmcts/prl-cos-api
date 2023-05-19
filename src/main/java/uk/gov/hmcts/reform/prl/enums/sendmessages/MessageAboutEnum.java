package uk.gov.hmcts.reform.prl.enums.sendmessages;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MessageAboutEnum {
    APPLICATION("APPLICATION", "An application"),
    HEARING("HEARING", "A hearing"),
    REVIEW_SUBMITTED_DOCUMENTS("REVIEW_SUBMITTED_DOCUMENTS", "Review submitted documents"),
    OTHER("OTHER", "Other");

    private final String code;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    public static MessageAboutEnum getValue(String key) {
        return MessageAboutEnum.valueOf(key);
    }

}
