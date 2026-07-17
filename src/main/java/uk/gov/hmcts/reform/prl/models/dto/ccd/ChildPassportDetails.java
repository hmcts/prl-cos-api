package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.NewPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildPassportDetails {

    @CCD(label = "Who is in possession of the children's passport(s)?", searchable = false)
    @JsonProperty("newChildPassportPossession")
    private List<NewPassportPossessionEnum> newChildPassportPossession;
    @CCD(label = "Do the children have more than one passport?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo newChildHasMultiplePassports;
    @CCD(label = "If other, specify ", searchable = false)
    private String newChildPassportPossessionOtherDetails;
}
