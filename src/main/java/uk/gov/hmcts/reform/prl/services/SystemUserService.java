package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.prl.config.SystemUserConfiguration;

import static uk.gov.hmcts.reform.prl.config.CacheConfiguration.SYS_USER_CACHE;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {

    private final OAuth2Configuration auth;

    private final SystemUserConfiguration userConfig;

    private final IdamClient idamClient;

    @Cacheable(value = SYS_USER_CACHE)
    public String getSysUserToken() {
        log.info("Fetching system user token");
        return idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }

    public String getUserId(String userToken) {
        return idamClient.getUserInfo(userToken).getUid();
    }
}
