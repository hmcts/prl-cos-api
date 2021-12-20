package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class UserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private IdamClient idamClient;

    @Mock
    private UserDetails userDetails;

    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setUp() {

        userDetails = UserDetails.builder()
            .surname("Solicitor")
            .forename("solicitor@example.com")
            .build();

    }

    @Test
    public void testToCheckUserDetails() {

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        String actualResult = userService.getUserDetails(authToken).getFullName();

        assertEquals("solicitor@example.com Solicitor", actualResult);

    }

    @Test
    public void testToCheckUserDetailsWithBadAuthToken() {

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        String actualResult = userService.getUserDetails(authToken).getFullName();

        assertNotEquals("Solicitor", actualResult);
    }

}
