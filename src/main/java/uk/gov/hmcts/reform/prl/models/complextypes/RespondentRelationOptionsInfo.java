package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;

@Data
@Builder
public class RespondentRelationOptionsInfo {
    private final ApplicantRelationshipOptionsEnum applicantRelationshipOptionsEnum;
}
