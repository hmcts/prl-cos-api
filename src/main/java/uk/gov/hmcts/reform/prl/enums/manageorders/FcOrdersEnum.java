package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum FcOrdersEnum {

    @CCD(label = "Contempt notice (FC600)")
    @JsonProperty("contemptNotice")
    contemptNotice("contemptNotice", "Contempt notice (FC600)"),

    @CCD(label = "Summons to appear at court (FC601)")
    @JsonProperty("summonToAppearToCourt")
    summonToAppearToCourt("summonToAppearToCourt", "Summons to appear at court (FC601)"),

    @CCD(label = "Warrant to secure attendance at court (FC602)")
    @JsonProperty("warrantToSecureAttendanceAtCourt")
    warrantToSecureAttendanceAtCourt("warrantToSecureAttendanceAtCourt", "Warrant to secure attendance at court (FC602)"),

    @CCD(label = "Order on determination of proceedings for contempt of court  (FC603)")
    @JsonProperty("orderOnProceedingDetermination")
    orderOnProceedingDetermination("orderOnProceedingDetermination", "Order on determination of proceedings for contempt of court  (FC603)"),

    @CCD(label = "Warrant of committal (FC604)")
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
