package uk.gov.hmcts.reform.prl.services;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.CommonDataRefApi;
import uk.gov.hmcts.reform.prl.clients.JudicialUserDetailsApi;
import uk.gov.hmcts.reform.prl.clients.StaffResponseDetailsApi;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.CaseFlag;
import uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.Flag;
import uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.FlagDetail;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategorySubValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffProfile;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGCHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_N;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGALOFFICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RD_STAFF_FIRST_PAGE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RD_STAFF_PAGE_SIZE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RD_STAFF_SECOND_PAGE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RD_STAFF_TOTAL_RECORDS_HEADER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFORDERASC;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFSORTCOLUMN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.VIDEOPLATFORM;


@RunWith(MockitoJUnitRunner.Silent.class)
public class RefDataUserServiceTest {

    public static final String FLAG_TYPE = "PARTY";
    @InjectMocks
    RefDataUserService refDataUserService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    IdamClient idamClient;

    @Mock
    StaffResponseDetailsApi staffResponseDetailsApi;

    @Mock
    StaffProfile staffProfile;

    @Mock
    StaffResponse staffResponse;

    @Mock
    JudicialUserDetailsApi judicialUserDetailsApi;

    @Mock
    JudicialUsersApiRequest judicialUsersApiRequest;

    @Mock
    CommonDataRefApi commonDataRefApi;

    @Mock
    LaunchDarklyClient launchDarklyClient;

    @Value("${prl.refdata.username}")
    private String refDataIdamUsername;

    @Value("${prl.refdata.password}")
    private String refDataIdamPassword;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "Bearer TestAuthToken";


    @Test
    public void testGetStaffDetailssWithNullStaffData() {
        when(staffResponseDetailsApi.getAllStaffResponseDetails(
            idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword),
            authTokenGenerator.generate(),
            SERVICENAME,
            STAFFSORTCOLUMN,
            STAFFORDERASC,
            RD_STAFF_PAGE_SIZE,
            RD_STAFF_FIRST_PAGE
        )).thenReturn(null);
        List<DynamicListElement> staffDetails = refDataUserService.getLegalAdvisorList();
        assertNull(staffDetails.get(0).getCode());
    }

    @Test
    public void testGetStaffDetailsWithException() {
        when(staffResponseDetailsApi.getAllStaffResponseDetails(
            idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword),
            authTokenGenerator.generate(),
            SERVICENAME,
            STAFFSORTCOLUMN,
            STAFFORDERASC,
            RD_STAFF_PAGE_SIZE,
            RD_STAFF_FIRST_PAGE
        ))
            .thenThrow(NullPointerException.class);
        List<DynamicListElement> legalAdvisor = refDataUserService.getLegalAdvisorList();
        assertNull(legalAdvisor.get(0).getCode());
    }

    @Test
    public void testGetStaffDetailsWithData() {

        StaffProfile staffProfile1 = StaffProfile.builder().userType("Legal office").lastName("David").emailId(
            "test2@com").build();

        StaffProfile staffProfile2 = StaffProfile.builder().userType(LEGALOFFICE).lastName("John").emailId(
            "test1@com").build();

        StaffResponse staffResponse1 = StaffResponse.builder().ccdServiceName("PRIVATELAW").staffProfile(staffProfile1).build();
        StaffResponse staffResponse2 = StaffResponse.builder().ccdServiceName("PRIVATELAW").staffProfile(staffProfile2).build();
        List<StaffResponse> listOfStaffResponse = new ArrayList<>();
        listOfStaffResponse.add(staffResponse1);
        listOfStaffResponse.add(staffResponse2);
        ResponseEntity<List<StaffResponse>> staffResponse = ResponseEntity.ok().body(listOfStaffResponse);
        when(idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword)).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn("s2sToken");
        when(staffResponseDetailsApi.getAllStaffResponseDetails(
            idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword),
            authTokenGenerator.generate(),
            SERVICENAME,
            STAFFSORTCOLUMN,
            STAFFORDERASC,
            RD_STAFF_PAGE_SIZE,
            RD_STAFF_FIRST_PAGE
        )).thenReturn(staffResponse);

        List<DynamicListElement> legalAdvisorList = refDataUserService.getLegalAdvisorList();
        assertNotNull(legalAdvisorList.get(0).getCode());
        assertEquals("David(test2@com)",legalAdvisorList.get(0).getCode());

    }

    @Test
    public void testGetAllJudicialUsersForV2() {
        when(idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword)).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        JudicialUsersApiResponse judge1 = JudicialUsersApiResponse.builder().surname("lastName1").fullName("judge1@test.com").build();
        JudicialUsersApiResponse judge2 = JudicialUsersApiResponse.builder().surname("lastName2").fullName("judge2@test.com").build();
        List<JudicialUsersApiResponse> listOfJudges = new ArrayList<>();
        listOfJudges.add(judge1);
        listOfJudges.add(judge2);
        when(launchDarklyClient.isFeatureEnabled(any())).thenReturn(true);
        JudicialUsersApiRequest judicialUsersApiRequest = JudicialUsersApiRequest.builder().personalCode(new String[3]).build();
        when(judicialUserDetailsApi.getAllJudicialUserDetailsV2(
            idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword),
            authTokenGenerator.generate(),
            judicialUsersApiRequest
        )).thenReturn(listOfJudges);
        List<JudicialUsersApiResponse> expectedRespose = refDataUserService.getAllJudicialUserDetails(judicialUsersApiRequest);
        assertNotNull(expectedRespose);
        assertEquals("lastName1",expectedRespose.get(0).getSurname());
    }

    @Test
    public void testGetAllJudicialUsersForV1() {
        when(idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword)).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        JudicialUsersApiResponse judge1 = JudicialUsersApiResponse.builder().surname("lastName1").fullName("judge1@test.com").build();
        JudicialUsersApiResponse judge2 = JudicialUsersApiResponse.builder().surname("lastName2").fullName("judge2@test.com").build();
        List<JudicialUsersApiResponse> listOfJudges = new ArrayList<>();
        listOfJudges.add(judge1);
        listOfJudges.add(judge2);
        when(launchDarklyClient.isFeatureEnabled(any())).thenReturn(false);
        JudicialUsersApiRequest judicialUsersApiRequest = JudicialUsersApiRequest.builder().personalCode(new String[3]).build();
        when(judicialUserDetailsApi.getAllJudicialUserDetails(
            idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword),
            authTokenGenerator.generate(),
            judicialUsersApiRequest
        )).thenReturn(listOfJudges);
        List<JudicialUsersApiResponse> expectedRespose = refDataUserService.getAllJudicialUserDetails(judicialUsersApiRequest);
        assertNotNull(expectedRespose);
        assertEquals("lastName1",expectedRespose.get(0).getSurname());
    }

    @Test
    public void testGetHearingTypeWithData() {
        when(authTokenGenerator.generate()).thenReturn(s2sToken);

        List<CategoryValues> listOfCategoryValues = new ArrayList<>();
        CategoryValues categoryValues1 = CategoryValues.builder().categoryKey("HearingType").valueEn("Celebration hearing").build();
        CategoryValues categoryValues2 = CategoryValues.builder().categoryKey("HearingType").valueEn("Case Management Conference").build();
        listOfCategoryValues.add(categoryValues1);
        listOfCategoryValues.add(categoryValues2);
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(listOfCategoryValues).build();
        when(commonDataRefApi.getAllCategoryValuesByCategoryId(authToken,
                                                               authTokenGenerator.generate(),
                                                               HEARINGTYPE,
                                                               SERVICE_ID,
                                                               IS_HEARINGCHILDREQUIRED_N)).thenReturn(commonDataResponse);
        CommonDataResponse commonResponse = refDataUserService.retrieveCategoryValues(
            authToken,
            HEARINGTYPE,
            IS_HEARINGCHILDREQUIRED_N
        );
        assertNotNull(commonResponse);
        assertEquals("Celebration hearing",commonResponse.getCategoryValues().get(0).getValueEn());
    }

    @Test
    public void testRetrieveCaseFlags() {
        FlagDetail flagDetail1 = FlagDetail.builder().flagCode("ABCD").externallyAvailable(true).flagComment(true).cateGoryId(0).build();
        FlagDetail flagDetail2 = FlagDetail.builder().flagCode("CDEF")
            .childFlags(List.of(flagDetail1)).externallyAvailable(false).flagComment(true).cateGoryId(0).build();
        List<FlagDetail> flagDetails = new ArrayList<>();
        flagDetails.add(flagDetail1);
        flagDetails.add(flagDetail2);
        Flag flag1 = Flag.builder().flagDetails(flagDetails).build();
        List<Flag> flags = new ArrayList<>();
        flags.add(flag1);
        CaseFlag caseFlagResponse = CaseFlag.builder().flags(flags).build();
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(commonDataRefApi.retrieveCaseFlagsByServiceId(authToken, authTokenGenerator.generate(), SERVICE_ID,
                                                           FLAG_TYPE)).thenReturn(caseFlagResponse);
        CaseFlag caseFlag = refDataUserService.retrieveCaseFlags(
            authToken,
            FLAG_TYPE
        );
        assertEquals("ABCD",caseFlag.getFlags().get(0).getFlagDetails().get(0).getFlagCode());

    }


    @Test
    public void testGetHearingTypeNullData() {
        when(authTokenGenerator.generate()).thenReturn(s2sToken);

        List<CategoryValues> listOfCategoryValues = new ArrayList<>();
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(listOfCategoryValues).build();
        when(commonDataRefApi.getAllCategoryValuesByCategoryId(authToken,
                                                               authTokenGenerator.generate(),
                                                               HEARINGTYPE,
                                                               SERVICE_ID,
                                                               IS_HEARINGCHILDREQUIRED_N)).thenReturn(commonDataResponse);
        CommonDataResponse commonResponse = refDataUserService.retrieveCategoryValues(
            authToken,
            HEARINGTYPE,
            IS_HEARINGCHILDREQUIRED_N
        );
        assertEquals(0,commonResponse.getCategoryValues().size());
    }

    @Test
    public void testGetHearingChannelWithData() {

        when(authTokenGenerator.generate()).thenReturn(s2sToken);

        List<CategoryValues> listOfCategoryValues = new ArrayList<>();
        CategoryValues categoryValues1 = CategoryValues.builder().key("ONPPRS").valueEn("On the Papers").build();
        CategoryValues categoryValues2 = CategoryValues.builder().key("INTER").valueEn("IN Person").build();
        listOfCategoryValues.add(categoryValues1);
        listOfCategoryValues.add(categoryValues2);
        CommonDataResponse commonDataResponse = CommonDataResponse.builder().categoryValues(listOfCategoryValues).build();
        when(commonDataRefApi.getAllCategoryValuesByCategoryId(authToken,
                                                               authTokenGenerator.generate(),
                                                               HEARINGTYPE,
                                                               SERVICE_ID,
                                                               IS_HEARINGCHILDREQUIRED_N)).thenReturn(commonDataResponse);
        CommonDataResponse commonResponse = refDataUserService.retrieveCategoryValues(
            authToken,
            HEARINGTYPE,
            IS_HEARINGCHILDREQUIRED_N
        );
        assertNotNull(commonResponse);
        assertEquals("ONPPRS",commonResponse.getCategoryValues().get(0).getKey());
        assertEquals("On the Papers",commonResponse.getCategoryValues().get(0).getValueEn());
    }

    @Test
    public void testFilterCategoryValuesByCategoryId() {

        List<CategoryValues> listOfCategoryValues = new ArrayList<>();
        CategoryValues categoryValues1 = CategoryValues.builder().categoryKey(HEARINGTYPE).key("ONPPRS").valueEn("On the Papers").build();
        CategoryValues categoryValues2 = CategoryValues.builder().categoryKey(HEARINGTYPE).key("INTER").valueEn("IN Person").build();
        CategoryValues categoryValues3 = CategoryValues.builder().categoryKey(HEARINGTYPE).key("AINTER").valueEn("AIN Person").build();
        listOfCategoryValues.add(categoryValues1);
        listOfCategoryValues.add(categoryValues2);
        listOfCategoryValues.add(categoryValues3);
        CommonDataResponse commonDataResponse =  CommonDataResponse.builder().categoryValues(listOfCategoryValues).build();
        List<DynamicListElement> expectedResponse = refDataUserService.filterCategoryValuesByCategoryId(
            commonDataResponse,
            HEARINGTYPE);
        assertEquals("AINTER",expectedResponse.get(0).getCode());
        assertEquals("AIN Person",expectedResponse.get(0).getLabel());

    }

    @Test
    public void testFilterCategoryValuesByNullResponse() {
        List<DynamicListElement> expectedResponse = refDataUserService.filterCategoryValuesByCategoryId(
            null,
            HEARINGTYPE);
        assertEquals(null,expectedResponse.get(0).getCode());
        assertEquals(null,expectedResponse.get(0).getLabel());

    }

    @Test
    public void testFilterSubCategoryValuesByCategoryId() {
        CategorySubValues value1 = CategorySubValues.builder().categoryKey("HearingSubChannel").key("VIDOTHER").valueEn("Video - Other").build();
        CategorySubValues value2 = CategorySubValues.builder().categoryKey("HearingSubChannel").key("VIDCVP").valueEn("Video - CVP").build();
        CategorySubValues value3 = CategorySubValues.builder().categoryKey("HearingSubChannel").key("VIDPVL").valueEn("Prison Video").build();
        CategorySubValues value4 = CategorySubValues.builder().categoryKey("HearingSubChannel").key("VIDSKYPE").valueEn("Video - Skype").build();
        List<CategorySubValues> listOfCategorySubValues = new ArrayList<>();
        listOfCategorySubValues.add(value1);
        listOfCategorySubValues.add(value2);
        listOfCategorySubValues.add(value3);
        listOfCategorySubValues.add(value4);
        List<CategoryValues> listOfCategoryValues = new ArrayList<>();
        CategoryValues categoryValues1 = CategoryValues.builder().categoryKey(HEARINGCHANNEL).key("ONPPRS").valueEn("Video")
            .childNodes(listOfCategorySubValues).build();
        listOfCategoryValues.add(categoryValues1);
        CommonDataResponse commonDataResponse =  CommonDataResponse.builder().categoryValues(listOfCategoryValues).build();
        List<DynamicListElement> expectedResponse = refDataUserService.filterCategorySubValuesByCategoryId(
            commonDataResponse,
            VIDEOPLATFORM);
        assertEquals("VIDPVL",expectedResponse.get(0).getCode());
        assertEquals("Prison Video",expectedResponse.get(0).getLabel());
        assertEquals("VIDCVP",expectedResponse.get(1).getCode());
        assertEquals("Video - CVP",expectedResponse.get(1).getLabel());
        assertEquals("VIDOTHER",expectedResponse.get(2).getCode());
        assertEquals("Video - Other",expectedResponse.get(2).getLabel());
        assertEquals("VIDSKYPE",expectedResponse.get(3).getCode());
        assertEquals("Video - Skype",expectedResponse.get(3).getLabel());

    }

    @Test
    public void testFilterCategorySubValuesByNullResponse() {
        List<DynamicListElement> expectedResponse = refDataUserService.filterCategorySubValuesByCategoryId(
            null,
            VIDEOPLATFORM);
        assertEquals(null,expectedResponse.get(0).getCode());
        assertEquals(null,expectedResponse.get(0).getLabel());

    }

    @Test
    public void testGetStaffDetailsDataSizeLtPageSize() {

        StaffProfile staffProfile1 = StaffProfile.builder().userType(LEGALOFFICE)
            .lastName("David").emailId("test2@com").build();
        StaffResponse staffResponse = StaffResponse.builder().ccdServiceName("PRIVATELAW").staffProfile(staffProfile1).build();
        List<StaffResponse> listOfStaffFirstPage = new ArrayList<>();
        listOfStaffFirstPage.add(staffResponse);
        //add a response header for total entries
        HttpHeaders headers = new HttpHeaders();
        headers.add(RD_STAFF_TOTAL_RECORDS_HEADER, "45");
        ResponseEntity<List<StaffResponse>> staffResponseFirstPage = ResponseEntity.ok().headers(headers).body(listOfStaffFirstPage);

        when(idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword)).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn("s2sToken");
        when(staffResponseDetailsApi.getAllStaffResponseDetails(
            idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword),
            authTokenGenerator.generate(),
            SERVICENAME,
            STAFFSORTCOLUMN,
            STAFFORDERASC,
            RD_STAFF_PAGE_SIZE,
            RD_STAFF_FIRST_PAGE
        )).thenReturn(staffResponseFirstPage);

        List<DynamicListElement> legalAdvisorList = refDataUserService.getLegalAdvisorList();

        assertNotNull(legalAdvisorList.get(0).getCode());
        assertEquals("David(test2@com)",legalAdvisorList.get(0).getCode());
        assertEquals(1, legalAdvisorList.size());
    }

    @Test
    public void testGetStaffDetailsDataSizeGtPageSize() {

        StaffProfile staffProfile1 = StaffProfile.builder().userType(LEGALOFFICE)
            .lastName("David").emailId("test2@com").build();
        StaffProfile staffProfile2 = StaffProfile.builder().userType(LEGALOFFICE)
            .lastName("John").emailId("test1@com").build();
        StaffResponse staffResponse1 = StaffResponse.builder().ccdServiceName("PRIVATELAW").staffProfile(staffProfile1).build();
        StaffResponse staffResponse2 = StaffResponse.builder().ccdServiceName("PRIVATELAW").staffProfile(staffProfile2).build();
        List<StaffResponse> listOfStaffFirstPage = new ArrayList<>();
        List<StaffResponse> listOfStaffSecondPage = new ArrayList<>();
        listOfStaffFirstPage.add(staffResponse1);
        listOfStaffSecondPage.add(staffResponse2);
        //add a response header for total entries
        HttpHeaders headers = new HttpHeaders();
        headers.add(RD_STAFF_TOTAL_RECORDS_HEADER, "67");
        ResponseEntity<List<StaffResponse>> staffResponseFirstPage = ResponseEntity.ok().headers(headers).body(listOfStaffFirstPage);
        ResponseEntity<List<StaffResponse>> staffResponseSecondPage = ResponseEntity.ok().headers(headers).body(listOfStaffSecondPage);

        when(idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword)).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn("s2sToken");
        when(staffResponseDetailsApi.getAllStaffResponseDetails(
            idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword),
            authTokenGenerator.generate(),
            SERVICENAME,
            STAFFSORTCOLUMN,
            STAFFORDERASC,
            RD_STAFF_PAGE_SIZE,
            RD_STAFF_FIRST_PAGE
        )).thenReturn(staffResponseFirstPage);
        when(staffResponseDetailsApi.getAllStaffResponseDetails(
            idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword),
            authTokenGenerator.generate(),
            SERVICENAME,
            STAFFSORTCOLUMN,
            STAFFORDERASC,
            RD_STAFF_PAGE_SIZE,
            RD_STAFF_SECOND_PAGE
        )).thenReturn(staffResponseSecondPage);

        List<DynamicListElement> legalAdvisorList = refDataUserService.getLegalAdvisorList();

        assertNotNull(legalAdvisorList.get(0).getCode());
        assertEquals("David(test2@com)",legalAdvisorList.get(0).getCode());
        assertEquals(1, legalAdvisorList.size());
    }

}

