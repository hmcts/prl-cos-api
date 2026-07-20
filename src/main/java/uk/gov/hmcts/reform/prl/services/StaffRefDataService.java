package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.clients.StaffResponseDetailsApi;
import uk.gov.hmcts.reform.prl.exception.NoStaffResponseException;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;

import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFORDERASC;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STAFFSORTCOLUMN;
import static uk.gov.hmcts.reform.prl.services.RefDataUserService.STAFF_REF_DATA_CACHE;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StaffRefDataService {

    private final AuthTokenGenerator authTokenGenerator;
    private final StaffResponseDetailsApi staffResponseDetailsApi;
    private final IdamClient idamClient;
    private final CacheManager cacheManager;

    @Value("${prl.refdata.username}")
    private String refDataIdamUsername;
    @Value("${prl.refdata.password}")
    private String refDataIdamPassword;

    @SuppressWarnings("unchecked")
    public List<StaffResponse> getAllStaffDetails() {
        Cache cache = cacheManager.getCache(STAFF_REF_DATA_CACHE);
        Cache.ValueWrapper cachedStaffDetails = cache != null ? cache.get(SimpleKey.EMPTY) : null;
        if (cachedStaffDetails == null) {
            log.info("Staff ref data cache is empty, fetching staff ref data");
            return refreshStaffDetailsCache();
        }
        List<StaffResponse> staffDetails = (List<StaffResponse>) cachedStaffDetails.get();
        log.info("Retrieved {} staff members from staff ref data cache", staffDetails == null ? 0 : staffDetails.size());
        return staffDetails;
    }

    public synchronized List<StaffResponse> refreshStaffDetailsCache() {
        List<StaffResponse> staffDetails = fetchAllStaffDetails();
        Cache cache = cacheManager.getCache(STAFF_REF_DATA_CACHE);
        if (cache != null) {
            cache.put(SimpleKey.EMPTY, staffDetails);
        }
        return staffDetails;
    }

    public boolean isStaffDetailsCacheEmpty() {
        Cache cache = cacheManager.getCache(STAFF_REF_DATA_CACHE);
        return cache == null || cache.get(SimpleKey.EMPTY) == null;
    }

    private List<StaffResponse> fetchAllStaffDetails() {
        log.info("Fetching all staff ref data from refdata API");
        try {
            ResponseEntity<List<StaffResponse>> response = staffResponseDetailsApi.getAllStaffResponseDetails(
                idamClient.getAccessToken(refDataIdamUsername, refDataIdamPassword),
                authTokenGenerator.generate(),
                SERVICENAME,
                STAFFSORTCOLUMN,
                STAFFORDERASC,
                Integer.MAX_VALUE,
                0
            );
            if (response == null) {
                log.warn("No staff ref data response received from refdata API");
                throw new NoStaffResponseException("No staff ref data response received");
            }
            List<StaffResponse> staffList = response.getBody();
            if (staffList == null || staffList.isEmpty()) {
                log.warn("No staff members returned from refdata API");
                throw new NoStaffResponseException("No staff members returned");
            }
            log.info("Fetched {} staff members", staffList.size());
            return staffList;
        } catch (FeignException e) {
            throw new NoStaffResponseException("Failed to retrieve staff response", e);
        }
    }
}
