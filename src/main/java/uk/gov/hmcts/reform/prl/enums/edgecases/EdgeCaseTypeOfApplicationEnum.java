package uk.gov.hmcts.reform.prl.enums.edgecases;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum EdgeCaseTypeOfApplicationEnum {

    @JsonProperty("FGM")
    FGM("FGM", "Female genital mutilation"),
    @JsonProperty("FMPO")
    FMPO("FMPO", "Forced marriage protection order"),
    @JsonProperty("SG")
    SG("SG", "Special guardianship"),
    @JsonProperty("DOP")
    DOP("DOP", "Declaration of parentage"),
    @JsonProperty("PO")
    PO("PO", "Parental orders"),
    @JsonProperty("PR")
    PR("PR", "Parental responsibility"),
    @JsonProperty("PRSFP")
    PRSFP("PRSFP", "Parental responsibility (second female parent)"),
    @JsonProperty("ACG")
    ACG("ACG", "Appointing a child’s guardian"),
    @JsonProperty("CCS")
    CCS("CCS", "Change of child’s surname or removal from jurisdiction");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static EdgeCaseTypeOfApplicationEnum fromKey(String key) {
        return EdgeCaseTypeOfApplicationEnum.valueOf(key);
    }
}
