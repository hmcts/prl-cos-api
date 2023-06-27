package uk.gov.hmcts.reform.prl.enums.serveorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ServingCitizenRespondentsEnum {

    @JsonProperty("courtBailiff")
    courtBailiff("courtBailiff", "Court bailiff"),
    @JsonProperty("unrepresentedApplicant")
    unrepresentedApplicant("unrepresentedApplicant", "Unrepresented applicant who is arranging service"),
    @JsonProperty("courtAdmin")
    courtAdmin("courtAdmin", "Court admin");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ServingCitizenRespondentsEnum getValue(String key) {
        return ServingCitizenRespondentsEnum.valueOf(key);
    }

}
