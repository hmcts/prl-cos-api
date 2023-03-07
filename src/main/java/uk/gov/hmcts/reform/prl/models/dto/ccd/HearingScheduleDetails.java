package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Builder(toBuilder = true)
public class HearingScheduleDetails {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYYY-MM-dd HH:mm")
    @JsonProperty("hearingDateTime")
    private final LocalDateTime hearingDateTime;

    @JsonCreator
    public HearingScheduleDetails(LocalDateTime hearingDateTime) {
        this.hearingDateTime = hearingDateTime;
    }
}
