package uk.gov.hmcts.reform.prl.models.complextypes.closingcase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class DateFinalDecisionWasMade {
    @JsonProperty("finalDecisionDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime finalDecisionDate;

    @JsonCreator
    public DateFinalDecisionWasMade(LocalDateTime finalDecisionDate) {
        this.finalDecisionDate  = finalDecisionDate;
    }
}
