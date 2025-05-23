package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavRespondent;

import java.time.LocalDate;

@Mapper(componentModel = "spring", uses = CourtNavAddressMapper.class)
public interface CourtNavRespondentMapper {

    @Mapping(target = "dateOfBirth", source = "dateOfBirth", qualifiedByName = "mapCourtNavDate")
    @Mapping(target = "isDateOfBirthKnown", source = "dateOfBirth", qualifiedByName = "isNotNull")
    @Mapping(target = "canYouProvideEmailAddress", source = "email", qualifiedByName = "isNotNull")
    @Mapping(target = "canYouProvidePhoneNumber", source = "phoneNumber", qualifiedByName = "isNotNull")
    @Mapping(target = "isCurrentAddressKnown", source = "address", qualifiedByName = "isNotNull")
    @Mapping(target = "respondentLivedWithApplicant", source = "respondentLivesWithApplicant", qualifiedByName = "booleanToYesOrNo")
    @Mapping(target = "partyId", expression = "java(java.util.UUID.randomUUID())")
    PartyDetails mapRespondent(CourtNavRespondent respondent);

    @Named("mapCourtNavDate")
    default LocalDate mapCourtNavDate(CourtNavDate courtNavDate) {
        return courtNavDate != null ? LocalDate.parse(courtNavDate.mergeDate()) : null;
    }

    @Named("isNotNull")
    default YesOrNo isNotNull(Object field) {
        return field != null ? YesOrNo.Yes : YesOrNo.No;
    }

    @Named("booleanToYesOrNo")
    default YesOrNo booleanToYesOrNo(Boolean value) {
        return Boolean.TRUE.equals(value) ? YesOrNo.Yes : YesOrNo.No;
    }
}
