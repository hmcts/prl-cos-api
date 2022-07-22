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

    @Value("${private-law.authorised-services}")
    private String s2sAuthorisedServices;

    public Boolean authorise(String serviceAuthHeader) {
        String callingService;
        callingService = serviceAuthorisationApi.getServiceName(serviceAuthHeader);
        if (callingService != null && Arrays.asList(s2sAuthorisedServices.split(","))
            .contains(callingService)) {
            return true;
        } else {
            throw new AuthorisationException("Request not authorised");
        }
    }
}
