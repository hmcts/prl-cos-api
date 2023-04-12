package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class DioDisclosureOtherDetails {

    @JsonProperty("dioDisclosureOtherDetails")
    private final String dioDisclosureOtherDetails;

    @JsonCreator
    public DioDisclosureOtherDetails(String dioDisclosureOtherDetails) {
        this.dioDisclosureOtherDetails  = dioDisclosureOtherDetails;
    }
}
