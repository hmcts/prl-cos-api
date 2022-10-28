package uk.gov.hmcts.reform.prl.models.dto.citizen.hearing;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class HearingDaySchedule {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime hearingStartDateTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime hearingEndDateTime;

    private final String listAssistSessionID;

    private final String hearingVenueId;

    private final String hearingRoomId;

    private final String hearingJudgeId;

    private final List<String> panelMemberIds;

    private final List<AttendeesDetails> attendees;
}
