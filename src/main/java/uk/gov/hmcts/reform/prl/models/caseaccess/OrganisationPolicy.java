package uk.gov.hmcts.reform.prl.models.caseaccess;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.CaseRole;
import uk.gov.hmcts.reform.prl.models.Organisation;

@Data
@Builder(toBuilder = true)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationPolicy {

    @JsonProperty("Organisation")
    private Organisation organisation;

    @JsonProperty("OrgPolicyReference")
    private String orgPolicyReference;

    @JsonProperty("OrgPolicyCaseAssignedRole")
    private String orgPolicyCaseAssignedRole;

    public static OrganisationPolicy organisationPolicy(String organisationId,
                                                        String organisationName,
                                                        CaseRole caseRole) {
        if (organisationId == null) {
            return null;
        }

        return OrganisationPolicy.builder()
            .organisation(Organisation.builder()
                              .organisationID(organisationId)
                              .organisationName(organisationName)
                              .build())
            .orgPolicyCaseAssignedRole(caseRole.formattedName())
            .build();
    }
}
