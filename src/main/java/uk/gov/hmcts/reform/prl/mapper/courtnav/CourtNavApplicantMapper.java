package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavApplicant;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreferredContactEnum;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Mapper(componentModel = "spring", uses = { CourtNavAddressMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourtNavApplicantMapper {

    @Mapping(target = "dateOfBirth", expression = "java(parseDate(applicant.getDateOfBirth()))")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "isAddressConfidential", expression
        = "java(resolveConfidentialFlag(applicant.getAddress(), applicant.isShareContactDetailsWithRespondent()))")
    @Mapping(target = "isEmailAddressConfidential", expression
        = "java(resolveConfidentialFlag(applicant.getEmail(), applicant.isShareContactDetailsWithRespondent()))")
    @Mapping(target = "isPhoneNumberConfidential", expression
        = "java(resolveConfidentialFlag(applicant.getPhoneNumber(), applicant.isShareContactDetailsWithRespondent()))")
    @Mapping(target = "canYouProvideEmailAddress", expression = "java(resolveYesNo(applicant.getEmail()))")
    @Mapping(target = "contactPreferences", expression = "java(resolveContactPreferences(applicant))")
    @Mapping(target = "solicitorAddress", source = "solicitorAddress")
    @Mapping(target = "solicitorOrg.organisationName", source = "solicitorFirmName")
    @Mapping(target = "partyId", expression = "java(java.util.UUID.randomUUID())")
    PartyDetails map(CourtNavApplicant applicant);

    // --- custom methods ---
    default LocalDate parseDate(CourtNavDate date) {
        return date != null ? LocalDate.parse(date.mergeDate()) : null;
    }

    default YesOrNo resolveConfidentialFlag(Object field, boolean shared) {
        return field != null && !shared ? Yes : No;
    }

    default YesOrNo resolveYesNo(Object field) {
        return field != null ? YesOrNo.Yes : YesOrNo.No;
    }

    default ContactPreferences resolveContactPreferences(CourtNavApplicant applicant) {
        List<PreferredContactEnum> preferences = applicant.getApplicantPreferredContact();
        if (preferences != null && !preferences.isEmpty()) {
            return preferences.contains(PreferredContactEnum.email) ? ContactPreferences.email : ContactPreferences.post;
        }
        return null;
    }
}
