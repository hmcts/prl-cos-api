package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.prl.config.RefDataSystemUserConfiguration;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RefDataSystemUserService {
    private final OAuth2Configuration auth;

    private final RefDataSystemUserConfiguration userConfig;

    private final IdamClient idamClient;

    public String getSysUserToken() {
        return idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }

    public String getUserId(String userToken) {
        return idamClient.getUserInfo(userToken).getUid();
    }
}
