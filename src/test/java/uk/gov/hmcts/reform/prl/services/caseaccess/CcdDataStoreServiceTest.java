package uk.gov.hmcts.reform.prl.services.caseaccess;



import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.services.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CcdDataStoreServiceTest {

    @InjectMocks
    private CcdDataStoreService ccdDataStoreService;

    @Mock
    private CaseRoleClient caseRoleClient;

    @Mock
    private UserService userService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Test
    public void testRemoveCreatorRole() {
        UserDetails userDetails = new UserDetails();
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        doNothing().when(this.caseRoleClient)
            .removeCaseRoles(Mockito.anyString(), Mockito.anyString(), Mockito.any());
        when(this.authTokenGenerator.generate()).thenReturn("Generate");
        this.ccdDataStoreService.removeCreatorRole("42", "ABC123");
        verify(this.userService, times(1)).getUserDetails(Mockito.anyString());
        verify(this.caseRoleClient, times(1)).removeCaseRoles(Mockito.anyString(), Mockito.anyString(), Mockito.any());
        verify(this.authTokenGenerator, times(1)).generate();
    }
}
