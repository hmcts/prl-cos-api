package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.RelationshipDateComplex;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationDateInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationObjectType;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationOptionsInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavRelationShipToRespondent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantRelationshipDescriptionEnum;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApplicantRelationshipMapperTest {

    private final ApplicantRelationshipMapper mapper = Mappers.getMapper(ApplicantRelationshipMapper.class);

    @Test
    void shouldMapRespondentRelationshipType() {
        CourtNavRelationShipToRespondent rel = CourtNavRelationShipToRespondent.builder()
            .relationshipDescription(ApplicantRelationshipDescriptionEnum.marriedOrCivil)
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder().relationshipWithRespondent(rel).build())
            .build();

        RespondentRelationObjectType result = mapper.mapRelationType(source);
        assertEquals(ApplicantRelationshipEnum.marriedOrCivil, result.getApplicantRelationship());
    }

    @Test
    void shouldMapDatesWhenRelationshipIsNotNoneOfAbove() {
        CourtNavRelationShipToRespondent rel = CourtNavRelationShipToRespondent.builder()
            .relationshipDescription(ApplicantRelationshipDescriptionEnum.engagedOrProposed)
            .relationshipStartDate(CourtNavDate.builder().day(1).month(1).year(2020).build())
            .relationshipEndDate(CourtNavDate.builder().day(31).month(12).year(2022).build())
            .ceremonyDate(CourtNavDate.builder().day(15).month(6).year(2021).build())
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder().relationshipWithRespondent(rel).build())
            .build();

        RespondentRelationDateInfo result = mapper.mapRelationDates(source);

        RelationshipDateComplex dates = result.getRelationStartAndEndComplexType();
        assertEquals(LocalDate.of(2020, 1, 1), dates.getRelationshipDateComplexStartDate());
        assertEquals(LocalDate.of(2022, 12, 31), dates.getRelationshipDateComplexEndDate());
        assertEquals(LocalDate.of(2021, 6, 15), result.getApplicantRelationshipDate());
    }

    @Test
    void shouldSkipDateMappingWhenRelationshipIsNoneOfAbove() {
        CourtNavRelationShipToRespondent rel = CourtNavRelationShipToRespondent.builder()
            .relationshipDescription(ApplicantRelationshipDescriptionEnum.noneOfAbove)
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder().relationshipWithRespondent(rel).build())
            .build();

        RespondentRelationDateInfo result = mapper.mapRelationDates(source);
        assertNull(result);
    }

    @Test
    void shouldMapRelationOptionsWhenRelationshipIsNoneOfAbove() {
        CourtNavRelationShipToRespondent rel = CourtNavRelationShipToRespondent.builder()
            .relationshipDescription(ApplicantRelationshipDescriptionEnum.noneOfAbove)
            .respondentsRelationshipToApplicant(ApplicantRelationshipOptionsEnum.other)
            .respondentsRelationshipToApplicantOther("Trusted third party")
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder().relationshipWithRespondent(rel).build())
            .build();

        RespondentRelationOptionsInfo result = mapper.mapRelationOptions(source);
        assertEquals(ApplicantRelationshipOptionsEnum.other, result.getApplicantRelationshipOptions());
        assertEquals("Trusted third party", result.getRelationOptionsOther());
    }

    @Test
    void shouldReturnNullForRelationOptionsIfNotNoneOfAbove() {
        CourtNavRelationShipToRespondent rel = CourtNavRelationShipToRespondent.builder()
            .relationshipDescription(ApplicantRelationshipDescriptionEnum.marriedOrCivil)
            .respondentsRelationshipToApplicant(ApplicantRelationshipOptionsEnum.niece)
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder().relationshipWithRespondent(rel).build())
            .build();

        RespondentRelationOptionsInfo result = mapper.mapRelationOptions(source);
        assertNull(result);
    }
}
