package uk.gov.hmcts.reform.prl.enums.serveorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DeliveryByEnum {
    @JsonProperty("email")
    email("email", "Email"),
    @JsonProperty("post")
    post("post", "Post");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DeliveryByEnum getValue(String key) {
        return DeliveryByEnum.valueOf(key);
    }
}
