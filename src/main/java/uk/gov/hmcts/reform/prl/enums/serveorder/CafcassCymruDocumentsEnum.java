package uk.gov.hmcts.reform.prl.enums.serveorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CafcassCymruDocumentsEnum {
    @JsonProperty("safeGuardingLetter")
    SAFEGUARDING_LETTER("safeGuardingLetter", "Safeguarding letter"),
    @JsonProperty("section7Report")
    SECTION_7_LETTER("section7Report", "Section 7 report"),
    @JsonProperty("s7AddendumReport")
    S7_ADDENDUM_REPORT("s7AddendumReport", "S7 addendum report"),
    @JsonProperty("report164")
    REPORT_164("report164", "16.4 report"),
    @JsonProperty("updateToSafeGuardingLetter")
    UPDATE_SAFEGUARDING_LETTER("updateToSafeGuardingLetter", "Update to safeguarding letter"),
    @JsonProperty("s16RiskAssessment")
    S16_RISK_ASSESSMENT("s16RiskAssessment", "S16A risk assessment"),
    @JsonProperty("childImpactReport")
    CHILD_IMPACT_REPORT("childImpactReport", "Child Impact report"),
    @JsonProperty("otherReports")
    OTHER_REPORTS("otherReports", "Other reports");


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

