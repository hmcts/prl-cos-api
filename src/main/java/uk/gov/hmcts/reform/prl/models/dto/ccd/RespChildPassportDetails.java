package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RespPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespChildPassportDetails {

    @CCD(
            label = "Who is in possession of the children's passport(s)?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "NewPassportPossessionEnum"
    )
    @JsonProperty("respChildPassportPossession")
    private List<RespPassportPossessionEnum> respChildPassportPossession;
    @CCD(label = "Do the children have more than one passport?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo respChildHasMultiplePassports;
    @CCD(label = "If other, specify ", searchable = false)
    private String respChildPassportPossessionOtherDetails;
}
