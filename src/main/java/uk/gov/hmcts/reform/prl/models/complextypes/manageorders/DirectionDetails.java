package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder(toBuilder = true)
@Data
public class DirectionDetails {

    private final List<String> isBold;
    private final String directionText;

}
