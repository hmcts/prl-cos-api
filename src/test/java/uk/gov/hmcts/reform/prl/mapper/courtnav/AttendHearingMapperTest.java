package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.GoingToCourt;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SpecialMeasuresEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AttendHearingMapperTest {

    private final AttendHearingMapper mapper = Mappers.getMapper(AttendHearingMapper.class);


    @Test
    void shouldMapAllFieldsCorrectlyWhenPresent() {
        GoingToCourt goingToCourt = GoingToCourt.builder()
            .isInterpreterRequired(true)
            .anyDisabilityNeeds(true)
            .disabilityNeedsDetails("Wheelchair access")
            .anySpecialMeasures(List.of(SpecialMeasuresEnum.shieldedByScreen, SpecialMeasuresEnum.joinByVideoLink))
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder().goingToCourt(goingToCourt).build())
            .build();

        AttendHearing result = mapper.mapAttendHearing(source);

        assertEquals(YesOrNo.Yes, result.getIsInterpreterNeeded());
        assertEquals(YesOrNo.Yes, result.getIsDisabilityPresent());
        assertEquals("Wheelchair access", result.getAdjustmentsRequired());
        assertEquals(YesOrNo.Yes, result.getIsSpecialArrangementsRequired());
        assertEquals(
            "To be shielded by a privacy screen in the courtroom,"
                + "To join the hearing by video link rather than in person",
            result.getSpecialArrangementsRequired()
        );
    }

    @Test
    void shouldHandleNullOrFalseFieldsGracefully() {
        GoingToCourt goingToCourt = GoingToCourt.builder()
            .isInterpreterRequired(false)
            .anyDisabilityNeeds(false)
            .anySpecialMeasures(null)
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder().goingToCourt(goingToCourt).build())
            .build();

        AttendHearing result = mapper.mapAttendHearing(source);

        assertEquals(YesOrNo.No, result.getIsInterpreterNeeded());
        assertEquals(YesOrNo.No, result.getIsDisabilityPresent());
        assertNull(result.getAdjustmentsRequired());
        assertEquals(YesOrNo.No, result.getIsSpecialArrangementsRequired());
        assertNull(result.getSpecialArrangementsRequired());
    }
}
