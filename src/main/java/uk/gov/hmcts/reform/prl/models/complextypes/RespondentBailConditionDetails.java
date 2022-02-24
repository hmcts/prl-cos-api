package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class RespondentBailConditionDetails {
    @JsonProperty("isRespondentAlreadyInBailCondition")
    private final YesNoDontKnow isRespondentAlreadyInBailCondition;
    @JsonProperty("bailConditionEndDate")
    private final LocalDate bailConditionEndDate;
}
