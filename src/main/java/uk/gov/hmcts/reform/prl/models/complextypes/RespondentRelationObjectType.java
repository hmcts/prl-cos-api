package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum;

@Data
@Builder
@Jacksonized
public class RespondentRelationObjectType {
    private final ApplicantRelationshipEnum applicantRelationship;
}
