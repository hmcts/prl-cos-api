package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class ProtectedChild {
    private final String fullName;
    private final CourtNavDate dateOfBirth;
    private final String relationship;
    private final boolean parentalResponsibility;
    private final String respondentRelationship;
}
