package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
public class HearingData {

    @JsonProperty("hearingTypes")
    private final DynamicList hearingTypes;

    @JsonProperty("hearingTypeOtherDetails")
    private String hearingTypeOtherDetails;

    @JsonProperty("hearingDateConfirmOptionEnum")
    private HearingDateConfirmOptionEnum hearingDateConfirmOptionEnum;

    @JsonProperty("confirmedHearingDates")
    private final DynamicList confirmedHearingDates;

    @JsonProperty("additionalHearingDetails")
    private String additionalHearingDetails;

    @JsonProperty("instructionsForRemoteHearing")
    private String instructionsForRemoteHearing;

}
