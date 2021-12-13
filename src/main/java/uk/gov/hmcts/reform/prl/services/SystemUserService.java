package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.prl.config.SystemUserConfiguration;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {

    @Autowired
    OAuth2Configuration oAuth2Configuration;

    private final SystemUserConfiguration userConfig;

    private final IdamClient idamClient;

    public String getSysUserToken() {

        log.info("*******************************************************");
        log.info(oAuth2Configuration.getClientId());
        log.info(oAuth2Configuration.getClientScope());
        log.info(oAuth2Configuration.getRedirectUri());
        log.info(oAuth2Configuration.getClientSecret());
        log.info(userConfig.getUserName());
        log.info("*******************************************************");

        return idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }

    public String getUserId(String userToken) {
        return idamClient.getUserInfo(userToken).getUid();
    }

}
