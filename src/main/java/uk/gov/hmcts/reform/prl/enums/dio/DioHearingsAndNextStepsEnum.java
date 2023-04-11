package uk.gov.hmcts.reform.prl.enums.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DioHearingsAndNextStepsEnum {

    @JsonProperty("caseReviewAtSecondGateKeeping")
    caseReviewAtSecondGateKeeping("caseReviewAtSecondGateKeeping", "Case review at second gatekeeping appointment  (PD36Q)"),
    @JsonProperty("allocationDecision")
    allocationDecision("allocationDecision", "Allocation decision"),
    @JsonProperty("allocateNamedJudge")
    allocateNamedJudge("allocateNamedJudge", "Allocate or reserve to a named judge"),
    @JsonProperty("urgentHearing")
    urgentHearing("urgentHearing", "Urgent hearing"),
    @JsonProperty("urgentFirstHearing")
    urgentFirstHearing("urgentFirstHearing", "Urgent first hearing"),
    @JsonProperty("urgentHearingRefused")
    urgentHearingRefused("urgentHearingRefused", "Urgent hearing refused"),
    @JsonProperty("withoutNoticeFirstHearing")
    withoutNoticeFirstHearing("withoutNoticeFirstHearing", "Without notice first hearing"),
    @JsonProperty("withoutNoticeHearingRefused")
    withoutNoticeHearingRefused("withoutNoticeHearingRefused", "Without notice hearing refused"),
    @JsonProperty("firstHearingDisputeResolution")
    firstHearingDisputeResolution("firstHearingDisputeResolution", "First hearing dispute resolution (FHDRA)"),
    @JsonProperty("participationDirections")
    participationDirections("participationDirections", "Participation directions"),
    @JsonProperty("positionStatement")
    positionStatement("positionStatement", "Position statements"),
    @JsonProperty("attendanceAtMIAM")
    attendanceAtMIAM("attendanceAtMIAM", "Mediation Information and Assessment Meeting (MIAM)"),
    @JsonProperty("permissionHearing")
    permissionHearing("permissionHearing", "Permission hearing for Direction 91 (14)"),
    @JsonProperty("arrangeInterpreters")
    arrangeInterpreters("arrangeInterpreters", "Court to arrange interpreters"),
    @JsonProperty("updateContactDetails")
    updateContactDetails("updateContactDetails", "Update your contact details");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DioHearingsAndNextStepsEnum getValue(String key) {
        return DioHearingsAndNextStepsEnum.valueOf(key);
    }


}
