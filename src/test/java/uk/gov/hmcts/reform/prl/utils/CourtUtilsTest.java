package uk.gov.hmcts.reform.prl.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.CourtDetailsPilotEnum;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CourtUtilsTest {


    @Test
    public void testConvertToDynamicListFromMatchingString() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("submitCountyCourtSelection", CourtDetailsPilotEnum.southamptonCountyCourt);
        caseDataMap.put("courtList", CourtDetailsPilotEnum.exeterCountyCourt);
        caseDataMap = CourtUtils.checkCourtIsDynamicList(caseDataMap);

        assertNotNull(caseDataMap.get("submitCountyCourtSelection"));
        assertNotEquals(CourtDetailsPilotEnum.southamptonCountyCourt,caseDataMap.get("submitCountyCourtSelection"));
        assertNotNull(caseDataMap.get("courtList"));
        assertNotEquals(CourtDetailsPilotEnum.exeterCountyCourt,caseDataMap.get("courtList"));

    }

    @Test
    public void testConvertToDynamicListFromNoMatchingString() {
        Map<String, Object> caseDataMap = new HashMap<>();
        String courtValue = "test";
        caseDataMap.put("submitCountyCourtSelection", courtValue);
        caseDataMap.put("courtList", courtValue);
        caseDataMap = CourtUtils.checkCourtIsDynamicList(caseDataMap);

        assertNotNull(caseDataMap.get("submitCountyCourtSelection"));
        assertEquals(courtValue,caseDataMap.get("submitCountyCourtSelection"));
        assertNotNull(caseDataMap.get("courtList"));
        assertEquals(courtValue,caseDataMap.get("courtList"));

    }

    @Test
    public void testConvertToDynamicListFromDynamicList() {
        Map<String, Object> caseDataMap = new HashMap<>();
        String courtValue = "{value={code=407494, label=Croydon Combined Court - THE LAW COURTS, ALTYRE ROAD - CR9 5AB},"
            + " list_items=[{code=827534, label=Aberystwyth Justice Centre - TREFECHAN - SY23 1AS},"
            + " {code=29656, label=Bromley County Court and Family Court - COURT HOUSE, COLLEGE ROAD - BR1 3PX}]}\n";
        caseDataMap.put("submitCountyCourtSelection", courtValue);
        caseDataMap.put("courtList", courtValue);
        caseDataMap = CourtUtils.checkCourtIsDynamicList(caseDataMap);

        assertNotNull(caseDataMap.get("submitCountyCourtSelection"));
        assertEquals(courtValue,caseDataMap.get("submitCountyCourtSelection"));
        assertNotNull(caseDataMap.get("courtList"));
        assertEquals(courtValue,caseDataMap.get("courtList"));

    }
}
