package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtherPersonRelationshipToChild {

    @CCD(label = "*What is this person’s relationship to Child?", searchable = false)
    private String personRelationshipToChild;

}
