package uk.gov.hmcts.reform.prl.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class OrganisationDetails {

    private String companyNumber;
    private String companyUrl;
    private List<ContactInformation> contactInformation;
    private String name;
    private String organisationIdentifier;
    private List<String> paymentAccount;
    private String sraId;
    private boolean sraRegulated;
    private String status;
    private SuperUser superUser;
}
