package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.reform.prl.enums.FamilyHomeEnum;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SignatureEnum {

    @JsonProperty("applicant")
    applicant("applicant", "Applicant"),

    @JsonProperty("applicantRepresentative")
    applicantRepresentative("applicantRepresentative", "Applicant’s legal representative");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SignatureEnum getValue(String key) {
        return SignatureEnum.valueOf(key);
    }
}
