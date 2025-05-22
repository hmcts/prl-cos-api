package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.mapstruct.Mapper;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SpecialMeasuresEnum;

import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Mapper(componentModel = "spring")
public interface AttendHearingMapper {

    default AttendHearing mapAttendHearing(CourtNavFl401 source) {
        var goingToCourt = source.getFl401().getGoingToCourt();

        return AttendHearing.builder()
            .isInterpreterNeeded(Boolean.TRUE.equals(goingToCourt.getIsInterpreterRequired()) ? Yes : No)
            .interpreterNeeds(null) // already mapped via InterpreterNeedsMapper separately
            .isDisabilityPresent(goingToCourt.isAnyDisabilityNeeds() ? Yes : No)
            .adjustmentsRequired(goingToCourt.isAnyDisabilityNeeds() ? goingToCourt.getDisabilityNeedsDetails() : null)
            .isSpecialArrangementsRequired(goingToCourt.getAnySpecialMeasures() != null ? Yes : No)
            .specialArrangementsRequired(
                goingToCourt.getAnySpecialMeasures() != null
                    ? goingToCourt.getAnySpecialMeasures().stream()
                    .map(SpecialMeasuresEnum::getDisplayedValue)
                    .collect(Collectors.joining(","))
                    : null)
            .build();
    }
}
