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
public enum JudgeOrMagistrateTitleEnum {
    @CCD(label = "Her Honour Judge")
    @JsonProperty("herHonourJudge")
    herHonourJudge("herHonourJudge", "Her Honour Judge"),

    @CCD(label = "His Honour Judge")
    @JsonProperty("hisHonourJudge")
    hisHonourJudge("hisHonourJudge", "His Honour Judge"),

    @CCD(label = "Circuit Judge")
    @JsonProperty("circuitJudge")
    circuitJudge("circuitJudge", "Circuit Judge"),

    @CCD(label = "Deputy Circuit Judge")
    @JsonProperty("deputyCircuitJudge")
    deputyCircuitJudge("deputyCircuitJudge", "Deputy Circuit Judge"),

    @CCD(label = "Recorder")
    @JsonProperty("recorder")
    recorder("recorder", "Recorder"),

    @CCD(label = "District Judge")
    @JsonProperty("districtJudge")
    districtJudge("districtJudge", "District Judge"),

    @CCD(label = "Deputy District Judge")
    @JsonProperty("deputyDistrictJudge")
    deputyDistrictJudge("deputyDistrictJudge", "Deputy District Judge"),

    @CCD(label = "District Judge Magistrates Court")
    @JsonProperty("districtJudgeMagistratesCourt")
    districtJudgeMagistratesCourt("districtJudgeMagistratesCourt",
                                  "District Judge Magistrates Court"),

    @CCD(label = "Magistrates")
    @JsonProperty("magistrate")
    magistrate("magistrate", "Magistrates"),

    @CCD(label = "Justices' Legal Adviser")
    @JsonProperty("justicesLegalAdviser")
    justicesLegalAdviser("justicesLegalAdviser", "Justices' Legal Adviser"),

    @CCD(label = "Justices' Clerk")
    @JsonProperty("justicesClerk")
    justicesClerk("justicesClerk", "Justices' Clerk"),

    @CCD(label = "The Honourable Mrs Justice")
    @JsonProperty("theHonourableMrsJustice")
    theHonourableMrsJustice("theHonourableMrsJustice", "The Honourable Mrs Justice"),

    @CCD(label = "The Honourable Mr Justice")
    @JsonProperty("theHonourableMrJustice")
    theHonourableMrJustice("theHonourableMrJustice", "The Honourable Mr Justice");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static JudgeOrMagistrateTitleEnum getValue(String key) {
        return JudgeOrMagistrateTitleEnum.valueOf(key);
    }
}
