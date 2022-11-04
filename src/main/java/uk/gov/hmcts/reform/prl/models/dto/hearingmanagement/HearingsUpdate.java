package uk.gov.hmcts.reform.prl.models.dto.hearingmanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Data
@Builder
public class HearingsUpdate {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonProperty("hearingResponseReceivedDateTime")
    private final LocalDate hearingResponseReceivedDateTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonProperty("hearingEventBroadcastDateTime")
    private final LocalDate hearingEventBroadcastDateTime;
    @JsonProperty("hearingListingStatus")
    private final String hearingListingStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonProperty("nextHearingDate")
    private final LocalDate nextHearingDate;
    @JsonProperty("hearingVenueId")
    private final String hearingVenueId;
    @JsonProperty("hearingJudgeId")
    private final String hearingJudgeId;
    @JsonProperty("hearingRoomId")
    private final String hearingRoomId;
    @JsonProperty("hmcStatus")
    private final String hmcStatus;
    @JsonProperty("listAssistCaseStatus")
    private final String listAssistCaseStatus;

}
