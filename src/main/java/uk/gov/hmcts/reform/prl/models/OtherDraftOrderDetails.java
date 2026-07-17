package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherDraftOrderDetails {

    @CCD(label = "Order made by", searchable = false)
    private final String createdBy;
    @CCD(label = "Date order created", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime dateCreated;
    @CCD(label = "Approved by", searchable = false)
    private final String approvedBy;
    @CCD(label = "Date order made", searchable = false)
    private final String approvedDate;
    @CCD(label = "Status", searchable = false)
    private final String status;
    @CCD(label = "Status", showCondition = "reviewRequiredBy=\"DO_NOT_SHOW\"", searchable = false)
    private final AmendOrderCheckEnum reviewRequiredBy;
    @CCD(label = "Name Judge For Review", showCondition = "nameOfJudgeForReview=\"DO_NOT_SHOW\"", searchable = false)
    private final String nameOfJudgeForReview;
    @CCD(label = "Name LA For Review", showCondition = "nameOfLaForReview=\"DO_NOT_SHOW\"", searchable = false)
    private final String nameOfLaForReview;
    @CCD(label = "Name LA For Review", showCondition = "nameOfLaForReview=\"DO_NOT_SHOW\"", searchable = false)
    private final String nameOfJudgeForReviewOrder;
    @CCD(label = "Name LA For Review", showCondition = "nameOfLaForReview=\"DO_NOT_SHOW\"", searchable = false)
    private final String nameOfLaForReviewOrder;
    @CCD(
            label = "Draft Order Approval Status",
            showCondition = "isJudgeApprovalNeeded=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isJudgeApprovalNeeded;
    @CCD(label = "Order created by", searchable = false)
    private final String orderCreatedBy;
    @CCD(label = "Order created by", showCondition = "orderCreatedBy = \"DO_NOT_SHOW\"", searchable = false)
    private String orderCreatedByEmailId;
    @CCD(label = "Additional requirements for hearing request", searchable = false, typeOverride = FieldType.TextArea)
    private final String additionalRequirementsForHearingReq;
    @CCD(label = "Instructions to legal representative", showCondition = "status = \"DO_NOT_SHOW\"", searchable = false)
    private final String instructionsToLegalRepresentative;
}
