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
public enum MessageAboutEnum {
    @CCD(label = "An application")
    @JsonProperty("APPLICATION")
    APPLICATION("APPLICATION", "An application"),
    @CCD(label = "A hearing")
    @JsonProperty("HEARING")
    HEARING("HEARING", "A hearing"),
    @CCD(label = "Review submitted documents")
    @JsonProperty("REVIEW_SUBMITTED_DOCUMENTS")
    REVIEW_SUBMITTED_DOCUMENTS("REVIEW_SUBMITTED_DOCUMENTS", "Review submitted documents"),
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
    public static MessageAboutEnum getValue(String key) {
        return MessageAboutEnum.valueOf(key);
    }

}
