package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SystemUserService {

    @Value("${prl.username")
    private final String username;
    @Value("${prl.password")
    private final String password;

    private final IdamClient idamClient;

    public String getSysUserToken() {
        return idamClient.getAccessToken(username, password);
    }

    public String getUserId(String userToken) {
        return idamClient.getUserInfo(userToken).getUid();
    }

}
