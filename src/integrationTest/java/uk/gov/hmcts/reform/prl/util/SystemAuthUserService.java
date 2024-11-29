package uk.gov.hmcts.reform.prl.util;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.prl.config.SystemUserConfiguration;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemAuthUserService {

    public static final String SYS_USER_CACHE = "systemUserCache";

    private final OAuth2Configuration auth;

    private final SystemUserConfiguration userConfig;

    private final IdamClient idamClient;

    @Value("${idam.api.url}")
    private String idamUserBaseUrl;

    public String getSysUserToken() {
        log.info("Fetching system user token");
        JsonPath jp = RestAssured.given().relaxedHTTPSValidation().post(idamUserBaseUrl + "/o/token?"
                                                                                + "client_secret=" + auth.getClientSecret()
                                                                                + "&client_id==prl-cos-api"
                                                                                + "&redirect_uri=" + auth.getRedirectUri()
                                                                                + "&username=" + userConfig.getUserName()
                                                                                + "&password=" + userConfig.getPassword()
                                                                                + "&grant_type=password&scope=openid profile roles manage-user")
            .body().jsonPath();
        String token = jp.get("access_token");
        return token;
    }

    @CacheEvict(allEntries = true, cacheNames = SYS_USER_CACHE)
    @Scheduled(fixedDelay = 1800000)
    public void cacheEvict() {
        log.info("Evicting system user cron cache");
    }

    public String getUserId(String userToken) {
        return idamClient.getUserInfo(userToken).getUid();
    }
}
