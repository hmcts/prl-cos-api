package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "Fl401ProceedingDetails", generate = true)
@Data
@Builder(toBuilder = true)
public class FL401OtherProceedingDetails {
    @CCD(
            label = "*Are there previous or ongoing family court proceedings involving the applicant and respondent?",
            searchable = false
    )
    private final YesNoDontKnow hasPrevOrOngoingOtherProceeding;
    @CCD(
            label = "Other Proceedings",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Fl401OtherProceedingsData"
    )
    private List<Element<FL401Proceedings>> fl401OtherProceedings;
}
