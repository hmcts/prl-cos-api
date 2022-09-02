package uk.gov.hmcts.reform.prl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@TestPropertySource("classpath:application.yaml")
@Service
public class IdamTokenGenerator {

    @Value("${idam.solicitor.username}")
    private String solicitorUsername;

    @Value("${idam.solicitor.password}")
    private String solicitorPassword;

    @Value("${idam.systemupdate.username}")
    private String systemUpdateUsername;

    @Value("${idam.systemupdate.password}")
    private String systemUpdatePassword;

    @Autowired
    private IdamClient idamClient;

    @Value("${idam.apigateway.username}")
    private String apiGwUserName;

    @Value("${idam.apigateway.password}")
    private String apiGwPassword;

    @Value("${idam.citizen.username}")
    private String citizenUsername;

    @Value("${idam.citizen.password}")
    private String citizenPassword;

    public String generateIdamTokenForSolicitor() {
        return idamClient.getAccessToken(solicitorUsername, solicitorPassword);
    }

    public String generateIdamTokenForSystem() {
        return idamClient.getAccessToken(systemUpdateUsername, systemUpdatePassword);
    }

    public String generateIdamTokenForCourtNav() {
        return idamClient.getAccessToken(apiGwUserName, apiGwPassword);
    }

    public String generateIdamTokenForUser(String username, String password) {
        return idamClient.getAccessToken(username, password);
    }

    public UserDetails getUserDetailsFor(final String token) {
        return idamClient.getUserDetails(token);
    }
}
