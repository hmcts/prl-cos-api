package uk.gov.hmcts.reform.prl.services.caseaccess;



import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.services.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);
        doNothing().when(this.caseRoleClient)
            .removeCaseRoles(anyString(), anyString(), any());
        when(this.authTokenGenerator.generate()).thenReturn("Generate");
        this.ccdDataStoreService.removeCreatorRole("42", "ABC123");
        verify(this.userService).getUserDetails(anyString());
        verify(this.caseRoleClient).removeCaseRoles(anyString(), anyString(), any());
        verify(this.authTokenGenerator).generate();
    }
}
