package uk.gov.hmcts.reform.prl.enums.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DioHearingsAndNextStepsEnum {

    @CCD(label = "Case review at second gatekeeping appointment  (PD36Y)")
    @JsonProperty("caseReviewAtSecondGateKeeping")
    caseReviewAtSecondGateKeeping("caseReviewAtSecondGateKeeping", "Case review at second gatekeeping appointment  (PD36Q)"),
    @CCD(label = "Allocation decision")
    @JsonProperty("allocationDecision")
    allocationDecision("allocationDecision", "Allocation decision"),
    @CCD(label = "Allocate or reserve to a named judge")
    @JsonProperty("allocateNamedJudge")
    allocateNamedJudge("allocateNamedJudge", "Allocate or reserve to a named judge"),
    @CCD(label = "Urgent hearing")
    @JsonProperty("urgentHearing")
    urgentHearing("urgentHearing", "Urgent hearing"),
    @CCD(label = "Urgent first hearing")
    @JsonProperty("urgentFirstHearing")
    urgentFirstHearing("urgentFirstHearing", "Urgent first hearing"),
    @CCD(label = "Urgent hearing refused ")
    @JsonProperty("urgentHearingRefused")
    urgentHearingRefused("urgentHearingRefused", "Urgent hearing refused"),
    @CCD(label = "Without notice first hearing")
    @JsonProperty("withoutNoticeFirstHearing")
    withoutNoticeFirstHearing("withoutNoticeFirstHearing", "Without notice first hearing"),
    @CCD(label = "Without notice hearing refused")
    @JsonProperty("withoutNoticeHearingRefused")
    withoutNoticeHearingRefused("withoutNoticeHearingRefused", "Without notice hearing refused"),
    @CCD(label = "First hearing dispute resolution (FHDRA)")
    @JsonProperty("firstHearingDisputeResolution")
    firstHearingDisputeResolution("firstHearingDisputeResolution", "First hearing dispute resolution (FHDRA)"),
    @CCD(label = "Participation directions")
    @JsonProperty("participationDirections")
    participationDirections("participationDirections", "Participation directions"),
    @CCD(label = "Position statements")
    @JsonProperty("positionStatement")
    positionStatement("positionStatement", "Position statements"),
    @CCD(label = "Mediation Information and Assessment Meeting (MIAM)")
    @JsonProperty("attendanceAtMIAM")
    attendanceAtMIAM("attendanceAtMIAM", "Mediation Information and Assessment Meeting (MIAM)"),
    @CCD(label = "Permission hearing for Direction 91 (14)")
    @JsonProperty("permissionHearing")
    permissionHearing("permissionHearing", "Permission hearing for Direction 91 (14)"),
    @CCD(label = "Court to arrange interpreters")
    @JsonProperty("arrangeInterpreters")
    arrangeInterpreters("arrangeInterpreters", "Court to arrange interpreters"),
    @CCD(label = "Update your contact details ")
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
