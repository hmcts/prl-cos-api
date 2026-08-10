package uk.gov.hmcts.reform.prl.enums.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DioOtherEnum {
    @CCD(label = "Disclosure of papers from previous proceedings")
    @JsonProperty("disclosureOfPapers")
    disclosureOfPapers("disclosureOfPapers", "Disclosure of papers from previous proceedings"),

    @CCD(label = "Parent with care can apply to transfer")
    @JsonProperty("parentWithCare")
    parentWithCare("parentWithCare", "Parent with care can apply to transfer"),

    @CCD(label = "Application to apply for permission to instruct an expert requires permission\t")
    @JsonProperty("applicationToApplyPermission")
    applicationToApplyPermission("applicationToApplyPermission", "Application to apply for permission to instruct an expert requires permission");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DioOtherEnum getValue(String key) {
        return DioOtherEnum.valueOf(key);
    }

}
