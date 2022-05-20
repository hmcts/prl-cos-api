package uk.gov.hmcts.reform.prl.services.validators;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_APPLICANT_FAMILY_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_SOT_AND_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_UPLOAD_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.prl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.LITIGATION_CAPACITY;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.Event.SUBMIT_AND_PAY;
import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.VIEW_PDF_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.WELSH_LANGUAGE_REQUIREMENTS;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EventsChecker.class, LocalValidatorFactoryBean.class})
@TestInstance(PER_CLASS)
class EventsCheckerTest {

    @MockBean
    CaseNameChecker caseNameChecker;

    @MockBean
    ApplicationTypeChecker applicationTypeChecker;

    @MockBean
    HearingUrgencyChecker hearingUrgencyChecker;

    @MockBean
    ApplicantsChecker applicantsChecker;

    @MockBean
    ChildChecker childChecker;

    @MockBean
    RespondentsChecker respondentsChecker;

    @MockBean
    MiamChecker miamChecker;

    @MockBean
    AllegationsOfHarmChecker allegationsOfHarmChecker;

    @MockBean
    OtherPeopleInTheCaseChecker otherPeopleInTheCaseChecker;

    @MockBean
    OtherProceedingsChecker otherProceedingsChecker;

    @MockBean
    AttendingTheHearingChecker attendingTheHearingChecker;

    @MockBean
    InternationalElementChecker internationalElementChecker;

    @MockBean
    LitigationCapacityChecker litigationCapacityChecker;

    @MockBean
    WelshLanguageRequirementsChecker welshLanguageRequirementsChecker;

    @MockBean
    PdfChecker pdfChecker;

    @MockBean
    SubmitAndPayChecker submitAndPayChecker;

    @MockBean
    FL401ApplicantFamilyChecker fl401ApplicantFamilyChecker;

    @MockBean
    FL401StatementOfTruthAndSubmitChecker fl401StatementOfTruthAndSubmitChecker;

    @MockBean
    SubmitChecker submitChecker;

    @Autowired
    private EventsChecker eventsChecker;

    private final CaseData caseData = CaseData.builder().build();

    @ParameterizedTest
    @MethodSource("getEventsValidators")
    void shouldCheckEventIsCompletedEvent(Event event, EventChecker validator) {
        final boolean isCompleted = RandomUtils.nextBoolean();

        when(validator.isFinished(caseData)).thenReturn(isCompleted);

        assertThat(eventsChecker.isFinished(event, caseData)).isEqualTo(isCompleted);

        verify(validator).isFinished(caseData);
    }

    @ParameterizedTest
    @MethodSource("getEventsValidators")
    void shouldCheckEventIsStarted(Event event, EventChecker validator) {
        final boolean isStartedExpected = RandomUtils.nextBoolean();

        when(validator.isStarted(caseData)).thenReturn(isStartedExpected);

        assertThat(eventsChecker.isStarted(event, caseData)).isEqualTo(isStartedExpected);

        verify(validator).isStarted(caseData);
    }

    @ParameterizedTest
    @MethodSource("getEventsValidators")
    void shouldCheckEventHasMAndatoryCompleted(Event event, EventChecker validator) {
        final boolean hasMandatory = RandomUtils.nextBoolean();

        when(validator.hasMandatoryCompleted(caseData)).thenReturn(hasMandatory);

        assertThat(eventsChecker.hasMandatoryCompleted(event, caseData)).isEqualTo(hasMandatory);

        verify(validator).hasMandatoryCompleted(caseData);
    }

    @AfterEach
    void verifyNoMoreInteractionsWithValidators() {
        verifyNoMoreInteractions(
            caseNameChecker,
            applicationTypeChecker,
            hearingUrgencyChecker,
            applicantsChecker,
            childChecker,
            respondentsChecker,
            miamChecker,
            allegationsOfHarmChecker,
            otherPeopleInTheCaseChecker,
            otherProceedingsChecker,
            attendingTheHearingChecker,
            internationalElementChecker,
            litigationCapacityChecker,
            welshLanguageRequirementsChecker,
            pdfChecker,
            submitAndPayChecker,
            fl401ApplicantFamilyChecker,
            fl401StatementOfTruthAndSubmitChecker,
            submitChecker);
    }

    private Stream<Arguments> getEventsValidators() {
        return Stream.of(
            Arguments.of(CASE_NAME, caseNameChecker),
            Arguments.of(TYPE_OF_APPLICATION, applicationTypeChecker),
            Arguments.of(HEARING_URGENCY, hearingUrgencyChecker),
            Arguments.of(APPLICANT_DETAILS, applicantsChecker),
            Arguments.of(CHILD_DETAILS, childChecker),
            Arguments.of(RESPONDENT_DETAILS, respondentsChecker),
            Arguments.of(MIAM, miamChecker),
            Arguments.of(ALLEGATIONS_OF_HARM, allegationsOfHarmChecker),
            Arguments.of(OTHER_PEOPLE_IN_THE_CASE, otherPeopleInTheCaseChecker),
            Arguments.of(OTHER_PROCEEDINGS, otherProceedingsChecker),
            Arguments.of(ATTENDING_THE_HEARING, attendingTheHearingChecker),
            Arguments.of(INTERNATIONAL_ELEMENT, internationalElementChecker),
            Arguments.of(LITIGATION_CAPACITY, litigationCapacityChecker),
            Arguments.of(WELSH_LANGUAGE_REQUIREMENTS, welshLanguageRequirementsChecker),
            Arguments.of(VIEW_PDF_DOCUMENT, pdfChecker),
            Arguments.of(SUBMIT_AND_PAY, submitAndPayChecker),
            Arguments.of(SUBMIT, submitChecker),
            Arguments.of(FL401_APPLICANT_FAMILY_DETAILS, fl401ApplicantFamilyChecker),
            Arguments.of(FL401_UPLOAD_DOCUMENTS, pdfChecker),
            Arguments.of(FL401_SOT_AND_SUBMIT, fl401StatementOfTruthAndSubmitChecker));
    }

}
