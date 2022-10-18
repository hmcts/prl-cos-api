package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class RespondentDetails {

    private final String respondentFirstName;
    private final String respondentLastName;
    private final String respondentOtherNames;
    private final CourtNavDate respondentDateOfBirth;
    private String respondentPhoneNumber;
    private String respondentEmailAddress;
    private CourtnavAddress respondentAddress;
    private final boolean respondentLivesWithApplicant;
}
