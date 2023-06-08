package uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdditionalApplicationsBundle {
    private final String uploadedDateTime;
    private final String author;
    private C2DocumentBundle c2DocumentBundle;
    private OtherApplicationsBundle otherApplicationsBundle;
    private final String applicationsFeesToPay;
    private final String paymentStatus;
    private final String paymentServiceRequestReferenceNumber;
}
