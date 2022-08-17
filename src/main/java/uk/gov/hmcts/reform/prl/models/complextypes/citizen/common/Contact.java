package uk.gov.hmcts.reform.prl.models.complextypes.citizen.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Contact {
    private String phoneNumber;
    private String email;
}
