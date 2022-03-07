package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.SuperUser;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class Organisations {
    @JsonProperty("companyNumber")
    private String companyNumber;

    @JsonProperty("companyUrl")
    private String companyUrl;

    @JsonProperty("contactInformation")
    private List<ContactInformation> contactInformation;

    @JsonProperty("name")
    private String name;

    @JsonProperty("organisationIdentifier")
    private String organisationIdentifier;

    @JsonProperty("paymentAccount")
    private List<String> paymentAccount;

    @JsonProperty("pendingPaymentAccount")
    private List<String> pendingPaymentAccount;

    @JsonProperty("sraId")
    private String sraId;

    @JsonProperty("sraRegulated")
    private boolean sraRegulated;

    @JsonProperty("status")
    private String status;

    @JsonProperty("statusMessage")
    private String statusMessage;

    @JsonProperty("superUser")
    private SuperUser superUser;
}
