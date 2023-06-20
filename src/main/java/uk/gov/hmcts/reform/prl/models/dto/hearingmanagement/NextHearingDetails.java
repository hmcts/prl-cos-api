package uk.gov.hmcts.reform.prl.models.dto.hearingmanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Data
@Builder(toBuilder = true)
public class NextHearingDetails {

    @JsonProperty("hearingID")
    private final String hearingID;
    @JsonProperty("hearingDateTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime hearingDateTime;

}
