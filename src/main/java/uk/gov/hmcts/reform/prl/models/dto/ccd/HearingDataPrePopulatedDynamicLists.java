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

    private DynamicList retrievedHearingDates = null;

    private DynamicList retrievedHearingChannels = null;

    private DynamicList retrievedVideoSubChannels = null;

    private DynamicList retrievedTelephoneSubChannels = null;

    private DynamicList retrievedCourtLocations = null;

    private DynamicList hearingListedLinkedCases = null;
}
