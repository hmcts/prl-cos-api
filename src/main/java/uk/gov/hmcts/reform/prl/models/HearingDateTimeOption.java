package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
public class HearingDateTimeOption {

    @CCD(label = "Enter date and time", searchable = false)
    @JsonProperty("hearingDateTimeOption")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime hearingDateTimeOption;

    @JsonCreator
    public HearingDateTimeOption(LocalDateTime hearingDateTimeOption) {
        this.hearingDateTimeOption  = hearingDateTimeOption;
    }
}
