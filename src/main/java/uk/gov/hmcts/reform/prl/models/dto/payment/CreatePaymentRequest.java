package uk.gov.hmcts.reform.prl.models.dto.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.FeeType;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePaymentRequest {
    @Schema(name = "Case ID", required = true)
    String caseId;
    @Schema(name = "Return URL", description = "Return URL where Fee & Pay transfers control after the payment", required = true)
    String returnUrl;
    @Schema(name = "Case name", required = true)
    String applicantCaseName;
    @Schema(name = "Help with fees reference number")
    String hwfRefNumber;
    @Schema(name = "Fee type code fetched from /feeCode API", required = true)
    FeeType feeType;
    @Schema(name = "Type of awp in case of awp payment", example = "C1, C2, D89, FP25 etc.")
    String awpType;
    @Schema(name = "Party type", example = "applicant or respondent")
    String partyType;
}
