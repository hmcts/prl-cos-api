package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.internationalelements;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CitizenInternationalElements {
    private final YesOrNo childrenLiveOutsideOfEnWl;
    private final String childrenLiveOutsideOfEnWlDetails;
    private final YesOrNo parentsAnyOneLiveOutsideEnWl;
    private final String parentsAnyOneLiveOutsideEnWlDetails;
    private final YesOrNo anotherPersonOrderOutsideEnWl;
    private final String anotherPersonOrderOutsideEnWlDetails;
    private final YesOrNo anotherCountryAskedInformation;
    private final String anotherCountryAskedInformationDetaails;
}
