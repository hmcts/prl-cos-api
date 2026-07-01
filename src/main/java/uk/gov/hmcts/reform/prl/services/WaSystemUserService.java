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
import uk.gov.hmcts.reform.prl.config.WaSystemUserConfiguration;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class WaSystemUserService {

    public static final String WA_SYSTEM_USER_CACHE = "waSystemUserCache";

    private final OAuth2Configuration auth;

    private final WaSystemUserConfiguration userConfig;

    private final IdamClient idamClient;

    @Cacheable(cacheNames = WA_SYSTEM_USER_CACHE)
    public String getSysUserToken() {
        log.info("Fetching system user token");
        return idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }

    @CacheEvict(allEntries = true, cacheNames = WA_SYSTEM_USER_CACHE)
    @Scheduled(fixedDelay = 1800000)
    public void cacheEvict() {
        log.info("Evicting system user cron cache");
    }

    public String getUserId(String userToken) {
        return idamClient.getUserInfo(userToken).getUid();
    }
}
