package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class HearingDateTimeOption {

    @JsonProperty("hearingDateTimeOption")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime hearingDateTimeOption;

    @JsonCreator
    public HearingDateTimeOption(LocalDateTime hearingDateTimeOption) {
        this.hearingDateTimeOption  = hearingDateTimeOption;
    }
}
