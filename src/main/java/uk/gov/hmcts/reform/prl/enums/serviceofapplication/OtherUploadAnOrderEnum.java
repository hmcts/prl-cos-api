package uk.gov.hmcts.reform.prl.enums.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum OtherUploadAnOrderEnum {

    other("Other (upload an order)");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static OtherUploadAnOrderEnum getValue(String key) {
        return OtherUploadAnOrderEnum.valueOf(key);
    }
}
