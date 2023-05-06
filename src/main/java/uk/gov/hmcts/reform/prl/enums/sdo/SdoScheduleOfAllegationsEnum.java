package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoScheduleOfAllegationsEnum {

    @JsonProperty("includeScheduleAllegationAndResponse")
    includeScheduleAllegationAndResponse("includeScheduleAllegationAndResponse",
                                         "Include example of schedule of allegation and responses for fact-finding"),
    @JsonProperty("other")
    other("other","Other direction for example schedule of allegations and responses for fact-finding");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoScheduleOfAllegationsEnum getValue(String key) {
        return SdoScheduleOfAllegationsEnum.valueOf(key);
    }
}
