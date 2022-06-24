package uk.gov.hmcts.reform.prl.services.tab.alltabs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AllTabServiceImplTest {

    @InjectMocks
    AllTabServiceImpl allTabService;

    @Mock
    ApplicationsTabService applicationsTabService;

    @Mock
    CoreCaseDataService coreCaseDataService;

    @Mock
    ConfidentialityTabService confidentialityTabService;

    @Mock
    @Qualifier("caseSummaryTab")
    CaseSummaryTabService caseSummaryTabService;

    private static final CaseData CASE_DATA = mock(CaseData.class);
    Map<String, Object> applicaionFieldsMap = Map.of(
        "field1", "value1",
        "field2", "value2",
        "field3", "value3"
        );
    Map<String, Object> summaryTabFields = Map.of(
        "field4", "value4",
        "field5", "value5"
    );

    @Before
    public void setUp() {
        when(applicationsTabService.updateTab(CASE_DATA)).thenReturn(applicaionFieldsMap);
        when(caseSummaryTabService.updateTab(CASE_DATA)).thenReturn(summaryTabFields);
        when(CASE_DATA.getDateSubmitted()).thenReturn("2022-02-02");
        when(CASE_DATA.getCourtName()).thenReturn("TEST COURT");
        when(CASE_DATA.getCourtId()).thenReturn("COURT_!");
        doNothing().when(coreCaseDataService).triggerEvent(anyString(), anyString(),anyLong(), anyString(), anyMap());
    }

    @Test
    public void testAllTabsService() {

        allTabService.updateAllTabs(CASE_DATA);

        verify(coreCaseDataService).triggerEvent(anyString(), anyString(),anyLong(), anyString(), anyMap());
        verify(applicationsTabService).updateTab(CASE_DATA);
        verify(caseSummaryTabService).updateTab(CASE_DATA);
    }

    @Test
    public void testAllTabsServiceIncConfTab() {

        allTabService.updateAllTabsIncludingConfTab(CASE_DATA);

        verify(coreCaseDataService).triggerEvent(anyString(), anyString(),anyLong(), anyString(), anyMap());
        verify(applicationsTabService).updateTab(CASE_DATA);
        verify(caseSummaryTabService).updateTab(CASE_DATA);
    }
}
