package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoCourtRequestedEnum {

    @CCD(label = "A list of allegations made")
    @JsonProperty("allegationsMade")
    allegationsMade("allegationsMade", "A list of allegations made"),
    @CCD(label = "A written response to each of the allegations made")
    @JsonProperty("writtenResponse")
    writtenResponse("writtenResponse", "A written response to each of the allegations made"),
    @CCD(label = "Both")
    @JsonProperty("both")
    both("both", "Both");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoCourtRequestedEnum getValue(String key) {
        return SdoCourtRequestedEnum.valueOf(key);
    }

}
