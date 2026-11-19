package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.CommonDataRefApi;
import uk.gov.hmcts.reform.prl.clients.JudicialUserDetailsApi;
import uk.gov.hmcts.reform.prl.config.CacheConfig;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.exception.NoHearingRefDataResponseException;
import uk.gov.hmcts.reform.prl.mapper.hearingrequest.HearingRequestDataMapper;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategorySubValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGCHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_N;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_Y;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TELEPHONESUBCHANNELS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.VIDEOSUBCHANNELS;
import static uk.gov.hmcts.reform.prl.services.HearingRefDataService.HEARING_REF_DATA_CACHE;

@SpringBootTest(classes = {
    CacheConfig.class, HearingRefDataService.class, RefDataUserService.class
}, properties = {"prl.refdata.username=refdata-user", "prl.refdata.password=refdata-password"})
class HearingRefDataServiceCacheTest {

    private static final String AUTH_TOKEN = "Bearer caller-token";
    private static final String REF_DATA_TOKEN = "Bearer refdata-token";
    private static final String S2S_TOKEN = "Bearer s2s-token";

    private final AtomicLong currentNanos = new AtomicLong();

    @Autowired
    private HearingRefDataService service;
    @Autowired
    private RefDataUserService refDataUserService;
    @Autowired
    private CacheManager cacheManager;
    @MockitoBean
    private CommonDataRefApi commonDataRefApi;
    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;
    @MockitoBean
    private IdamClient idamClient;
    @MockitoBean
    private StaffRefDataService staffRefDataService;
    @MockitoBean
    private JudicialUserDetailsApi judicialUserDetailsApi;
    @MockitoBean
    private LaunchDarklyClient launchDarklyClient;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(HEARING_REF_DATA_CACHE).clear();
        ((Map<?, ?>) ReflectionTestUtils.getField(service, "failedFetchTimes")).clear();
        ReflectionTestUtils.setField(service, "nanoTime", (LongSupplier) currentNanos::get);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(commonDataRefApi.getAllCategoryValuesByCategoryId(
            anyString(), eq(S2S_TOKEN), anyString(), eq(SERVICE_ID), anyString()
        )).thenAnswer(invocation -> response(invocation.getArgument(2), "Default"));
        when(idamClient.getAccessToken("refdata-user", "refdata-password")).thenReturn(REF_DATA_TOKEN);
    }

    @ParameterizedTest
    @CsvSource({"HearingChannel,Y", "HearingType,N"})
    void shouldShareCachedResponseAcrossRepeatedCallsAndDifferentUsers(String categoryId, String childFlag) {
        CommonDataResponse response = response(categoryId, "Video");
        whenRequested(categoryId, childFlag).thenReturn(response);

        for (int i = 0; i < 4; i++) {
            assertThat(refDataUserService.retrieveCategoryValues(
                i == 0 ? AUTH_TOKEN : "Bearer another-user", categoryId, childFlag
            )).isEqualTo(response);
        }

        verifyRequests(1, categoryId, childFlag);
        verifyNoInteractions(idamClient);
        verify(authTokenGenerator, times(1)).generate();
    }

    @ParameterizedTest
    @CsvSource({"HearingChannel,Y,true", "HearingType,N,true", "HearingChannel,Y,false", "HearingType,N,false"})
    void shouldFetchOnlyOnceForConcurrentCacheMisses(String categoryId, String childFlag, boolean succeeds) throws Exception {
        CommonDataResponse response = succeeds ? response(categoryId, "Video") : null;
        CountDownLatch ready = new CountDownLatch(4);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch requested = new CountDownLatch(1);
        CountDownLatch releaseResponse = new CountDownLatch(1);
        whenRequested(categoryId, childFlag).thenAnswer(invocation -> {
            requested.countDown();
            assertThat(releaseResponse.await(5, TimeUnit.SECONDS)).isTrue();
            if (!succeeds) {
                throw failure(503);
            }
            return response;
        });

        try (var executor = Executors.newFixedThreadPool(4)) {
            List<Future<CommonDataResponse>> results = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    assertThat(start.await(5, TimeUnit.SECONDS)).isTrue();
                    return refDataUserService.retrieveCategoryValues(AUTH_TOKEN, categoryId, childFlag);
                }));
            }
            try {
                assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
                start.countDown();
                assertThat(requested.await(5, TimeUnit.SECONDS)).isTrue();
            } finally {
                start.countDown();
                releaseResponse.countDown();
            }
            for (Future<CommonDataResponse> result : results) {
                assertThat(result.get(5, TimeUnit.SECONDS)).isEqualTo(response);
            }
        }
        verifyRequests(1, categoryId, childFlag);
    }

    @ParameterizedTest
    @CsvSource({"HearingChannel,Y", "HearingType,N"})
    void shouldReturnSuccessfulEmptyResponsesUnchangedWithoutCaching(String categoryId, String childFlag) {
        CommonDataResponse empty = CommonDataResponse.builder().categoryValues(List.of()).build();
        whenRequested(categoryId, childFlag).thenReturn(empty);

        assertThat(refDataUserService.retrieveCategoryValues(AUTH_TOKEN, categoryId, childFlag)).isSameAs(empty);
        assertThat(refDataUserService.retrieveCategoryValues(AUTH_TOKEN, categoryId, childFlag)).isSameAs(empty);
        assertThat(cacheManager.getCache(HEARING_REF_DATA_CACHE)
            .get(new SimpleKey(categoryId, SERVICE_ID, childFlag))).isNull();
        if (HEARINGTYPE.equals(categoryId)) {
            assertThat(hearingDataService().prePopulateHearingType(AUTH_TOKEN)).isEmpty();
        } else {
            assertThat(hearingDataService().prePopulateHearingChannel(AUTH_TOKEN).get(HEARINGCHANNEL)).isEmpty();
        }
        verifyRequests(3, categoryId, childFlag);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 429, 500, 503})
    void shouldBrieflySuppressTransientFailuresWithoutSuppressingOtherCategory(int status) {
        whenRequested(HEARINGCHANNEL, IS_HEARINGCHILDREQUIRED_Y).thenThrow(failure(status));

        for (int i = 0; i < 4; i++) {
            assertThat(refDataUserService.retrieveCategoryValues(
                AUTH_TOKEN, HEARINGCHANNEL, IS_HEARINGCHILDREQUIRED_Y)).isNull();
        }
        assertThat(refDataUserService.retrieveCategoryValues(
            AUTH_TOKEN, HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N)).isNotNull();

        verifyRequests(1, HEARINGCHANNEL, IS_HEARINGCHILDREQUIRED_Y);
        verifyRequests(1, HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N);
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 404})
    void shouldAllowAnotherCallerToRetryAfterClientError(int status) {
        CommonDataResponse response = response(HEARINGTYPE, "Directions hearing");
        whenRequested(HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N).thenThrow(failure(status)).thenReturn(response);

        assertThat(refDataUserService.retrieveCategoryValues(AUTH_TOKEN, HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N)).isNull();
        assertThat(refDataUserService.retrieveCategoryValues(
            "Bearer another-user", HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N)).isEqualTo(response);

        verifyRequests(2, HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N);
    }

    @ParameterizedTest
    @CsvSource({"HearingChannel,Y", "HearingType,N"})
    void shouldClearFailureDelayWhenBackgroundRefreshRecovers(String categoryId, String childFlag) {
        CommonDataResponse response = response(categoryId, "Recovered");
        whenRequested(categoryId, childFlag).thenThrow(failure(503)).thenReturn(response);
        assertThat(refDataUserService.retrieveCategoryValues(AUTH_TOKEN, categoryId, childFlag)).isNull();

        service.refreshEmptyHearingRefDataCache();

        assertThat(service.getCategoryValues(AUTH_TOKEN, categoryId, childFlag)).isEqualTo(response);
        verifyRequests(2, categoryId, childFlag);
    }

    @ParameterizedTest
    @CsvSource({"HearingChannel,Y", "HearingType,N"})
    void shouldRefreshUsingReferenceDataAccountAndReplaceCachedValues(String categoryId, String childFlag) {
        CommonDataResponse first = response(categoryId, "Video");
        CommonDataResponse second = response(categoryId, "Telephone");
        whenRequested(categoryId, childFlag).thenReturn(first, second);

        assertThat(service.getCategoryValues(AUTH_TOKEN, categoryId, childFlag)).isEqualTo(first);
        service.refreshHearingRefDataCache();

        assertThat(service.getCategoryValues(AUTH_TOKEN, categoryId, childFlag)).isEqualTo(second);
        verify(commonDataRefApi).getAllCategoryValuesByCategoryId(
            REF_DATA_TOKEN, S2S_TOKEN, categoryId, SERVICE_ID, childFlag);
        verifyRequests(2, categoryId, childFlag);
    }

    @ParameterizedTest
    @MethodSource("invalidResponses")
    void shouldKeepPreviousValuesWhenRefreshReturnsInvalidResponse(String categoryId, String childFlag, CommonDataResponse invalid) {
        CommonDataResponse response = response(categoryId, "Video");
        whenRequested(categoryId, childFlag).thenReturn(response, invalid);
        service.getCategoryValues(AUTH_TOKEN, categoryId, childFlag);

        assertThatCode(service::refreshHearingRefDataCache).doesNotThrowAnyException();

        assertThat(service.getCategoryValues(AUTH_TOKEN, categoryId, childFlag)).isEqualTo(response);
        verifyRequests(2, categoryId, childFlag);
    }

    @ParameterizedTest
    @MethodSource("invalidResponses")
    void shouldRetryAfterInvalidInitialResponse(String categoryId, String childFlag, CommonDataResponse invalid) {
        CommonDataResponse response = response(categoryId, "Video");
        whenRequested(categoryId, childFlag).thenReturn(invalid, response);

        assertThat(refDataUserService.retrieveCategoryValues(
            AUTH_TOKEN, categoryId, childFlag)).isEqualTo(invalid);
        service.refreshEmptyHearingRefDataCache();

        assertThat(service.getCategoryValues(AUTH_TOKEN, categoryId, childFlag)).isEqualTo(response);
        verifyRequests(2, categoryId, childFlag);
    }

    @ParameterizedTest
    @CsvSource({"HearingChannel,Y", "HearingType,N"})
    void shouldKeepPreviousValuesWhenRefreshThrowsFeignException(String categoryId, String childFlag) {
        CommonDataResponse response = response(categoryId, "Video");
        whenRequested(categoryId, childFlag).thenReturn(response).thenThrow(mock(FeignException.class));
        service.getCategoryValues(AUTH_TOKEN, categoryId, childFlag);

        assertThatCode(service::refreshHearingRefDataCache).doesNotThrowAnyException();

        assertThat(service.getCategoryValues(AUTH_TOKEN, categoryId, childFlag)).isEqualTo(response);
        verifyRequests(2, categoryId, childFlag);
    }

    @ParameterizedTest
    @CsvSource({"HearingChannel,Y", "HearingType,N"})
    void shouldRetryAfterFailedInitialFetch(String categoryId, String childFlag) {
        CommonDataResponse response = response(categoryId, "Video");
        whenRequested(categoryId, childFlag).thenThrow(failure(503)).thenReturn(response);

        assertThatThrownBy(() -> service.getCategoryValues(AUTH_TOKEN, categoryId, childFlag))
            .isInstanceOf(NoHearingRefDataResponseException.class);

        assertThat(refDataUserService.retrieveCategoryValues(AUTH_TOKEN, categoryId, childFlag)).isNull();
        verifyRequests(1, categoryId, childFlag);
        currentNanos.addAndGet(TimeUnit.MILLISECONDS.toNanos(4999));
        assertThat(refDataUserService.retrieveCategoryValues(AUTH_TOKEN, categoryId, childFlag)).isNull();
        verifyRequests(1, categoryId, childFlag);
        currentNanos.addAndGet(TimeUnit.MILLISECONDS.toNanos(1));

        assertThat(service.getCategoryValues(AUTH_TOKEN, categoryId, childFlag)).isEqualTo(response);
        verifyRequests(2, categoryId, childFlag);
    }

    @ParameterizedTest
    @CsvSource({"HearingChannel,Y", "HearingType,N"})
    void shouldKeepPreviousValuesWhenRefreshAuthenticationFails(String categoryId, String childFlag) {
        CommonDataResponse response = response(categoryId, "Video");
        whenRequested(categoryId, childFlag).thenReturn(response);
        service.getCategoryValues(AUTH_TOKEN, categoryId, childFlag);
        when(idamClient.getAccessToken("refdata-user", "refdata-password")).thenThrow(mock(FeignException.class));

        assertThatCode(service::refreshHearingRefDataCache).doesNotThrowAnyException();

        assertThat(service.getCategoryValues(AUTH_TOKEN, categoryId, childFlag)).isEqualTo(response);
        verifyRequests(1, categoryId, childFlag);
    }

    @ParameterizedTest
    @CsvSource({"HearingChannel,Y", "HearingType,N"})
    void shouldWarmEmptyCacheAndSkipRetryWhenPopulated(String categoryId, String childFlag) {
        CommonDataResponse response = response(categoryId, "Video");
        whenRequested(categoryId, childFlag).thenReturn(response);

        service.refreshEmptyHearingRefDataCache();
        service.refreshEmptyHearingRefDataCache();

        assertThat(service.getCategoryValues(AUTH_TOKEN, categoryId, childFlag)).isEqualTo(response);
        verifyRequests(1, categoryId, childFlag);
        verify(idamClient, times(2)).getAccessToken("refdata-user", "refdata-password");
    }

    @ParameterizedTest
    @CsvSource({"HearingChannel,Y,HearingType,N", "HearingType,N,HearingChannel,Y"})
    void shouldRefreshOtherCategoryWhenOneFails(String failingCategory, String failingChildFlag,
                                               String otherCategory, String otherChildFlag) {
        CommonDataResponse original = response(failingCategory, "Original");
        CommonDataResponse otherOriginal = response(otherCategory, "Other original");
        CommonDataResponse updated = response(otherCategory, "Updated");
        whenRequested(failingCategory, failingChildFlag).thenReturn(original).thenThrow(mock(FeignException.class));
        whenRequested(otherCategory, otherChildFlag).thenReturn(otherOriginal, updated);
        service.getCategoryValues(AUTH_TOKEN, failingCategory, failingChildFlag);
        service.getCategoryValues(AUTH_TOKEN, otherCategory, otherChildFlag);

        service.refreshHearingRefDataCache();

        assertThat(service.getCategoryValues(AUTH_TOKEN, failingCategory, failingChildFlag)).isEqualTo(original);
        assertThat(service.getCategoryValues(AUTH_TOKEN, otherCategory, otherChildFlag)).isEqualTo(updated);
        verifyRequests(2, failingCategory, failingChildFlag);
        verifyRequests(2, otherCategory, otherChildFlag);
    }

    @ParameterizedTest
    @CsvSource({"HearingChannel,Y,HearingType,N", "HearingType,N,HearingChannel,Y"})
    void shouldRetryOnlyTheEmptyCategory(String emptyCategory, String emptyChildFlag,
                                        String populatedCategory, String populatedChildFlag) {
        CommonDataResponse populated = response(populatedCategory, "Populated");
        CommonDataResponse recovered = response(emptyCategory, "Recovered");
        whenRequested(populatedCategory, populatedChildFlag).thenReturn(populated);
        whenRequested(emptyCategory, emptyChildFlag).thenReturn(null, recovered);
        service.getCategoryValues(AUTH_TOKEN, populatedCategory, populatedChildFlag);

        service.refreshEmptyHearingRefDataCache();
        service.refreshEmptyHearingRefDataCache();

        assertThat(service.getCategoryValues(AUTH_TOKEN, populatedCategory, populatedChildFlag)).isEqualTo(populated);
        assertThat(service.getCategoryValues(AUTH_TOKEN, emptyCategory, emptyChildFlag)).isEqualTo(recovered);
        verifyRequests(1, populatedCategory, populatedChildFlag);
        verifyRequests(2, emptyCategory, emptyChildFlag);
    }

    @Test
    void shouldLeaveOtherCategoryAndChildQueriesUncached() {
        refDataUserService.retrieveCategoryValues(AUTH_TOKEN, "OtherCategory", IS_HEARINGCHILDREQUIRED_N);
        refDataUserService.retrieveCategoryValues(AUTH_TOKEN, "OtherCategory", IS_HEARINGCHILDREQUIRED_N);
        refDataUserService.retrieveCategoryValues(AUTH_TOKEN, HEARINGCHANNEL, IS_HEARINGCHILDREQUIRED_N);
        refDataUserService.retrieveCategoryValues(AUTH_TOKEN, HEARINGTYPE, IS_HEARINGCHILDREQUIRED_Y);

        verify(commonDataRefApi, times(2)).getAllCategoryValuesByCategoryId(
            AUTH_TOKEN, S2S_TOKEN, "OtherCategory", SERVICE_ID, IS_HEARINGCHILDREQUIRED_N);
        verify(commonDataRefApi).getAllCategoryValuesByCategoryId(
            AUTH_TOKEN, S2S_TOKEN, HEARINGCHANNEL, SERVICE_ID, IS_HEARINGCHILDREQUIRED_N);
        verify(commonDataRefApi).getAllCategoryValuesByCategoryId(
            AUTH_TOKEN, S2S_TOKEN, HEARINGTYPE, SERVICE_ID, IS_HEARINGCHILDREQUIRED_Y);
        assertThat(cacheManager.getCache(HEARING_REF_DATA_CACHE).getNativeCache()).isEqualTo(java.util.Map.of());
    }

    @Test
    void shouldPreserveSortedHearingTypeChannelAndSubchannelDropdowns() {
        CommonDataResponse response = CommonDataResponse.builder().categoryValues(List.of(
            channel("VID", "Video", "ZOOM", "Zoom", "TEAMS", "Teams"),
            channel("TEL", "Telephone", "BT", "BT MeetMe", "CONF", "Conference call")
        )).build();
        whenRequested(HEARINGCHANNEL, IS_HEARINGCHILDREQUIRED_Y).thenReturn(response);
        CommonDataResponse hearingTypes = CommonDataResponse.builder().categoryValues(List.of(
            CategoryValues.builder().categoryKey(HEARINGTYPE).key("FINAL").valueEn("Final hearing").build(),
            CategoryValues.builder().categoryKey(HEARINGTYPE).key("DIRECTIONS").valueEn("Directions hearing").build()
        )).build();
        whenRequested(HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N).thenReturn(hearingTypes);
        HearingDataService hearingDataService = hearingDataService();

        for (int i = 0; i < 4; i++) {
            var lists = hearingDataService.prePopulateHearingChannel(AUTH_TOKEN);
            assertThat(lists.get(HEARINGCHANNEL)).extracting("code").containsExactly("TEL", "VID");
            assertThat(lists.get(HEARINGCHANNEL)).extracting("label").containsExactly("Telephone", "Video");
            assertThat(lists.get(VIDEOSUBCHANNELS)).extracting("label").containsExactly("Teams", "Zoom");
            assertThat(lists.get(TELEPHONESUBCHANNELS)).extracting("label")
                .containsExactly("BT MeetMe", "Conference call");
            assertThat(hearingDataService.prePopulateHearingType(AUTH_TOKEN)).extracting("code")
                .containsExactly("DIRECTIONS", "FINAL");
            assertThat(hearingDataService.prePopulateHearingType(AUTH_TOKEN)).extracting("label")
                .containsExactly("Directions hearing", "Final hearing");
        }
        verifyRequests(1, HEARINGCHANNEL, IS_HEARINGCHILDREQUIRED_Y);
        verifyRequests(1, HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N);
    }

    private HearingDataService hearingDataService() {
        return new HearingDataService(
            refDataUserService, mock(HearingService.class), mock(LocationRefDataService.class),
            mock(AllocatedJudgeService.class), mock(HearingRequestDataMapper.class));
    }

    private static FeignException failure(int status) {
        return mock(FeignException.class, invocation -> "status".equals(invocation.getMethod().getName())
            ? status : RETURNS_DEFAULTS.answer(invocation));
    }

    private static Stream<Arguments> invalidResponses() {
        return Stream.of(HEARINGCHANNEL, HEARINGTYPE).flatMap(categoryId ->
            Stream.of(null, CommonDataResponse.builder().build(),
                      CommonDataResponse.builder().categoryValues(List.of()).build())
                .map(response -> Arguments.of(categoryId,
                    HEARINGCHANNEL.equals(categoryId) ? IS_HEARINGCHILDREQUIRED_Y : IS_HEARINGCHILDREQUIRED_N,
                    response)));
    }

    private static CommonDataResponse response(String categoryId, String label) {
        return CommonDataResponse.builder().categoryValues(List.of(
            CategoryValues.builder().categoryKey(categoryId).key("VALUE").valueEn(label).build()
        )).build();
    }

    private static CategoryValues channel(String code, String label, String firstCode, String firstLabel,
                                          String secondCode, String secondLabel) {
        return CategoryValues.builder().categoryKey(HEARINGCHANNEL).key(code).valueEn(label).childNodes(List.of(
            CategorySubValues.builder().key(firstCode).valueEn(firstLabel).build(),
            CategorySubValues.builder().key(secondCode).valueEn(secondLabel).build()
        )).build();
    }

    private org.mockito.stubbing.OngoingStubbing<CommonDataResponse> whenRequested(String categoryId, String childFlag) {
        return when(commonDataRefApi.getAllCategoryValuesByCategoryId(
            anyString(), eq(S2S_TOKEN), eq(categoryId), eq(SERVICE_ID), eq(childFlag)));
    }

    private void verifyRequests(int count, String categoryId, String childFlag) {
        verify(commonDataRefApi, times(count)).getAllCategoryValuesByCategoryId(
            anyString(), eq(S2S_TOKEN), eq(categoryId), eq(SERVICE_ID), eq(childFlag));
    }
}
