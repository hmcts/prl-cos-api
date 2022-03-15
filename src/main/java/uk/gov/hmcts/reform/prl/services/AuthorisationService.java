package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.prl.exception.AuthorisationException;

import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthorisationService {

    private final ServiceAuthorisationApi serviceAuthorisationApi;

    @Value("${payments.authorised-services}")
    private String s2sAuthorisedServices;

    public Boolean authorise(String serviceAuthHeader) {
        String callingService;
        log.info("About to authorise request");
        callingService = serviceAuthorisationApi.getServiceName(serviceAuthHeader);
        if (callingService != null && Arrays.asList(s2sAuthorisedServices.split(","))
            .contains(callingService)) {
            log.info("Request authorised for {}", callingService);
            return true;
        } else {
            log.info("Request not authorised for {}", callingService);
            throw new AuthorisationException("Request not authorised");
        }
    }
}
