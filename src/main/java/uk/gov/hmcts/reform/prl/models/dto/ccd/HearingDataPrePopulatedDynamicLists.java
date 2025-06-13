package uk.gov.hmcts.reform.prl.models.dto.ccd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class HearingDataPrePopulatedDynamicLists {
    private DynamicList retrievedHearingTypes;

    private DynamicList retrievedHearingDates;

    private DynamicList retrievedHearingChannels;

    private DynamicList retrievedVideoSubChannels;

    private DynamicList retrievedTelephoneSubChannels;

    private DynamicList retrievedCourtLocations;

    private DynamicList hearingListedLinkedCases;
}
