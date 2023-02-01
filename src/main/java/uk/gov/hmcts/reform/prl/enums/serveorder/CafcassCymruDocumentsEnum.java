package uk.gov.hmcts.reform.prl.enums.serveorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CafcassCymruDocumentsEnum {
    @JsonProperty("safeGuardingLetter")
    safeGuardingLetter("safeGuardingLetter", "Safeguarding letter"),
    @JsonProperty("section7Report")
    section7Report("section7Report", "Section 7 report"),
    @JsonProperty("s7AddendumReport")
    s7AddendumReport("s7AddendumReport", "S7 addendum report"),
    @JsonProperty("report164")
    report164("report164", "16.4 report"),
    @JsonProperty("updateToSafeGuardingLetter")
    updateToSafeGuardingLetter("updateToSafeGuardingLetter", "Update to safeguarding letter"),
    @JsonProperty("s16RiskAssessment")
    s16RiskAssessment("s16RiskAssessment", "S16A risk assessment"),
    @JsonProperty("childImpactReport")
    childImpactReport("childImpactReport", "Child Impact report"),
    @JsonProperty("otherReports")
    otherReports("otherReports", "Other reports");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static CafcassCymruDocumentsEnum getValue(String key) {
        return CafcassCymruDocumentsEnum.valueOf(key);
    }
}

