package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class DioLocalAuthorityDetails {
    @JsonProperty("dioLocalAuthorityDetails")
    private final String dioLocalAuthorityDetails;

    @JsonCreator
    public DioLocalAuthorityDetails(String dioLocalAuthorityDetails) {
        this.dioLocalAuthorityDetails  = dioLocalAuthorityDetails;
    }
}
