package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.InterpreterNeed;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.GoingToCourt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterpreterNeedsMapperTest {

    private final InterpreterNeedsMapper mapper = Mappers.getMapper(InterpreterNeedsMapper.class);

    @Test
    void shouldReturnEmptyListWhenInterpreterNotRequired() {
        GoingToCourt goingToCourt = GoingToCourt.builder()
            .isInterpreterRequired(false)
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .goingToCourt(goingToCourt)
                       .build())
            .build();

        List<Element<InterpreterNeed>> result = mapper.mapInterpreterNeeds(source);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldMapInterpreterNeedWithLanguageOnly() {
        GoingToCourt goingToCourt = GoingToCourt.builder()
            .isInterpreterRequired(true)
            .interpreterLanguage("Polish")
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .goingToCourt(goingToCourt)
                       .build())
            .build();

        List<Element<InterpreterNeed>> result = mapper.mapInterpreterNeeds(source);
        assertEquals(1, result.size());
        InterpreterNeed need = result.getFirst().getValue();
        assertEquals("Polish", need.getLanguage());
        assertEquals(1, need.getParty().size());
        assertEquals("applicant", need.getParty().getFirst().name().toLowerCase());
    }

    @Test
    void shouldMapInterpreterNeedWithLanguageAndDialect() {
        GoingToCourt goingToCourt = GoingToCourt.builder()
            .isInterpreterRequired(true)
            .interpreterLanguage("Chinese")
            .interpreterDialect("Cantonese")
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder()
                       .goingToCourt(goingToCourt)
                       .build())
            .build();

        List<Element<InterpreterNeed>> result = mapper.mapInterpreterNeeds(source);
        assertEquals(1, result.size());
        InterpreterNeed need = result.getFirst().getValue();
        assertEquals("Chinese - Cantonese", need.getLanguage());
    }
}

