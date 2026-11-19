package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.Fm5PendingParty;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.FM5_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Fm5ReminderServiceTest {
    private static final String AUTH_TOKEN = "authToken";
    private static final String S2S_TOKEN = "s2sAuthToken";

    private CaseDetails caseDetails;
    private CaseData caseData;
    private List<Element<NotificationDetails>> fm5ReminderNotifications;
    private QuarantineLegalDoc quarantineLegalDoc;
    private Document document;

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

    @BeforeEach
    void setUp() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);

        PartyDetails applicant = PartyDetails.builder()
            .firstName("app FN")
            .lastName("app LN")
            .email("app@test.com")
            .solicitorEmail("app.sol@test.com")
            .representativeFirstName("app LR FN")
            .representativeLastName("app LR LN")
            .address(Address.builder().addressLine1("test").build())
            .build();

        PartyDetails respondent = PartyDetails.builder()
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
        when(coreCaseDataApi.searchCases(AUTH_TOKEN, S2S_TOKEN, CASE_TYPE, null)).thenReturn(searchResult);

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

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            S2S_TOKEN,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(objectMapper),
            caseData, null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(CaseDetails.builder().build());

        fm5ReminderNotifications = List.of(element(NotificationDetails.builder().build()), element(NotificationDetails.builder().build()));
        when(fm5NotificationService.sendFm5ReminderNotifications(caseData, Fm5PendingParty.BOTH)).thenReturn(fm5ReminderNotifications);

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
    void testSendFm5ReminderNotificationsToBothParties() {
        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verify(fm5NotificationService, times(1))
            .sendFm5ReminderNotifications(caseData, Fm5PendingParty.BOTH);
    }

    @Test
    void testSendFm5ReminderNotificationsToApplicant() {

        caseData = caseData.toBuilder()
            .respondents(new ArrayList<>())
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            S2S_TOKEN,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(objectMapper),
            caseData, null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(CaseDetails.builder().build());

        fm5ReminderNotifications = List.of(element(NotificationDetails.builder().build()), element(NotificationDetails.builder().build()));
        when(fm5NotificationService.sendFm5ReminderNotifications(caseData, Fm5PendingParty.APPLICANT)).thenReturn(fm5ReminderNotifications);

        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verify(fm5NotificationService, times(1))
            .sendFm5ReminderNotifications(caseData, Fm5PendingParty.APPLICANT);
    }

    @Test
    void testSendFm5ReminderNotificationsToRespondent() {
        caseData = caseData.toBuilder()
            .applicants(new ArrayList<>())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            S2S_TOKEN,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(objectMapper),
            caseData, null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(CaseDetails.builder().build());

        fm5ReminderNotifications = List.of(element(NotificationDetails.builder().build()), element(NotificationDetails.builder().build()));
        when(fm5NotificationService.sendFm5ReminderNotifications(caseData, Fm5PendingParty.RESPONDENT)).thenReturn(fm5ReminderNotifications);

        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verify(fm5NotificationService, times(1))
            .sendFm5ReminderNotifications(caseData, Fm5PendingParty.RESPONDENT);
    }

    @Test
    void testSendFm5ReminderNotificationToBothPartiesWhenDatesAreAfterHearingAwayDays() {
        List<Hearings> hearings = List.of(Hearings.hearingsWith()
            .caseRef("123")
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                .hmcStatus("LISTED")
                .hearingDaySchedule(List.of(HearingDaySchedule.hearingDayScheduleWith()
                    .hearingStartDateTime(LocalDateTime.now().plusDays(5))
                    .build()))
                .build()))
            .build());

        when(hearingApiClient.getHearingsForAllCaseIdsWithCourtVenue(any(), any(), anyList())).thenReturn(hearings);

        fm5ReminderService.sendFm5ReminderNotifications(18L);

        //verify
        verify(fm5NotificationService, times(1))
            .sendFm5ReminderNotifications(caseData, Fm5PendingParty.BOTH);
    }

    @Test
    void testSendFm5ReminderNotificationsToNoPartiesWhenHearingWasYesterday() {
        List<Hearings> hearings = List.of(Hearings.hearingsWith()
            .caseRef("123")
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                .hmcStatus("LISTED")
                .hearingDaySchedule(List.of(HearingDaySchedule.hearingDayScheduleWith()
                    .hearingStartDateTime(LocalDateTime.now().minusDays(1))
                    .build()))
                .build()))
            .build());
        when(hearingApiClient.getHearingsForAllCaseIdsWithCourtVenue(any(), any(), anyList())).thenReturn(hearings);

        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verifyNoInteractions(fm5NotificationService);
    }

    @Test
    void testSendFm5ReminderNotificationsToNonePartiesWhenC1AAvailable() {
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
        when(coreCaseDataApi.searchCases(AUTH_TOKEN, S2S_TOKEN, CASE_TYPE, null)).thenReturn(searchResult);

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
    void testSendFm5ReminderNotificationsToNonePartiesWhenDraftConsentOrderFileAvailable() {
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
        when(coreCaseDataApi.searchCases(AUTH_TOKEN, S2S_TOKEN, CASE_TYPE, null)).thenReturn(searchResult);

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
    void testSendFm5ReminderNotificationsToNonePartiesWhenMiamPolicyUpgradeDetailsAvailable() {
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
        when(coreCaseDataApi.searchCases(AUTH_TOKEN, S2S_TOKEN, CASE_TYPE, null)).thenReturn(searchResult);

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
    void testSendFm5ReminderNotificationsToNonePartiesWhenReviewDocumentsDetailsAvailable() {
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
        when(coreCaseDataApi.searchCases(AUTH_TOKEN, S2S_TOKEN, CASE_TYPE, null)).thenReturn(searchResult);

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
    void testSendFm5ReminderNotificationsWithNullSearchResult() {
        SearchResult searchResult = SearchResult.builder()
            .total(0)
            .build();
        when(coreCaseDataApi.searchCases(AUTH_TOKEN, S2S_TOKEN, CASE_TYPE, null)).thenReturn(searchResult);
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class)).thenReturn(null);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verifyNoInteractions(fm5NotificationService);
    }

    @Test
    void testSendFm5ReminderNotificationsWithEmptyHearingList() {
        List<Hearings> hearings = new ArrayList<>();
        hearings.add(
            Hearings.hearingsWith()
                .caseRef("123")
                .caseHearings(new ArrayList<>())
                .build()
        );
        when(hearingApiClient.getHearingsForAllCaseIdsWithCourtVenue(any(), any(), anyList())).thenReturn(hearings);

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            S2S_TOKEN,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(objectMapper),
            caseData, null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(CaseDetails.builder().build());

        fm5ReminderService.sendFm5ReminderNotifications(null);

        //verify
        verifyNoInteractions(fm5NotificationService);
    }

    @Test
    void testSendFm5ReminderNotificationsToNoneParties() {
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
        when(coreCaseDataApi.searchCases(AUTH_TOKEN, S2S_TOKEN, CASE_TYPE, null)).thenReturn(searchResult);

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
    void testSendFm5ReminderNotificationsWithException() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("") {});

        fm5ReminderService.sendFm5ReminderNotifications(null);

        verifyNoInteractions(fm5NotificationService);
    }

    @Test
    void testSendFm5ReminderNotificationsToNonePartiesWhenLegalProfQuarantineDocsListAvailable() {
        List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTabInitial = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .categoryId(RESPONDENT_C1A_APPLICATION)
            .restrictedDetails("test details")
            .build();
        legalProfUploadDocListDocTabInitial.add(element(quarantineLegalDoc));

        caseData = caseData.toBuilder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .legalProfQuarantineDocsList(legalProfUploadDocListDocTabInitial)
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
        when(coreCaseDataApi.searchCases(AUTH_TOKEN, S2S_TOKEN, CASE_TYPE, null)).thenReturn(searchResult);

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
    void testSendFm5ReminderNotificationsToNonePartiesWhenRestrictedDocumentsListAvailable() {
        List<Element<QuarantineLegalDoc>> quarantineList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .categoryId(RESPONDENT_C1A_APPLICATION)
            .restrictedDetails("test details")
            .build();
        quarantineList.add(element(quarantineLegalDoc));

        caseData = caseData.toBuilder()
            .reviewDocuments(ReviewDocuments.builder()
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
        when(coreCaseDataApi.searchCases(AUTH_TOKEN, S2S_TOKEN, CASE_TYPE, null)).thenReturn(searchResult);

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
    void testSendFm5ReminderNotificationsToNonePartiesWhenCitizenQuarantineDocsListAvailable() {
        List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .categoryId(RESPONDENT_C1A_APPLICATION)
            .restrictedDetails("test details")
            .build();
        legalProfQuarantineDocsList.add(element(quarantineLegalDoc));

        caseData = caseData.toBuilder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .citizenQuarantineDocsList(legalProfQuarantineDocsList)
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
        when(coreCaseDataApi.searchCases(AUTH_TOKEN, S2S_TOKEN, CASE_TYPE, null)).thenReturn(searchResult);

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
    void testSendFm5ReminderNotificationsToNonePartiesWhenCitizenUploadedCaseDocsListAvailable() {
        List<Element<QuarantineLegalDoc>> quarantineList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .categoryId(RESPONDENT_C1A_APPLICATION)
            .restrictedDetails("restricted test details")
            .build();
        quarantineList.add(element(quarantineLegalDoc));

        caseData = caseData.toBuilder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .citizenUploadedDocListDocTab(quarantineList)
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
        when(coreCaseDataApi.searchCases(AUTH_TOKEN, S2S_TOKEN, CASE_TYPE, null)).thenReturn(searchResult);

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
    void shouldCallHearingApiInBatchesAndAggregateResults() {
        // Arrange: 25 cases -> expected 3 batches for batch size 10
        List<CaseDetails> caseDetailsList = new ArrayList<>();
        for (long i = 1; i <= 25; i++) {
            caseDetailsList.add(caseDetailsWithMinimalData(i));
        }

        when(hearingApiClient.getHearingsForAllCaseIdsWithCourtVenue(anyString(), anyString(), anyList()))
            .thenAnswer(invocation -> {
                List<String> ids = invocation.getArgument(2);
                List<Hearings> hearings = new ArrayList<>();
                for (String id : ids) {
                    hearings.add(listedHearingForCase(id));
                }
                return hearings;
            });

        // Act
        Map<String, Fm5PendingParty> result =
            fm5ReminderService.getQualifiedCasesAndHearingsForNotifications(caseDetailsList, 18L);

        // Assert API batching
        ArgumentCaptor<List<String>> batchCaptor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(hearingApiClient, org.mockito.Mockito.times(3))
            .getHearingsForAllCaseIdsWithCourtVenue(
                eq(AUTH_TOKEN),
                eq(S2S_TOKEN),
                batchCaptor.capture()
            );

        List<List<String>> batches = batchCaptor.getAllValues();
        assertThat(batches).hasSize(3);
        assertThat(batches.get(0)).hasSize(10);
        assertThat(batches.get(1)).hasSize(10);
        assertThat(batches.get(2)).hasSize(5);

        // Assert all 25 made it through qualification pipeline
        assertThat(result).hasSize(25);
        assertThat(result).containsKey("1");
        assertThat(result).containsKey("25");
    }

    @Test
    void shouldHandleExceptionInCallHearingApiInBatchesAndAggregateResults() {
        // Arrange: 25 cases -> expected 3 batches for batch size 10
        List<CaseDetails> caseDetailsList = new ArrayList<>();
        for (long i = 1; i <= 25; i++) {
            caseDetailsList.add(caseDetailsWithMinimalData(i));
        }

        // Mock responses from fis
        // Call 1: return hearings for all case ids
        // Call 2: throw exception
        // Call 3: return hearings for all case ids
        when(hearingApiClient.getHearingsForAllCaseIdsWithCourtVenue(anyString(), anyString(), anyList()))
            .thenAnswer(invocation -> {
                List<String> ids = invocation.getArgument(2);
                List<Hearings> hearings = new ArrayList<>();
                for (String id : ids) {
                    hearings.add(listedHearingForCase(id));
                }
                return hearings;
            })
            .thenThrow(new FeignException.GatewayTimeout(
                "504 Gateway Timeout",
                mock(feign.Request.class),
                new byte[0],
                Map.of()
            ))
            .thenAnswer(invocation -> {
                List<String> ids = invocation.getArgument(2);
                List<Hearings> hearings = new ArrayList<>();
                for (String id : ids) {
                    hearings.add(listedHearingForCase(id));
                }
                return hearings;
            });

        // Act
        Map<String, Fm5PendingParty> result =
            fm5ReminderService.getQualifiedCasesAndHearingsForNotifications(caseDetailsList, 18L);

        // Assert API batching
        ArgumentCaptor<List<String>> batchCaptor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(hearingApiClient, org.mockito.Mockito.times(3))
            .getHearingsForAllCaseIdsWithCourtVenue(
                eq(AUTH_TOKEN),
                eq(S2S_TOKEN),
                batchCaptor.capture()
            );

        List<List<String>> batches = batchCaptor.getAllValues();
        assertThat(batches).hasSize(3);
        assertThat(batches.get(0)).hasSize(10);
        assertThat(batches.get(1)).hasSize(10);
        assertThat(batches.get(2)).hasSize(5);

        // Assert all 15 made it through qualification pipeline
        assertThat(result).hasSize(15);
        assertThat(result).containsKey("1");
        assertThat(result).containsKey("25");
    }

    @Test
    void shouldHandleExceptionGetQualifiedCasesAndHearingsForNotifications() {
        CaseDetails caseDetails1 = mock(CaseDetails.class);
        CaseData caseData1 = mock(CaseData.class);
        when(objectMapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData1);
        CaseDetails caseDetails2 = mock(CaseDetails.class);
        CaseData caseData2 = mock(CaseData.class);
        when(objectMapper.convertValue(caseDetails2.getData(), CaseData.class)).thenReturn(caseData2);
        CaseDetails caseDetails3 = mock(CaseDetails.class);
        CaseData caseData3 = mock(CaseData.class);
        when(objectMapper.convertValue(caseDetails3.getData(), CaseData.class)).thenReturn(caseData3);

        when(objectMapper.convertValue(caseData1, CaseData.class)).thenThrow(new IllegalArgumentException());
        when(objectMapper.convertValue(caseData2, CaseData.class)).thenThrow(new IllegalArgumentException());
        when(objectMapper.convertValue(caseData3, CaseData.class)).thenThrow(new IllegalArgumentException());

        List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2, caseDetails3);

        Map<String, Fm5PendingParty> result = fm5ReminderService.getQualifiedCasesAndHearingsForNotifications(caseDetailsList, 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void retrieveCasesInHearingStatePendingFm5Reminders_shouldPageAndAggregateResults() throws Exception {
        // PAGE_SIZE in service = 10, so total 15 should trigger 2 calls (page 0 + page 1)
        CaseDetails case1 = CaseDetails.builder().id(1L).data(Map.of()).build();
        CaseDetails case2 = CaseDetails.builder().id(2L).data(Map.of()).build();
        CaseDetails case3 = CaseDetails.builder().id(3L).data(Map.of()).build();

        SearchResult firstPageSearchResult = SearchResult.builder()
            .total(15)
            .build();
        SearchResult secondPageSearchResult = SearchResult.builder()
            .total(15)
            .build();

        SearchResultResponse firstPageResponse = SearchResultResponse.builder()
            .total(15)
            .cases(List.of(case1, case2))
            .build();

        SearchResultResponse secondPageResponse = SearchResultResponse.builder()
            .total(15)
            .cases(List.of(case3))
            .build();

        when(coreCaseDataApi.searchCases(eq(AUTH_TOKEN), eq(S2S_TOKEN), eq(CASE_TYPE), anyString()))
            .thenReturn(firstPageSearchResult)
            .thenReturn(secondPageSearchResult);

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"query\":\"q\"}");

        when(objectMapper.convertValue(any(SearchResult.class), eq(SearchResultResponse.class)))
            .thenReturn(firstPageResponse)
            .thenReturn(secondPageResponse);

        List<CaseDetails> result = fm5ReminderService.retrieveCasesInHearingStatePendingFm5Reminders();

        assertThat(result).containsExactly(case1, case2, case3);
        verify(coreCaseDataApi, times(2))
            .searchCases(eq(AUTH_TOKEN), eq(S2S_TOKEN), eq(CASE_TYPE), anyString());
    }

    @Test
    void retrieveCasesInHearingStatePendingFm5Reminders_shouldNotPageWhenSinglePage() throws Exception {
        CaseDetails case1 = CaseDetails.builder().id(1L).data(Map.of()).build();

        SearchResult firstPageSearchResult = SearchResult.builder()
            .total(10) // exactly PAGE_SIZE => 1 page
            .build();

        SearchResultResponse firstPageResponse = SearchResultResponse.builder()
            .total(10)
            .cases(List.of(case1))
            .build();

        when(coreCaseDataApi.searchCases(eq(AUTH_TOKEN), eq(S2S_TOKEN), eq(CASE_TYPE), anyString()))
            .thenReturn(firstPageSearchResult);

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"query\":\"q\"}");
        when(objectMapper.convertValue(firstPageSearchResult, SearchResultResponse.class))
            .thenReturn(firstPageResponse);

        List<CaseDetails> result = fm5ReminderService.retrieveCasesInHearingStatePendingFm5Reminders();

        assertThat(result).containsExactly(case1);
        verify(coreCaseDataApi, times(1))
            .searchCases(eq(AUTH_TOKEN), eq(S2S_TOKEN), eq(CASE_TYPE), anyString());
    }

    @Test
    void retrieveCasesInHearingStatePendingFm5Reminders_shouldReturnEmptyWhenNoResults() throws Exception {
        SearchResult emptySearchResult = SearchResult.builder()
            .total(0)
            .build();

        when(coreCaseDataApi.searchCases(eq(AUTH_TOKEN), eq(S2S_TOKEN), eq(CASE_TYPE), anyString()))
            .thenReturn(emptySearchResult);

        when(objectMapper.writeValueAsString(any())).thenReturn("{\"query\":\"q\"}");

        List<CaseDetails> result = fm5ReminderService.retrieveCasesInHearingStatePendingFm5Reminders();

        assertThat(result).isEmpty();
        verify(coreCaseDataApi, times(1))
            .searchCases(eq(AUTH_TOKEN), eq(S2S_TOKEN), eq(CASE_TYPE), anyString());
        verify(objectMapper, times(0)).convertValue(any(), eq(SearchResultResponse.class));
    }

    private CaseDetails caseDetailsWithMinimalData(long caseId) {
        // Minimal CaseData to avoid NOT_REQUIRED branches:
        // - no consent order
        // - no C1A docs
        // - no MIAM exclusion
        // - at least one applicant/respondent so pending party resolves to BOTH
        CaseData caseData = CaseData.builder()
            .id(caseId)
            .applicants(new ArrayList<>())
            .respondents(new ArrayList<>())
            .build();

        return CaseDetails.builder()
            .id(caseId)
            .data(objectMapper.convertValue(caseData, Map.class))
            .build();
    }

    private Hearings listedHearingForCase(String caseRef) {
        HearingDaySchedule schedule = HearingDaySchedule.hearingDayScheduleWith()
            .hearingStartDateTime(LocalDateTime.now().plusDays(2))
            .build();

        CaseHearing caseHearing = CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .hearingDaySchedule(List.of(schedule))
            .build();

        return Hearings.hearingsWith()
            .caseRef(caseRef)
            .caseHearings(List.of(caseHearing))
            .build();
    }
}
