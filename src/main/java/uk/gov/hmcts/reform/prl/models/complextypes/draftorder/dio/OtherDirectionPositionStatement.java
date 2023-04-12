package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class OtherDirectionPositionStatement {
    @JsonProperty("dioPositionStatementOtherDetails")
    private final String dioPositionStatementOtherDetails;

    @JsonCreator
    public OtherDirectionPositionStatement(String dioPositionStatementOtherDetails) {
        this.dioPositionStatementOtherDetails  = dioPositionStatementOtherDetails;
    }
}
