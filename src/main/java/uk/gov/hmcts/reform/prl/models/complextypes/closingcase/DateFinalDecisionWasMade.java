package uk.gov.hmcts.reform.prl.models.complextypes.closingcase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
public class DateFinalDecisionWasMade {
    @CCD(label = "Date final decision was made")
    @JsonProperty("finalDecisionDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime finalDecisionDate;

    @JsonCreator
    public DateFinalDecisionWasMade(LocalDateTime finalDecisionDate) {
        this.finalDecisionDate  = finalDecisionDate;
    }
}
