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

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Component
@AllArgsConstructor
public class CourtNavApplicantMapper {

    private final CourtNavAddressMapper courtNavAddressMapper;

    public PartyDetails map(ApplicantsDetails applicant) {

        ContactPreferences contactPreferences = null;
        if (applicant.getApplicantPreferredContact() != null
            && !applicant.getApplicantPreferredContact().isEmpty()) {
            contactPreferences = applicant.getApplicantPreferredContact().contains(PreferredContactEnum.email)
                ? ContactPreferences.email
                : ContactPreferences.post;
        }

        return PartyDetails.builder()
            .firstName(applicant.getFirstName())
            .lastName(applicant.getLastName())
            .previousName(applicant.getPreviousName())
            .dateOfBirth(LocalDate.parse(applicant.getDateOfBirth().mergeDate()))
            .gender(applicant.getGender())
            .otherGender(Gender.other.equals(applicant.getGender())
                             ? applicant.getOtherGender()
                             : null)
            .address(null != applicant.getEmail()
                         ? courtNavAddressMapper.map(applicant.getAddress()) : null)
            .isAddressConfidential(ObjectUtils.isNotEmpty(applicant.getAddress())
                                       && !applicant.isShareContactDetailsWithRespondent() ? Yes : No)
            .canYouProvideEmailAddress(YesOrNo.valueOf(null != applicant.getEmail() ? "Yes" : "No"))
            .email(applicant.getEmail())
            .isEmailAddressConfidential(StringUtils.isNotEmpty(applicant.getEmail())
                                            && !applicant.isShareContactDetailsWithRespondent() ? Yes : No)
            .phoneNumber(applicant.getPhoneNumber())
            .isPhoneNumberConfidential(StringUtils.isNotEmpty(applicant.getPhoneNumber())
                                           && !applicant.isShareContactDetailsWithRespondent() ? Yes : No)
            .applicantPreferredContact(applicant.getApplicantPreferredContact())
            .applicantContactInstructions(applicant.getApplicantContactInstructions())
            .representativeFirstName(applicant.getSolicitorFirmName())
            .representativeLastName(applicant.getRepresentativeLastName())
            .solicitorTelephone(applicant.getSolicitorTelephone())
            .solicitorReference(applicant.getSolicitorReference())
            .contactPreferences(contactPreferences)
            .solicitorOrg(Organisation.builder()
                              .organisationName(applicant.getSolicitorFirmName())
                              .build())
            .solicitorEmail(applicant.getSolicitorEmail())
            .solicitorAddress(null != applicant.getSolicitorAddress()
                                  ? courtNavAddressMapper.map(applicant.getSolicitorAddress()) : null)
            .dxNumber(applicant.getDxNumber())
            .partyId(UUID.randomUUID())
            .build();
    }
}
