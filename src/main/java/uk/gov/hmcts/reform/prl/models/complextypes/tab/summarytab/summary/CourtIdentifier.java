package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtIdentifier {

    @JsonProperty("courtIdentifier")
    private final String courtIdentifier;

    @JsonCreator
    public CourtIdentifier(String courtIdentifier) {
        this.courtIdentifier = courtIdentifier;
    }
}
