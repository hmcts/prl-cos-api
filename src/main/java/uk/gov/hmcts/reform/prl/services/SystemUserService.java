package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {

    private final String username = "privatelaw-system-update@mailnesia.com";
    private final String password = "Password12!";


    //TODO: use class to manage user config
    //private final SystemUpdateUserConfiguration userConfig;
    private final IdamClient idamClient;

    public String getSysUserToken() {
        return idamClient.getAccessToken(username, password);
    }

    public String getUserId(String userToken) {
        return idamClient.getUserInfo(userToken).getUid();
    }

}
