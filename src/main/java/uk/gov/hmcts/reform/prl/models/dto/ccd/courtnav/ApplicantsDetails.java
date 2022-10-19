package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantGenderEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreferredContactEnum;

import java.util.List;


@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class ApplicantsDetails {

    private final String applicantFirstName;
    private final String applicantLastName;
    private final String applicantOtherNames;
    private final CourtNavDate applicantDateOfBirth;
    private final ApplicantGenderEnum applicantGender;
    private final String applicantGenderOther;
    private final boolean shareContactDetailsWithRespondent;
    private String applicantPhoneNumber;
    private String applicantEmailAddress;
    private String applicantContactInstructions;
    private CourtnavAddress applicantAddress;
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
