package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoHearingsAndNextStepsEnum {

    @CCD(label = "Case review at second gatekeeping appointment (PD36Y)")
    @JsonProperty("nextStepsAfterGateKeeping")
    nextStepsAfterGateKeeping("nextStepsAfterGateKeeping", "Case review at second gatekeeping appointment (PD36Y)"),
    @CCD(label = "Allocation decision")
    @JsonProperty("allocationDecision")
    allocationDecision("allocationDecision", "Allocation decision"),
    @CCD(label = "Allocate or reserve to a named judge")
    @JsonProperty("allocateAndReserveToNamedJudge")
    allocateAndReserveToNamedJudge("allocateAndReserveToNamedJudge", "Allocate or reserve to a named judge"),
    @CCD(label = "Urgent hearing")
    @JsonProperty("urgentHearing")
    urgentHearing("urgentHearing", "Urgent hearing"),
    @CCD(label = "Hearing is not needed")
    @JsonProperty("hearingNotNeeded")
    hearingNotNeeded("hearingNotNeeded", "Hearing is not needed"),
    @CCD(label = "First hearing dispute resolution appointment (FHDRA)")
    @JsonProperty("fhdra")
    fhdra("fhdra", "First hearing dispute resolution appointment (FHDRA)"),
    @CCD(label = "Position statement")
    @JsonProperty("positionStatement")
    positionStatement("positionStatement", "Position statement"),
    @CCD(label = "Participation directions")
    @JsonProperty("participationDirections")
    participationDirections("participationDirections", "Participation directions"),
    @CCD(label = "Mediation Information and Assessment Meeting (MIAM)")
    @JsonProperty("miamAttendance")
    miamAttendance("miamAttendance", "Mediation Information and Assessment Meeting (MIAM)"),
    @CCD(label = "Permission hearing for Direction 91(14)")
    @JsonProperty("permissionHearing")
    permissionHearing("permissionHearing", "Permission hearing for Direction 91(14)"),
    @CCD(label = "Directions for dispute resolution appointment (DRA)")
    @JsonProperty("directionForDra")
    directionForDra("directionForDra", "Directions for dispute resolution appointment (DRA)"),
    @CCD(label = "Settlement conference")
    @JsonProperty("settlementConference")
    settlementConference("settlementConference", "Settlement conference"),
    @CCD(label = "Joining instructions for remote hearing")
    @JsonProperty("joiningInstructions")
    joiningInstructions("joiningInstructions", "Joining instructions for remote hearing"),
    @CCD(label = "Directions for Fact-finding hearing")
    @JsonProperty("factFindingHearing")
    factFindingHearing("factFindingHearing", "Directions for Fact-finding hearing"),
    @CCD(label = "Court to arrange interpreters")
    @JsonProperty("interpreters")
    interpreters("interpreters", "Court to arrange interpreters"),
    @CCD(label = "Update your contact details")
    @JsonProperty("updateContactDetails")
    updateContactDetails("updateContactDetails", "Update your contact details"),
    @CCD(label = "Party or parties raising domestic abuse issues")
    @JsonProperty("partyRaisedDomesticAbuse")
    partyRaisedDomesticAbuse("partyRaisedDomesticAbuse", "Party or parties raising domestic abuse issues"),
    @CCD(label = "Next steps after second gatekeeping appointment")
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

