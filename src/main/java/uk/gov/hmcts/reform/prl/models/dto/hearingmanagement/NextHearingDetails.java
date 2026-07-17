package uk.gov.hmcts.reform.prl.models.dto.hearingmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@AllArgsConstructor
@Getter
@Data
@Builder(toBuilder = true)
public class NextHearingDetails {

    @CCD(label = "Hearing Id", searchable = false, typeOverride = FieldType.Number)
    @JsonProperty("hearingID")
    private final String hearingID;
    @CCD(label = "Next Hearing Date", searchable = false)
    @JsonProperty("hearingDateTime")
    private final LocalDateTime hearingDateTime;

}
