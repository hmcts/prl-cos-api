package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

class CaseUtilsTest {

    @Test
    void shouldMapCaseDetailsToCaseData() {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules(); // picks up ParameterNamesModule, JavaTimeModule, etc.

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        objectMapper.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        Map<String, Object> data = new HashMap<>();
        data.put("taskListVersion", "V2");
        data.put("caseTypeOfApplication", "C100");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .state("SUBMITTED_PAID")
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .data(data)
            .build();

        // Act
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        // Assert
        assertThat(caseData.getTaskListVersion()).isEqualTo("V2");
        assertThat(caseData.getCaseTypeOfApplication()).isEqualTo("C100");
        assertThat(caseData.getId()).isEqualTo(12345L);
        assertThat(caseData.getState()).isEqualTo(State.SUBMITTED_PAID);
        assertThat(caseData.getCreatedDate()).isNotNull();
        assertThat(caseData.getLastModifiedDate()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource
    void testIsC100CaseIssued(CaseData caseData, boolean expected) {
        assertThat(CaseUtils.isC100CaseIssued(caseData)).isEqualTo(expected);
    }

    private static Stream<Arguments> testIsC100CaseIssued() {
        return Stream.of(
            Arguments.of(caseData(C100_CASE_TYPE, null), false),
            Arguments.of(caseData(C100_CASE_TYPE, LocalDate.now()), true),
            Arguments.of(caseData(FL401_CASE_TYPE, null), false),
            Arguments.of(caseData(FL401_CASE_TYPE, LocalDate.now()), false)
        );
    }

    private static CaseData caseData(String caseType, LocalDate issueDate) {
        return CaseData.builder()
            .caseTypeOfApplication(caseType)
            .issueDate(issueDate)
            .build();
    }
}
