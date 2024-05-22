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
    nextStepsAfterGateKeeping("nextStepsAfterGateKeeping", "Case review at second gatekeeping appointment (PD36Y)"),
    @JsonProperty("allocationDecision")
    allocationDecision("allocationDecision", "Allocation decision"),
    @JsonProperty("allocateAndReserveToNamedJudge")
    allocateAndReserveToNamedJudge("allocateAndReserveToNamedJudge", "Allocate or reserve to a named judge"),
    @JsonProperty("urgentHearing")
    urgentHearing("urgentHearing", "Urgent hearing"),
    @JsonProperty("hearingNotNeeded")
    hearingNotNeeded("hearingNotNeeded", "Hearing is not needed"),
    @JsonProperty("fhdra")
    fhdra("fhdra", "First hearing dispute resolution appointment (FHDRA)"),
    @JsonProperty("positionStatement")
    positionStatement("positionStatement", "Position statement"),
    @JsonProperty("participationDirections")
    participationDirections("participationDirections", "Participation directions"),
    @JsonProperty("miamAttendance")
    miamAttendance("miamAttendance", "Mediation Information and Assessment Meeting (MIAM)"),
    @JsonProperty("permissionHearing")
    permissionHearing("permissionHearing", "Permission hearing for Direction 91(14)"),
    @JsonProperty("directionForDra")
    directionForDra("directionForDra", "Directions for dispute resolution appointment (DRA)"),
    @JsonProperty("settlementConference")
    settlementConference("settlementConference", "Settlement conference"),
    @JsonProperty("joiningInstructions")
    joiningInstructions("joiningInstructions", "Joining instructions for remote hearing"),
    @JsonProperty("factFindingHearing")
    factFindingHearing("factFindingHearing", "Directions for Fact-finding hearing"),
    @JsonProperty("interpreters")
    interpreters("interpreters", "Court to arrange interpreters"),
    @JsonProperty("updateContactDetails")
    updateContactDetails("updateContactDetails", "Update your contact details"),
    @JsonProperty("partyRaisedDomesticAbuse")
    partyRaisedDomesticAbuse("partyRaisedDomesticAbuse", "Party or parties raising domestic abuse issues"),
    @JsonProperty("nextStepsAfterSecondGatekeeping")
    nextStepsAfterSecondGatekeeping("nextStepsAfterSecondGatekeeping", "Next steps after second gatekeeping appointment");

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

