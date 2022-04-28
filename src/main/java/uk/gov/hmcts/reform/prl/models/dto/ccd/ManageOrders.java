package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManageOrders {

    private final String recitalsOrPreamble;
    private final String orderDirections;
    private final String furtherDirectionsIfRequired;
    private final String childListForSpecialGuardianship;

}
