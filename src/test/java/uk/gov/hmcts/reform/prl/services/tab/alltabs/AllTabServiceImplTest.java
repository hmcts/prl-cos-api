package uk.gov.hmcts.reform.prl.services.tab.alltabs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

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
    CcdCoreCaseDataService coreCaseDataServiceCcdClient;

    @Mock
    ObjectMapper objectMapper;

    private StartEventResponse startEventResponse;
    private List<Element<CaseInvite>> caseInvites;

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
        CaseInvite caseInvite1 = new CaseInvite("abc1@de.com", "ABCD1234", "abc1",
                                                UUID.randomUUID(), YesOrNo.Yes
        );
        CaseInvite caseInvite2 = new CaseInvite("abc2@de.com", "WXYZ5678", "abc2",
                                                UUID.randomUUID(), YesOrNo.No
        );
        caseInvites = List.of(element(caseInvite1), element(caseInvite2));

        startEventResponse = StartEventResponse.builder().build();

        when(applicationsTabService.updateTab(CASE_DATA)).thenReturn(applicaionFieldsMap);
        when(caseSummaryTabService.updateTab(CASE_DATA)).thenReturn(summaryTabFields);
        when(CASE_DATA.getCaseTypeOfApplication()).thenReturn("C100");
        when(CASE_DATA.getDateSubmitted()).thenReturn("2022-02-02");
        when(CASE_DATA.getCourtName()).thenReturn("TEST COURT");
        when(CASE_DATA.getCourtId()).thenReturn("COURT_!");
        when(CASE_DATA.getApplicantsFL401()).thenReturn(PartyDetails.builder().build());

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

    @Test
    public void testUpdateAllTabsIncludingConfTabRefactored() {
        allTabService.updateAllTabsIncludingConfTabRefactored("auth", "caseId", startEventResponse, EventRequestData
            .builder()
            .build(), CASE_DATA);

        verify(applicationsTabService).updateTab(CASE_DATA);
        verify(caseSummaryTabService).updateTab(CASE_DATA);
    }

    @Test
    public void testAllTabsServiceIncConfTabWithDocs() {
        when(CASE_DATA.getC8Document()).thenReturn(Document.builder().build());
        when(CASE_DATA.getC1ADocument()).thenReturn(Document.builder().build());
        when(CASE_DATA.getC8WelshDocument()).thenReturn(Document.builder().build());
        when(CASE_DATA.getC1AWelshDocument()).thenReturn(Document.builder().build());
        when(CASE_DATA.getFinalDocument()).thenReturn(Document.builder().build());
        when(CASE_DATA.getFinalWelshDocument()).thenReturn(Document.builder().build());
        allTabService.updateAllTabsIncludingConfTab(CASE_DATA);

        verify(coreCaseDataService).triggerEvent(anyString(), anyString(),anyLong(), anyString(), anyMap());
        verify(applicationsTabService).updateTab(CASE_DATA);
        verify(caseSummaryTabService).updateTab(CASE_DATA);
    }

    @Test
    public void testAllTabFields() {
        when(CASE_DATA.getC8Document()).thenReturn(Document.builder().build());
        when(CASE_DATA.getC1ADocument()).thenReturn(Document.builder().build());
        when(CASE_DATA.getC8WelshDocument()).thenReturn(Document.builder().build());
        when(CASE_DATA.getC1AWelshDocument()).thenReturn(Document.builder().build());
        when(CASE_DATA.getFinalDocument()).thenReturn(Document.builder().build());
        when(CASE_DATA.getFinalWelshDocument()).thenReturn(Document.builder().build());
        assertNotNull(allTabService.getAllTabsFields(CASE_DATA));

    }

    @Test
    public void testAllTabsServiceIncConfidentailWithEmptyDocs() {
        when(CASE_DATA.getC8Document()).thenReturn(null);
        when(CASE_DATA.getC1ADocument()).thenReturn(null);
        when(CASE_DATA.getC8WelshDocument()).thenReturn(null);
        when(CASE_DATA.getC1AWelshDocument()).thenReturn(null);
        when(CASE_DATA.getFinalDocument()).thenReturn(null);
        when(CASE_DATA.getFinalWelshDocument()).thenReturn(null);
        allTabService.updateAllTabsIncludingConfTab(CASE_DATA);

        verify(coreCaseDataService).triggerEvent(anyString(), anyString(),anyLong(), anyString(), anyMap());
        verify(applicationsTabService).updateTab(CASE_DATA);
        verify(caseSummaryTabService).updateTab(CASE_DATA);
    }

    @Test
    public void testUpdatePartyDetailsForNocC100Applicant() {
        allTabService.updatePartyDetailsForNoc(caseInvites,
                                               "auth",
                                               "caseId",
                                               startEventResponse,
                                               EventRequestData.builder().build(), CASE_DATA);
        verify(coreCaseDataServiceCcdClient).submitUpdate(anyString(), any(), any(), anyString(), anyBoolean());
    }

    @Test
    public void testUpdatePartyDetailsForNocC100Respondent() {
        allTabService.updatePartyDetailsForNoc(caseInvites,
                                               "auth",
                                               "caseId",
                                               startEventResponse,
                                               EventRequestData.builder().build(), CASE_DATA);
        verify(coreCaseDataServiceCcdClient).submitUpdate(anyString(), any(), any(), anyString(), anyBoolean());
    }

    @Test
    public void testUpdatePartyDetailsForNocFL401Applicant() {
        when(CASE_DATA.getCaseTypeOfApplication()).thenReturn("FL401");
        allTabService.updatePartyDetailsForNoc(caseInvites,
                                               "auth",
                                               "caseId",
                                               startEventResponse,
                                               EventRequestData.builder().build(), CASE_DATA);
        verify(coreCaseDataServiceCcdClient).submitUpdate(anyString(), any(), any(), anyString(), anyBoolean());
    }

    @Test
    public void testUpdatePartyDetailsForNocFL401Respondent() {
        when(CASE_DATA.getCaseTypeOfApplication()).thenReturn("FL401");
        allTabService.updatePartyDetailsForNoc(caseInvites,
                                               "auth",
                                               "caseId",
                                               startEventResponse,
                                               EventRequestData.builder().build(), CASE_DATA);
        verify(coreCaseDataServiceCcdClient).submitUpdate(anyString(), any(), any(), anyString(), anyBoolean());
    }
}
