package uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class Payment {

    @CCD(label = "Fee", searchable = false)
    private final String fee;
    @CCD(label = "Status", searchable = false)
    private final String status;
    @CCD(label = "Help with fees reference number", searchable = false)
    private final String hwfReferenceNumber;
    @CCD(label = "Service request reference number", searchable = false)
    private final String paymentServiceRequestReferenceNumber;
    //PRL-4045 - Map citizen to solicitor fields
    @CCD(label = "Payment reference number", searchable = false)
    private final String paymentReferenceNumber;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false)
  private String paymentAmount;
  @CCD(label = " ", searchable = false)
  private String paymentReference;
  @CCD(label = " ", searchable = false)
  private String paymentMethod;
  @CCD(label = " ", searchable = false)
  private String caseReference;
  @CCD(label = " ", searchable = false)
  private String accountNumber;
  // ==== end synthesised definition-only fields ====
}
