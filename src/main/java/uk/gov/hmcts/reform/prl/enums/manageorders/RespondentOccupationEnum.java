package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "theRespondentEnum", generate = true)
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum RespondentOccupationEnum {
    @CCD(label = "shall allow the applicant to occupy the address")
    @JsonProperty("allowApplicantToOccupy")
    allowApplicantToOccupy("allowApplicantToOccupy", "shall allow the applicant to occupy the address"),

    @CCD(label = "must not occupy the address")
    @JsonProperty("mustNotOccupyAddress")
    mustNotOccupyAddress("mustNotOccupyAddress", "must not occupy the address"),

    @CCD(label = "shall leave the address")
    @JsonProperty("shallLeaveAddress")
    shallLeaveAddress("shallLeaveAddress", "shall leave the address"),

    @CCD(label = "having left, must not return to, enter or attempt to enter the address")
    @JsonProperty("attemptToEnterAddress")
    attemptToEnterAddress("attemptToEnterAddress", "having left, must not return to,enter or attempt to enter the address"),

    @CCD(label = "must not obstruct, harass,or interfere with the applicant's peaceful occupation of the address")
    @JsonProperty("obstructHarassOrInterfere")
    obstructHarassOrInterfere("obstructHarassOrInterfere",
                              "must not obstruct, harass, or interfere with the applicant's peaceful occupation of the address"),

    @CCD(label = "other")
    @JsonProperty("other2")
    other2("other2", "other");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static RespondentOccupationEnum getValue(String key) {
        return RespondentOccupationEnum.valueOf(key);
    }
}
