package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.CommonDataRefApi;
import uk.gov.hmcts.reform.prl.clients.JudicialUserDetailsApi;
import uk.gov.hmcts.reform.prl.clients.StaffResponseDetailsApi;
import uk.gov.hmcts.reform.prl.config.CacheConfig;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.exception.NoStaffResponseException;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffProfile;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGALOFFICE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFORDERASC;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFSORTCOLUMN;
import static uk.gov.hmcts.reform.prl.services.RefDataUserService.STAFF_REF_DATA_CACHE;

@SpringBootTest(classes = {
    CacheConfig.class,
    StaffRefDataService.class,
    RefDataUserService.class,
    StaffRefDataServiceCacheTest.CachingTestConfig.class
})
class StaffRefDataServiceCacheTest {

    private static final String AUTH_TOKEN = "Bearer auth-token";
    private static final String S2S_TOKEN = "Bearer s2s-token";

    @Autowired
    private StaffRefDataService staffRefDataService;

    @Autowired
    private RefDataUserService refDataUserService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @MockitoBean
    private StaffResponseDetailsApi staffResponseDetailsApi;

    @MockitoBean
    private IdamClient idamClient;

    @MockitoBean
    private JudicialUserDetailsApi judicialUserDetailsApi;

    @MockitoBean
    private CommonDataRefApi commonDataRefApi;

    @MockitoBean
    private LaunchDarklyClient launchDarklyClient;

    @BeforeEach
    void setUp() {
        reset(
            authTokenGenerator,
            staffResponseDetailsApi,
            idamClient,
            judicialUserDetailsApi,
            commonDataRefApi,
            launchDarklyClient
        );
        Cache cache = cacheManager.getCache(STAFF_REF_DATA_CACHE);
        assertThat(cache).isNotNull();
        cache.clear();
        when(idamClient.getAccessToken(nullable(String.class), nullable(String.class))).thenReturn(AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
    }

    @Test
    void shouldReturnCachedStaffRefDataWithoutCallingRefData() {
        List<StaffResponse> staffResponse = List.of(staffResponse("cached@example.com"));
        whenStaffRefDataIsRequested().thenReturn(ResponseEntity.ok(staffResponse));

        staffRefDataService.refreshStaffDetailsCache();
        List<StaffResponse> firstResponse = staffRefDataService.getAllStaffDetails();
        List<StaffResponse> secondResponse = staffRefDataService.getAllStaffDetails();

        assertThat(firstResponse).isEqualTo(staffResponse);
        assertThat(secondResponse).isEqualTo(staffResponse);
        verifyStaffRefDataRequested(times(1));
    }

    @Test
    void shouldCallRefDataWhenStaffRefDataCacheIsEmpty() {
        List<StaffResponse> staffResponse = List.of(staffResponse("cache-miss@example.com"));
        whenStaffRefDataIsRequested().thenReturn(ResponseEntity.ok(staffResponse));

        List<StaffResponse> response = staffRefDataService.getAllStaffDetails();

        assertThat(response).isEqualTo(staffResponse);
        verifyStaffRefDataRequested(times(1));
    }

    @Test
    void shouldRetryEmptyCacheRefreshAfterBadResponse() {
        List<StaffResponse> staffResponse = List.of(staffResponse("retried@example.com"));
        whenStaffRefDataIsRequested()
            .thenReturn(ResponseEntity.ok(List.of()))
            .thenReturn(ResponseEntity.ok(staffResponse));

        refDataUserService.refreshStaffRefDataCache();
        assertThat(staffRefDataService.isStaffDetailsCacheEmpty()).isTrue();

        refDataUserService.refreshEmptyStaffRefDataCache();

        assertThat(staffRefDataService.getAllStaffDetails()).isEqualTo(staffResponse);
        verifyStaffRefDataRequested(times(2));
    }

    @Test
    void shouldNotRefreshEmptyStaffRefDataCacheWhenCacheHasData() {
        List<StaffResponse> staffResponse = List.of(staffResponse("cached@example.com"));
        whenStaffRefDataIsRequested().thenReturn(ResponseEntity.ok(staffResponse));

        staffRefDataService.refreshStaffDetailsCache();
        refDataUserService.refreshEmptyStaffRefDataCache();

        assertThat(staffRefDataService.getAllStaffDetails()).isEqualTo(staffResponse);
        verifyStaffRefDataRequested(times(1));
        verifyNoMoreInteractions(staffResponseDetailsApi);
    }

    @Test
    void shouldRefreshStaffRefDataCache() {
        List<StaffResponse> firstStaffResponse = List.of(staffResponse("first@example.com"));
        List<StaffResponse> secondStaffResponse = List.of(staffResponse("second@example.com"));
        whenStaffRefDataIsRequested()
            .thenReturn(ResponseEntity.ok(firstStaffResponse))
            .thenReturn(ResponseEntity.ok(secondStaffResponse));

        staffRefDataService.refreshStaffDetailsCache();
        List<StaffResponse> firstResponse = staffRefDataService.getAllStaffDetails();
        refDataUserService.refreshStaffRefDataCache();
        List<StaffResponse> secondResponse = staffRefDataService.getAllStaffDetails();

        assertThat(firstResponse).isEqualTo(firstStaffResponse);
        assertThat(secondResponse).isEqualTo(secondStaffResponse);
        verifyStaffRefDataRequested(times(2));
    }

    @Test
    void shouldKeepExistingStaffRefDataCacheWhenRefreshReturnsEmptyResponse() {
        List<StaffResponse> firstStaffResponse = List.of(staffResponse("first@example.com"));
        List<StaffResponse> secondStaffResponse = List.of(staffResponse("second@example.com"));
        whenStaffRefDataIsRequested()
            .thenReturn(ResponseEntity.ok(firstStaffResponse))
            .thenReturn(ResponseEntity.ok(List.of()))
            .thenReturn(ResponseEntity.ok(secondStaffResponse));

        staffRefDataService.refreshStaffDetailsCache();
        List<StaffResponse> firstResponse = staffRefDataService.getAllStaffDetails();
        assertThatCode(() -> refDataUserService.refreshStaffRefDataCache()).doesNotThrowAnyException();
        List<StaffResponse> secondResponse = staffRefDataService.getAllStaffDetails();
        refDataUserService.refreshStaffRefDataCache();
        List<StaffResponse> thirdResponse = staffRefDataService.getAllStaffDetails();

        assertThat(firstResponse).isEqualTo(firstStaffResponse);
        assertThat(secondResponse).isEqualTo(firstStaffResponse);
        assertThat(thirdResponse).isEqualTo(secondStaffResponse);
        verifyStaffRefDataRequested(times(3));
    }

    @Test
    void shouldNotCacheEmptyStaffRefDataWhenRefreshResponseIsNull() {
        List<StaffResponse> staffResponse = List.of(staffResponse("retried@example.com"));
        whenStaffRefDataIsRequested()
            .thenReturn(null)
            .thenReturn(ResponseEntity.ok(staffResponse));

        assertThatThrownBy(() -> staffRefDataService.refreshStaffDetailsCache())
            .isInstanceOf(NoStaffResponseException.class);
        List<StaffResponse> secondResponse = staffRefDataService.getAllStaffDetails();

        assertThat(secondResponse).isEqualTo(staffResponse);
        verifyStaffRefDataRequested(times(2));
    }

    @Test
    void shouldNotCacheEmptyStaffRefDataWhenRefreshResponseBodyIsNull() {
        List<StaffResponse> staffResponse = List.of(staffResponse("retried@example.com"));
        whenStaffRefDataIsRequested()
            .thenReturn(ResponseEntity.ok(null))
            .thenReturn(ResponseEntity.ok(staffResponse));

        assertThatThrownBy(() -> staffRefDataService.refreshStaffDetailsCache())
            .isInstanceOf(NoStaffResponseException.class);
        List<StaffResponse> secondResponse = staffRefDataService.getAllStaffDetails();

        assertThat(secondResponse).isEqualTo(staffResponse);
        verifyStaffRefDataRequested(times(2));
    }

    @Test
    void shouldNotCacheEmptyStaffRefDataWhenRefreshResponseBodyIsEmpty() {
        List<StaffResponse> staffResponse = List.of(staffResponse("retried@example.com"));
        whenStaffRefDataIsRequested()
            .thenReturn(ResponseEntity.ok(List.of()))
            .thenReturn(ResponseEntity.ok(staffResponse));

        assertThatThrownBy(() -> staffRefDataService.refreshStaffDetailsCache())
            .isInstanceOf(NoStaffResponseException.class);
        List<StaffResponse> secondResponse = staffRefDataService.getAllStaffDetails();

        assertThat(secondResponse).isEqualTo(staffResponse);
        verifyStaffRefDataRequested(times(2));
    }

    private org.mockito.stubbing.OngoingStubbing<ResponseEntity<List<StaffResponse>>> whenStaffRefDataIsRequested() {
        return when(staffResponseDetailsApi.getAllStaffResponseDetails(
            eq(AUTH_TOKEN),
            eq(S2S_TOKEN),
            eq(SERVICENAME),
            eq(STAFFSORTCOLUMN),
            eq(STAFFORDERASC),
            eq(Integer.MAX_VALUE),
            eq(0)
        ));
    }

    private void verifyStaffRefDataRequested(org.mockito.verification.VerificationMode verificationMode) {
        verify(staffResponseDetailsApi, verificationMode).getAllStaffResponseDetails(
            eq(AUTH_TOKEN),
            eq(S2S_TOKEN),
            eq(SERVICENAME),
            eq(STAFFSORTCOLUMN),
            eq(STAFFORDERASC),
            eq(Integer.MAX_VALUE),
            eq(0)
        );
    }

    private StaffResponse staffResponse(String emailId) {
        return StaffResponse.builder()
            .ccdServiceName("PRIVATELAW")
            .staffProfile(StaffProfile.builder()
                .userType(LEGALOFFICE)
                .lastName("User")
                .emailId(emailId)
                .build())
            .build();
    }

    @TestConfiguration
    @EnableCaching
    static class CachingTestConfig {
    }
}
