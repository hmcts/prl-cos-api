package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventCheckerTest {

    @Mock
    CaseNameChecker caseNameChecker;

    @Mock
    ApplicationTypeChecker applicationTypeChecker;

    @Mock
    HearingUrgencyChecker hearingUrgencyChecker;

    @Mock
    ApplicantsChecker applicantsChecker;

    @Mock
    ChildChecker childChecker;

    @Mock
    RespondentsChecker respondentsChecker;

    @Mock
    MiamChecker miamChecker;

    @Mock
    AllegationsOfHarmChecker allegationsOfHarmChecker;

    @Mock
    OtherPeopleInTheCaseChecker otherPeopleInTheCaseChecker;

    @Mock
    OtherProceedingsChecker otherProceedingsChecker;

    @Mock
    AttendingTheHearingChecker attendingTheHearingChecker;

    @Mock
    InternationalElementChecker internationalElementChecker;

    @Mock
    LitigationCapacityChecker litigationCapacityChecker;

    @Mock
    WelshLanguageRequirementsChecker welshLanguageRequirementsChecker;

    @Mock
    PdfChecker pdfChecker;

    @Mock
    SubmitAndPayChecker submitAndPayChecker;

    @Mock
    FL401ApplicantFamilyChecker fl401ApplicantFamilyChecker;

    @Mock
    FL401StatementOfTruthAndSubmitChecker fl401StatementOfTruthAndSubmitChecker;

    @InjectMocks
    private EventsChecker eventsChecker;

    private final CaseData caseData = CaseData.builder().build();

    @Before
    public void init() {
        eventsChecker.init();
    }


    @Test
    public void whenEventIsFinished_thenEventCheckerFinishedReturnsTrue() {
        when(applicantsChecker.isFinished(caseData)).thenReturn(true);
        assertTrue(eventsChecker.getEventStatus().containsKey(Event.APPLICANT_DETAILS));
        assertTrue(eventsChecker.isFinished(Event.APPLICANT_DETAILS, caseData));

    }

    @Test
    public void whenEventHasMandatoryCompleted_thenEventCheckerMandatoryReturnsTrue() {
        when(allegationsOfHarmChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        assertTrue(eventsChecker.hasMandatoryCompleted(Event.ALLEGATIONS_OF_HARM, caseData));

    }

    @Test
    public void whenEventIsStarted_thenEventCheckerStartedReturnsTrue() {
        when(fl401ApplicantFamilyChecker.isStarted(caseData)).thenReturn(true);
        assertTrue(eventsChecker.isStarted(Event.FL401_APPLICANT_FAMILY_DETAILS, caseData));
    }

    @Test
    public void checkGetEventStatus() {
        assertTrue(eventsChecker.getEventStatus().containsKey(Event.APPLICANT_DETAILS));
        assertTrue(eventsChecker.getEventStatus().containsValue(applicantsChecker));
    }




}
