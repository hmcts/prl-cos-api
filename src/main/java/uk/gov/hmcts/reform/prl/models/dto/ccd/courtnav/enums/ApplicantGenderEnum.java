package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ApplicantGenderEnum {

    @JsonProperty("Female")
    Female("Female", "Female"),
    @JsonProperty("Male")
    Male("Male", "Male"),
    @JsonProperty("Non-binary")
    NonBinary("Non-binary", "Non-binary"),
    @JsonProperty("Transgender")
    Transgender("Transgender", "Transgender"),
    @JsonProperty("other")
    other("other", "other");

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
    public static ApplicantGenderEnum getValue(String key) {
        return ApplicantGenderEnum.valueOf(key);
    }
}
