package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.*;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantGenderEnum;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class CourtNavGender {

    private final ApplicantGenderEnum value;
    private final String other;
}
