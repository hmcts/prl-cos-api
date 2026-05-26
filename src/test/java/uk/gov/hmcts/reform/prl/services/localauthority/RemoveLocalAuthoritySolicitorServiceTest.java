package uk.gov.hmcts.reform.prl.services.localauthority;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.prl.exception.GrantCaseAccessException;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class RemoveLocalAuthoritySolicitorServiceTest {

    public static final String LOCAL_AUTHORITY_SOLICITOR_CASE_ROLE = "[LASOLICITOR]";
    public static final String SYS_TOKEN = "sysToken";
    public static final String AUTH_TOKEN = "svcToken";
    @InjectMocks
    public RemoveLocalAuthoritySolicitorService service;

    @Mock
    private CaseAssignmentApi caseAssignmentApi;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private AuthTokenGenerator tokenGenerator;

    private CaseData buildCaseData(long caseId, String orgId) {
        return CaseData.builder()
            .id(caseId)
            .localAuthoritySolicitorOrganisationPolicy(
                OrganisationPolicy.builder()
                    .organisation(
                        Organisation.builder()
                            .organisationID(orgId)
                            .organisationName("Some Org")
                            .build()
                    )
                    .build()
            )
            .build();
    }

    @Test
    void removeLocalAuthoritySolicitors_shouldRemoveAllLocalAuthoritySolicitorRoles() {
        // Arrange
        long caseId = 1234567890123456L;

        // Mock role assignments: two with [LASOLICITOR], one with other role
        CaseAssignmentUserRole la1 = mock(CaseAssignmentUserRole.class);
        when(la1.getCaseRole()).thenReturn(LOCAL_AUTHORITY_SOLICITOR_CASE_ROLE);
        when(la1.getUserId()).thenReturn("user-1");

        CaseAssignmentUserRole la2 = mock(CaseAssignmentUserRole.class);
        when(la2.getCaseRole()).thenReturn(LOCAL_AUTHORITY_SOLICITOR_CASE_ROLE);
        when(la2.getUserId()).thenReturn("user-2");

        CaseAssignmentUserRole other = mock(CaseAssignmentUserRole.class);
        when(other.getCaseRole()).thenReturn("[OTHERROLE]");

        // Tokens
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(tokenGenerator.generate()).thenReturn(SYS_TOKEN);

        CaseAssignmentUserRolesResource raResponse = mock(CaseAssignmentUserRolesResource.class);
        when(raResponse.getCaseAssignmentUserRoles()).thenReturn(Arrays.asList(la1, la2, other));
        when(caseAssignmentApi.getUserRoles(AUTH_TOKEN, SYS_TOKEN, List.of(String.valueOf(caseId))))
            .thenReturn(raResponse);

        ArgumentCaptor<CaseAssignmentUserRolesRequest> requestCaptor =
            ArgumentCaptor.forClass(CaseAssignmentUserRolesRequest.class);

        String orgId = "ORG-123";
        CaseData caseData = buildCaseData(caseId, orgId);

        // Act
        service.removeLocalAuthoritySolicitor(caseData);

        // Assert: CCD API called once with tokens and correctly built request
        verify(caseAssignmentApi, times(1))
            .removeCaseUserRoles(eq(AUTH_TOKEN), eq(SYS_TOKEN), requestCaptor.capture());

        CaseAssignmentUserRolesRequest captured = requestCaptor.getValue();
        assertNotNull(captured, "Expected a CaseAssignmentUserRolesRequest");
        List<CaseAssignmentUserRoleWithOrganisation> entries =
            captured.getCaseAssignmentUserRolesWithOrganisation();

        assertNotNull(entries, "Entries list should not be null");
        assertEquals(2, entries.size(), "Only LASOLICITOR users must be included");

        // Validate each entry
        for (CaseAssignmentUserRoleWithOrganisation e : entries) {
            assertEquals(String.valueOf(caseId), e.getCaseDataId(), "Case id must match");
            assertEquals(orgId, e.getOrganisationId(), "Org id must match");
            assertEquals(LOCAL_AUTHORITY_SOLICITOR_CASE_ROLE, e.getCaseRole(), "Case role must be LASOLICITOR");
            assertTrue(
                Arrays.asList("user-1", "user-2").contains(e.getUserId()),
                "Unexpected user id in request: " + e.getUserId()
            );
        }

        // Make sure role assignment was fetched once
        verify(caseAssignmentApi, times(1))
            .getUserRoles(AUTH_TOKEN, SYS_TOKEN, List.of(String.valueOf(caseId)));
    }

    @Test
    void removeLocalAuthoritySolicitor_whenGetCaseRolesCcdApiThrowsFeign_shouldThrowGrantCaseAccessException() {
        // Arrange
        long caseId = 9876543210L;

        when(systemUserService.getSysUserToken()).thenReturn("svc");
        when(tokenGenerator.generate()).thenReturn("sys");

        // Force CCD API to throw FeignException (mock instance is fine as FeignException is a RuntimeException)
        doThrow(mock(FeignException.class))
            .when(caseAssignmentApi)
            .getUserRoles(eq("svc"), eq("sys"), eq(List.of(String.valueOf(caseId))));

        String orgId = "ORG-XYZ";
        CaseData caseData = buildCaseData(caseId, orgId);

        // Act + Assert
        GrantCaseAccessException ex = assertThrows(
            GrantCaseAccessException.class,
            () -> service.removeLocalAuthoritySolicitor(caseData),
            "Expected GrantCaseAccessException when FeignException occurs"
        );

        assertTrue(
            ex.getMessage().contains(String.valueOf(caseId)),
            String.format("Could not get case user role(s) for the case %s", caseId)
        );

        verify(caseAssignmentApi, times(1))
            .getUserRoles(eq("svc"), eq("sys"), eq(List.of(String.valueOf(caseId))));
    }

    @Test
    void removeLocalAuthoritySolicitor_whenRemoveCaseRolesCcdApiThrowsFeign_shouldThrowGrantCaseAccessException() {
        // Arrange
        long caseId = 9876543210L;

        // Mock role assignments: two with [LASOLICITOR], one with other role
        CaseAssignmentUserRole la1 = mock(CaseAssignmentUserRole.class);
        when(la1.getCaseRole()).thenReturn(LOCAL_AUTHORITY_SOLICITOR_CASE_ROLE);
        when(la1.getUserId()).thenReturn("la-1");

        // Tokens
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(tokenGenerator.generate()).thenReturn(SYS_TOKEN);

        CaseAssignmentUserRolesResource raResponse = mock(CaseAssignmentUserRolesResource.class);
        when(raResponse.getCaseAssignmentUserRoles()).thenReturn(Arrays.asList(la1));
        when(caseAssignmentApi.getUserRoles(AUTH_TOKEN, SYS_TOKEN, List.of(String.valueOf(caseId))))
            .thenReturn(raResponse);

        // Force CCD API to throw FeignException (mock instance is fine as FeignException is a RuntimeException)
        ArgumentCaptor<CaseAssignmentUserRolesRequest> requestCaptor =
            ArgumentCaptor.forClass(CaseAssignmentUserRolesRequest.class);

        doThrow(mock(FeignException.class))
            .when(caseAssignmentApi)
            .removeCaseUserRoles(eq(AUTH_TOKEN), eq(SYS_TOKEN), requestCaptor.capture());

        String orgId = "ORG-XYZ";
        CaseData caseData = buildCaseData(caseId, orgId);

        // Act + Assert
        GrantCaseAccessException ex = assertThrows(
            GrantCaseAccessException.class,
            () -> service.removeLocalAuthoritySolicitor(caseData),
            "Expected GrantCaseAccessException when FeignException occurs"
        );

        assertTrue(
            ex.getMessage().contains(String.valueOf(caseId))
                && ex.getMessage().contains(LOCAL_AUTHORITY_SOLICITOR_CASE_ROLE),
            "Error message should contain case id and role"
        );

        verify(caseAssignmentApi, times(1))
            .getUserRoles(eq(AUTH_TOKEN), eq(SYS_TOKEN), eq(List.of(String.valueOf(caseId))));

        verify(caseAssignmentApi, times(1))
            .removeCaseUserRoles(eq(AUTH_TOKEN), eq(SYS_TOKEN), any(CaseAssignmentUserRolesRequest.class));

    }

    @Test
    void removeLocalAuthoritySolicitor_whenNoMatchingRoles_callsApiWithEmptyList() {
        // Arrange
        CaseAssignmentUserRole other1 = mock(CaseAssignmentUserRole.class);
        when(other1.getCaseRole()).thenReturn("[CITIZEN]");

        // Tokens
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(tokenGenerator.generate()).thenReturn(SYS_TOKEN);

        CaseAssignmentUserRolesResource raResponse = mock(CaseAssignmentUserRolesResource.class);
        when(raResponse.getCaseAssignmentUserRoles()).thenReturn(Collections.singletonList(other1));
        long caseId = 1122334455L;
        when(caseAssignmentApi.getUserRoles(AUTH_TOKEN, SYS_TOKEN, List.of(String.valueOf(caseId))))
            .thenReturn(raResponse);

        ArgumentCaptor<CaseAssignmentUserRolesRequest> requestCaptor =
            ArgumentCaptor.forClass(CaseAssignmentUserRolesRequest.class);

        String orgId = "ORG-empty";
        CaseData caseData = buildCaseData(caseId, orgId);

        // Act
        service.removeLocalAuthoritySolicitor(caseData);

        // Assert
        verify(caseAssignmentApi, never())
            .removeCaseUserRoles(eq("sysTok"), eq("svcTok"), requestCaptor.capture());
    }
}
