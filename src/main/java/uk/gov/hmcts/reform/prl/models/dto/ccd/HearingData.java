package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HearingData {

    @JsonProperty("hearingTypes")
    public final DynamicList hearingTypes;

    @JsonProperty("hearingTypeOtherDetails")
    public String hearingTypeOtherDetails;

    @JsonProperty("hearingDateConfirmOptionEnum")
    public HearingDateConfirmOptionEnum hearingDateConfirmOptionEnum;

}
