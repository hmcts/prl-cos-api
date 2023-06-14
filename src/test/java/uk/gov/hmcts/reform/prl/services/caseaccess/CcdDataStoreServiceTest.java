package uk.gov.hmcts.reform.prl.services.caseaccess;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.UserService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CcdDataStoreServiceTest {

    @InjectMocks
    private CcdDataStoreService ccdDataStoreService;

    @Mock
    private CaseRoleClient caseRoleClient;

    @Mock
    private UserService userService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private CaseData caseData;
    private UserDetails userDetails;

    @Before
    public void setup() {

        userDetails = UserDetails.builder()
            .surname("Solicitor")
            .forename("solicitor@example.com")
            .id("123")
            .email("test@demo.com")
            .build();


        caseData = CaseData.builder().id(1234567891234567L).applicantCaseName("xyz").build();
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        doNothing().when(this.caseRoleClient)
            .removeCaseRoles(Mockito.anyString(), Mockito.anyString(), Mockito.any());
        when(this.authTokenGenerator.generate()).thenReturn("Generate");

    }

    @Test
    public void testRemoveCreatorRole() {

        this.ccdDataStoreService.removeCreatorRole("42", "ABC123");
        verify(this.userService, times(1)).getUserDetails(Mockito.anyString());
        verify(this.caseRoleClient, times(1)).removeCaseRoles(Mockito.anyString(), Mockito.anyString(), Mockito.any());
        verify(this.authTokenGenerator, times(1)).generate();
    }

    @Test
    public void testFindUserCaseRoles() {
        this.ccdDataStoreService.findUserCaseRoles("42", "test");
        verify(this.userService, times(1)).getUserDetails(Mockito.anyString());
        verify(this.caseRoleClient, times(1)).findUserCaseRoles(Mockito.anyString(), Mockito.anyString(), Mockito.any());
        verify(this.authTokenGenerator, times(1)).generate();
    }
}
