package uk.gov.hmcts.reform.prl.models.dto.hearingmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Data
@Builder(toBuilder = true)
public class HearingRequest {

    @JsonProperty("hmctsServiceCode")
    private final String hmctsServiceCode;
    @JsonProperty("caseRef")
    private final String caseRef;
    @JsonProperty("hearingId")
    private final String hearingId;
    private final HearingsUpdate hearingUpdate;
    private NextHearingDateRequest nextHearingDateRequest;

}
