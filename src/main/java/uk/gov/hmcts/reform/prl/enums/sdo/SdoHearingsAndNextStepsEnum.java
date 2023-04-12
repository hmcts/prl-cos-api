package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoHearingsAndNextStepsEnum {

    @JsonProperty("nextStepsAfterGateKeeping")
    nextStepsAfterGateKeeping("nextStepsAfterGateKeeping", "Next steps after second gatekeeping appointment (PD36Q)"),
    @JsonProperty("allocationDecision")
    allocationDecision("allocationDecision", "Allocation decision"),
    @JsonProperty("allocationDecisionNamedJudge")
    allocateNamedJudge("allocationDecisionNamedJudge", "Allocate or reserve to a named judge"),
    @JsonProperty("urgentHearing")
    urgentHearing("urgentHearing", "Urgent hearing"),
    @JsonProperty("hearingNotNeeded")
    hearingNotNeeded("hearingNotNeeded", "Hearing is not needed"),
    @JsonProperty("fhdra")
    fhdra("fhdra", "First hearing dispute resolution (FHDRA)"),
    @JsonProperty("positionStatement")
    positionStatement("positionStatement", "Position statements"),
    @JsonProperty("participationDirections")
    participationDirections("participationDirections", "Participation directions"),
    @JsonProperty("miamAttendance")
    miamAttendance("miamAttendance", "MIAM attendance"),
    @JsonProperty("permissionHearing")
    permissionHearing("permissionHearing", "Permission hearing for Direction 91(14)"),
    @JsonProperty("directionForDra")
    directionForDra("directionForDra", "Directions for DRA"),
    @JsonProperty("settlementConference")
    settlementConference("settlementConference", "Settlement conference"),
    @JsonProperty("joiningInstructions")
    joiningInstructions("joiningInstructions", "Joining instructions for remote hearing"),
    @JsonProperty("factFindingHearing")
    factFindingHearing("factFindingHearing", "Directions for Fact-finding hearing"),
    @JsonProperty("interpreters")
    interpreters("interpreters", "Interpreters"),
    @JsonProperty("updateContactDetails")
    updateContactDetails("updateContactDetails", "Update your contact details");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoHearingsAndNextStepsEnum getValue(String key) {
        return SdoHearingsAndNextStepsEnum.valueOf(key);
    }

}

