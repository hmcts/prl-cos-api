package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum JudgeOrMagistrateTitleEnum {
    @JsonProperty("herHonourJudge")
    herHonourJudge("herHonourJudge", "Her Honour Judge"),

    @JsonProperty("hisHonourJudge")
    hisHonourJudge("hisHonourJudge", "His Honour Judge"),

    @JsonProperty("circuitJudge")
    circuitJudge("circuitJudge", "Circuit Judge"),

    @JsonProperty("deputyCircuitJudge")
    deputyCircuitJudge("deputyCircuitJudge", "Deputy Circuit Judge"),

    @JsonProperty("recorder")
    recorder("recorder", "Recorder"),

    @JsonProperty("districtJudge")
    districtJudge("districtJudge", "District Judge"),

    @JsonProperty("deputyDistrictJudge")
    deputyDistrictJudge("deputyDistrictJudge", "Deputy District Judge"),

    @JsonProperty("districtJudgeMagistratesCourt")
    districtJudgeMagistratesCourt("districtJudgeMagistratesCourt",
                                  "District Judge Magistrates Court"),

    @JsonProperty("magistrate")
    magistrate("magistrate", "Magistrates"),

    @JsonProperty("justicesLegalAdviser")
    justicesLegalAdviser("justicesLegalAdviser", "Justices' Legal Adviser"),

    @JsonProperty("justicesClerk")
    justicesClerk("justicesClerk", "Justices' Clerk"),

    @JsonProperty("theHonourableMrsJustice")
    theHonourableMrsJustice("theHonourableMrsJustice", "The Honourable Mrs Justice"),

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
