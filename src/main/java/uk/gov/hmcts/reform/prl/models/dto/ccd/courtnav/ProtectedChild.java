package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class ProtectedChild {
    private final String fullName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    private final String relationship;
    private final YesOrNo parentalResponsibility;
    private final String respondentRelationship;
}
