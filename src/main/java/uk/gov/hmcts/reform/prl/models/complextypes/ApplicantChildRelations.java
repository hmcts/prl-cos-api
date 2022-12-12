package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;



@Data
@Builder
public class ApplicantChildRelations {

    private final String applicantFirstName;
    private final String applicantLastName;
    private final String childName;
    private final RelationshipsEnum applicantRelationshipToChild;
}
