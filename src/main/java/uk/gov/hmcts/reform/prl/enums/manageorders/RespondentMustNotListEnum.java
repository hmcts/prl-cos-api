package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum RespondentMustNotListEnum {

    @JsonProperty("notUseOrThreatenViolence")
    notUseOrThreatenViolence("notUseOrThreatenViolence", "Use or threaten violence against the applicant, and must not instruct, encourage or in any way suggest that any other person should do so"),

    @JsonProperty("notIntimidateHarassOrPester")
    notIntimidateHarassOrPester("notIntimidateHarassOrPester", "Must not intimidate, harass or pester the applicant, and must not instruct, encourage or in any way suggest that any other person should do so"),

    @JsonProperty("notContact")
    notContact("notContact", "Must not telephone, text, email or otherwise contact or attempt to contact the applicant"),

    @JsonProperty("notDamageApplicantProperty")
    notDamageApplicantProperty("notDamageApplicantProperty", "Must not damage, attempt to damage or threaten to damage any property owned by or in the possession or control of the applicant, and must not instruct, encourage or in any way suggest that any other person should do so"),

    @JsonProperty("notDamageApplicantAddressOrContents")
    notDamageApplicantAddressOrContents("notDamageApplicantAddressOrContents", "Must not damage, attempt to damage or threaten to damage the property or contents of the property, and must not instruct, encourage or in any way suggest that any other person should do so"),

    @JsonProperty("notAttendProperty")
    notAttendProperty("notAttendProperty", "Must not go to, enter or attempt to enter the property"),

    @JsonProperty("notUseOrThreatenViolenceChildren")
    notUseOrThreatenViolenceChildren("notUseOrThreatenViolenceChildren", "Must not use or threaten violence against the relevant children, and must not instruct, encourage or in any way suggest that any other person should do so"),

    @JsonProperty("notIntimidateOrHarassChildren")
    notIntimidateOrHarassChildren("notIntimidateOrHarassChildren", "Must not intimidate, harass or pester the relevant children, and must not instruct, encourage or in any way suggest that any other person should do so"),

    @JsonProperty("notContactChildren")
    notContactChildren("notContactChildren", "Must not telephone, text, email or otherwise contact or attempt to contact the relevant children"),

    @JsonProperty("notAttendSchool")
    notAttendSchool("notAttendSchool", "Must not go to, enter or attempt to enter the school");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static RespondentMustNotListEnum getValue(String key) {
        return RespondentMustNotListEnum.valueOf(key);
    }

}
