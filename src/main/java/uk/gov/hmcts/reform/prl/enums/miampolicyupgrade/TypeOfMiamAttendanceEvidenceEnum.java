package uk.gov.hmcts.reform.prl.enums.miampolicyupgrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum TypeOfMiamAttendanceEvidenceEnum {

    @JsonProperty("miamCertificate")
    miamCertificate(
            "miamCertificate",
            "A MIAM certificate"
    ),
    @JsonProperty("miamAttendanceDetails")
    miamAttendanceDetails(
            "miamAttendanceDetails",
            "MIAM attendance details"
    );

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static TypeOfMiamAttendanceEvidenceEnum getValue(String key) {
        return TypeOfMiamAttendanceEvidenceEnum.valueOf(key);
    }

}
