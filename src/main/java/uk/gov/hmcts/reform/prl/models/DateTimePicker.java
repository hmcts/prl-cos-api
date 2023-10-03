package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class DateTimePicker {

    @JsonProperty("selectedDateTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime selectedDateTime;

    @JsonCreator
    public DateTimePicker(LocalDateTime selectedDateTime) {
        this.selectedDateTime  = selectedDateTime;
    }
}
