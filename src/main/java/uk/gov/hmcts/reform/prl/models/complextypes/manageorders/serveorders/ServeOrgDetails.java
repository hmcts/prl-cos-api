package uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ServeOrgDetails {
    @CCD(label = " ", searchable = false)
    @JsonProperty("serveByPostOrEmail")
    private final DeliveryByEnum serveByPostOrEmail;
    @CCD(label = " ", searchable = false)
    @JsonProperty("emailInformation")
    private final EmailInformation emailInformation;
    @CCD(label = " ", searchable = false)
    @JsonProperty("postalInformation")
    private final PostalInformation postalInformation;
}
