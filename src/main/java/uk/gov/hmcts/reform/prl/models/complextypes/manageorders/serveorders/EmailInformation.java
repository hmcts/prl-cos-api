package uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class EmailInformation {

    private final String emailName;
    private final String emailAddress;
}
