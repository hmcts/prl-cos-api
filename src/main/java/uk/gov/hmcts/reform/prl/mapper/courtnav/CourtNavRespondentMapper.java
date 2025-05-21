package uk.gov.hmcts.reform.prl.mapper.courtnav;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.RespondentDetails;

import java.time.LocalDate;
import java.util.UUID;

@Component
@AllArgsConstructor
public class CourtNavRespondentMapper {

    private final CourtNavAddressMapper addressMapper;

    public PartyDetails mapRespondent(RespondentDetails respondent) {
        return PartyDetails.builder()
            .firstName(respondent.getRespondentFirstName())
            .lastName(respondent.getRespondentLastName())
            .previousName(respondent.getRespondentOtherNames())
            .dateOfBirth(null != respondent.getRespondentDateOfBirth()
                             ? LocalDate.parse(respondent.getRespondentDateOfBirth().mergeDate()) : null)
            .isDateOfBirthKnown(YesOrNo.valueOf(null != respondent.getRespondentDateOfBirth() ? "Yes" : "No"))
            .email(respondent.getRespondentEmailAddress())
            .canYouProvideEmailAddress(YesOrNo.valueOf(null != respondent.getRespondentEmailAddress() ? "Yes" : "No"))
            .phoneNumber(respondent.getRespondentPhoneNumber())
            .canYouProvidePhoneNumber(YesOrNo.valueOf(null != respondent.getRespondentPhoneNumber() ? "Yes" : "No"))
            .address(null != respondent.getRespondentAddress()
                         ? addressMapper.map(respondent.getRespondentAddress()) : null)
            .isCurrentAddressKnown(YesOrNo.valueOf(null != respondent.getRespondentAddress() ? "Yes" : "No"))
            .respondentLivedWithApplicant(respondent.isRespondentLivesWithApplicant() ? YesOrNo.Yes : YesOrNo.No)
            .applicantContactInstructions(null)
            .applicantPreferredContact(null)
            .partyId(UUID.randomUUID())
            .build();
    }
}
