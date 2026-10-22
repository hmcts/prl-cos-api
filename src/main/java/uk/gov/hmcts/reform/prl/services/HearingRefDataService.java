package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.CommonDataRefApi;
import uk.gov.hmcts.reform.prl.exception.NoHearingRefDataResponseException;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGCHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_N;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_Y;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_ID;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingRefDataService {

    public static final String HEARING_REF_DATA_CACHE = "hearingRefDataCache";

    private static final List<CategoryQuery> CACHED_QUERIES = List.of(
        new CategoryQuery(HEARINGCHANNEL, IS_HEARINGCHILDREQUIRED_Y),
        new CategoryQuery(HEARINGTYPE, IS_HEARINGCHILDREQUIRED_N)
    );

    private final CommonDataRefApi commonDataRefApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdamClient idamClient;
    private final CacheManager cacheManager;

    private final Map<CategoryQuery, Long> failedFetchTimes = new ConcurrentHashMap<>();
    private LongSupplier nanoTime = System::nanoTime;

    @Value("${commonData.cache.failureRetryDelayMillis}")
    private long failureRetryDelayMillis;

    @Value("${prl.refdata.username}")
    private String refDataIdamUsername;
    @Value("${prl.refdata.password}")
    private String refDataIdamPassword;

    public CommonDataResponse getCategoryValues(String authorization, String categoryId, String isChildRequired) {
        CategoryQuery query = CACHED_QUERIES.stream()
            .filter(value -> value.categoryId().equals(categoryId) && value.isChildRequired().equals(isChildRequired))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported hearing reference data query"));
        CommonDataResponse cachedResponse = getCachedResponse(query);
        if (cachedResponse != null) {
            return cachedResponse;
        }
        synchronized (query) {
            cachedResponse = getCachedResponse(query);
            if (cachedResponse != null) {
                return cachedResponse;
            }
            Long failedAt = failedFetchTimes.get(query);
            if (failedAt != null
                && nanoTime.getAsLong() - failedAt < TimeUnit.MILLISECONDS.toNanos(failureRetryDelayMillis)) {
                throw new NoHearingRefDataResponseException("Temporarily unavailable: " + query.categoryId());
            }
            return fetchAndCache(authorization, query);
        }
    }

    @Scheduled(fixedDelayString = "${commonData.cache.refreshDelayMillis}")
    public void refreshHearingRefDataCache() {
        for (CategoryQuery query : CACHED_QUERIES) {
            synchronized (query) {
                refreshCache(query);
            }
        }
    }

    @Scheduled(fixedDelayString = "${commonData.cache.emptyRefreshDelayMillis}")
    public void refreshEmptyHearingRefDataCache() {
        for (CategoryQuery query : CACHED_QUERIES) {
            synchronized (query) {
                if (getCachedResponse(query) == null) {
                    refreshCache(query);
                }
            }
        }
    }

    private void refreshCache(CategoryQuery query) {
        try {
            fetchAndCache(idamClient.getAccessToken(refDataIdamUsername, refDataIdamPassword), query);
        } catch (FeignException | NoHearingRefDataResponseException e) {
            log.warn("Hearing reference data cache refresh failed for {}", query.categoryId(), e);
        }
    }

    private CommonDataResponse getCachedResponse(CategoryQuery query) {
        return getCache().get(query.cacheKey(), CommonDataResponse.class);
    }

    private Cache getCache() {
        return Objects.requireNonNull(cacheManager.getCache(HEARING_REF_DATA_CACHE));
    }

    private CommonDataResponse fetchAndCache(String authorization, CategoryQuery query) {
        log.info("Fetching {} from common reference data API", query.categoryId());
        CommonDataResponse response;
        try {
            response = commonDataRefApi.getAllCategoryValuesByCategoryId(
                authorization,
                authTokenGenerator.generate(),
                query.categoryId(),
                SERVICE_ID,
                query.isChildRequired()
            );
        } catch (FeignException e) {
            // A caller's rejected token must not suppress requests from other users.
            if (e.status() < 0 || e.status() == 429 || e.status() >= 500) {
                failedFetchTimes.put(query, nanoTime.getAsLong());
            }
            throw new NoHearingRefDataResponseException("Failed to retrieve " + query.categoryId(), e);
        }
        failedFetchTimes.remove(query);
        if (response == null || response.getCategoryValues() == null || response.getCategoryValues().isEmpty()) {
            log.warn("No values returned for {}, leaving cache unchanged", query.categoryId());
            return response;
        }
        getCache().put(query.cacheKey(), response);
        return response;
    }

    private record CategoryQuery(String categoryId, String isChildRequired) {
        private SimpleKey cacheKey() {
            return new SimpleKey(categoryId, SERVICE_ID, isChildRequired);
        }
    }
}
