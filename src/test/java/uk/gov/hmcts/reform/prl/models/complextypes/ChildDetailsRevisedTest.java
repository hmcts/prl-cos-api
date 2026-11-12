package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChildDetailsRevisedTest {

    private static final String PARTY_ID = "00000000-0000-4000-8000-000000000001";
    private static final String CHILD_ID = "00000000-0000-4000-8000-000000000002";
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @ParameterizedTest
    @ValueSource(strings = {PARTY_ID, "Application - 27/10/2026 12:30:00"})
    void shouldRejectStringSelectionWhenMappingWholeCase(String code) {
        assertThatThrownBy(() -> CaseUtils.getCaseData(caseDetails(code), objectMapper))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("whoDoesTheChildLiveWith");
    }

    @Test
    void shouldPreserveObjectSelectionAndOptions() {
        Map<String, String> selectedParty = Map.of("code", PARTY_ID, "label", "Test Parent");
        Map<String, Object> dynamicList = Map.of("value", selectedParty, "list_items", List.of(selectedParty));

        CaseData caseData = CaseUtils.getCaseData(caseDetails(dynamicList), objectMapper);

        assertThat(caseData.getNewChildDetails().getFirst().getId().toString()).isEqualTo(CHILD_ID);
        assertThat(caseData.getNewChildDetails().getFirst().getValue().getDateOfBirth())
            .isEqualTo(LocalDate.of(2015, 1, 2));
        JsonNode actual = objectMapper.valueToTree(caseData.getNewChildDetails().getFirst()
                                                       .getValue().getWhoDoesTheChildLiveWith());
        assertThat(actual).isEqualTo(objectMapper.valueToTree(dynamicList));
    }

    @Test
    void shouldWriteObjectSelectionAndReadItAgain() {
        Map<String, String> selectedParty = Map.of("code", PARTY_ID, "label", "Test Parent");
        CaseData caseData = CaseUtils.getCaseData(caseDetails(
            Map.of("value", selectedParty, "list_items", List.of(selectedParty))), objectMapper);

        JsonNode serializedCase = objectMapper.valueToTree(caseData);
        JsonNode selection = serializedCase.at("/newChildDetails/0/value/whoDoesTheChildLiveWith");
        assertThat(selection.isObject()).isTrue();
        assertThat(selection.at("/value/code").asText()).isEqualTo(PARTY_ID);

        CaseData restored = objectMapper.convertValue(serializedCase, CaseData.class);
        assertThat(restored.getNewChildDetails()).isEqualTo(caseData.getNewChildDetails());
    }

    @Test
    void shouldReadMissingSelection() {
        CaseData caseData = CaseUtils.getCaseData(caseDetails(null), objectMapper);

        assertThat(caseData.getNewChildDetails().getFirst().getValue().getWhoDoesTheChildLiveWith()).isNull();
    }

    @ParameterizedTest
    @MethodSource("unsupportedSelections")
    void shouldRejectUnsupportedSelectionTypes(Object selection) {
        assertThatThrownBy(() -> CaseUtils.getCaseData(caseDetails(selection), objectMapper))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("whoDoesTheChildLiveWith");
    }

    private static Stream<Object> unsupportedSelections() {
        return Stream.of(123, true, List.of(PARTY_ID));
    }

    private CaseDetails caseDetails(Object selection) {
        Map<String, Object> child = new HashMap<>();
        child.put("firstName", "Test");
        child.put("lastName", "Child");
        child.put("dateOfBirth", "2015-01-02");
        child.put("whoDoesTheChildLiveWith", selection);
        return CaseDetails.builder()
            .id(12345L)
            .state("AWAITING_SUBMISSION_TO_HMCTS")
            .data(Map.of(
                "caseTypeOfApplication", "C100",
                "taskListVersion", "v3",
                "newChildDetails", List.of(Map.of("id", CHILD_ID, "value", child))))
            .build();
    }
}
