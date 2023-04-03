package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.RespondentWelshNeedsListEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentInterpreterNeeds;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AttendToCourtCheckerTest {

    @InjectMocks
    AttendToCourtChecker attendToCourtChecker;

    CaseData caseData;

    @Before
    public void setUp() {

        List<RespondentWelshNeedsListEnum> welshNeedsListEnum = new ArrayList<>();
        welshNeedsListEnum.add(RespondentWelshNeedsListEnum.speakWelsh);

        List<PartyEnum> party = new ArrayList<>();
        party.add(PartyEnum.respondent);

        RespondentInterpreterNeeds interpreterNeeds = RespondentInterpreterNeeds.builder()
                                 .party(party)
                                 .relationName("Test")
                                 .requiredLanguage("Cornish")
                .build();

        Element<RespondentInterpreterNeeds> wrappedInterpreter = Element.<RespondentInterpreterNeeds>builder().value(interpreterNeeds).build();
        List<Element<RespondentInterpreterNeeds>> interpreterList = Collections.singletonList(wrappedInterpreter);

        PartyDetails respondent = PartyDetails.builder()
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

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder().respondents(respondentList).build();
    }

    @Test
    public void isStartedTest() {

        Boolean bool = attendToCourtChecker.isStarted(caseData, "A");

        assertTrue(bool);
    }

    @Test
    public void mandatoryCompletedTest() {
        Boolean bool = attendToCourtChecker.isFinished(caseData, "A");

        assertTrue(bool);
    }
}
