package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum;

@Data
@Builder
public class RespondentRelationObjectType {
    private final ApplicantRelationshipEnum applicantRelationshipEnum;
}
