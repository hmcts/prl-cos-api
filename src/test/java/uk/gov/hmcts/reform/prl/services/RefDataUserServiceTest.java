package uk.gov.hmcts.reform.prl.services;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.JudicialUserDetailsApi;
import uk.gov.hmcts.reform.prl.clients.StaffResponseDetailsApi;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGALOFFICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFORDERASC;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFSORTCOLUMN;


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
}
