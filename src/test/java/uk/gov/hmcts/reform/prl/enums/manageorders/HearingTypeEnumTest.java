package uk.gov.hmcts.reform.prl.enums.manageorders;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HearingTypeEnumTest {

    @Test
    void getIdFromValue() {
        assertEquals(HearingTypeEnum.ABA5ALL, HearingTypeEnum.getIdFromValue("Allocation"));
        assertEquals(HearingTypeEnum.ABA5BRE, HearingTypeEnum.getIdFromValue("Breach"));
    }

    @Test
    void getDisplayedValueInWelshFromDisplayValueString() {
        assertEquals("Dyrannu", HearingTypeEnum.getDisplayedValueInWelshFromDisplayValueString("Allocation"));
        assertEquals("Cais", HearingTypeEnum.getDisplayedValueInWelshFromDisplayValueString("Application"));
        assertEquals("UNKNOWN", HearingTypeEnum.getDisplayedValueInWelshFromDisplayValueString("UNKNOWN"));
    }
}
