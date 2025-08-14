package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;

@Data
@Builder
@Jacksonized
public class RespondentRelationOptionsInfo {
    private final ApplicantRelationshipOptionsEnum applicantRelationshipOptions;
    private final String relationOptionsOther;
}
