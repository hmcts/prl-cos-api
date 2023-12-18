package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum AwpApplicationTypeEnum {

    @JsonProperty("C1")
    C1("C1"),
    @JsonProperty("C2")
    C2("C2"),
    @JsonProperty("C3")
    C3("C3"),
    @JsonProperty("C4")
    C4("C4"),
    @JsonProperty("C79")
    C79("C79"),
    @JsonProperty("D89")
    D89("D89"),
    @JsonProperty("EX740")
    EX740("EX740"),
    @JsonProperty("EX741")
    EX741("EX741"),
    @JsonProperty("FP25")
    FP25("FP25"),
    @JsonProperty("FC600")
    FC600("FC600"),
    @JsonProperty("N161")
    N161("N161"),
    @JsonProperty("FL403")
    FL403("FL403"),
    @JsonProperty("FL403")
    FL407("FL407");

    private final String displayedValue;

    @JsonCreator
    public static AwpApplicationTypeEnum getValue(String key) {
        return AwpApplicationTypeEnum.valueOf(key);
    }
}
