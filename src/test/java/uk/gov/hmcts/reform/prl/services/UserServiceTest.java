package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    IdamClient idamClient;

    @InjectMocks
    UserService userService;

    @Test
    public void testGetUserDetails() {
        UserDetails userDetails = UserDetails
            .builder()
            .id("test id")
            .email("test@test.com")
            .forename("Test")
            .surname("tester")
            .roles(Collections.singletonList("caseworker"))
            .build();

        String auth = "testAuth";

        when(idamClient.getUserDetails(auth)).thenReturn(userDetails);

        assert userService.getUserDetails(auth).equals(userDetails);

    }

}
