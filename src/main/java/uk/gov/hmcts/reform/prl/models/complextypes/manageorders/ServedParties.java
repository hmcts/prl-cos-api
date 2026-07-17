package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ServedParties {

    @CCD(label = " ", showCondition = "partyId=\"DO_NOT_SHOW\"", searchable = false)
    private final String partyId;

    @CCD(label = " ", showCondition = "partyId=\"DO_NOT_SHOW\"", searchable = false)
    private final String partyName;

    @CCD(label = " ", showCondition = "partyId=\"DO_NOT_SHOW\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime servedDateTime;
}
