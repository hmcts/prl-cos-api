package uk.gov.hmcts.reform.prl.enums.sendmessages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MessageAboutEnum {
    @JsonProperty("APPLICATION")
    APPLICATION("APPLICATION", "An application"),
    @JsonProperty("HEARING")
    HEARING("HEARING", "A hearing"),
    @JsonProperty("REVIEW_SUBMITTED_DOCUMENTS")
    REVIEW_SUBMITTED_DOCUMENTS("REVIEW_SUBMITTED_DOCUMENTS", "Review submitted documents"),
    @JsonProperty("OTHER")
    OTHER("OTHER", "Other");

    private final String code;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MessageAboutEnum getValue(String key) {
        return MessageAboutEnum.valueOf(key);
    }

}
