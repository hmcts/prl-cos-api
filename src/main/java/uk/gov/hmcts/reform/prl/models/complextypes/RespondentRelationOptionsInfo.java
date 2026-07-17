package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder
@Jacksonized
public class RespondentRelationOptionsInfo {
    @CCD(label = "*What is the respondent’s relationship with the applicant?", searchable = false)
    private final ApplicantRelationshipOptionsEnum applicantRelationshipOptions;
    @CCD(label = "Please specify other below", searchable = false)
    private final String relationOptionsOther;
}
