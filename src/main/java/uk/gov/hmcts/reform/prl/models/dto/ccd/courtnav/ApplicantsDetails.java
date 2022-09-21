package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantGenderEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreferredContactEnum;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@Valid
public class ApplicantsDetails {

    @NotNull
    @NotEmpty
    private final String applicantFirstName;
    @NotNull
    @NotEmpty
    private final String applicantLastName;
    private final String applicantOtherNames;
    @NotNull
    private final CourtNavDate applicantDateOfBirth;
    @NotNull
    @NotEmpty
    private final ApplicantGenderEnum applicantGender;
    private final String applicantGenderOther;
    @NotNull
    private final boolean shareContactDetailsWithRespondent;
    @NotBlank
    private String applicantPhoneNumber;
    @NotNull
    @Email
    private String applicantEmailAddress;

    @NotEmpty
    private String applicantContactInstructions;
    private CourtnavAddress applicantAddress;
    @NotNull
    private List<PreferredContactEnum> applicantPreferredContact;
    private final String legalRepresentativeFirm;
    private final CourtnavAddress legalRepresentativeAddress;
    private final String legalRepresentativeDx;
    private final String legalRepresentativeReference;
    private final String legalRepresentativeFirstName;
    private final String legalRepresentativeLastName;
    private final boolean applicantHasLegalRepresentative;
    private final String legalRepresentativeEmail;
    private final String legalRepresentativePhone;

}
