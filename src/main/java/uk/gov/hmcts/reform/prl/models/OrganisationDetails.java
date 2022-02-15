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

    private List<Element<ContactInformation>> contactInformation;
    private String name;
    private String organisationIdentifier;
}
