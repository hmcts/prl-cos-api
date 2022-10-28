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
public class CaseHearing {

    private final long hearingID;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime hearingRequestDateTime;

    private final String hearingType;

    private final String hmcStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime lastResponseReceivedDateTime;

    private final String responseVersion;

    private final String hearingListingStatus;

    private final String listAssistCaseStatus;

    private final boolean hearingIsLinkedFlag;

    private final String hearingGroupRequestId;

    private final List<String> hearingChannels;

    private List<HearingDaySchedule> hearingDaySchedules;

}
