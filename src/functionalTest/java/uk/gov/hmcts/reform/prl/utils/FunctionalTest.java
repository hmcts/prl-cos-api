package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

@Slf4j
public abstract class FunctionalTest {

    @Autowired
    SystemUserService systemUserService;

    public String getValidToken() {
        return systemUserService.getSysUserToken();
    }

}
