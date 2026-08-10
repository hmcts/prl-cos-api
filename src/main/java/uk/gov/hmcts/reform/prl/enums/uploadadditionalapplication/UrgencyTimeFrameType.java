package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;


@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum UrgencyTimeFrameType {

    @CCD(label = "On the same day")
    @JsonProperty("SAME_DAY")
    SAME_DAY("SAME_DAY", "On the same day"),
    @CCD(label = "Within 2 days")
    @JsonProperty("WITHIN_2_DAYS")
    WITHIN_2_DAYS("WITHIN_2_DAYS", "Within 2 days"),
    @CCD(label = "Within 5 days")
    @JsonProperty("WITHIN_5_DAYS")
    WITHIN_5_DAYS("WITHIN_5_DAYS", "Within 5 days");


    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static UrgencyTimeFrameType getValue(String key) {
        return UrgencyTimeFrameType.valueOf(key);
    }
}
