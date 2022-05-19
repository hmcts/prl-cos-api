package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherOrderDetails {

    private final String createdBy;
    private final String orderCreatedDate;
    private final LocalDate orderAmendedDate;
    private final String orderMadeDate;
    private final String orderRecipients;
}
