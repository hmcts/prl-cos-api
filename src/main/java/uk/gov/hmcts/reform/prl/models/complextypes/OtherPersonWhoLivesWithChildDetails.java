package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "OtherPersonLivingWithChild", generate = true)
@Data
@Builder
public class OtherPersonWhoLivesWithChildDetails {

    @CCD(label = "*First name(s) of the adult living with the child", searchable = false)
    private String firstName;
    @CCD(label = "*Last name", searchable = false)
    private String lastName;
    @CCD(
            label = "*Give details of their relationship to (or involvement with) the child",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String relationshipToChildDetails;
    @CCD(label = "*Enter UK Postcode", searchable = false, typeOverride = FieldType.AddressUK)
    private Address address;
    @CCD(
            label = "*Do you need to keep the identity of the person that the child lives with confidential? ",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo isPersonIdentityConfidential;
}
