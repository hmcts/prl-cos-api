package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;


@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@Jacksonized
public class BeforeStart {

    @NotNull(message = "Applicant age should be provided")
    private final ApplicantAge applicantHowOld;

}
