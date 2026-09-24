package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.prl.config.HmcUserConfiguration;
import uk.gov.hmcts.reform.prl.config.SystemUserConfiguration;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {

    public static final String SYS_USER_CACHE = "systemUserCache";
    public static final String HMC_USER_CACHE = "hmcUserCache";

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MILLIS = 250L;

    private final OAuth2Configuration auth;

    private final SystemUserConfiguration userConfig;

    private final IdamClient idamClient;

    private final HmcUserConfiguration hmcUserConfig;

    @Cacheable(cacheNames = SYS_USER_CACHE)
    public String getSysUserToken() {
        log.info("Fetching system user token");
        return idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }

    @Cacheable(cacheNames = HMC_USER_CACHE)
    public String getHmcUserToken() {
        log.info("Fetching HMC user token");
        validateCredentials(hmcUserConfig.getHmcCftUserName(), hmcUserConfig.getHmcCftUserPassword());
        return getAccessTokenWithRetry(hmcUserConfig.getHmcCftUserName(), hmcUserConfig.getHmcCftUserPassword());
    }

    @CacheEvict(allEntries = true, cacheNames = {SYS_USER_CACHE, HMC_USER_CACHE})
    @Scheduled(fixedDelay = 1800000)
    public void cacheEvict() {
        log.info("Evicting system user cron cache");
    }

    public String getUserId(String userToken) {
        return idamClient.getUserInfo(userToken).getUid();
    }

    private String getAccessTokenWithRetry(String username, String password) {
        RuntimeException lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return idamClient.getAccessToken(username, password);
            } catch (RuntimeException ex) {
                lastException = ex;
                log.warn("Failed to generate HMC IDAM token on attempt {}/{}", attempt, MAX_RETRIES);
                if (attempt < MAX_RETRIES) {
                    sleepBeforeRetry();
                }
            }
        }
        throw new IllegalStateException("Failed to generate HMC IDAM token after retries", lastException);
    }

    private void validateCredentials(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new IllegalStateException("Missing IDAM credentials for hearing CFT user");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrupted while generating HMC IDAM token", ex);
        }
    }
}
