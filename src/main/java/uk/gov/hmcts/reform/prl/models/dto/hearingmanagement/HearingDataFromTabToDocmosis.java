package uk.gov.hmcts.reform.prl.models.dto.hearingmanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Data
@Builder(toBuilder = true)
public class HearingDataFromTabToDocmosis {

    private String hearingDate;
    private String hearingTime;
    private String hearingLocation;
    private String hearingEstimatedDuration;
}
