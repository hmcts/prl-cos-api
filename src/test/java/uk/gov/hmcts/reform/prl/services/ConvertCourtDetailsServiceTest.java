package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ConvertCourtDetailsServiceTest {

    @InjectMocks
    ConvertCourtDetailsService convertCourtDetailsService;

    @Test
    public void testConvertToDynamicListFromString() {
        Map<String, Object> caseDataMap = new HashMap<>();
        String courtValue = "southamptonCountyCourt";
        caseDataMap.put("submitCountyCourtSelection", courtValue);
        caseDataMap = convertCourtDetailsService.verifyIfDynamicList(caseDataMap, "submitCountyCourtSelection");

        assertNotNull(caseDataMap.get("submitCountyCourtSelection"));
        assertNotEquals(courtValue,caseDataMap.get("submitCountyCourtSelection"));

    }

    @Test
    public void testConvertToDynamicListFromDynamicList() {
        Map<String, Object> caseDataMap = new HashMap<>();
        String courtValue = "{value={code=407494, label=Croydon Combined Court - THE LAW COURTS, ALTYRE ROAD - CR9 5AB}, \" +\n" +
            "            \"list_items=[{code=827534, label=Aberystwyth Justice Centre - TREFECHAN - SY23 1AS}, {code=29656, label=Bromley County Court and Family Court - COURT HOUSE, COLLEGE ROAD - BR1 3PX}]}";
        caseDataMap.put("submitCountyCourtSelection", courtValue);
        caseDataMap = convertCourtDetailsService.verifyIfDynamicList(caseDataMap, "submitCountyCourtSelection");

        assertNotNull(caseDataMap.get("submitCountyCourtSelection"));
        assertEquals(courtValue,caseDataMap.get("submitCountyCourtSelection"));

    }
}
