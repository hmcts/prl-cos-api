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


    private final OAuth2Configuration auth;

    private final SystemUserConfiguration userConfig;

    private final IdamClient idamClient;

    private final String username = "privatelaw-system-update@mailnesia.com";
    private final String password = "Password12!";

    public String getSysUserToken() {
        log.info("*******************************************************");
        log.info(auth.getClientId());
        log.info(auth.getClientScope());
        log.info(auth.getRedirectUri());
        log.info(auth.getClientSecret());
        log.info("*******************************************************");
        return idamClient.getAccessToken(username,password);
    }

    public String getUserId(String userToken) {
        return idamClient.getUserInfo(userToken).getUid();
    }

}
