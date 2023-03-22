package uk.gov.hmcts.reform.prl.services;

import groovy.util.logging.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.HearingPriorityTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioBeforeAEnum;
import uk.gov.hmcts.reform.prl.mapper.hearingrequest.HearingRequestDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_N;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;

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
    HearingRequestDataMapper hearingRequestDataMapper;

    @Mock
    LocationRefDataService locationRefDataService;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    CoreCaseDataApi coreCaseDataApi;
    @Mock
    AllocatedJudgeService allocatedJudgeService;

    public static final String authToken = "Bearer TestAuthToken";
    @Mock
    HearingApiClient hearingApiClient;

    @Mock
    Hearings hearingDetails;

    @Test()
    public void testPopulateHearingDynamicLists() {
        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.retrieveCategoryValues(authToken,HEARINGTYPE,IS_HEARINGCHILDREQUIRED_N)).thenReturn(commonDataResponse);
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse,HEARINGTYPE)).thenReturn(listHearingTypes);
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
            .hearingEstimatedHours(5)
            .hearingEstimatedMinutes(40)
            .hearingEstimatedDays(15)
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
        List<Element<HearingData>> ordersHearingDetails = Collections.singletonList(childElement);
        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .manageOrders(ManageOrders.builder()
                              .ordersHearingDetails(ordersHearingDetails).build())
            .build();
        HearingDataPrePopulatedDynamicLists expectedResponse = hearingDataService.populateHearingDynamicLists(authToken, "45654654", caseData);
        assertNotNull(expectedResponse);
    }


    @Test()
    public void testPrePopulateHearingType() {
        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.retrieveCategoryValues(authToken,HEARINGTYPE,IS_HEARINGCHILDREQUIRED_N)).thenReturn(commonDataResponse);
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse,HEARINGTYPE)).thenReturn(listHearingTypes);
        List<DynamicListElement> expectedResponse = hearingDataService.prePopulateHearingType(authToken);
        assertEquals(expectedResponse.get(0).getCode(),"ABA5-REV");
        assertEquals(expectedResponse.get(0).getLabel(),"Review");
    }

    @Test()
    public void testPrePopulateHearingDates() {

        List<HearingDaySchedule> hearingDaySchedules = new ArrayList<>();
        hearingDaySchedules.add(HearingDaySchedule.hearingDayScheduleWith().hearingJudgeId("123").hearingJudgeName("hearingJudgeName")
                                    .hearingVenueId("venueId").hearingVenueAddress("venueAddress")
                                    .hearingStartDateTime(LocalDateTime.now()).build());
        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith().hmcStatus(LISTED).hearingDaySchedule(hearingDaySchedules).build());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .build();
        when((hearingService.getHearings(any(),any())))
            .thenReturn(Hearings.hearingsWith().hmctsServiceCode("CaseName-Test10")
            .caseRef("1677767515750127").caseHearings(caseHearings).build());
        List<DynamicListElement> expectedResponse = hearingDataService.getHearingStartDate(authToken,caseData);
        assertNotNull(expectedResponse);
    }

    @Test()
    public void testGetHearingData() {

        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.retrieveCategoryValues(authToken,HEARINGTYPE,IS_HEARINGCHILDREQUIRED_N)).thenReturn(commonDataResponse);
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse,HEARINGTYPE)).thenReturn(listHearingTypes);
        when(locationRefDataService.getCourtLocations(authToken)).thenReturn(listHearingTypes);

        JudicialUser judicialUser = JudicialUser.builder()
            .personalCode("Test")
            .idamId("Test")
            .build();

        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList = DynamicList.builder()
            //.listItems(dynamicListElementsList)
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
            .hearingEstimatedHours(5)
            .hearingEstimatedMinutes(40)
            .hearingEstimatedDays(15)
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .applicantName("Test")
            .hearingJudgeNameAndEmail(judicialUser)
            .build();

        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> ordersHearingDetails = Collections.singletonList(childElement);

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .manageOrders(ManageOrders.builder().ordersHearingDetails(ordersHearingDetails).build())
            .build();
        List<JudicialUsersApiResponse> judicialUsersApiResponses = new ArrayList<>();
        JudicialUsersApiResponse judicialUsersApiResponse = JudicialUsersApiResponse.builder()
            //.emailId("Test")
            //.fullName("Test")
            //.surname("Test")
            .personalCode("Test")
            .build();
        judicialUsersApiResponses.add(judicialUsersApiResponse);
        JudicialUsersApiRequest judicialUsersApiRequest = JudicialUsersApiRequest.builder()
            .personalCode(new String[]{"Test2", "test","test5"}).build();
        when(allocatedJudgeService.getPersonalCode(judicialUser)).thenReturn(new String[]{"Test2", "test","test5"});
        when(refDataUserService.getAllJudicialUserDetails(judicialUsersApiRequest)).thenReturn(judicialUsersApiResponses);
        DynamicList dynamicList1 = DynamicList.builder()
            .listItems(dynamicListElementsList)
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
        List<Element<HearingData>>  expectedResponse =
            hearingDataService.getHearingData(ordersHearingDetails,hearingDataPrePopulatedDynamicLists);
        assertNotNull(expectedResponse);
    }



    @Test()
    public void testGetHearingDataSetJ() {

        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.retrieveCategoryValues(authToken,HEARINGTYPE,IS_HEARINGCHILDREQUIRED_N)).thenReturn(commonDataResponse);
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse,HEARINGTYPE)).thenReturn(listHearingTypes);
        when(locationRefDataService.getCourtLocations(authToken)).thenReturn(listHearingTypes);

        JudicialUser judicialUser = JudicialUser.builder()
            .personalCode("Test")
            .build();

        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList = DynamicList.builder()
            //.listItems(dynamicListElementsList)
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
            .hearingEstimatedHours(5)
            .hearingEstimatedMinutes(40)
            .hearingEstimatedDays(15)
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .applicantName("Test")
            .hearingJudgeNameAndEmail(judicialUser)
            .build();

        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> ordersHearingDetails = Collections.singletonList(childElement);

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .manageOrders(ManageOrders.builder().ordersHearingDetails(ordersHearingDetails).build())
            .build();
        List<JudicialUsersApiResponse> judicialUsersApiResponses = new ArrayList<>();
        JudicialUsersApiResponse judicialUsersApiResponse = JudicialUsersApiResponse.builder()
            //.emailId("Test")
            //.fullName("Test")
            //.surname("Test")
            .personalCode("Test")
            .build();
        judicialUsersApiResponses.add(judicialUsersApiResponse);
        JudicialUsersApiRequest judicialUsersApiRequest = JudicialUsersApiRequest.builder()
            .personalCode(new String[]{"Test2", "test","test5"}).build();
        when(allocatedJudgeService.getPersonalCode(judicialUser)).thenReturn(new String[]{"Test2", "test","test5"});
        when(refDataUserService.getAllJudicialUserDetails(judicialUsersApiRequest)).thenReturn(judicialUsersApiResponses);
        DynamicList dynamicList1 = DynamicList.builder()
            .listItems(dynamicListElementsList)
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
        List<Element<HearingData>>  expectedResponse =
            hearingDataService.getHearingData(ordersHearingDetails,hearingDataPrePopulatedDynamicLists);
        assertEquals(expectedResponse.get(0).getValue().getHearingJudgePersonalCode(),"Test");
    }


    @Test()
    @Ignore
    public void testGenerateHearingData() {
        List<CategoryValues> categoryValues = new ArrayList<>();
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Review").build());
        categoryValues.add(CategoryValues.builder().categoryKey(HEARINGTYPE).valueEn("Allocation").build());
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(categoryValues).build();
        when(refDataUserService.retrieveCategoryValues(authToken,HEARINGTYPE,IS_HEARINGCHILDREQUIRED_N)).thenReturn(commonDataResponse);
        List<DynamicListElement> listHearingTypes = new ArrayList<>();
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-REV").label("Review").build());
        listHearingTypes.add(DynamicListElement.builder().code("ABA5-ALL").label("Allocation").build());
        when(refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse,HEARINGTYPE)).thenReturn(listHearingTypes);
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
        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .applicantName("test")
            .solicitorName("Test")
            .respondentName("Test")
            .applicantsFL401(applicant)
            .caseTypeOfApplication("FL401")
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
        HearingData expectedResponse = hearingDataService.generateHearingData(hearingDataPrePopulatedDynamicLists, caseData);
        assertNotNull(expectedResponse);
    }

    @Test()
    public void testGetLinkedCases() {
        List<CaseLinkedData> caseLinkedDataList = new ArrayList<>();
        CaseLinkedData caseLinkedData = CaseLinkedData.hearingValuesWith()
            .caseName("CaseName-Test10")
            .caseReference("1677767515750127")
            .build();
        caseLinkedDataList.add(caseLinkedData);
        when(hearingService.getCaseLinkedData(any(), any())).thenReturn(caseLinkedDataList);
        CaseHearing caseHearing = CaseHearing.caseHearingWith()
            .hmcStatus("LISTED").build();
        List<CaseHearing> caseHearings =  new ArrayList<>();
        caseHearings.add(caseHearing);
        hearingDetails = Hearings.hearingsWith()
            .hmctsServiceCode("CaseName-Test10")
            .caseRef("1677767515750127")
            .caseHearings(caseHearings)
            .build();
        when(hearingService.getHearings(any(), any())).thenReturn(hearingDetails);

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .build();
        List<DynamicListElement> expectedResponse = hearingDataService.getLinkedCases(authToken, caseData);
        assertEquals(expectedResponse.get(0).getCode(),"1677767515750127");
        assertEquals(expectedResponse.get(0).getLabel(),"CaseName-Test10");
    }
}





