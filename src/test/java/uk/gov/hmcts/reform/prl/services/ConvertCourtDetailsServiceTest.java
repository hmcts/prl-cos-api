package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ConvertCourtDetailsServiceTest {

    @Mock
    LocationRefDataService locationRefDataService;

    @InjectMocks
    ConvertCourtDetailsService convertCourtDetailsService;

    @Test
    public void testConvertToDynamicList() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("submitCountyCourtSelection", "southamptonCountyCourt");
        caseDataMap = convertCourtDetailsService.convertToDynamicList(caseDataMap, "submitCountyCourtSelection");

        assertNotNull(caseDataMap.get("submitCountyCourtSelection"));
        assertNotNull(((DynamicList)caseDataMap.get("submitCountyCourtSelection")).getListItems());

    }
}
