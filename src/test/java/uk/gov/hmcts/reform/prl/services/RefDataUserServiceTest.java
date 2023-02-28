package uk.gov.hmcts.reform.prl.services;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.CommonDataRefApi;
import uk.gov.hmcts.reform.prl.clients.JudicialUserDetailsApi;
import uk.gov.hmcts.reform.prl.clients.StaffResponseDetailsApi;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGCHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_N;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGALOFFICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFORDERASC;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFSORTCOLUMN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.VIDEOPLATFORM;


@RunWith(MockitoJUnitRunner.Silent.class)
public class RefDataUserServiceTest {

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
            STAFFORDERASC)).thenReturn(null);
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
            STAFFORDERASC))
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
        when(idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword)).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn("s2sToken");
        when(staffResponseDetailsApi.getAllStaffResponseDetails(
            idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword),
            authTokenGenerator.generate(),
            SERVICENAME,
            STAFFSORTCOLUMN,
            STAFFORDERASC))
            .thenReturn(listOfStaffResponse);

        List<DynamicListElement> legalAdvisorList = refDataUserService.getLegalAdvisorList();
        assertNotNull(legalAdvisorList.get(0).getCode());
        assertEquals("David(test2@com)",legalAdvisorList.get(0).getCode());

    }

    @Test
    public void testGetAllJudicialUsers() {
        when(idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword)).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        JudicialUsersApiResponse judge1 = JudicialUsersApiResponse.builder().surname("lastName1").fullName("judge1@test.com").build();
        JudicialUsersApiResponse judge2 = JudicialUsersApiResponse.builder().surname("lastName2").fullName("judge2@test.com").build();
        List<JudicialUsersApiResponse> listOfJudges = new ArrayList<>();
        listOfJudges.add(judge1);
        listOfJudges.add(judge2);
        JudicialUsersApiRequest judicialUsersApiRequest = JudicialUsersApiRequest.builder().personalCode(new String[3]).build();
        when(judicialUserDetailsApi.getAllJudicialUserDetails(
            idamClient.getAccessToken(refDataIdamUsername,refDataIdamPassword),
            authTokenGenerator.generate(),
            judicialUsersApiRequest
        )).thenReturn(listOfJudges);
        List<JudicialUsersApiResponse> expectedRespose = refDataUserService.getAllJudicialUserDetails(judicialUsersApiRequest);
        assertNotNull(expectedRespose);
        assertEquals(expectedRespose.get(0).getSurname(),"lastName1");
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
        assertEquals(commonResponse.getCategoryValues().get(0).getValueEn(),"Celebration hearing");
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
        assertEquals(commonResponse.getCategoryValues().size(),0);
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
        assertEquals(commonResponse.getCategoryValues().get(0).getKey(),"ONPPRS");
        assertEquals(commonResponse.getCategoryValues().get(0).getValueEn(),"On the Papers");
    }

    @Test
    public void testFilterCategoryValuesByCategoryId() {

        List<CategoryValues> listOfCategoryValues = new ArrayList<>();
        CategoryValues categoryValues1 = CategoryValues.builder().categoryKey(HEARINGTYPE).key("ONPPRS").valueEn("On the Papers").build();
        CategoryValues categoryValues2 = CategoryValues.builder().categoryKey(HEARINGTYPE).key("INTER").valueEn("IN Person").build();
        listOfCategoryValues.add(categoryValues1);
        listOfCategoryValues.add(categoryValues2);
        CommonDataResponse commonDataResponse =  CommonDataResponse.builder().categoryValues(listOfCategoryValues).build();
        List<DynamicListElement> expectedResponse = refDataUserService.filterCategoryValuesByCategoryId(
            commonDataResponse,
            HEARINGTYPE);
        assertEquals(expectedResponse.get(0).getCode(),"ONPPRS");
        assertEquals(expectedResponse.get(0).getLabel(),"On the Papers");

    }

    @Test
    public void testFilterCategoryValuesByNullResponse() {
        List<DynamicListElement> expectedResponse = refDataUserService.filterCategoryValuesByCategoryId(
            null,
            HEARINGTYPE);
        assertEquals(expectedResponse.get(0).getCode(),null);
        assertEquals(expectedResponse.get(0).getLabel(),null);

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
        assertEquals(expectedResponse.get(0).getCode(),"VIDOTHER");
        assertEquals(expectedResponse.get(0).getLabel(),"Video - Other");

    }

    @Test
    public void testFilterCategorySubValuesByNullResponse() {
        List<DynamicListElement> expectedResponse = refDataUserService.filterCategorySubValuesByCategoryId(
            null,
            VIDEOPLATFORM);
        assertEquals(expectedResponse.get(0).getCode(),null);
        assertEquals(expectedResponse.get(0).getLabel(),null);

    }


}

