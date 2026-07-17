package uk.gov.hmcts.reform.prl.models.dto.hearingmanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@AllArgsConstructor
@Getter
@Data
@Builder(toBuilder = true)
public class HearingDataFromTabToDocmosis {

    @CCD(label = " ", searchable = false)
    private String hearingType;
    @CCD(label = " ", searchable = false)
    private String hearingDate;
    @CCD(label = " ", searchable = false)
    private String hearingTime;
    @CCD(label = " ", searchable = false)
    private String hearingLocation;
    @CCD(label = " ", searchable = false)
    private String hearingEstimatedDuration;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList hearingArrangementsFromHmc;
}
