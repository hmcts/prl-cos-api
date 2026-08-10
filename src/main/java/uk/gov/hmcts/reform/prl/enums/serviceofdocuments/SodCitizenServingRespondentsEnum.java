package uk.gov.hmcts.reform.prl.enums.serviceofdocuments;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SodCitizenServingRespondentsEnum {

    @CCD(label = "Unrepresented applicant who is arranging service")
    unrepresentedApplicant("unrepresentedApplicant", "Unrepresented applicant who is arranging service");

    private final String id;
    private final String displayedValue;

    public String getId() {
        return id;
    }

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SodCitizenServingRespondentsEnum getValue(String key) {
        return SodCitizenServingRespondentsEnum.valueOf(key);
    }
}
