package uk.gov.hmcts.reform.prl.services.validators;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.prl.services.validators.ChildLivingArrangementsValidator.REMOVAL_ERROR;
import static uk.gov.hmcts.reform.prl.services.validators.ChildLivingArrangementsValidator.SUBMISSION_ERROR;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

class ChildLivingArrangementsValidatorTest {

    private static final UUID APPLICANT_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID RESPONDENT_ID = UUID.fromString("00000000-0000-4000-8000-000000000002");
    private static final UUID OTHER_PERSON_ID = UUID.fromString("00000000-0000-4000-8000-000000000003");
    private static final UUID CHILD_ID = UUID.fromString("00000000-0000-4000-8000-000000000004");
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final ChildLivingArrangementsValidator validator = new ChildLivingArrangementsValidator(mapper);

    @ParameterizedTest
    @ValueSource(strings = {"applicants", "respondents", "otherPartyInTheCaseRevised"})
    void shouldBlockRemovalOfAnyPartyAChildLivesWith(String collection) {
        String selectedId = switch (collection) {
            case "applicants" -> APPLICANT_ID.toString();
            case "respondents" -> RESPONDENT_ID.toString();
            default -> OTHER_PERSON_ID.toString();
        };
        CaseData previous = caseData(selectedId);
        CaseData.CaseDataBuilder current = previous.toBuilder();
        switch (collection) {
            case "applicants" -> current.applicants(List.of());
            case "respondents" -> current.respondents(List.of());
            default -> current.otherPartyInTheCaseRevised(List.of());
        }

        assertThat(validator.validateRemoval(request(previous, current.build()))).containsExactly(REMOVAL_ERROR);
    }

    @Test
    void shouldAllowRemovalWhenChildLivesWithAnotherParty() {
        CaseData previous = caseData(RESPONDENT_ID.toString());
        CaseData current = previous.toBuilder().otherPartyInTheCaseRevised(List.of()).build();

        assertThat(validator.validateRemoval(request(previous, current))).isEmpty();
    }

    @Test
    void shouldAllowRemovalAfterLivingArrangementsAreChangedToAnExistingParty() {
        CaseData previous = caseData(OTHER_PERSON_ID.toString());
        CaseData current = caseData(RESPONDENT_ID.toString()).toBuilder().otherPartyInTheCaseRevised(List.of()).build();

        assertThat(validator.validateRemoval(request(previous, current))).isEmpty();
    }

    @Test
    void shouldBlockReplacingReferencedPersonWithANewEntryOfTheSameName() {
        CaseData previous = caseData(OTHER_PERSON_ID.toString());
        CaseData current = previous.toBuilder().otherPartyInTheCaseRevised(List.of(party(UUID.randomUUID()))).build();

        assertThat(validator.validateRemoval(request(previous, current))).containsExactly(REMOVAL_ERROR);
    }

    @Test
    void shouldNotAllowClearingTheSelectionToBypassRemovalValidation() {
        CaseData previous = caseData(OTHER_PERSON_ID.toString());
        CaseData current = caseData(null).toBuilder().otherPartyInTheCaseRevised(List.of()).build();

        assertThat(validator.validateRemoval(request(previous, current))).containsExactly(REMOVAL_ERROR);
    }

    @Test
    void shouldCheckEveryChildBeforeAllowingRemoval() {
        CaseData previous = caseData(RESPONDENT_ID.toString()).toBuilder()
            .newChildDetails(List.of(child(CHILD_ID, RESPONDENT_ID.toString()), child(UUID.randomUUID(), OTHER_PERSON_ID.toString())))
            .build();
        CaseData current = previous.toBuilder().otherPartyInTheCaseRevised(List.of()).build();

        assertThat(validator.validateRemoval(request(previous, current))).containsExactly(REMOVAL_ERROR);
    }

    @Test
    void shouldRejectMalformedStringSelectionInsteadOfInferringALivingArrangement() {
        CaseData previous = caseData(OTHER_PERSON_ID.toString());
        Map<String, Object> current = Map.of(
            "caseTypeOfApplication", "C100", "respondents", List.of(party(RESPONDENT_ID)),
            "otherPartyInTheCaseRevised", List.of(),
            "newChildDetails", List.of(Map.of("id", CHILD_ID.toString(), "value", Map.of(
                "whoDoesTheChildLiveWith", OTHER_PERSON_ID.toString()))));
        CallbackRequest request = CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder().data(mapper.convertValue(previous, Map.class)).build())
            .caseDetails(CaseDetails.builder().data(current).build()).build();

        assertThatThrownBy(() -> validator.validateRemoval(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("whoDoesTheChildLiveWith");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "00000000-0000-4000-8000-000000000099"})
    void shouldBlockSubmissionWithMissingOrUnavailableLivingWithSelection(String code) {
        assertThat(validator.validateSubmission(caseData(code))).containsExactly(SUBMISSION_ERROR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"00000000-0000-4000-8000-000000000001", "00000000-0000-4000-8000-000000000002",
        "00000000-0000-4000-8000-000000000003"})
    void shouldAllowSubmissionWithAValidCurrentParty(String code) {
        assertThat(validator.validateSubmission(caseData(code))).isEmpty();
    }

    @Test
    void shouldNotApplyToFl401OrLegacyChildRecords() {
        CaseData fl401 = caseData(null).toBuilder().caseTypeOfApplication("FL401").build();
        assertThat(validator.validateSubmission(fl401)).isEmpty();
        assertThat(validator.validateRemoval(request(fl401, fl401))).isEmpty();
        assertThat(validator.validateSubmission(CaseData.builder().caseTypeOfApplication("C100").build())).isEmpty();
    }

    private CaseData caseData(String selectedCode) {
        return CaseData.builder().caseTypeOfApplication("C100").taskListVersion("v3")
            .applicants(List.of(party(APPLICANT_ID))).respondents(List.of(party(RESPONDENT_ID)))
            .otherPartyInTheCaseRevised(List.of(party(OTHER_PERSON_ID)))
            .newChildDetails(List.of(child(CHILD_ID, selectedCode))).build();
    }

    private Element<PartyDetails> party(UUID id) {
        return element(id, PartyDetails.builder().firstName("Test").lastName("Parent").build());
    }

    private Element<ChildDetailsRevised> child(UUID id, String selectedCode) {
        return element(id, ChildDetailsRevised.builder().firstName("Test").lastName("Child")
            .whoDoesTheChildLiveWith(selectedCode == null ? null : DynamicList.builder()
                .value(DynamicListElement.builder().code(selectedCode).label("Test Parent").build()).build()).build());
    }

    private CallbackRequest request(CaseData previous, CaseData current) {
        return CallbackRequest.builder()
            .caseDetailsBefore(CaseDetails.builder().data(mapper.convertValue(previous, Map.class)).build())
            .caseDetails(CaseDetails.builder().data(mapper.convertValue(current, Map.class)).build()).build();
    }
}
