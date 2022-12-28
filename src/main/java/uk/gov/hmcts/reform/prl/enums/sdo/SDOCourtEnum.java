package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SDOCourtEnum {

    @JsonProperty("transferApplication")
    transferApplication("transferApplication", "Transfer application to another family court"),
    @JsonProperty("crossExaminationProhibition")
    crossExaminationProhibition("crossExaminationProhibition", "Cross examination prohibition applies"),
    @JsonProperty("crossExaminationEx740")
    crossExaminationEx740("crossExaminationEx740", "Cross examination prohibition: EX740"),
    @JsonProperty("crossExaminationQualifiedLegal")
    crossExaminationQualifiedLegal(
        "crossExaminationQualifiedLegal",
        "Cross examination prohibition: Qualified legal representative to be appointed"
    );

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SDOCourtEnum getValue(String key) {
        return SDOCourtEnum.valueOf(key);
    }

}

