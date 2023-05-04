package uk.gov.hmcts.reform.prl.services.tab.alltabs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
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
    public void testAllTabsServiceIncConfTabRefactored() {

        Element<ApplicantConfidentialityDetails> applicantConfidentialityDetailsElement = Element.<ApplicantConfidentialityDetails>builder().value(ApplicantConfidentialityDetails.builder().build()).build();
        Element<ChildConfidentialityDetails> childConfidentialityDetailsElement = Element.<ChildConfidentialityDetails>builder().value(ChildConfidentialityDetails.builder().build()).build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .caseTypeOfApplication("FL401")
            .applicantsConfidentialDetails(List.of(applicantConfidentialityDetailsElement))
            .childrenConfidentialDetails(List.of(childConfidentialityDetailsElement))
            .c8Document(null)
            .c8WelshDocument(null)
            .c1ADocument(null)
            .finalDocument(null)
            .finalWelshDocument(null)
            .draftOrderDoc(null)
            .draftOrderDocWelsh(null)
            .dateSubmitted("04-05-2023")
            .courtName("test court")
            .courtId("1234")
            .build();

        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .id(12345L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .eventId(Event.SUBMIT_AND_PAY.getId())
            .build();
        EventRequestData eventRequestData = EventRequestData.builder()
            .jurisdictionId("PRIVATELAW")
            .eventId(Event.SUBMIT_AND_PAY.getId())
            .caseTypeId("PRLAPPS")
            .userId("Test")
            .userToken("test token")
            .ignoreWarning(true)
            .build();

        when(CASE_DATA.getC8Document()).thenReturn(null);
        when(CASE_DATA.getC1ADocument()).thenReturn(null);
        when(CASE_DATA.getC8WelshDocument()).thenReturn(null);
        when(CASE_DATA.getC1AWelshDocument()).thenReturn(null);
        when(CASE_DATA.getFinalDocument()).thenReturn(null);
        when(CASE_DATA.getFinalWelshDocument()).thenReturn(null);
        allTabService.updateAllTabsIncludingConfTabRefactored("test auth",
                                                              String.valueOf(caseData.getId()),
                                                              startEventResponse, eventRequestData, caseData);
        verify(coreCaseDataServiceCcdClient).submitUpdate(anyString(),any(),any(),anyString(),anyBoolean());
        verify(caseSummaryTabService).updateTab(any());

        assertNotNull(caseData.getCourtName());

    }
}
