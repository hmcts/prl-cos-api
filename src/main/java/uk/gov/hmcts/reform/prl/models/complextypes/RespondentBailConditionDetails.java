package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
public class RespondentBailConditionDetails {
    @CCD(label = "Is the respondent subject to any bail conditions?", searchable = false)
    @JsonProperty("isRespondentAlreadyInBailCondition")
    private final YesNoDontKnow isRespondentAlreadyInBailCondition;
    @CCD(label = " ", hint = "If 'Yes' when do the bail conditions end (for example, 12 11 2007)", searchable = false)
    @JsonProperty("bailConditionEndDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate bailConditionEndDate;
}
