package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherPersonWhoLivesWithChild {

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
