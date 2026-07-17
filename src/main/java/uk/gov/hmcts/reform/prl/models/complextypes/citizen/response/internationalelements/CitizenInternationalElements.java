package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.internationalelements;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CitizenInternationalElements {
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo childrenLiveOutsideOfEnWl;
    @CCD(label = " ", searchable = false)
    private final String childrenLiveOutsideOfEnWlDetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo parentsAnyOneLiveOutsideEnWl;
    @CCD(label = " ", searchable = false)
    private final String parentsAnyOneLiveOutsideEnWlDetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo anotherPersonOrderOutsideEnWl;
    @CCD(label = " ", searchable = false)
    private final String anotherPersonOrderOutsideEnWlDetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo anotherCountryAskedInformation;
    @CCD(label = " ", searchable = false)
    private final String anotherCountryAskedInformationDetaails;
}
