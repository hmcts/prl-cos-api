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

    @Value("${idam.citizen.username}")
    private String citizenUsername;

    @Value("${idam.citizen.password}")
    private String citizenPassword;

    @Autowired
    private IdamClient idamClient;

    @Value("${idam.apigateway.username}")
    private String apiGwUserName;

    @Value("${idam.apigateway.password}")
    private String apiGwPassword;

    @Value("${idam.cafcass.username}")
    private String cafcassUserName;

    @Value("${idam.cafcass.password}")
    private String cafcassPassword;

    @Value("${idam.judge.username}")
    private String judgeUserName;

    @Value("${idam.judge.password}")
    private String judgePassword;

    @Value("${idam.admin.username}")
    private String courtAdminUsername;

    @Value("${idam.admin.password}")
    private String courtAdminPassword;

    public String generateIdamTokenForSolicitor() {
        return idamClient.getAccessToken(solicitorUsername, solicitorPassword);
    }

    public String generateIdamTokenForSystem() {
        return idamClient.getAccessToken(systemUpdateUsername, systemUpdatePassword);
    }

    public String generateIdamTokenForJudge() {
        return idamClient.getAccessToken(judgeUserName, judgePassword);
    }

    public String generateIdamTokenForCourtNav() {
        return idamClient.getAccessToken(apiGwUserName, apiGwPassword);
    }

    public String generateIdamTokenForUser(String username, String password) {
        return idamClient.getAccessToken(username, password);
    }

    public String generateIdamTokenForCafcass() {
        return idamClient.getAccessToken(cafcassUserName, cafcassPassword);
    }

    public UserDetails getUserDetailsFor(final String token) {
        return idamClient.getUserDetails(token);
    }

    public String generateIdamTokenForCitizen() {
        return idamClient.getAccessToken(citizenUsername, citizenPassword);
    }

    public String generateIdamTokenForCourtAdmin() {
        return idamClient.getAccessToken(courtAdminUsername, courtAdminPassword);
    }

}
