package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class AllegationsOfHarmRevisedOtherConcerns {

    @CCD(
            label = "What steps or orders does the applicant want the court to take or make to protect the safety of the child(ren) and/or themselves?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String newAllegationsOfHarmOtherConcernsCourtActions;

}
