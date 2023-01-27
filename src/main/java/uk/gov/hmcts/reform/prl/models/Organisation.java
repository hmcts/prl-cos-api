package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Organisation {
    @JsonProperty("OrganisationID")
    private String organisationID;
    @JsonProperty("OrganisationName")
    private String organisationName;

    private UUID solicitorOrgId;
    public void setSolicitorOrgId(UUID solicitorOrgId) {
        if (this.getSolicitorOrgId() == null) {
            if (solicitorOrgId != null) {
                this.solicitorOrgId = solicitorOrgId;
            } else {
                this.solicitorOrgId = UUID.randomUUID();
            }
        }
    }

    public static Organisation organisation(String id) {
        return Organisation.builder()
            .organisationID(id)
            .build();
    }
}
