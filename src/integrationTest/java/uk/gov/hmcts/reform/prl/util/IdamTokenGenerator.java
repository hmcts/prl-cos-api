package uk.gov.hmcts.reform.prl.util;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.SystemUserConfiguration;

@TestPropertySource("classpath:application.yaml")
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class IdamTokenGenerator {

    @Value("${idam.solicitor.username}")
    private String solicitorUsername;

    @Value("${idam.solicitor.password}")
    private String solicitorPassword;

    @Value("${prl.system-update.username}")
    private String systemUpdateUsername;

    @Value("${prl.system-update.password}")
    private String systemUpdatePassword;

    @Value("${idam.api.url}")
    private String idamUserBaseUrl;

    @Value("${idam.client.id}")
    private String idamClientId;

    private IdamClient idamClient;

    private final OAuth2Configuration auth;

    private final SystemUserConfiguration userConfig;

    public String generateIdamTokenForSolicitor() {
        return idamClient.getAccessToken(solicitorUsername, solicitorPassword);
    }

    public String generateIdamTokenForSystem() {
        return idamClient.getAccessToken(systemUpdateUsername, systemUpdatePassword);
    }

    public String generateIdamTokenForUser(String username, String password) {
        return idamClient.getAccessToken(username, password);
    }

    public UserDetails getUserDetailsFor(final String token) {
        return idamClient.getUserDetails(token);
    }

    public String getSysUserToken() {
        log.info("idamUserBaseUrl: {}", idamUserBaseUrl);
        log.info("idamClientId: {}", idamClientId);
        log.info("auth.getClientSecret(): {}", auth.getClientSecret());
        log.info("auth.getRedirectUri(): {}", auth.getRedirectUri());
        log.info("userConfig.getUserName(): {}", userConfig.getUserName());
        log.info("userConfig.getPassword(): {}", userConfig.getPassword());
        JsonPath jp = RestAssured.given().relaxedHTTPSValidation().post(idamUserBaseUrl + "/o/token?"
                                                                            + "client_secret=" + auth.getClientSecret()
                                                                            + "&client_id=" + idamClientId
                                                                            + "&redirect_uri=" + auth.getRedirectUri()
                                                                            + "&username=" + userConfig.getUserName()
                                                                            + "&password=" + userConfig.getPassword()
                                                                            + "&grant_type=password&scope=openid profile roles manage-user")
            .body().jsonPath();
        String token = jp.get("access_token");
        log.info("System user token: {}", token);
        return "Bearer " + token;
    }
}
