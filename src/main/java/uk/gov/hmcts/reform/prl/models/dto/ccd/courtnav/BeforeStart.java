package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class BeforeStart {
    private final ApplicantAge applicantHowOld;

}
