package uk.gov.hmcts.reform.prl.enums.serviceofdocuments;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SodCitizenServingRespondentsEnum {

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
