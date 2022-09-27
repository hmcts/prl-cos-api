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

    private final IdamClient idamClient;

    public Boolean authoriseService(String serviceAuthHeader) {
        String callingService;
        log.info(" s2sAuthorisedServices:::" + s2sAuthorisedServices);
        try {
            callingService = serviceAuthorisationApi.getServiceName(serviceAuthHeader);
            if (callingService != null && Arrays.asList(s2sAuthorisedServices.split(","))
                .contains(callingService)) {
                log.info(" callingService::::" + callingService);

                return true;
            }
        } catch (Exception ex) {
            //do nothing
            log.error("S2S token is not authorised");
        }
        return false;
    }

    public Boolean authoriseUser(String authorisation) {
        try {
            UserInfo userInfo = idamClient.getUserInfo(authorisation);
            if (null != userInfo) {
                log.info(" authoriseUser::::" + userInfo.getName());
                return true;
            }
        } catch (Exception ex) {
            //do nothing
            log.error("User token is invalid");
        }
        return false;
    }
}
