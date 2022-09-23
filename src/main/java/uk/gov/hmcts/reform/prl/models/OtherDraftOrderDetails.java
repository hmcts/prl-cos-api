package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherDraftOrderDetails {

    private final String createdBy;
    private final LocalDateTime dateCreated;
    private final String approvedBy;
    private final String approvedDate;
    private final String status;
}
