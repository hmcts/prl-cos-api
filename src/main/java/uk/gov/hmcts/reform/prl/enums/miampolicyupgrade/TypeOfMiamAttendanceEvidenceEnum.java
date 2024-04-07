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
    MIAM_ATT_EVIDENCE_MIAM_CERTIFICATE(
            "miamCertificate",
            "A MIAM certificate"
    ),
    @JsonProperty("miamAttendanceDetails")
    MIAM_ATTENDANCE_EVIDENCE_ATT_DETAILS(
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
