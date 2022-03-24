package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.prl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.LITIGATION_CAPACITY;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.WELSH_LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ALLEGATIONS_OF_HARM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.APPLICANTS_DETAILS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ATTENDING_THE_HEARING_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.CHILD_DETAILS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.HEARING_URGENCY_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.INTERNATIONAL_ELEMENT_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.LITIGATION_CAPACITY_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.MIAM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.OTHER_PEOPLE_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.OTHER_PROCEEDINGS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.RESPONDENT_DETAILS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.TYPE_OF_APPLICATION_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.WELSH_LANGUAGE_ERROR;

@RunWith(MockitoJUnitRunner.class)
public class WhyCantISubmitListTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    MiamChecker miamChecker;

    @InjectMocks
    ChildChecker childChecker;

    @InjectMocks
    AllegationsOfHarmChecker allegationsOfHarmChecker;

    @InjectMocks
    ApplicantsChecker applicantsChecker;

    @InjectMocks
    ApplicationTypeChecker applicationTypeChecker;

    @InjectMocks
    RespondentsChecker respondentsChecker;

    @InjectMocks
    LitigationCapacityChecker litigationCapacityChecker;

    @InjectMocks
    InternationalElementChecker internationalElementChecker;

    @InjectMocks
    HearingUrgencyChecker hearingUrgencyChecker;

    @InjectMocks
    OtherPeopleInTheCaseChecker otherPeopleInTheCaseChecker;

    @InjectMocks
    AttendingTheHearingChecker attendingTheHearingChecker;

    @InjectMocks
    WelshLanguageRequirementsChecker welshLanguageRequirementsChecker;

    @InjectMocks
    OtherProceedingsChecker otherProceedingsChecker;

    private CaseData caseData;

    @Before
    public void setUp() {
        caseData = CaseData.builder().build();
    }

    @Test
    public void testMiamCheckerAddsError() {
        caseData = caseData.toBuilder().consentOrder(YesOrNo.No).build();
        miamChecker.isFinished(caseData);
        verify(taskErrorService).addEventError(MIAM, MIAM_ERROR, MIAM_ERROR.getError());
    }

    @Test
    public void testMiamCheckerDoesNotAddError() {
        caseData = caseData.toBuilder().consentOrder(YesOrNo.Yes).build();
        miamChecker.isFinished(caseData);
        verify(taskErrorService).removeError(MIAM_ERROR);
    }

    @Test
    public void testChildCheckerAddsError() {

        childChecker.isFinished(caseData);
        verify(taskErrorService).addEventError(CHILD_DETAILS, CHILD_DETAILS_ERROR, CHILD_DETAILS_ERROR.getError());
    }

    @Test
    public void testAllegationOfHarmCheckerAddsError() {

        allegationsOfHarmChecker.isFinished(caseData);
        verify(taskErrorService).addEventError(ALLEGATIONS_OF_HARM, ALLEGATIONS_OF_HARM_ERROR, ALLEGATIONS_OF_HARM_ERROR.getError());
    }

    @Test
    public void testApplicantsCheckerAddsError() {

        applicantsChecker.hasMandatoryCompleted(caseData);
        verify(taskErrorService).addEventError(APPLICANT_DETAILS, APPLICANTS_DETAILS_ERROR, APPLICANTS_DETAILS_ERROR.getError());
    }

    @Test
    public void testApplicationTypeCheckerAddsError() {

        applicationTypeChecker.isFinished(caseData);
        verify(taskErrorService).addEventError(TYPE_OF_APPLICATION, TYPE_OF_APPLICATION_ERROR, TYPE_OF_APPLICATION_ERROR.getError());
    }

    @Test
    public void testRespondentsCheckerAddsError() {

        respondentsChecker.isFinished(caseData);
        verify(taskErrorService).addEventError(RESPONDENT_DETAILS, RESPONDENT_DETAILS_ERROR, RESPONDENT_DETAILS_ERROR.getError());
    }

    @Test
    public void testLitigationCapacityCheckerAddsError() {
        caseData = caseData.toBuilder().litigationCapacityOtherFactors(YesOrNo.Yes).build();
        litigationCapacityChecker.isStarted(caseData);
        verify(taskErrorService).addEventError(LITIGATION_CAPACITY, LITIGATION_CAPACITY_ERROR, LITIGATION_CAPACITY_ERROR.getError());
    }

    @Test
    public void testInternationalElementCheckerAddsError() {
        caseData = caseData.toBuilder().habitualResidentInOtherState(YesOrNo.Yes).build();
        internationalElementChecker.isStarted(caseData);
        verify(taskErrorService,times(2)).addEventError(INTERNATIONAL_ELEMENT, INTERNATIONAL_ELEMENT_ERROR, INTERNATIONAL_ELEMENT_ERROR.getError());
    }

    @Test
    public void testHearingUrgencyCheckerAddsError() {

        hearingUrgencyChecker.isFinished(caseData);
        verify(taskErrorService).addEventError(HEARING_URGENCY, HEARING_URGENCY_ERROR, HEARING_URGENCY_ERROR.getError());
    }

    @Test
    public void testOtherPeopleCheckerAddsError() {
        caseData = caseData.toBuilder()
            .othersToNotify(List.of(Element.<PartyDetails>builder()
                                        .value(PartyDetails.builder().build()).build())).build();
        otherPeopleInTheCaseChecker.isStarted(caseData);
        verify(taskErrorService).addEventError(OTHER_PEOPLE_IN_THE_CASE, OTHER_PEOPLE_ERROR, OTHER_PEOPLE_ERROR.getError());
    }

    @Test
    public void testAttendingTheHearingCheckerAddsError() {
        caseData = caseData.toBuilder().isWelshNeeded(YesOrNo.Yes).build();
        attendingTheHearingChecker.isStarted(caseData);
        verify(taskErrorService).addEventError(ATTENDING_THE_HEARING, ATTENDING_THE_HEARING_ERROR, ATTENDING_THE_HEARING_ERROR.getError());
    }

    @Test
    public void testWelshLanguageReqCheckerAddsError() {
        caseData = caseData.toBuilder().welshLanguageRequirement(YesOrNo.Yes).build();
        welshLanguageRequirementsChecker.isStarted(caseData);
        verify(taskErrorService).addEventError(WELSH_LANGUAGE_REQUIREMENTS, WELSH_LANGUAGE_ERROR, WELSH_LANGUAGE_ERROR.getError());
    }

    @Test
    public void testOtherProceedingsCheckerAddsError() {
        caseData = caseData.toBuilder().previousOrOngoingProceedingsForChildren(YesNoDontKnow.yes).build();
        otherProceedingsChecker.isStarted(caseData);
        verify(taskErrorService).addEventError(OTHER_PROCEEDINGS, OTHER_PROCEEDINGS_ERROR, OTHER_PROCEEDINGS_ERROR.getError());
    }
}
