package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Organisation {
    @JsonProperty("OrganisationID")
    private String organisationID;
    @JsonProperty("OrganisationName")
    private String organisationName;
    private String companyNumber;
    private String companyUrl;
    private List<ContactInformation> contactInformation;
    private String name;
    private List<String> paymentAccount;
    private String sraId;
    private boolean sraRegulated;
    private String status;
    private SuperUser superUser;

    public static Organisation organisation(String id) {
        return Organisation.builder()
            .organisationID(id)
            .build();
    }
}
