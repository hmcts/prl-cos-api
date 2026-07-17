package uk.gov.hmcts.reform.prl.models.complextypes.citizen.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Contact {
    @CCD(label = "*Telephone number ", searchable = false)
    private String phoneNumber;
    @CCD(label = "*Email address ", searchable = false)
    private String email;
}
