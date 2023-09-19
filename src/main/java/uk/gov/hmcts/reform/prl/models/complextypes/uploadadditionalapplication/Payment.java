package uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class Payment {

    private final String fee;
    private final String status;
    private final String hwfReferenceNumber;
    private final String paymentServiceRequestReferenceNumber;
    //PRL-4045 - Map citizen to solicitor fields
    private final String paymentReferenceNumber;

}
