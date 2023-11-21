package uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ServeOther {
    @JsonProperty("serveByPostOrEmail")
    private final DeliveryByEnum serveByPostOrEmail;
    @JsonProperty("emailInformationCA")
    private final List<Element<EmailInformation>> emailInformation;
    @JsonProperty("postalInformationCA")
    private final List<Element<PostalInformation>> postalInformation;
}
