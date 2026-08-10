package uk.gov.hmcts.reform.prl.enums.respondentsolicitor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum RespondentProceedingsEnum {

    @CCD(label = "Ongoing")
    @JsonProperty("ongoing")
    ongoing("ongoing", "Ongoing"),
    @CCD(label = "Previous")
    @JsonProperty("previous")
    previous("previous", "Previous");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static RespondentProceedingsEnum getValue(String key) {
        return RespondentProceedingsEnum.valueOf(key);
    }
}
