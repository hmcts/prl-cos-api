package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum  AdditionalApplicationTypeEnum {

    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other specific orders"),
    @JsonProperty("c2Order")
    c2Order("c2Order", "C2 - to add or remove someone on a case, or for a specific request to the judge");

    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static AdditionalApplicationTypeEnum getValue(String key) {
        return AdditionalApplicationTypeEnum.valueOf(key);
    }

}
