package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.Address;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class RespondentDetails {

    private final String respondentFirstName;
    private final String respondentLastName;
    private final String respondentOtherNames;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentDateOfBirth;
    private String respondentPhoneNumber;
    private String respondentEmailAddress;
    private Address respondentAddress;
    private final boolean respondentLivesWithApplicant;
}
