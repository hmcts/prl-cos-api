package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.RespondentWelshNeedsListEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentInterpreterNeeds;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class AttendToCourtCheckerTest {

    @InjectMocks
    private AttendToCourtChecker attendToCourtChecker;

    @Mock
    private RespondentTaskErrorService respondentTaskErrorService;

    private PartyDetails respondent;

    @BeforeEach
    void setUp() {

        List<RespondentWelshNeedsListEnum> welshNeedsListEnum = new ArrayList<>();
        welshNeedsListEnum.add(RespondentWelshNeedsListEnum.speakWelsh);

        List<PartyEnum> party = new ArrayList<>();
        party.add(PartyEnum.respondent);

        RespondentInterpreterNeeds interpreterNeeds = RespondentInterpreterNeeds.builder()
            .party(party)
            .relationName("Test")
            .requiredLanguage("Cornish")
            .build();

        Element<RespondentInterpreterNeeds> wrappedInterpreter = Element.<RespondentInterpreterNeeds>builder().value(
            interpreterNeeds).build();
        List<Element<RespondentInterpreterNeeds>> interpreterList = Collections.singletonList(wrappedInterpreter);

        respondent = PartyDetails.builder()
            .response(Response.builder()
                          .attendToCourt(AttendToCourt.builder()
                                             .respondentWelshNeeds(Yes)
                                             .respondentWelshNeedsList(welshNeedsListEnum)
                                             .isRespondentNeededInterpreter(Yes)
                                             .respondentInterpreterNeeds(interpreterList)
                                             .haveAnyDisability(Yes)
                                             .disabilityNeeds("Test")
                                             .respondentSpecialArrangements(Yes)
                                             .respondentSpecialArrangementDetails("Test")
                                             .respondentIntermediaryNeeds(Yes)
                                             .respondentIntermediaryNeedDetails("Test")
                                             .build())
                          .build())
            .build();
    }

    @Test
    void isStartedTest() {
        assertTrue(attendToCourtChecker.isStarted(respondent, true));
    }

    @Test
    void mandatoryCompletedTest() {
        assertTrue(attendToCourtChecker.isFinished(respondent, true));
    }
}
