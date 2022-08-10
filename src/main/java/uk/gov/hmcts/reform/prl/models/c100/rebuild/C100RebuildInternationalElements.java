package uk.gov.hmcts.reform.prl.models.c100.rebuild;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
public class C100RebuildInternationalElements {

    @JsonProperty
    private final YesOrNo doChildHaveInternationalResidence;
    @JsonProperty
    private final String childInternationalResidenceDetails;
    @JsonProperty
    private final YesOrNo doChildsParentHaveInternationalResidence;
    @JsonProperty
    private final String childsParentHaveInternationalResidenceDetails;
    @JsonProperty
    private final YesOrNo doesApplicationLinkedPeopleHaveInternationalOrder;
    @JsonProperty
    private final String applicationLinkedPeopleHaveInternationalOrderDetails;
    @JsonProperty
    private final YesOrNo hasAnotherCountryRequestedChildInformation;
    @JsonProperty
    private final String anotherCountryRequestedChildInformationDetails;
}
