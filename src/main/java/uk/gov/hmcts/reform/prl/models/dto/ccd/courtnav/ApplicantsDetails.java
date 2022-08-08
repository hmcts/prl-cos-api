package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantGenderEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreferredContactEnum;

import java.time.LocalDate;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ApplicantsDetails {

    private final String applicantFirstName;
    private final String applicantLastName;
    private final String applicantOtherNames;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate applicantDateOfBirth;
    private final ApplicantGenderEnum applicantGender;
    private final String otherGender;
    private final YesOrNo shareContactDetailsWithRespondent;
    private final YesOrNo isAddressConfidential;
    private final YesOrNo isEmailAddressConfidential;
    private final YesOrNo isPhoneNumberConfidential;
    private String applicantPhoneNumber;
    private String applicantEmailAddress;
    private String applicantContactInstructions;
    private Address applicantAddress;
    private PreferredContactEnum applicantPreferredContact;
    private final Organisation legalRepresentativeFirm;
    private final Address legalRepresentativeAddress;
    private final String legalRepresentativeDx;
    private final String legalRepresentativeReference;
    private final String legalRepresentativeFirstName;
    private final String legalRepresentativeLastName;
    private final YesOrNo applicantHasLegalRepresentative;
    private final String legalRepresentativeEmail;
    private final String legalRepresentativePhone;

}
