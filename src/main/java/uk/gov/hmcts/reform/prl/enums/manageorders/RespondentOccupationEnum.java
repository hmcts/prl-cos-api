package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum RespondentOccupationEnum {
    @JsonProperty("allowApplicantToOccupy")
    allowApplicantToOccupy("allowApplicantToOccupy", "shall allow the applicant to occupy the address"),

    @JsonProperty("mustNotOccupyAddress")
    mustNotOccupyAddress("mustNotOccupyAddress", "must not occupy the address"),

    @JsonProperty("shallLeaveAddress")
    shallLeaveAddress("shallLeaveAddress", "shall leave the address"),

    @JsonProperty("attemptToEnterAddress")
    attemptToEnterAddress("attemptToEnterAddress", "having left, must not return to,enter or attempt to enter the address"),

    @JsonProperty("obstructHarassOrInterfere")
    obstructHarassOrInterfere("obstructHarassOrInterfere",
                              "must not obstruct, harass, or interfere with the applicant's peaceful occupation of the address"),

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
