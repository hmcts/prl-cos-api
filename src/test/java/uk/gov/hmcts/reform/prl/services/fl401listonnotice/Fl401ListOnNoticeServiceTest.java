package uk.gov.hmcts.reform.prl.services.fl401listonnotice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AddCaseNoteService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_LIST_ON_NOTICE_HEARING_INSTRUCTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_REASONS_FOR_LIST_WITHOUT_NOTICE_REQUESTED;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.fl401listonnotice.Fl401ListOnNoticeService.CONFIRMATION_BODY;
import static uk.gov.hmcts.reform.prl.services.fl401listonnotice.Fl401ListOnNoticeService.CONFIRMATION_HEADER;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class Fl401ListOnNoticeServiceTest {

    @InjectMocks
    Fl401ListOnNoticeService fl401ListOnNoticeService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AddCaseNoteService addCaseNoteService;

    public static final String authToken = "Bearer TestAuthToken";

    @Mock
    UserService userService;


    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    private StartAllTabsUpdateDataContent startAllTabsUpdateDataContent;

    private CaseData caseData;
    private CallbackRequest callbackRequest;



    @Before
    public void setUp() {
        caseData = CaseData.builder()
            .courtName("testcourt")
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .build();

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );
    }

    @Test
    public void testListOnNoticeSubmission() throws Exception {

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .orderWithoutGivingNoticeToRespondent(WithoutNoticeOrderDetails.builder()
                                                      .orderWithoutGivingNotice(Yes)
                                                      .build())

            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put(FL401_LIST_ON_NOTICE_HEARING_INSTRUCTION, "test");
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .state(State.JUDICIAL_REVIEW.getValue())
            .data(stringObjectMap)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(addCaseNoteService.getCurrentCaseNoteDetails(
            anyString(),
            anyString(),
            any()
        )).thenReturn(CaseNoteDetails.builder().build());
        List<Element<CaseNoteDetails>> caseNotesCollection = new ArrayList<>();
        when(addCaseNoteService.getCaseNoteDetails(any(), any())).thenReturn(caseNotesCollection);

        Map<String, Object> responseDataMap = fl401ListOnNoticeService
            .fl401ListOnNoticeSubmission(caseDetails, authToken);
        assertTrue(responseDataMap.containsKey(CASE_NOTES));

    }

    @Test
    public void testPrePopulateHearingPageDataForFl401ListOnNotice() throws Exception {
        CaseData caseData = CaseData.builder()
            .orderWithoutGivingNoticeToRespondent(WithoutNoticeOrderDetails.builder()
                                                      .orderWithoutGivingNotice(Yes).build())
            .build();
        Map<String, Object> response = fl401ListOnNoticeService.prePopulateHearingPageDataForFl401ListOnNotice(caseData);
        assertEquals("Yes", response.get(FL401_CASE_WITHOUT_NOTICE));
    }

    @Test
    public void testPrePopulateHearingPageDataForFl401ListWithoutNotice() throws Exception {
        CaseData caseData = CaseData.builder()
            .orderWithoutGivingNoticeToRespondent(WithoutNoticeOrderDetails.builder()
                                                      .orderWithoutGivingNotice(No).build())
            .build();
        Map<String, Object> response = fl401ListOnNoticeService.prePopulateHearingPageDataForFl401ListOnNotice(caseData);
        assertEquals("No", response.get(FL401_CASE_WITHOUT_NOTICE));
    }

    @Test
    public void testSendNotificationScenario1() throws Exception {

        CaseData caseData = CaseData.builder()
            .applicantsFL401(PartyDetails.builder().solicitorEmail("test@test.com").build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put(FL401_REASONS_FOR_LIST_WITHOUT_NOTICE_REQUESTED, "test");
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(allTabService.getStartAllTabsUpdate(Mockito.anyString()))
            .thenReturn(startAllTabsUpdateDataContent);
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .state(State.JUDICIAL_REVIEW.getValue())
            .data(stringObjectMap)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);

        ResponseEntity<SubmittedCallbackResponse> response = fl401ListOnNoticeService
            .sendNotification(stringObjectMap, authToken);
        assertEquals(CONFIRMATION_HEADER, Objects.requireNonNull(response.getBody()).getConfirmationHeader());

    }

    @Test
    public void testSendNotificationScenario2() throws Exception {
        UUID uuid = UUID.randomUUID();
        CaseData caseData = CaseData.builder()
            .applicantsFL401(PartyDetails.builder().partyId(uuid).user(
                User.builder().idamId(uuid.toString()).build()).build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put(FL401_REASONS_FOR_LIST_WITHOUT_NOTICE_REQUESTED, "test");
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(allTabService.getStartAllTabsUpdate(Mockito.anyString()))
            .thenReturn(startAllTabsUpdateDataContent);
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .state(State.JUDICIAL_REVIEW.getValue())
            .data(stringObjectMap)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);

        ResponseEntity<SubmittedCallbackResponse> response = fl401ListOnNoticeService
            .sendNotification(stringObjectMap, authToken);
        assertEquals(CONFIRMATION_HEADER, Objects.requireNonNull(response.getBody()).getConfirmationHeader());
    }

    @Test
    public void testSendNotificationScenario3() throws Exception {
        UUID uuid = UUID.randomUUID();
        CaseData caseData = CaseData.builder()
            .applicantsFL401(PartyDetails.builder()
                                 .partyId(uuid)
                                 .address(Address.builder().build()).build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put(FL401_REASONS_FOR_LIST_WITHOUT_NOTICE_REQUESTED, "test");
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .state(State.JUDICIAL_REVIEW.getValue())
            .data(stringObjectMap)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(allTabService.getStartAllTabsUpdate(Mockito.anyString()))
            .thenReturn(startAllTabsUpdateDataContent);

        ResponseEntity<SubmittedCallbackResponse> response = fl401ListOnNoticeService
            .sendNotification(stringObjectMap, authToken);
        assertEquals(CONFIRMATION_HEADER, Objects.requireNonNull(response.getBody()).getConfirmationHeader());

    }

    @Test
    public void testSendNotificationScenario4() throws Exception {
        UUID uuid = UUID.randomUUID();
        CaseData caseData = CaseData.builder()
            .applicantsFL401(PartyDetails.builder()
                                 .partyId(uuid)
                                 .address(Address.builder().build()).build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(allTabService.getStartAllTabsUpdate(Mockito.anyString()))
            .thenReturn(startAllTabsUpdateDataContent);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .state(State.JUDICIAL_REVIEW.getValue())
            .data(stringObjectMap)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        ResponseEntity<SubmittedCallbackResponse> response = fl401ListOnNoticeService
            .sendNotification(stringObjectMap, authToken);
        assertEquals(CONFIRMATION_BODY, Objects.requireNonNull(response.getBody()).getConfirmationBody());

    }
}
