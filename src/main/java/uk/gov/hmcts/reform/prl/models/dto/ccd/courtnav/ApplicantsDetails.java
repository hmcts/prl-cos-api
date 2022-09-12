package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Organisation;
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
    private final CourtNavGender applicantGender;
    private final String otherGender;
    private final boolean shareContactDetailsWithRespondent;
    private final YesOrNo isAddressConfidential;
    private final YesOrNo isEmailAddressConfidential;
    private final YesOrNo isPhoneNumberConfidential;
    private String applicantPhoneNumber;
    private String applicantEmailAddress;
    private String applicantContactInstructions;
    private Address applicantAddress;
    private List<PreferredContactEnum> applicantPreferredContact;
    private final Organisation legalRepresentativeFirm;
    private final Address legalRepresentativeAddress;
    private final String legalRepresentativeDx;
    private final String legalRepresentativeReference;
    private final String legalRepresentativeFirstName;
    private final String legalRepresentativeLastName;
    private final boolean applicantHasLegalRepresentative;
    private final String legalRepresentativeEmail;
    private final String legalRepresentativePhone;

}
