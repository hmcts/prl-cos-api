package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.FmPendingParty;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.notification.NotificationDetails;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.FM5_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class Fm5ReminderServiceTest {

    private final String authToken = "authToken";
    private final String s2sAuthToken = "s2sAuthToken";
    private CaseDetails caseDetails;
    private CaseData caseData;
    private PartyDetails applicant;
    private PartyDetails respondent;
    List<Element<NotificationDetails>> fm5ReminderNotifications;
    QuarantineLegalDoc quarantineLegalDoc;
    Document document;

    @InjectMocks
    private Fm5ReminderService fm5ReminderService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    SystemUserService systemUserService;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    CoreCaseDataApi coreCaseDataApi;

    @Mock
    HearingApiClient hearingApiClient;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    Fm5NotificationService fm5NotificationService;

    @Before
    public void setUp() {
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn(s2sAuthToken);

        applicant = PartyDetails.builder()
            .firstName("app FN")
            .lastName("app LN")
            .email("app@test.com")
            .solicitorEmail("app.sol@test.com")
            .representativeFirstName("app LR FN")
            .representativeLastName("app LR LN")
            .address(Address.builder().addressLine1("test").build())
            .build();

        respondent = PartyDetails.builder()
            .firstName("resp FN")
            .lastName("resp LN")
            .email("resp@test.com")
            .solicitorEmail("resp.sol@test.com")
            .representativeFirstName("resp LR FN")
            .representativeLastName("resp LR LN")
            .address(Address.builder().addressLine1("test").build())
            .build();

        caseData = CaseData.builder()
            .id(123L)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .applicants(List.of(element(applicant)))
            .respondents(List.of(element(respondent)))
            .build();
        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();

        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult);

        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class)).thenReturn(response);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        List<Hearings> hearings = List.of(Hearings.hearingsWith()
                                              .caseRef("123")
                                              .caseHearings(List.of(CaseHearing.caseHearingWith()
                                                                        .hmcStatus("LISTED")
                                                                        .hearingDaySchedule(List.of(HearingDaySchedule.hearingDayScheduleWith()
                                                                                                        .hearingStartDateTime(
                                                                                                            LocalDateTime.now().plusDays(18))
                                                                                                        .build()))
                                                                        .build()))
                                              .build());
        when(hearingApiClient.getHearingsForAllCaseIdsWithCourtVenue(any(), any(), anyList())).thenReturn(hearings);

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(s2sAuthToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseData.toMap(objectMapper),
                                                                                                        caseData, null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(CaseDetails.builder().build());

        fm5ReminderNotifications = List.of(element(NotificationDetails.builder().build()), element(NotificationDetails.builder().build()));
        when(fm5NotificationService.sendFm5ReminderNotifications(caseData, FmPendingParty.BOTH)).thenReturn(fm5ReminderNotifications);

        document = Document.builder()
            .documentFileName("test.pdf")
            .documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15")
            .build();

        quarantineLegalDoc = QuarantineLegalDoc.builder()
            .documentParty(DocumentPartyEnum.APPLICANT.getDisplayedValue())
            .categoryId("respondentC1AApplication")
            .documentUploadedDate(LocalDateTime.now())
            .build();

    }

    @Test
    public void testSendFm5ReminderNotificationsToBothParties() {
        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verify(fm5NotificationService, times(1))
            .sendFm5ReminderNotifications(caseData, FmPendingParty.BOTH);
    }

    @Test
    public void testSendFm5ReminderNotificationsToApplicant() {

        CaseData caseDatas = CaseData.builder()
            .id(123L)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .applicants(List.of(element(applicant)))
            .respondents(new ArrayList<>())
            .build();
        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseDatas.toMap(objectMapper))
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDatas);

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(s2sAuthToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseDatas.toMap(objectMapper),
                                                                                                        caseDatas, null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(CaseDetails.builder().build());

        fm5ReminderNotifications = List.of(element(NotificationDetails.builder().build()), element(NotificationDetails.builder().build()));
        when(fm5NotificationService.sendFm5ReminderNotifications(caseDatas, FmPendingParty.APPLICANT)).thenReturn(fm5ReminderNotifications);

        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verify(fm5NotificationService, times(1))
            .sendFm5ReminderNotifications(caseDatas, FmPendingParty.APPLICANT);
    }

    @Test
    public void testSendFm5ReminderNotificationsToRespondent() {

        CaseData caseDatas = CaseData.builder()
            .id(123L)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .applicants(new ArrayList<>())
            .respondents(List.of(element(respondent)))
            .build();
        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseDatas.toMap(objectMapper))
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDatas);

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(s2sAuthToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseDatas.toMap(objectMapper),
                                                                                                        caseDatas, null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(CaseDetails.builder().build());

        fm5ReminderNotifications = List.of(element(NotificationDetails.builder().build()), element(NotificationDetails.builder().build()));
        when(fm5NotificationService.sendFm5ReminderNotifications(caseDatas, FmPendingParty.RESPONDENT)).thenReturn(fm5ReminderNotifications);

        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verify(fm5NotificationService, times(1))
            .sendFm5ReminderNotifications(caseDatas, FmPendingParty.RESPONDENT);
    }


    @Test
    public void testSendFm5ReminderNotificationsToNonePartiesWhenC1AAvailable() {

        caseData = caseData.toBuilder()
            .c1ADocument(Document.builder().build())
            .build();
        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();

        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult);

        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class)).thenReturn(response);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verifyNoInteractions(fm5NotificationService);
    }

    @Test
    public void testSendFm5ReminderNotificationsToNonePartiesWhenDraftConsentOrderFileAvailable() {

        caseData = caseData.toBuilder()
            .draftConsentOrderFile(Document.builder().build())
            .build();
        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();

        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult);

        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class)).thenReturn(response);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verifyNoInteractions(fm5NotificationService);
    }

    @Test
    public void testSendFm5ReminderNotificationsToNonePartiesWhenMiamPolicyUpgradeDetailsAvailable() {

        caseData = caseData.toBuilder()
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                                          .builder()
                                          .mpuChildInvolvedInMiam(YesOrNo.Yes)
                                          .build())
            .build();
        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();

        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult);

        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class)).thenReturn(response);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verifyNoInteractions(fm5NotificationService);
    }

    @Test
    public void testSendFm5ReminderNotificationsToNonePartiesWhenReviewDocumentsDetailsAvailable() {

        List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTabInitial = new ArrayList<>();
        List<Element<QuarantineLegalDoc>> courtStaffUploadDocListDocTabInitial = new ArrayList<>();
        List<Element<QuarantineLegalDoc>> quarantineList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .build();
        legalProfUploadDocListDocTabInitial.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
            quarantineLegalDoc));

        caseData = caseData.toBuilder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .legalProfQuarantineDocsList(new ArrayList<>())
                                           .build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .legalProfUploadDocListDocTab(legalProfUploadDocListDocTabInitial)
                                 .courtStaffUploadDocListDocTab(courtStaffUploadDocListDocTabInitial)
                                 .restrictedDocuments(quarantineList).build())
            .build();
        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();

        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult);

        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class)).thenReturn(response);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verifyNoInteractions(fm5NotificationService);
    }

    @Test
    public void testSendFm5ReminderNotificationsWithEmptyList() {
        SearchResult searchResult = SearchResult.builder()
            .total(0)
            .build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult);
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class)).thenReturn(null);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verifyNoInteractions(fm5NotificationService);
    }

    @Test
    public void testSendFm5ReminderNotificationsToNoneParties() {

        List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTabInitial = new ArrayList<>();
        List<Element<QuarantineLegalDoc>> courtStaffUploadDocListDocTabInitial = new ArrayList<>();

        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .categoryId(FM5_STATEMENTS)
            .documentParty(DocumentPartyEnum.APPLICANT.getDisplayedValue())
            .build();
        QuarantineLegalDoc quarantineLegalDocRespondent = QuarantineLegalDoc.builder()
            .document(document)
            .categoryId(FM5_STATEMENTS)
            .documentParty(DocumentPartyEnum.RESPONDENT.getDisplayedValue())
            .build();

        legalProfUploadDocListDocTabInitial.add(element(quarantineLegalDoc));
        courtStaffUploadDocListDocTabInitial.add(element(quarantineLegalDocRespondent));

        List<Element<QuarantineLegalDoc>> quarantineList = new ArrayList<>();

        caseData = caseData.toBuilder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .legalProfQuarantineDocsList(new ArrayList<>())
                                           .build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .legalProfUploadDocListDocTab(legalProfUploadDocListDocTabInitial)
                                 .courtStaffUploadDocListDocTab(courtStaffUploadDocListDocTabInitial)
                                 .restrictedDocuments(quarantineList).build())
            .build();
        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();

        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult);

        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class)).thenReturn(response);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verifyNoInteractions(fm5NotificationService);
    }

    @Test
    public void testSendFm5ReminderNotificationsWithException() throws JsonProcessingException {

        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("") {});

        fm5ReminderService.sendFm5ReminderNotifications(null);
        //verify
        verifyNoInteractions(fm5NotificationService);

    }

}
