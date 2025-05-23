package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.mapstruct.Mapper;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.RelationshipDateComplex;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationDateInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationObjectType;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationOptionsInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface ApplicantRelationshipMapper {

    default RespondentRelationObjectType mapRelationType(CourtNavFl401 source) {
        String id = source.getFl401().getRelationshipWithRespondent().getRelationshipDescription().getId();
        return RespondentRelationObjectType.builder()
            .applicantRelationship(ApplicantRelationshipEnum.getDisplayedValueFromEnumString(id))
            .build();
    }

    default RespondentRelationDateInfo mapRelationDates(CourtNavFl401 source) {
        String id = source.getFl401().getRelationshipWithRespondent().getRelationshipDescription().getId();

        if ("noneOfAbove".equalsIgnoreCase(id)) {
            return null;
        }

        LocalDate startDate = parseDate(source.getFl401().getRelationshipWithRespondent().getRelationshipStartDate().mergeDate());
        LocalDate endDate = source.getFl401().getRelationshipWithRespondent().getRelationshipEndDate() != null
            ? parseDate(source.getFl401().getRelationshipWithRespondent().getRelationshipEndDate().mergeDate())
            : null;
        LocalDate ceremonyDate = source.getFl401().getRelationshipWithRespondent().getCeremonyDate() != null
            ? parseDate(source.getFl401().getRelationshipWithRespondent().getCeremonyDate().mergeDate())
            : null;

        return RespondentRelationDateInfo.builder()
            .relationStartAndEndComplexType(RelationshipDateComplex.builder()
                                                .relationshipDateComplexStartDate(startDate)
                                                .relationshipDateComplexEndDate(endDate)
                                                .build())
            .applicantRelationshipDate(ceremonyDate)
            .build();
    }

    default RespondentRelationOptionsInfo mapRelationOptions(CourtNavFl401 source) {
        String id = source.getFl401().getRelationshipWithRespondent().getRelationshipDescription().getId();

        if (!"noneOfAbove".equalsIgnoreCase(id)) {
            return null;
        }

        return RespondentRelationOptionsInfo.builder()
            .applicantRelationshipOptions(source.getFl401().getRelationshipWithRespondent().getRespondentsRelationshipToApplicant())
            .relationOptionsOther(source.getFl401().getRelationshipWithRespondent().getRespondentsRelationshipToApplicantOther())
            .build();
    }

    private LocalDate parseDate(String mergedDate) {
        return LocalDate.parse(mergedDate);
    }
}

