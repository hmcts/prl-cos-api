package uk.gov.hmcts.reform.prl.enums.serviceofdocuments;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum AdditionalRecipients {

    @CCD(label = "Additional recipients (optional)")
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
