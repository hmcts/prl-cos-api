package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.PenalNoticeOptionEnum;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManageOrders {

    @JsonProperty("cafcassEmailAddress")
    private final List<Element<String>> cafcassEmailAddress;
    @JsonProperty("otherEmailAddress")
    private final List<Element<String>> otherEmailAddress;
    @JsonProperty("parentName")
    private String parentName;
    @JsonProperty("recitalsOrPreamble")
    private final String recitalsOrPreamble;
    @JsonProperty("orderDirections")
    private final String orderDirections;
    @JsonProperty("penalNoticeNeeded")
    private final String partiesAndRepresentation;
    @JsonProperty("partiesAndRepresentation")
    private final List<PenalNoticeOptionEnum> penalNoticeNeeded;
    @JsonProperty("penalNoticeRtf")
    private final String penalNoticeRtf;
    @JsonProperty("recitalsOrPreambleRtf")
    private final String recitalsOrPreambleRtf;
    @JsonProperty("orderDirectionsRtf")
    private final String orderDirectionsRtf;
    @JsonProperty("scheduleToOrderRtf")
    private final String scheduleToOrderRtf;
    @JsonProperty("furtherDirectionsIfRequired")
    private final String furtherDirectionsIfRequired;
    @JsonProperty("furtherInformationIfRequired")
    private final String furtherInformationIfRequired;
}
