package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private IdamClient idamClient;

    @Mock
    private IdamApi idamApi;

    @Mock
    private UserDetails userDetails;

    @Mock
    private UserInfo userInfo;

    public static final String authToken = "Bearer TestAuthToken";
    public List<String> roles;


    @Before
    public void setUp() {

        roles =  new ArrayList<>();
        roles.add("solicitor");
        roles.add("courtadmin");

        userDetails = UserDetails.builder()
            .surname("Solicitor")
            .forename("solicitor@example.com")
            .id("123")
            .email("test@demo.com")
            .build();

        userInfo = UserInfo.builder()
            .idamId(userDetails.getId())
            .firstName("solicitor@example.com Solicitor")
            .lastName(userDetails.getSurname().get())
            .emailAddress(userDetails.getEmail())
            .role((UserRoles.SOLICITOR).name())
            .build();

    }

    @Test
    public void testToCheckUserDetails() {

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);

        userService.getUserDetails(authToken);

        String actualResult = idamClient.getUserDetails(authToken).getFullName();

        assertEquals("solicitor@example.com Solicitor", actualResult);

    }

    @Test
    public void testToCheckUserDetailsWithBadAuthToken() {

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);

        String actualResult = idamClient.getUserDetails(authToken).getFullName();

        assertNotEquals("Solicitor", actualResult);
    }

    @Test
    public void testToCheckUserInfo() {

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);

        UserInfo userInfor = userService.getUserInfo(authToken, UserRoles.SOLICITOR);

        assertEquals(userInfor, userInfo);
    }

    @Test
    public void testToCheckGetUsersByUserId() {

        when(idamClient.getUserByUserId(authToken,"")).thenReturn(userDetails);

        UserDetails userDetails1 = userService.getUserByUserId(authToken, "");

        assertEquals(userDetails, userDetails1);
    }

    @Test
    public void testToCheckGetUsersByEmailId() {

        when(idamClient.searchUsers(authToken,"email:")).thenReturn(List.of(userDetails));

        List<UserDetails> userDetails1 = userService.getUserByEmailId(authToken, "");

        assertEquals(List.of(userDetails), userDetails1);
    }
}
