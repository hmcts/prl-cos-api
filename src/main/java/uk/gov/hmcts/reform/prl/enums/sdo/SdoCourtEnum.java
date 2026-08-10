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
public enum SdoCourtEnum {

    @CCD(label = "Transfer application to another family court")
    @JsonProperty("transferApplication")
    transferApplication("transferApplication", "Transfer application to another family court"),
    @CCD(label = "Cross-examination prohibition applies")
    @JsonProperty("crossExaminationProhibition")
    crossExaminationProhibition("crossExaminationProhibition", "Cross-examination prohibition applies"),
    @CCD(label = "Cross-examination prohibition: EX740")
    @JsonProperty("crossExaminationEx740")
    crossExaminationEx740("crossExaminationEx740", "Cross-examination prohibition: EX740"),
    @CCD(label = "Cross-examination prohibition: EX741")
    @JsonProperty("crossExaminationEx741")
    crossExaminationEx741("crossExaminationEx741", "Cross-examination prohibition: EX741"),
    @CCD(label = "Cross-examination prohibition: Qualified legal representative to be appointed")
    @JsonProperty("crossExaminationQualifiedLegal")
    crossExaminationQualifiedLegal(
        "crossExaminationQualifiedLegal",
        "Cross-examination prohibition: Qualified legal representative to be appointed"
    );

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoCourtEnum getValue(String key) {
        return SdoCourtEnum.valueOf(key);
    }

}

