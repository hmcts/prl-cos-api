package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MiamAttendingPersonName {

    @JsonProperty("miamAttendingPersonName")
    private final String miamAttendingPersonName;

}
