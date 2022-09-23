package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class C100RebuildInternationalElements {

    @JsonProperty("internationalStart")
    private YesOrNo doChildHaveInternationalResidence;
    @JsonProperty("provideDetailsStart")
    private String childInternationalResidenceDetails;
    @JsonProperty("internationalParents")
    private YesOrNo doChildsParentHaveInternationalResidence;
    @JsonProperty("provideDetailsParents")
    private String childsParentHaveInternationalResidenceDetails;
    @JsonProperty("internationalJurisdiction")
    private YesOrNo doesApplicationLinkedPeopleHaveInternationalOrder;
    @JsonProperty("provideDetailsJurisdiction")
    private String applicationLinkedPeopleHaveInternationalOrderDetails;
    @JsonProperty("internationalRequest")
    private YesOrNo hasAnotherCountryRequestedChildInformation;
    @JsonProperty("provideDetailsRequest")
    private String anotherCountryRequestedChildInformationDetails;
}