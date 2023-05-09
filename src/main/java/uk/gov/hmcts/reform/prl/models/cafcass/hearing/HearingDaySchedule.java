package uk.gov.hmcts.reform.prl.models.cafcass.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(builderMethodName = "hearingDayScheduleWith")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HearingDaySchedule {

    private String hearingVenueName;

    private LocalDateTime hearingStartDateTime;

    private LocalDateTime hearingEndDateTime;

    private String courtTypeId;

    private String epimsId;

    private String hearingVenueId;

}
