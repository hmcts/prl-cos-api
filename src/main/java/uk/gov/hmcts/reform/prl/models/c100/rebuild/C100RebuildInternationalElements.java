package uk.gov.hmcts.reform.prl.models.c100.rebuild;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class C100RebuildInternationalElements {

    @JsonProperty
    private final String doChildHaveInternationalResidence;
    @JsonProperty
    private final String childInternationalResidenceDetails;
    @JsonProperty
    private final String doChildsParentHaveInternationalResidence;
    @JsonProperty
    private final String childsParentHaveInternationalResidenceDetails;
    @JsonProperty
    private final String doesApplicationLinkedPeopleHaveInternationalOrder;
    @JsonProperty
    private final String applicationLinkedPeopleHaveInternationalOrderDetails;
    @JsonProperty
    private final String hasAnotherCountryRequestedChildInformation;
    @JsonProperty
    private final String anotherCountryRequestedChildInformationDetails;
}
