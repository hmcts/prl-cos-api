package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoTransferApplicationReasonEnum {

    @JsonProperty("courtInAreaChildLives")
    courtInAreaChildLives("courtInAreaChildLives", "Another court is in the area where the child usually lives"),
    @JsonProperty("ongoingProceedings")
    ongoingProceedings("ongoingProceedings", "There are ongoing proceedings in another court"),
    @JsonProperty("other")
    other("other", "Other");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoTransferApplicationReasonEnum getValue(String key) {
        return SdoTransferApplicationReasonEnum.valueOf(key);
    }
}
