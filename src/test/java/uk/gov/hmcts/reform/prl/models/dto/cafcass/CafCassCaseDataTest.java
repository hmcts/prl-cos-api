package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class CafCassCaseDataTest {

    private final CafCassCaseData caseData = new CafCassCaseData();

    @Test
    void testBuildFullNameWhenFirstAndLastProvided() {
        String result = caseData.buildFullName("John", "Doe");
        Assertions.assertEquals("John Doe", result);
    }

    @Test
    void testBuildFullNameWhenFirstIsNull() {
        String result = caseData.buildFullName(null, "Doe");
        Assertions.assertEquals("Doe", result);
    }

    @Test
    void testBuildFullNameWhenLastIsNull() {
        String result = caseData.buildFullName("John", null);
        Assertions.assertEquals("John", result);
    }

    @Test
    void testBuildFullNameWhenBothAreNull() {
        String result = caseData.buildFullName(null, null);
        Assertions.assertEquals("", result);
    }

    @Test
    void testBuildFullNameWhenFirstAndLastAreEmpty() {
        String result = caseData.buildFullName("", "");
        Assertions.assertEquals("", result);
    }
}
