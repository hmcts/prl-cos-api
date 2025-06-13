package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class RespondentsCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    RespondentsChecker respondentsChecker;

    Address address;

    @BeforeEach
    void setUp() {
        address = Address.builder()
            .addressLine1("55 Test Street")
            .postTown("Town")
            .postCode("N12 3BH")
            .build();
    }

    @Test
    void whenNoCaseDataThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(respondentsChecker.isStarted(caseData));
    }

    @Test
    void whenNoCaseDataThenIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(respondentsChecker.isFinished(caseData));
    }

    @Test
    void whenIncompleteCaseDataThenIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder().caseTypeOfApplication(FL401_CASE_TYPE)
            .respondentsFL401(PartyDetails.builder().build()).build();

        assertFalse(respondentsChecker.isFinished(caseData));
    }

    @Test
    void whenNoCaseDataThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(respondentsChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenMinimalRelevantCaseDataThenIsStartedReturnsTrue() {
        PartyDetails respondent = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedRespondents);

        CaseData caseData = CaseData.builder()
            .respondents(applicantList)
            .build();

        assertTrue(respondentsChecker.isStarted(caseData));
    }

    @Test
    void whenIncompleteCaseDataValidateMandatoryFieldsForRespondentReturnsFalse() {
        PartyDetails respondent = PartyDetails.builder().firstName("TestName").build();

        CaseData caseData = CaseData.builder().caseTypeOfApplication("Test")
            .build();

        assertFalse(respondentsChecker.validateMandatoryFieldsForRespondent(respondent, caseData.getCaseTypeOfApplication()));
    }

    @Test
    void whenIncompleteC100CaseDataValidateMandatoryFieldsForRespondentReturnsFalse() {
        PartyDetails respondent = PartyDetails.builder()
            .firstName("TestName")
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();

        assertFalse(respondentsChecker.validateMandatoryFieldsForRespondent(respondent, caseData.getCaseTypeOfApplication()));
    }

    @Test
    void whenIncompleteCaseDataRespondentsDetailsStartedReturnsTrue() {
        PartyDetails respondent = PartyDetails.builder().firstName("TestName").build();

        assertTrue(respondentsChecker.respondentDetailsStarted(respondent));
    }

    @Test
    void whenNoDataIsGenderCompletedFieldShouldEmpty() {
        PartyDetails respondent = PartyDetails.builder().build();
        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isGenderCompleted(respondent,fields);
        assertFalse(fields.size() > 1 && !fields.getFirst().isEmpty());
    }

    @Test
    void whenDataPresentIsGenderCompletedFieldShouldNotNull() {
        PartyDetails respondent = PartyDetails.builder()
            .firstName("TestName")
            .gender(Gender.other)
            .otherGender("Testing")
            .build();
        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isGenderCompleted(respondent,fields);
        assertTrue(fields.size() > 1 && !fields.getFirst().isEmpty());

    }

    @Test
    void whenNoDataIsPlaceOfBirthCompletedFieldShouldEmpty() {
        PartyDetails respondent = PartyDetails.builder().build();
        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isPlaceOfBirthCompleted(respondent,fields);
        assertFalse(fields.size() > 1 && !fields.getFirst().isEmpty());
    }

    @Test
    void whenDataPresentIsPlaceOfBirthCompletedFieldShouldNotNull() {
        PartyDetails respondent = PartyDetails.builder()
            .firstName("TestName")
            .isPlaceOfBirthKnown(Yes)
            .placeOfBirth("testing")
            .build();

        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isPlaceOfBirthCompleted(respondent,fields);
        assertTrue(fields.size() > 1 && !fields.getFirst().isEmpty());

    }

    @Test
    void whenNoDataIsCurrentAddressCompletedFieldShouldEmpty() {
        PartyDetails respondent = PartyDetails.builder().build();
        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isCurrentAddressCompleted(respondent,fields);
        assertFalse(fields.size() > 1 && !fields.getFirst().isEmpty());
    }

    @Test
    void whenDataPresentIsCurrentAddressCompletedFieldShouldNotNull() {
        PartyDetails respondent = PartyDetails.builder()
            .isCurrentAddressKnown(Yes)
            .isPlaceOfBirthKnown(Yes)
            .placeOfBirth("testing")
            .address(address)
            .build();

        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isCurrentAddressCompleted(respondent,fields);
        assertTrue(fields.size() > 1 && !fields.getFirst().isEmpty());

    }

    @Test
    void whenNoDataIsCanYouProvideEmailAddressCompletedFieldShouldEmpty() {
        PartyDetails respondent = PartyDetails.builder().build();
        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isCanYouProvideEmailAddressCompleted(respondent,fields);
        assertFalse(fields.size() > 1 && !fields.getFirst().isEmpty());
    }

    @Test
    void whenDataPresentIsCanYouProvideEmailAddressCompletedFieldShouldNotNull() {
        PartyDetails respondent = PartyDetails.builder()
            .canYouProvideEmailAddress(Yes)
            .email("testing@gmail.com")
            .build();

        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isCanYouProvideEmailAddressCompleted(respondent,fields);
        assertTrue(fields.size() > 1 && !fields.getFirst().isEmpty());

    }

    @Test
    void whenNoDataIsAtAddressLessThan5YearsCompletedFieldShouldEmpty() {
        PartyDetails respondent = PartyDetails.builder().build();
        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isAtAddressLessThan5YearsCompleted(respondent,fields);
        assertFalse(fields.size() > 1 && !fields.getFirst().isEmpty());
    }

    @Test
    void whenDataPresentIsAtAddressLessThan5YearsCompletedFieldShouldNotNull() {
        PartyDetails respondent = PartyDetails.builder()
            .isAtAddressLessThan5YearsWithDontKnow(YesNoDontKnow.yes)
            .addressLivedLessThan5YearsDetails("testing")
            .build();

        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isAtAddressLessThan5YearsCompleted(respondent,fields);
        assertTrue(fields.size() > 1 && !fields.getFirst().isEmpty());

    }

    @Test
    void whenNoDataIsDoTheyHaveLegalRepresentationCompletedFieldShouldEmpty() {
        PartyDetails respondent = PartyDetails.builder().build();
        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isDoTheyHaveLegalRepresentationCompleted(respondent,fields);
        assertFalse(fields.size() > 1 && !fields.getFirst().isEmpty());
    }

    @Test
    void whenDataPresentIsDoTheyHaveLegalRepresentationCompletedFieldShouldNotNull() {
        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorEmail("solicitor@gmail.com")
            .build();

        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isDoTheyHaveLegalRepresentationCompleted(respondent,fields);
        assertTrue(fields.size() > 1 && !fields.getFirst().isEmpty());

    }

    @Test
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(respondentsChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
