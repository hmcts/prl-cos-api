package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("organisations")
    private List<Organisations> organisations;
}
