package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProtectedChild {
    private String fullName;
    private CourtNavDate dateOfBirth;
    private String relationship;
    private boolean parentalResponsibility;
    private String respondentRelationship;
}
