package uk.gov.hmcts.reform.prl.enums.serviceofdocuments;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum AdditionalRecipients {

    @JsonProperty("additionalRecipients")
    additionalRecipients("additionalRecipients", "Additional recipients (optional)");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static AdditionalRecipients getValue(String key) {
        return AdditionalRecipients.valueOf(key);
    }
}
