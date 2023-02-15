package uk.gov.hmcts.reform.prl.models.dto.hearingmanagement;

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
public class HearingDetails {

    @JsonProperty("hearingID")
    private final String hearingID;
    @JsonProperty("nextHearingDate")
    private final LocalDateTime nextHearingDate;

}
