package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prl.config.SystemUserConfiguration;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {


    private final SystemUserConfiguration userConfig;

    private final IdamClient idamClient;

    public String getSysUserToken() {
        return idamClient.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
    }

    public String getUserId(String userToken) {
        return idamClient.getUserInfo(userToken).getUid();
    }

}
