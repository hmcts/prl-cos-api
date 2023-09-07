package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthorisationService {

    private final ServiceAuthorisationApi serviceAuthorisationApi;

    @Value("${private-law.authorised-services}")
    private String s2sAuthorisedServices;

    @Value("${idam.s2s-auth.microservice}")
    private String s2sServiceForSolicitor;

    private final IdamClient idamClient;

    private UserInfo userInfo;

    public Boolean authoriseService(String serviceAuthHeader) {
        String callingService;
        try {
            callingService = serviceAuthorisationApi.getServiceName(serviceAuthHeader);
            if (callingService != null && Arrays.asList(s2sAuthorisedServices.split(","))
                .contains(callingService)) {
                return true;
            }
        } catch (Exception ex) {
            log.error("S2S token is not authorised");
        }
        return false;
    }

    public Boolean authoriseUser(String authorisation) {
        try {
            userInfo = idamClient.getUserInfo(authorisation);
            if (null != userInfo) {
                return true;
            }
        } catch (Exception ex) {
            log.error("User token is invalid");
        }
        return false;
    }

    public UserInfo getUserInfo() {
        return this.userInfo;
    }

    public boolean isAuthorized(String authorisation, String s2sToken) {
        return Boolean.TRUE.equals(authoriseUser(authorisation))
            && Boolean.TRUE.equals(authoriseService(s2sToken));
    }
}
