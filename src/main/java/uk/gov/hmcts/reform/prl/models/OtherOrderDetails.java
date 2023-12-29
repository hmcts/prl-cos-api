package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherOrderDetails {

    private final String createdBy;
    private final String orderCreatedDate;
    private final String orderAmendedDate;
    private final String orderMadeDate;
    private final String orderRecipients;
    private final String orderServedDate;
    private final String approvalDate;
    private final String status;
    private final String orderCreatedBy;
    private String orderCreatedByEmailId;
    private final String additionalRequirementsForHearingReq;

}
