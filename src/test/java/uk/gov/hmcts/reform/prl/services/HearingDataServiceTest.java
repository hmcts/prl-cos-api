package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.CommonDataRefApi;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.HearingPriorityTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioBeforeAEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.mapper.hearingrequest.HearingRequestDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ListWithoutNoticeDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Attendee;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMPLETED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONFIRMED_HEARING_DATES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CUSTOM_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_CONFIRMED_IN_HEARINGS_TAB;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_DATE_CONFIRM_OPTION_ENUM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_N;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTWITHOUTNOTICE_HEARINGDETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TEST_UUID;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class HearingDataServiceTest {

    @InjectMocks
    HearingDataService hearingDataService;

    @Mock
    RefDataUserService refDataUserService;

    @Mock
    HearingService hearingService;

    @Mock
    CaseService caseService;

    @Mock
    CaseDetails caseDetails;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    HearingRequestDataMapper hearingRequestDataMapper;

    @Mock
    LocationRefDataService locationRefDataService;

    @Mock
    CommonDataRefApi commonDataRefApi;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    CoreCaseDataApi coreCaseDataApi;
    @Mock
    AllocatedJudgeService allocatedJudgeService;

    @Mock
    DateTimeFormatter dateTimeFormatter;

    public static final String authToken = "Bearer TestAuthToken";
    @Mock
    HearingApiClient hearingApiClient;

    @Mock
    Hearings hearingDetails;

    private PartyDetails applicant;
    private PartyDetails respondent;

    @Before
    public void init() {
        applicant = PartyDetails.builder()
            .firstName("AppFN")
            .lastName("AppLN")
            .email("app1@test.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("AppSolFN")
            .representativeLastName("AppSolLN")
            .build();
        respondent = PartyDetails.builder()
            .firstName("RespFN")
            .lastName("RespLN")
            .email("resp1@test.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("RespSolFN")
            .representativeLastName("RespSolLN")
            .build();
    }

    @Test()
    public void testPopulateHearingDynamicLists() {
        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.retrieveCategoryValues(authToken, HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N)).thenReturn(
            commonDataResponse);
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, HEARINGTYPE)).thenReturn(
            listHearingTypes);
        when(locationRefDataService.getCourtLocations(authToken)).thenReturn(listHearingTypes);
        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        HearingData hearingData = HearingData.builder()
            .hearingTypes(dynamicList)
            .confirmedHearingDates(dynamicList)
            .hearingChannels(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingVideoChannels(dynamicList)
            .hearingTelephoneChannels(dynamicList)
            .courtList(dynamicList)
            .localAuthorityHearingChannel(dynamicList)
            .hearingListedLinkedCases(dynamicList)
            .applicantSolicitorHearingChannel(dynamicList)
            .respondentHearingChannel(dynamicList)
            .respondentSolicitorHearingChannel(dynamicList)
            .cafcassHearingChannel(dynamicList)
            .cafcassCymruHearingChannel(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours("5")
            .hearingEstimatedMinutes("40")
            .hearingEstimatedDays("15")
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .hearingPriorityTypeEnum(HearingPriorityTypeEnum.StandardPriority)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .build();

        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> listWithoutNoticeHearingDetails = Collections.singletonList(childElement);
        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(ListWithoutNoticeDetails.builder().listWithoutNoticeHearingDetails(
                listWithoutNoticeHearingDetails).build())
            .build();
        HearingDataPrePopulatedDynamicLists expectedResponse = hearingDataService
            .populateHearingDynamicLists(authToken, "45654654", caseData, Hearings.hearingsWith().build());
        assertNotNull(expectedResponse);
    }

    @Test()
    public void testPrePopulateHearingChannelException() {
        when(refDataUserService.filterCategoryValuesByCategoryId(any(), any())).thenThrow(new RuntimeException());
        Map<String, List<DynamicListElement>> expectedResponse = hearingDataService.prePopulateHearingChannel(authToken);
        Assert.assertEquals(0, expectedResponse.size());
    }

    @Test()
    public void testPrePopulateHearingType() {
        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.retrieveCategoryValues(authToken, HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N)).thenReturn(
            commonDataResponse);
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, HEARINGTYPE)).thenReturn(
            listHearingTypes);
        List<DynamicListElement> expectedResponse = hearingDataService.prePopulateHearingType(authToken);
        assertEquals("ABA5-REV", expectedResponse.get(0).getCode());
        assertEquals("Review", expectedResponse.get(0).getLabel());
    }

    @Test()
    public void testPrePopulateHearingDates() {

        List<HearingDaySchedule> hearingDaySchedules = new ArrayList<>();
        hearingDaySchedules.add(HearingDaySchedule.hearingDayScheduleWith().hearingJudgeId("123").hearingJudgeName(
                "hearingJudgeName")
                                    .hearingVenueId("venueId").hearingVenueAddress("venueAddress")
                                    .hearingStartDateTime(LocalDateTime.now()).build());
        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith().hmcStatus(LISTED).hearingDaySchedule(hearingDaySchedules).build());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .build();
        when((hearingService.getHearings(any(), any())))
            .thenReturn(Hearings.hearingsWith().hmctsServiceCode("CaseName-Test10")
                            .caseRef("1677767515750127").caseHearings(caseHearings).build());
        List<DynamicListElement> expectedResponse = hearingDataService.getHearingStartDate(
            "1677767515750127",
            Hearings.hearingsWith().build()
        );
        assertNotNull(expectedResponse);
    }

    @Test()
    public void testGetHearingData() {

        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.retrieveCategoryValues(authToken, HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N)).thenReturn(
            commonDataResponse);
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, HEARINGTYPE)).thenReturn(
            listHearingTypes);
        when(locationRefDataService.getCourtLocations(authToken)).thenReturn(listHearingTypes);

        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        List<JudicialUsersApiResponse> judicialUsersApiResponses = new ArrayList<>();
        JudicialUsersApiResponse judicialUsersApiResponse = JudicialUsersApiResponse.builder()
            //.emailId("Test")
            //.fullName("Test")
            //.surname("Test")
            .personalCode("Test")
            .build();
        judicialUsersApiResponses.add(judicialUsersApiResponse);
        JudicialUsersApiRequest judicialUsersApiRequest = JudicialUsersApiRequest.builder()
            .personalCode(new String[]{"Test2", "test", "test5"}).build();
        when(refDataUserService.getAllJudicialUserDetails(judicialUsersApiRequest)).thenReturn(judicialUsersApiResponses);
        DynamicList dynamicList1 = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        DynamicList dynamicList = DynamicList.builder()
            //.listItems(dynamicListElementsList)
            .build();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            HearingDataPrePopulatedDynamicLists.builder()
                .retrievedHearingTypes(dynamicList)
                .hearingListedLinkedCases(dynamicList1)
                .retrievedHearingDates(dynamicList1)
                .retrievedHearingChannels(dynamicList1)
                .retrievedVideoSubChannels(dynamicList1)
                .retrievedTelephoneSubChannels(dynamicList1)
                .retrievedCourtLocations(dynamicList)
                .hearingListedLinkedCases(dynamicList)
                .build();
        JudicialUser judicialUser = JudicialUser.builder()
            .personalCode("Test")
            .idamId("Test")
            .build();
        HearingData hearingData = HearingData.builder()
            .hearingTypes(dynamicList)
            .confirmedHearingDates(dynamicList)
            .hearingChannels(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingVideoChannels(dynamicList)
            .hearingTelephoneChannels(dynamicList)
            .courtList(dynamicList)
            .localAuthorityHearingChannel(dynamicList)
            .hearingListedLinkedCases(dynamicList)
            .applicantSolicitorHearingChannel(dynamicList)
            .respondentHearingChannel(dynamicList)
            .respondentSolicitorHearingChannel(dynamicList)
            .cafcassHearingChannel(dynamicList)
            .cafcassCymruHearingChannel(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours("5")
            .hearingEstimatedMinutes("40")
            .hearingEstimatedDays("15")
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .applicantName("Test")
            .hearingJudgeNameAndEmail(judicialUser)
            .build();
        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> listWithoutNoticeHearingDetails = Collections.singletonList(childElement);

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(ListWithoutNoticeDetails.builder().listWithoutNoticeHearingDetails(
                listWithoutNoticeHearingDetails).build())
            .build();
        List<Element<HearingData>> expectedResponse =
            hearingDataService.getHearingDataForOtherOrders(listWithoutNoticeHearingDetails,
                                                            hearingDataPrePopulatedDynamicLists,
                                                            caseData);
        assertNotNull(expectedResponse);
    }

    @Test()
    public void testGetHearingDataForSdo() {

        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.retrieveCategoryValues(authToken, HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N)).thenReturn(
            commonDataResponse);
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, HEARINGTYPE)).thenReturn(
            listHearingTypes);
        when(locationRefDataService.getCourtLocations(authToken)).thenReturn(listHearingTypes);

        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        List<JudicialUsersApiResponse> judicialUsersApiResponses = new ArrayList<>();
        JudicialUsersApiResponse judicialUsersApiResponse = JudicialUsersApiResponse.builder()
            //.emailId("Test")
            //.fullName("Test")
            //.surname("Test")
            .personalCode("Test")
            .build();
        judicialUsersApiResponses.add(judicialUsersApiResponse);
        JudicialUsersApiRequest judicialUsersApiRequest = JudicialUsersApiRequest.builder()
            .personalCode(new String[]{"Test2", "test", "test5"}).build();
        when(refDataUserService.getAllJudicialUserDetails(judicialUsersApiRequest)).thenReturn(judicialUsersApiResponses);
        DynamicList dynamicList1 = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        DynamicList dynamicList = DynamicList.builder()
            //.listItems(dynamicListElementsList)
            .build();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            HearingDataPrePopulatedDynamicLists.builder()
                .retrievedHearingTypes(dynamicList)
                .hearingListedLinkedCases(dynamicList1)
                .retrievedHearingDates(dynamicList1)
                .retrievedHearingChannels(dynamicList1)
                .retrievedVideoSubChannels(dynamicList1)
                .retrievedTelephoneSubChannels(dynamicList1)
                .retrievedCourtLocations(dynamicList)
                .hearingListedLinkedCases(dynamicList)
                .build();
        JudicialUser judicialUser = JudicialUser.builder()
            .personalCode("Test")
            .idamId("Test")
            .build();
        HearingData hearingData = HearingData.builder()
            .hearingTypes(dynamicList)
            .confirmedHearingDates(dynamicList)
            .hearingChannels(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingVideoChannels(dynamicList)
            .hearingTelephoneChannels(dynamicList)
            .courtList(dynamicList)
            .localAuthorityHearingChannel(dynamicList)
            .hearingListedLinkedCases(dynamicList)
            .applicantSolicitorHearingChannel(dynamicList)
            .respondentHearingChannel(dynamicList)
            .respondentSolicitorHearingChannel(dynamicList)
            .cafcassHearingChannel(dynamicList)
            .cafcassCymruHearingChannel(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours("5")
            .hearingEstimatedMinutes("40")
            .hearingEstimatedDays("15")
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .applicantName("Test")
            .hearingJudgeNameAndEmail(judicialUser)
            .build();


        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .build();
        HearingData expectedResponse =
            hearingDataService.getHearingDataForSdo(hearingData, hearingDataPrePopulatedDynamicLists, caseData);
        assertNotNull(expectedResponse);
    }

    @Test()
    public void testGetHearingDataSetJ() {

        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.retrieveCategoryValues(authToken, HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N)).thenReturn(
            commonDataResponse);
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, HEARINGTYPE)).thenReturn(
            listHearingTypes);
        when(locationRefDataService.getCourtLocations(authToken)).thenReturn(listHearingTypes);

        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        List<JudicialUsersApiResponse> judicialUsersApiResponses = new ArrayList<>();
        JudicialUsersApiResponse judicialUsersApiResponse = JudicialUsersApiResponse.builder()
            .emailId("Test")
            .fullName("Test")
            .surname("Test")
            .personalCode("Test")
            .build();
        judicialUsersApiResponses.add(judicialUsersApiResponse);
        JudicialUsersApiRequest judicialUsersApiRequest = JudicialUsersApiRequest.builder()
            .personalCode(new String[]{"Test", null, null}).build();
        when(refDataUserService.getAllJudicialUserDetails(judicialUsersApiRequest)).thenReturn(judicialUsersApiResponses);
        DynamicList dynamicList1 = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        DynamicList dynamicList = DynamicList.builder()
            //.listItems(dynamicListElementsList)
            .build();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            HearingDataPrePopulatedDynamicLists.builder()
                .retrievedHearingTypes(dynamicList)
                .hearingListedLinkedCases(dynamicList1)
                .retrievedHearingDates(dynamicList1)
                .retrievedHearingChannels(dynamicList1)
                .retrievedVideoSubChannels(dynamicList1)
                .retrievedTelephoneSubChannels(dynamicList1)
                .retrievedCourtLocations(dynamicList)
                .hearingListedLinkedCases(dynamicList)
                .build();
        JudicialUser judicialUser = JudicialUser.builder()
            .personalCode("Test")
            .build();
        HearingData hearingData = HearingData.builder()
            .hearingTypes(dynamicList)
            .confirmedHearingDates(dynamicList)
            .hearingChannels(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingVideoChannels(dynamicList)
            .hearingTelephoneChannels(dynamicList)
            .courtList(dynamicList)
            .localAuthorityHearingChannel(dynamicList)
            .hearingListedLinkedCases(dynamicList)
            .applicantSolicitorHearingChannel(dynamicList)
            .respondentHearingChannel(dynamicList)
            .respondentSolicitorHearingChannel(dynamicList)
            .cafcassHearingChannel(dynamicList)
            .cafcassCymruHearingChannel(dynamicList)
            .applicantHearingChannel(dynamicList)
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedByListingTeam)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours("5")
            .hearingJudgeNameAndEmail(JudicialUser.builder().personalCode("Test").build())
            .hearingEstimatedMinutes("40")
            .hearingEstimatedDays("15")
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .applicantName("Test")
            .hearingJudgeNameAndEmail(judicialUser)
            .build();
        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> listWithoutNoticeHearingDetails = Collections.singletonList(childElement);

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeDetails(ListWithoutNoticeDetails.builder().listWithoutNoticeHearingDetails(
                listWithoutNoticeHearingDetails).build())
            .build();

        List<Element<HearingData>> expectedResponse =
            hearingDataService.getHearingDataForOtherOrders(listWithoutNoticeHearingDetails,
                                                            hearingDataPrePopulatedDynamicLists,
                                                            caseData);
        assertEquals("Test", expectedResponse.get(0).getValue().getHearingJudgePersonalCode());
    }


    @Test()
    public void testGenerateHearingData() {
        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.retrieveCategoryValues(authToken, HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N)).thenReturn(
            commonDataResponse);
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, HEARINGTYPE)).thenReturn(
            listHearingTypes);
        when(locationRefDataService.getCourtLocations(authToken)).thenReturn(listHearingTypes);
        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestName")
            .representativeFirstName("Ram")
            .representativeLastName("Mer")
            .build();
        PartyDetails respondent = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .build();

        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .applicantName("test")
            .solicitorName("Test")
            .respondentName("Test")
            .applicants(applicantList)
            .respondents(respondentList)
            .applicantsFL401(applicant)
            .respondentsFL401(respondent)
            .caseTypeOfApplication("FL401")
            .caseManagementLocation(CaseManagementLocation.builder().region("2").build())
            .build();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            HearingDataPrePopulatedDynamicLists.builder()
                .retrievedHearingTypes(dynamicList)
                .hearingListedLinkedCases(dynamicList)
                .retrievedHearingDates(dynamicList)
                .retrievedHearingChannels(dynamicList)
                .retrievedCourtLocations(dynamicList)
                .retrievedVideoSubChannels(dynamicList)
                .retrievedTelephoneSubChannels(dynamicList)
                .retrievedCourtLocations(dynamicList)
                .hearingListedLinkedCases(dynamicList)
                .build();
        HearingData expectedResponse = hearingDataService.generateHearingData(
            hearingDataPrePopulatedDynamicLists,
            caseData
        );
        assertNotNull(expectedResponse);
    }


    @Test()
    public void testGetLinkedCasesWithBaseLocationAndRegion() {
        List<CaseLinkedData> caseLinkedDataList = new ArrayList<>();
        CaseLinkedData caseLinkedData = CaseLinkedData.caseLinkedDataWith()
            .caseName("CaseName-Test10")
            .caseReference("1677767515750127")
            .build();
        caseLinkedDataList.add(caseLinkedData);
        when(hearingService.getCaseLinkedData(any(), any())).thenReturn(caseLinkedDataList);
        CaseHearing caseHearing = CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .nextHearingDate(LocalDateTime.of(2023, 11, 8, 9, 0))
            .hearingID(123L)
            .hearingTypeValue("test")
            .build();
        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(caseHearing);
        Hearings hearings = Hearings.hearingsWith()
            .caseRef("1677767515750127")
            .caseHearings(caseHearings)
            .build();

        when(hearingService.getHearings(any(), any())).thenReturn(hearings);

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .caseManagementLocation(CaseManagementLocation.builder()
                                        .baseLocation("7")
                                        .region("1111")
                                        .build()).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CaseDetails caseDetails = CaseDetails.builder().id(
            1677767515750127L).data(stringObjectMap).build();

        when(caseService.getCase(any(),any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        List<DynamicListElement> expectedResponse = hearingDataService.getLinkedCases(authToken, caseData);
        assertEquals("1677767515750127_123",expectedResponse.get(0).getCode());
        assertEquals("1677767515750127_test - 08 Nov 2023",expectedResponse.get(0).getLabel());
    }

    @Test()
    public void testGetLinkedCasesWithBaseLocationIdAndRegionId() {
        List<CaseLinkedData> caseLinkedDataList = new ArrayList<>();
        CaseLinkedData caseLinkedData = CaseLinkedData.caseLinkedDataWith()
            .caseName("CaseName-Test10")
            .caseReference("1677767515750127")
            .build();
        caseLinkedDataList.add(caseLinkedData);
        when(hearingService.getCaseLinkedData(any(), any())).thenReturn(caseLinkedDataList);
        CaseHearing caseHearing = CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .nextHearingDate(LocalDateTime.of(2023, 11, 8, 9, 0))
            .hearingID(123L)
            .hearingTypeValue("test")
            .build();

        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(caseHearing);
        Hearings hearings = Hearings.hearingsWith()
            .caseRef("1677767515750127")
            .caseHearings(caseHearings)
            .build();

        when(hearingService.getHearings(any(), any())).thenReturn(hearings);

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .caseManagementLocation(CaseManagementLocation.builder()
                                        .baseLocationId("123")
                                        .regionId("1111")
                                        .build()).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CaseDetails caseDetails = CaseDetails.builder().id(
            1677767515750127L).data(stringObjectMap).build();

        when(caseService.getCase(any(), any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        List<DynamicListElement> expectedResponse = hearingDataService.getLinkedCases(authToken, caseData);
        assertEquals("1677767515750127_123", expectedResponse.get(0).getCode());
        assertEquals("1677767515750127_test - 08 Nov 2023", expectedResponse.get(0).getLabel());
    }

    @Test()
    public void testNullifyUnncessaryFieldsPopulated() {
        Map<String, Object> hearingDateConfirmOptionEnumMap = new LinkedHashMap<>();
        Map<String, Object> objectMap = new LinkedHashMap<>();
        hearingDateConfirmOptionEnumMap.put(HEARING_DATE_CONFIRM_OPTION_ENUM, DATE_CONFIRMED_IN_HEARINGS_TAB);
        List<Object> listWithoutNoticeHeardetailsObj = new ArrayList<>();
        objectMap.put("value", hearingDateConfirmOptionEnumMap);
        objectMap.put(LISTWITHOUTNOTICE_HEARINGDETAILS, objectMap);
        listWithoutNoticeHeardetailsObj.add(objectMap);

        hearingDataService.nullifyUnncessaryFieldsPopulated(listWithoutNoticeHeardetailsObj);

        assertEquals(
            null,
            ((LinkedHashMap) ((LinkedHashMap) listWithoutNoticeHeardetailsObj.get(0)).get("value")).get(CUSTOM_DETAILS)
        );

    }

    @Test()
    public void testNullifyUnncessaryFieldsPopulatedWithoutHearingDateConfirmOption() {
        Map<String, Object> hearingDateConfirmOptionEnumMap = new LinkedHashMap<>();
        Map<String, Object> objectMap = new LinkedHashMap<>();
        hearingDateConfirmOptionEnumMap.put(HEARING_DATE_CONFIRM_OPTION_ENUM, CONFIRMED_HEARING_DATES);
        List<Object> listWithoutNoticeHeardetailsObj = new ArrayList<>();
        objectMap.put("value", hearingDateConfirmOptionEnumMap);
        objectMap.put(LISTWITHOUTNOTICE_HEARINGDETAILS, objectMap);
        listWithoutNoticeHeardetailsObj.add(objectMap);

        hearingDataService.nullifyUnncessaryFieldsPopulated(listWithoutNoticeHeardetailsObj);

        assertEquals(
            null,
            ((LinkedHashMap) ((LinkedHashMap) listWithoutNoticeHeardetailsObj.get(0)).get("value")).get(CUSTOM_DETAILS)
        );

    }

    @Test()
    public void testGetLinkedCasesDynamicList() {
        String caseId = "testCaseRefNo";
        List<CaseLinkedData> caseLinkedDataList = new ArrayList<>();
        CaseLinkedData caseLinkedData = CaseLinkedData.caseLinkedDataWith()
            .caseName("CaseName-Test10")
            .caseReference("testCaseRefNo")
            .build();
        caseLinkedDataList.add(caseLinkedData);
        when(hearingService.getCaseLinkedData(any(), any())).thenReturn(caseLinkedDataList);
        List<DynamicListElement> dynamicListElementList = hearingDataService.getLinkedCasesDynamicList(authToken,
                                                                                                       caseId);

        assertEquals("testCaseRefNo", (dynamicListElementList.get(0).getCode()));

    }

    @Test()
    public void testGetLinkedCasesDynamicListException() {
        String caseId = "testCaseRefNo";
        List<CaseLinkedData> caseLinkedDataList = new ArrayList<>();
        CaseLinkedData caseLinkedData = CaseLinkedData.caseLinkedDataWith()
            .caseName("CaseName-Test10")
            .caseReference("testCaseRefNo")
            .build();
        caseLinkedDataList.add(caseLinkedData);
        when(hearingService.getCaseLinkedData(any(), any())).thenThrow(new RuntimeException());

        List<DynamicListElement> dynamicListElementList = hearingDataService.getLinkedCasesDynamicList(authToken,
                                                                                                       caseId);
        Assert.assertEquals(0, dynamicListElementList.size());
    }

    @Test()
    public void testPrePopulateHearingTypeExceptionRetrieveCategoryValues() {
        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        when(refDataUserService.retrieveCategoryValues(authToken,
                                                       HEARINGTYPE,
                                                       IS_HEARINGCHILDREQUIRED_N)).thenThrow(new RuntimeException());
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, HEARINGTYPE)).thenReturn(
            listHearingTypes);
        List<DynamicListElement> expectedResponse = hearingDataService.prePopulateHearingType(authToken);
        assertNull(expectedResponse.get(0).getCode());
    }

    @Test()
    public void testPrePopulateHearingTypeExceptionWhileFilterCategoryValuesByCategoryId() {
        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.retrieveCategoryValues(authToken, HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N)).thenReturn(
            commonDataResponse);
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse,
                                                                 HEARINGTYPE)).thenThrow(new RuntimeException());
        List<DynamicListElement> expectedResponse = hearingDataService.prePopulateHearingType(authToken);
        assertNull(expectedResponse.get(0).getCode());
    }

    @Test
    public void testHearingDataForSelectedHearing() {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(List.of(Element.<HearingData>builder()
                                                                .id(UUID.fromString(TEST_UUID))
                                                                .value(HearingData.builder()
                                                                           .confirmedHearingDates(DynamicList.builder()
                                                                                                      .value(
                                                                                                          DynamicListElement.builder()
                                                                                                              .code(
                                                                                                                  "123")
                                                                                                              .build())
                                                                                                      .build())
                                                                           .hearingDateConfirmOptionEnum(
                                                                               HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
                                                                           .build())
                                                                .build()))
                              .build())
            .applicantsFL401(PartyDetails.builder().partyId(UUID.fromString(TEST_UUID)).build())
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                                      .hearingID(123L)
                                      .hearingDaySchedule(List.of(HearingDaySchedule
                                                                      .hearingDayScheduleWith()
                                                                      .hearingStartDateTime(LocalDateTime.now())
                                                                      .hearingEndDateTime(LocalDateTime.now())
                                                                      .hearingVenueAddress("abc")
                                                                      .attendees(List.of(
                                                                          Attendee.attendeeWith().partyID(TEST_UUID)
                                                                              .hearingSubChannel("TEL").build()))
                                                                      .build()))
                                      .build())).build();
        assertNotNull(hearingDataService.getHearingDataForSelectedHearing(caseData, hearings, "testAuth"));
    }

    @Test
    public void testHearingDataForSelectedHearingWithoutDayLightSavingHearingTime() {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(List.of(Element.<HearingData>builder()
                                                                .id(UUID.fromString(TEST_UUID))
                                                                .value(HearingData.builder()
                                                                           .confirmedHearingDates(DynamicList.builder()
                                                                                                      .value(
                                                                                                          DynamicListElement.builder()
                                                                                                              .code(
                                                                                                                  "123")
                                                                                                              .build())
                                                                                                      .build())
                                                                           .hearingDateConfirmOptionEnum(
                                                                               HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
                                                                           .build())
                                                                .build()))
                              .build())
            .applicantsFL401(PartyDetails.builder().partyId(UUID.fromString(TEST_UUID)).build())
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                                      .hearingID(123L)
                                      .hearingDaySchedule(List.of(HearingDaySchedule
                                                                      .hearingDayScheduleWith()
                                                                      .hearingStartDateTime(LocalDateTime.of(
                                                                          2024,
                                                                          03,
                                                                          11,
                                                                          10,
                                                                          0,
                                                                          0
                                                                      ))
                                                                      .hearingEndDateTime(LocalDateTime.of(
                                                                          2024,
                                                                          03,
                                                                          11,
                                                                          11,
                                                                          1,
                                                                          0
                                                                      ))
                                                                      .hearingVenueAddress("abc")
                                                                      .attendees(List.of(
                                                                          Attendee.attendeeWith().partyID(TEST_UUID)
                                                                              .hearingSubChannel("TEL").build()))
                                                                      .build()))
                                      .build())).build();
        List<Element<HearingData>> hearingDataForSelectedHearing = hearingDataService.getHearingDataForSelectedHearing(
            caseData,
            hearings,
            "testAuth"
        );
        assertNotNull(hearingDataForSelectedHearing);
        assert (hearingDataForSelectedHearing.get(0).getValue().getHearingdataFromHearingTab().get(0).getValue().getHearingTime().equals(
            "10:00 AM"));
    }

    @Test
    public void testHearingDataForSelectedHearingDaylightSavingsHearingTime() {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(List.of(Element.<HearingData>builder()
                                                                .id(UUID.fromString(TEST_UUID))
                                                                .value(HearingData.builder()
                                                                           .confirmedHearingDates(DynamicList.builder()
                                                                                                      .value(
                                                                                                          DynamicListElement.builder()
                                                                                                              .code(
                                                                                                                  "123")
                                                                                                              .build())
                                                                                                      .build())
                                                                           .hearingDateConfirmOptionEnum(
                                                                               HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
                                                                           .build())
                                                                .build()))
                              .build())
            .applicantsFL401(PartyDetails.builder().partyId(UUID.fromString(TEST_UUID)).build())
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                                      .hearingID(123L)
                                      .hearingDaySchedule(List.of(HearingDaySchedule
                                                                      .hearingDayScheduleWith()
                                                                      .hearingStartDateTime(LocalDateTime.now())
                                                                      .hearingStartDateTime(LocalDateTime.of(
                                                                          2023,
                                                                          10,
                                                                          17,
                                                                          9,
                                                                          0,
                                                                          0
                                                                      ))
                                                                      .hearingEndDateTime(LocalDateTime.of(
                                                                          2023,
                                                                          10,
                                                                          17,
                                                                          11,
                                                                          30,
                                                                          0
                                                                      ))
                                                                      .attendees(List.of(
                                                                          Attendee.attendeeWith().partyID(TEST_UUID)
                                                                              .hearingSubChannel("TEL").build()))
                                                                      .build()))
                                      .build())).build();
        List<Element<HearingData>> hearingDataForSelectedHearing = hearingDataService.getHearingDataForSelectedHearing(
            caseData,
            hearings,
            "testAuth"
        );
        assertNotNull(hearingDataForSelectedHearing);
        assert (hearingDataForSelectedHearing.get(0).getValue().getHearingdataFromHearingTab().get(0).getValue().getHearingTime().equals(
            "10:00 AM"));
    }

    @Test
    public void testgetHearingStartDate() {
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                                      .hearingID(123L)
                                      .hmcStatus(LISTED)
                                      .nextHearingDate(LocalDateTime.now())
                                      .hearingDaySchedule(List.of(HearingDaySchedule
                                                                      .hearingDayScheduleWith()
                                                                      .hearingStartDateTime(LocalDateTime.now())
                                                                      .hearingEndDateTime(LocalDateTime.now())
                                                                      .hearingVenueAddress("abc")
                                                                      .attendees(List.of(
                                                                          Attendee.attendeeWith().partyID(TEST_UUID)
                                                                              .hearingSubChannel("TELOTHER").build()))
                                                                      .build()))
                                      .build())).build();
        assertNotNull(hearingDataService.getHearingStartDate("123", hearings));
    }

    @Test
    public void testgetgetHearingDaySchedule() {
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                                      .hearingID(123L)
                                      .hmcStatus(LISTED)
                                      .hearingDaySchedule(List.of(HearingDaySchedule
                                                                      .hearingDayScheduleWith()
                                                                      .hearingStartDateTime(LocalDateTime.now())
                                                                      .hearingEndDateTime(LocalDateTime.now())
                                                                      .hearingVenueAddress("abc")
                                                                      .attendees(List.of(
                                                                          Attendee.attendeeWith().partyID(TEST_UUID)
                                                                              .hearingSubChannel("TELOTHER").build()))
                                                                      .build()))
                                      .build())).build();
        assertNotNull(hearingDataService.getHearingStartDate("123", hearings));
    }

    @Test
    public void testgetgetHearingBlank() {
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                                      .hearingID(123L)
                                      .hmcStatus(LISTED)
                                      /*.hearingDaySchedule(List.of(HearingDaySchedule
                                                                      .hearingDayScheduleWith()
                                                                      .hearingStartDateTime(LocalDateTime.now())
                                                                      .hearingEndDateTime(LocalDateTime.now())
                                                                      .hearingVenueAddress("abc")
                                                                      .attendees(List.of(
                                                                          Attendee.attendeeWith().partyID(TEST_UUID)
                                                                              .hearingSubChannel("TELOTHER").build()))
                                                                      .build()))*/
                                      .build())).build();
        assertNotNull(hearingDataService.getHearingStartDate("123", hearings));
    }

    @Test()
    public void testGenerateHearingDataForCafcassCymru() {
        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.retrieveCategoryValues(authToken, HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N)).thenReturn(
            commonDataResponse);
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, HEARINGTYPE)).thenReturn(
            listHearingTypes);
        when(locationRefDataService.getCourtLocations(authToken)).thenReturn(listHearingTypes);
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestName")
            .representativeFirstName("Ram")
            .representativeLastName("Mer")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(element(applicant));
        applicantList.add(element(applicant));
        applicantList.add(element(applicant));
        applicantList.add(element(applicant));
        applicantList.add(element(applicant));
        PartyDetails respondent = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(element(respondent));
        respondentList.add(element(respondent));
        respondentList.add(element(respondent));
        respondentList.add(element(respondent));
        respondentList.add(element(respondent));
        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .applicantName("test")
            .solicitorName("Test")
            .respondentName("Test")
            .applicants(applicantList)
            .respondents(respondentList)
            .caseTypeOfApplication("C100")
            .caseManagementLocation(CaseManagementLocation.builder().region("9").build())
            .build();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            HearingDataPrePopulatedDynamicLists.builder()
                .retrievedHearingTypes(dynamicList)
                .hearingListedLinkedCases(dynamicList)
                .retrievedHearingDates(dynamicList)
                .retrievedHearingChannels(dynamicList)
                .retrievedCourtLocations(dynamicList)
                .retrievedVideoSubChannels(dynamicList)
                .retrievedTelephoneSubChannels(dynamicList)
                .retrievedCourtLocations(dynamicList)
                .hearingListedLinkedCases(dynamicList)
                .build();
        HearingData expectedResponse = hearingDataService.generateHearingData(
            hearingDataPrePopulatedDynamicLists,
            caseData
        );
        assertNotNull(expectedResponse);
    }

    @Test
    public void testHearingDataForSelectedHearingForSdo() {
        CaseData caseData = CaseData.builder()
            .id(123456789000000L)
            .applicantsFL401(PartyDetails.builder().partyId(UUID.fromString(TEST_UUID)).build())
            .build();
        HearingData hearingData = HearingData.builder()
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .confirmedHearingDates(DynamicList.builder()
                                       .value(
                                           DynamicListElement.builder()
                                               .code("123")
                                               .build())
                                       .build())
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                                      .hearingID(123L)
                                      .hearingDaySchedule(List.of(HearingDaySchedule
                                                                      .hearingDayScheduleWith()
                                                                      .hearingStartDateTime(LocalDateTime.now())
                                                                      .hearingEndDateTime(LocalDateTime.now())
                                                                      .hearingVenueAddress("abc")
                                                                      .attendees(List.of(
                                                                          Attendee.attendeeWith().partyID(TEST_UUID)
                                                                              .hearingSubChannel("TEL").build()))
                                                                      .build()))
                                      .build())).build();
        assertNotNull(hearingDataService.getHearingDataForSelectedHearingForSdo(hearingData, hearings, caseData));
    }

    @Test
    public void testHearingDataForSelectedHearingForSolicitorOrdersHearingDetails() {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder()
                              .solicitorOrdersHearingDetails(List.of(Element.<HearingData>builder()
                                                                         .id(UUID.fromString(TEST_UUID))
                                                                         .value(HearingData.builder()
                                                                                    .confirmedHearingDates(DynamicList.builder()
                                                                                                               .value(
                                                                                                                   DynamicListElement.builder()
                                                                                                                       .code(
                                                                                                                           "123")
                                                                                                                       .build())
                                                                                                               .build())
                                                                                    .hearingDateConfirmOptionEnum(
                                                                                        HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
                                                                                    .build())
                                                                         .build()))
                              .build())
            .applicantsFL401(PartyDetails.builder().partyId(UUID.fromString(TEST_UUID)).build())
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                                      .hearingID(123L)
                                      .hearingDaySchedule(List.of(HearingDaySchedule
                                                                      .hearingDayScheduleWith()
                                                                      .hearingStartDateTime(LocalDateTime.now())
                                                                      .hearingEndDateTime(LocalDateTime.now())
                                                                      .hearingVenueAddress("abc")
                                                                      .attendees(List.of(
                                                                          Attendee.attendeeWith().partyID(TEST_UUID)
                                                                              .hearingSubChannel("TEL").build()))
                                                                      .build()))
                                      .build())).build();
        assertNotNull(hearingDataService.getHearingDataForSelectedHearing(caseData, hearings, "testAuth"));
    }

    @Test
    public void testSetHearingDataForSelectedHearing() {
        CaseData caseData = CaseData.builder()
            .id(123)
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(List.of(Element.<HearingData>builder()
                                                           .id(UUID.fromString(TEST_UUID))
                                                           .value(HearingData.builder()
                                                                      .confirmedHearingDates(DynamicList.builder()
                                                                                                 .value(
                                                                                                     DynamicListElement.builder()
                                                                                                         .code(
                                                                                                             "123")
                                                                                                         .build())
                                                                                                 .build())
                                                                      .hearingDateConfirmOptionEnum(
                                                                          HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
                                                                      .build())
                                                           .build()))
                              .build())
            .applicantsFL401(PartyDetails.builder().partyId(UUID.fromString(TEST_UUID)).build())
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                                      .hearingID(123L)
                                      .hearingDaySchedule(List.of(HearingDaySchedule
                                                                      .hearingDayScheduleWith()
                                                                      .hearingStartDateTime(LocalDateTime.now())
                                                                      .hearingEndDateTime(LocalDateTime.now())
                                                                      .hearingVenueAddress("abc")
                                                                      .attendees(List.of(
                                                                          Attendee.attendeeWith().partyID(TEST_UUID)
                                                                              .hearingSubChannel("TEL").build()))
                                                                      .build()))
                                      .build())).build();
        when(hearingService.getHearings(authToken,"123")).thenReturn(hearings);
        assertNotNull(hearingDataService.setHearingDataForSelectedHearing(authToken, caseData,
                                                                          CreateSelectOrderOptionsEnum.occupation
        ));
    }

    @Test
    public void testSetHearingDataForSelectedHearingForSolicitorOrder() {
        CaseData caseData = CaseData.builder()
            .id(123)
            .manageOrders(ManageOrders.builder()
                              .solicitorOrdersHearingDetails(List.of(Element.<HearingData>builder()
                                                                .id(UUID.fromString(TEST_UUID))
                                                                .value(HearingData.builder()
                                                                           .confirmedHearingDates(DynamicList.builder()
                                                                                                      .value(
                                                                                                          DynamicListElement.builder()
                                                                                                              .code(
                                                                                                                  "123")
                                                                                                              .build())
                                                                                                      .build())
                                                                           .hearingDateConfirmOptionEnum(
                                                                               HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
                                                                           .build())
                                                                .build()))
                              .build())
            .applicantsFL401(PartyDetails.builder().partyId(UUID.fromString(TEST_UUID)).build())
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                                      .hearingID(123L)
                                      .hearingDaySchedule(List.of(HearingDaySchedule
                                                                      .hearingDayScheduleWith()
                                                                      .hearingStartDateTime(LocalDateTime.now())
                                                                      .hearingEndDateTime(LocalDateTime.now())
                                                                      .hearingVenueAddress("abc")
                                                                      .attendees(List.of(
                                                                          Attendee.attendeeWith().partyID(TEST_UUID)
                                                                              .hearingSubChannel("TEL").build()))
                                                                      .build()))
                                      .build())).build();
        when(hearingService.getHearings(authToken,"123")).thenReturn(hearings);
        assertNotNull(hearingDataService.setHearingDataForSelectedHearing(authToken, caseData,
                                                                          CreateSelectOrderOptionsEnum.occupation));
    }

    @Test
    public void testFetchingAwaitingAndCompletedHearings() {
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                                      .hearingID(123L)
                                      .hmcStatus(COMPLETED)
                                      .nextHearingDate(LocalDateTime.now())
                                      .hearingDaySchedule(List.of(HearingDaySchedule
                                                                      .hearingDayScheduleWith()
                                                                      .hearingVenueAddress("abc")
                                                                      .attendees(List.of(
                                                                          Attendee.attendeeWith().partyID(TEST_UUID)
                                                                              .hearingSubChannel("TEL").build()))
                                                                      .build()))
                                      .build())).build();
        when(hearingService.getHearings(authToken,"123")).thenReturn(hearings);
        assertFalse(hearingDataService.getListOfRequestedStatusHearings(authToken, "123", List.of(COMPLETED)).isEmpty());
    }

    @Test
    public void testFetchingAwaitingAndCompletedHearingsForException() {
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                                      .hearingID(123L)
                                      .hmcStatus(COMPLETED)
                                      .hearingDaySchedule(List.of(HearingDaySchedule
                                                                      .hearingDayScheduleWith()
                                                                      .hearingVenueAddress("abc")
                                                                      .attendees(List.of(
                                                                          Attendee.attendeeWith().partyID(TEST_UUID)
                                                                              .hearingSubChannel("TEL").build()))
                                                                      .build()))
                                      .build())).build();
        when(hearingService.getHearings(authToken,"123")).thenThrow(new RuntimeException());
        assertTrue(hearingDataService.getListOfRequestedStatusHearings(authToken, "123", List.of(COMPLETED)).isEmpty());
    }

    @Test
    public void testPopulatePartiesNamesForC100() {
        Map<String, Object> tempCaseDetails = new HashMap<>();
        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .applicants(Arrays.asList(element(applicant), element(applicant), element(applicant), element(applicant), element(applicant)))
            .respondents(Arrays.asList(element(respondent), element(respondent), element(respondent), element(respondent), element(respondent)))
            .caseTypeOfApplication("C100")
            .build();

        //invoke service
        hearingDataService.populatePartiesAndSolicitorsNames(caseData, tempCaseDetails);

        //validate
        assertFalse(tempCaseDetails.isEmpty());
        Map<String, Object> tempPartyNamesMap = (Map<String, Object>) tempCaseDetails.get("tempPartyNamesForDocGen");
        assertNotNull(tempPartyNamesMap);
        assertEquals("AppFN AppLN (Applicant1)", tempPartyNamesMap.get("applicantName1"));
        assertEquals("AppFN AppLN (Applicant2)", tempPartyNamesMap.get("applicantName2"));
        assertEquals("AppFN AppLN (Applicant3)", tempPartyNamesMap.get("applicantName3"));
        assertEquals("AppFN AppLN (Applicant4)", tempPartyNamesMap.get("applicantName4"));
        assertEquals("AppFN AppLN (Applicant5)", tempPartyNamesMap.get("applicantName5"));
        assertEquals("AppSolFN AppSolLN (Applicant1 solicitor)", tempPartyNamesMap.get("applicantSolicitor1"));
        assertEquals("AppSolFN AppSolLN (Applicant2 solicitor)", tempPartyNamesMap.get("applicantSolicitor2"));
        assertEquals("AppSolFN AppSolLN (Applicant3 solicitor)", tempPartyNamesMap.get("applicantSolicitor3"));
        assertEquals("AppSolFN AppSolLN (Applicant4 solicitor)", tempPartyNamesMap.get("applicantSolicitor4"));
        assertEquals("AppSolFN AppSolLN (Applicant5 solicitor)", tempPartyNamesMap.get("applicantSolicitor5"));
        assertEquals("RespFN RespLN (Respondent1)", tempPartyNamesMap.get("respondentName1"));
        assertEquals("RespFN RespLN (Respondent2)", tempPartyNamesMap.get("respondentName2"));
        assertEquals("RespFN RespLN (Respondent3)", tempPartyNamesMap.get("respondentName3"));
        assertEquals("RespFN RespLN (Respondent4)", tempPartyNamesMap.get("respondentName4"));
        assertEquals("RespFN RespLN (Respondent5)", tempPartyNamesMap.get("respondentName5"));
        assertEquals("RespSolFN RespSolLN (Respondent1 solicitor)", tempPartyNamesMap.get("respondentSolicitor1"));
        assertEquals("RespSolFN RespSolLN (Respondent2 solicitor)", tempPartyNamesMap.get("respondentSolicitor2"));
        assertEquals("RespSolFN RespSolLN (Respondent3 solicitor)", tempPartyNamesMap.get("respondentSolicitor3"));
        assertEquals("RespSolFN RespSolLN (Respondent4 solicitor)", tempPartyNamesMap.get("respondentSolicitor4"));
        assertEquals("RespSolFN RespSolLN (Respondent5 solicitor)", tempPartyNamesMap.get("respondentSolicitor5"));
    }

    @Test
    public void testPopulatePartiesNamesForC100WithEmptyData() {
        Map<String, Object> tempCaseDetails = new HashMap<>();
        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .applicants(Collections.emptyList())
            .respondents(Collections.emptyList())
            .caseTypeOfApplication("C100")
            .build();

        //invoke service
        hearingDataService.populatePartiesAndSolicitorsNames(caseData, tempCaseDetails);

        //validate
        assertFalse(tempCaseDetails.isEmpty());
        Map<String, Object> tempPartyNamesMap = (Map<String, Object>) tempCaseDetails.get("tempPartyNamesForDocGen");
        assertTrue(tempPartyNamesMap.isEmpty());
    }

    @Test
    public void testPopulatePartiesNamesForFl401() {
        Map<String, Object> tempCaseDetails = new HashMap<>();
        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .applicantName("App")
            .respondentName("Resp")
            .applicantsFL401(applicant)
            .respondentsFL401(respondent)
            .caseTypeOfApplication("FL401")
            .build();

        //invoke service
        hearingDataService.populatePartiesAndSolicitorsNames(caseData, tempCaseDetails);

        //validate
        assertFalse(tempCaseDetails.isEmpty());
        Map<String, Object> tempPartyNamesMap = (Map<String, Object>) tempCaseDetails.get("tempPartyNamesForDocGen");
        assertFalse(tempPartyNamesMap.isEmpty());
        assertEquals("App (Applicant)", tempPartyNamesMap.get("applicantName"));
        assertEquals("AppSolFN AppSolLN (Applicant solicitor)", tempPartyNamesMap.get("applicantSolicitor"));
        assertEquals("Resp (Respondent)", tempPartyNamesMap.get("respondentName"));
        assertEquals("RespSolFN RespSolLN (Respondent solicitor)", tempPartyNamesMap.get("respondentSolicitor"));
    }

    @Test
    public void testPopulatePartiesNamesForFl401WithEmptyData() {
        Map<String, Object> tempCaseDetails = new HashMap<>();
        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .applicantsFL401(PartyDetails.builder().build())
            .respondentsFL401(PartyDetails.builder().build())
            .caseTypeOfApplication("FL401")
            .build();

        //invoke service
        hearingDataService.populatePartiesAndSolicitorsNames(caseData, tempCaseDetails);

        //validate
        assertFalse(tempCaseDetails.isEmpty());
        Map<String, Object> tempPartyNamesMap = (Map<String, Object>) tempCaseDetails.get("tempPartyNamesForDocGen");
        assertTrue(tempPartyNamesMap.isEmpty());
    }

    @Test
    public void testSetHearingDataForSelectedHearingForCaOrderDaCase() {
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestName")
            .representativeFirstName("Ram")
            .representativeLastName("Mer")
            .partyId(UUID.fromString(TEST_UUID))
            .build();
        PartyDetails respondent = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .partyId(UUID.fromString(TEST_UUID))
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().id(UUID.fromString(TEST_UUID)).value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);
        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().id(UUID.fromString(TEST_UUID)).value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);
        CaseData caseData = CaseData.builder()
            .id(123)
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(List.of(Element.<HearingData>builder()
                                                                .id(UUID.fromString(TEST_UUID))
                                                                .value(HearingData.builder()
                                                                           .confirmedHearingDates(DynamicList.builder()
                                                                                                      .value(
                                                                                                          DynamicListElement.builder()
                                                                                                              .code(
                                                                                                                  "123")
                                                                                                              .build())
                                                                                                      .build())
                                                                           .hearingDateConfirmOptionEnum(
                                                                               HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
                                                                           .build())
                                                                .build()))
                              .build())
            //.applicantsFL401(PartyDetails.builder().partyId(UUID.fromString(TEST_UUID)).build())
            .applicants(applicantList)
            .respondents(respondentList)
            .caseTypeOfApplication("C100")
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(CaseHearing.caseHearingWith()
                                      .hearingID(123L)
                                      .hearingDaySchedule(List.of(HearingDaySchedule
                                                                      .hearingDayScheduleWith()
                                                                      .hearingStartDateTime(LocalDateTime.now())
                                                                      .hearingEndDateTime(LocalDateTime.now())
                                                                      .hearingVenueAddress("abc")
                                                                      .attendees(List.of(
                                                                          Attendee.attendeeWith().partyID(TEST_UUID)
                                                                              .hearingSubChannel("TEL").build()))
                                                                      .build()))
                                      .build())).build();
        when(hearingService.getHearings(authToken,"123")).thenReturn(hearings);
        assertNotNull(hearingDataService.setHearingDataForSelectedHearing(authToken, caseData,
                                                                          CreateSelectOrderOptionsEnum.amendDischargedVaried
        ));
    }
}
