package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class C100RebuildInternationalElements {

    @JsonProperty("ie_internationalStart")
    private YesOrNo doChildHaveInternationalResidence;
    @JsonProperty("ie_provideDetailsStart")
    private String childInternationalResidenceDetails;
    @JsonProperty("ie_internationalParents")
    private YesOrNo doChildsParentHaveInternationalResidence;
    @JsonProperty("ie_provideDetailsParents")
    private String childsParentHaveInternationalResidenceDetails;
    @JsonProperty("ie_internationalJurisdiction")
    private YesOrNo doesApplicationLinkedPeopleHaveInternationalOrder;
    @JsonProperty("ie_provideDetailsJurisdiction")
    private String applicationLinkedPeopleHaveInternationalOrderDetails;
    @JsonProperty("ie_internationalRequest")
    private YesOrNo hasAnotherCountryRequestedChildInformation;
    @JsonProperty("ie_provideDetailsRequest")
    private String anotherCountryRequestedChildInformationDetails;
}