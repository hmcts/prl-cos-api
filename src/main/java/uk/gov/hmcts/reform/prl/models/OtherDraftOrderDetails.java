package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherDraftOrderDetails {

    private final String createdBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime dateCreated;
    private final String approvedBy;
    private final String approvedDate;
    private final String status;
    private final AmendOrderCheckEnum reviewRequiredBy;
    private final String nameOfJudgeForReview;
    private final String nameOfLaForReview;
    private final String nameOfJudgeForReviewOrder;
    private final String nameOfLaForReviewOrder;
    private final YesOrNo isJudgeApprovalNeeded;
    private final String orderCreatedBy;
    private String orderCreatedByEmailId;
    private final String additionalRequirementsForHearingReq;
    private final String instructionsToLegalRepresentative;
}
