package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Builder
@Data
@Jacksonized
public class DateOfSubmission {
    @JsonProperty("dateOfSubmission")
    private final String dateOfSubmission;
}
