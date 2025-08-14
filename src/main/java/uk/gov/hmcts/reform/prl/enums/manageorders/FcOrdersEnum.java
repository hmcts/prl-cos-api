package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum FcOrdersEnum {

    @JsonProperty("contemptNotice")
    contemptNotice("contemptNotice", "Contempt notice (FC600)"),

    @JsonProperty("summonToAppearToCourt")
    summonToAppearToCourt("summonToAppearToCourt", "Summons to appear at court (FC601)"),

    @JsonProperty("warrantToSecureAttendanceAtCourt")
    warrantToSecureAttendanceAtCourt("warrantToSecureAttendanceAtCourt", "Warrant to secure attendance at court (FC602)"),

    @JsonProperty("orderOnProceedingDetermination")
    orderOnProceedingDetermination("orderOnProceedingDetermination", "Order on determination of proceedings for contempt of court  (FC603)"),

    @JsonProperty("warrantOfCommittal")
    warrantOfCommittal("warrantOfCommittal", "Warrant of committal (FC604)");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static FcOrdersEnum getValue(String key) {
        return FcOrdersEnum.valueOf(key);
    }
}
