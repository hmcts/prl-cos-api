package uk.gov.hmcts.reform.prl.services.caseaccess;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.services.UserService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssignCaseAccessServiceTest {

    @Mock
    private AssignCaseAccessClient assignCaseAccessClient;

    @InjectMocks
    private AssignCaseAccessService assignCaseAccessService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdDataStoreService ccdDataStoreService;

    @Mock
    private UserService userService;

    @Mock
    private LaunchDarklyClient launchDarklyClient;


    @Test
    public void testAssignCaseAccess() {

        UserDetails userDetails = new UserDetails();
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        doNothing().when(ccdDataStoreService).removeCreatorRole(Mockito.anyString(), Mockito.anyString());
        when(authTokenGenerator.generate()).thenReturn("Generate");
        doNothing().when(assignCaseAccessClient)
            .assignCaseAccess(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.any());
        when(launchDarklyClient.isFeatureEnabled("share-a-case")).thenReturn(true);
        assignCaseAccessService.assignCaseAccess("42", "ABC123");
        verify(userService, times(1)).getUserDetails(Mockito.anyString());
        verify(ccdDataStoreService, times(1)).removeCreatorRole(Mockito.anyString(), Mockito.anyString());
        verify(authTokenGenerator, times(1)).generate();
        verify(assignCaseAccessClient, times(1)).assignCaseAccess(Mockito.anyString(), Mockito.anyString(),
                                                        Mockito.anyBoolean(), Mockito.any());
    }

    @Test
    public void testAssignCaseAccess2() {

        when(launchDarklyClient.isFeatureEnabled("share-a-case")).thenReturn(false);
        assignCaseAccessService.assignCaseAccess("42", "ABC123");
        verifyNoMoreInteractions(userService);
        verifyNoMoreInteractions(ccdDataStoreService);
        verifyNoMoreInteractions(assignCaseAccessClient);
    }


}

