package uk.gov.hmcts.reform.prl.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

class CaseUtilsTest {

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
