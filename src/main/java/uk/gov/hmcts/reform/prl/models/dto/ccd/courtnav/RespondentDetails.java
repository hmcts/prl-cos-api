package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.Address;


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
    private Address respondentAddress;
    private final boolean respondentLivesWithApplicant;
}
