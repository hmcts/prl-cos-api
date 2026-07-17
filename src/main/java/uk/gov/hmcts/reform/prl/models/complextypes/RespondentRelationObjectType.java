package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder
@Jacksonized
public class RespondentRelationObjectType {
    @CCD(label = "*Select the applicant's relationship to the respondent:", searchable = false)
    private final ApplicantRelationshipEnum applicantRelationship;
}
