package uk.gov.hmcts.reform.prl.mapper.courtnav;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.ApplicantsDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreferredContactEnum;

import java.time.LocalDate;
import java.util.UUID;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Component
@AllArgsConstructor
public class CourtNavApplicantMapper {

    private final CourtNavAddressMapper courtNavAddressMapper;

    public PartyDetails mapApplicant(ApplicantsDetails applicant) {

        ContactPreferences contactPreferences = null;
        if (isNotEmpty(applicant.getApplicantPreferredContact())) {
            contactPreferences = applicant.getApplicantPreferredContact()
                .contains(PreferredContactEnum.email) ? ContactPreferences.email : ContactPreferences.post;
        }

        return PartyDetails.builder()
            .firstName(applicant.getApplicantFirstName())
            .lastName(applicant.getApplicantLastName())
            .previousName(applicant.getApplicantOtherNames())
            .dateOfBirth(LocalDate.parse(applicant.getApplicantDateOfBirth().mergeDate()))
            .gender(applicant.getApplicantGender())
            .otherGender(Gender.other.equals(applicant.getApplicantGender())
                             ? applicant.getApplicantGenderOther()
                             : null)
            .address(null != applicant.getApplicantAddress()
                         ? courtNavAddressMapper.map(applicant.getApplicantAddress()) : null)
            .isAddressConfidential(ObjectUtils.isNotEmpty(applicant.getApplicantAddress())
                                       && !applicant.isShareContactDetailsWithRespondent() ? YesOrNo.Yes : YesOrNo.No)
            .canYouProvideEmailAddress(YesOrNo.valueOf(null != applicant.getApplicantEmailAddress() ? "Yes" : "No"))
            .email(applicant.getApplicantEmailAddress())
            .isEmailAddressConfidential(StringUtils.isNotEmpty(applicant.getApplicantEmailAddress())
                                            && !applicant.isShareContactDetailsWithRespondent() ? YesOrNo.Yes : YesOrNo.No)
            .phoneNumber(applicant.getApplicantPhoneNumber())
            .isPhoneNumberConfidential(StringUtils.isNotEmpty(applicant.getApplicantPhoneNumber())
                                           && !applicant.isShareContactDetailsWithRespondent() ? YesOrNo.Yes : YesOrNo.No)
            .applicantPreferredContact(applicant.getApplicantPreferredContact())
            .applicantContactInstructions(applicant.getApplicantContactInstructions())
            .representativeFirstName(applicant.getLegalRepresentativeFirstName())
            .representativeLastName(applicant.getLegalRepresentativeLastName())
            .solicitorTelephone(applicant.getLegalRepresentativePhone())
            .solicitorReference(applicant.getLegalRepresentativeReference())
            .contactPreferences(contactPreferences)
            .solicitorOrg(Organisation.builder()
                              .organisationName(applicant.getLegalRepresentativeFirm())
                              .build())
            .solicitorEmail(applicant.getLegalRepresentativeEmail())
            .solicitorAddress(null != applicant.getLegalRepresentativeAddress()
                                  ? courtNavAddressMapper.map(applicant.getLegalRepresentativeAddress()) : null)
            .dxNumber(applicant.getLegalRepresentativeDx())
            .partyId(UUID.randomUUID())
            .build();
    }
}
