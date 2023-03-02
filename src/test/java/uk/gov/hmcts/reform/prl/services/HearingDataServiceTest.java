package uk.gov.hmcts.reform.prl.services;

import groovy.util.logging.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioBeforeAEnum;
import uk.gov.hmcts.reform.prl.mapper.hearingrequest.HearingRequestDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

    public static final String authToken = "Bearer TestAuthToken";


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
            .hearingChannelDynamicRadioList(dynamicList)
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
            .mainApplicantName("Test")
            .build();

        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> listWithoutNoticeHearingDetails = Collections.singletonList(childElement);
        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeHearingDetails(listWithoutNoticeHearingDetails)
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
        when((hearingService.getHearings(authToken,"1234")))
            .thenReturn(Hearings.hearingsWith().caseHearings(caseHearings).build());
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
            .hearingChannelDynamicRadioList(dynamicList)
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
            .mainApplicantName("Test")
            .build();

        Element<HearingData> childElement = Element.<HearingData>builder().value(hearingData).build();
        List<Element<HearingData>> listWithoutNoticeHearingDetails = Collections.singletonList(childElement);

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .listWithoutNoticeHearingDetails(listWithoutNoticeHearingDetails)
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
        List<Element<HearingData>>  expectedResponse =
            hearingDataService.getHearingData(listWithoutNoticeHearingDetails,hearingDataPrePopulatedDynamicLists);
        assertNotNull(expectedResponse);
    }

    @Test()
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

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
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
        HearingData expectedResponse = hearingDataService.generateHearingData(hearingDataPrePopulatedDynamicLists, "test");
        assertNotNull(expectedResponse);
    }

    @Test()
    public void testGetLinkedCase() {
        List<DynamicListElement> expectedResponse = hearingDataService.getLinkedCase(authToken, "test");
        assertNotNull(expectedResponse);
    }
}





