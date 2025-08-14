package uk.gov.hmcts.reform.prl.utils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

@TestPropertySource("classpath:application.yaml")
@Service
@Slf4j
public class IdamTokenGenerator {

    @Value("${idam.solicitor.username}")
    private String solicitorUsername;

    @Value("${idam.solicitor.password}")
    private String solicitorPassword;

    @Value("${idam.citizen.username}")
    private String citizenUsername;

    @Value("${idam.citizen.password}")
    private String citizenPassword;

    @Autowired
    private IdamClient idamClient;

    @Autowired
    private SystemUserService systemUserService;

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

    private String solicitorIdamToken;

    private String systemIdamToken;

    private String judgeIdamToken;

    private String cafcassIdamToken;

    private String citizenIdamToken;

    private String courtAdminIdamToken;

    public String generateIdamTokenForSolicitor() {
        return solicitorIdamToken;
    }

    public String generateIdamTokenForSystem() {
        return systemIdamToken;
    }

    public String generateIdamTokenForJudge() {
        return judgeIdamToken;
    }

    public String generateIdamTokenForCourtNav() {
        return idamClient.getAccessToken(apiGwUserName, apiGwPassword);
    }

    public String generateIdamTokenForUser(String username, String password) {
        return idamClient.getAccessToken(username, password);
    }

    public String generateIdamTokenForCafcass() {
        return cafcassIdamToken;
    }

    public UserDetails getUserDetailsFor(final String token) {
        return idamClient.getUserDetails(token);
    }

    public String generateIdamTokenForCitizen() {
        return citizenIdamToken;
    }

    public String generateIdamTokenForCourtAdmin() {
        return courtAdminIdamToken;
    }

    @PostConstruct
    public void beforeTestClass() {
        log.info(":::: Generating Bearer Token From Idam");
        solicitorIdamToken = idamClient.getAccessToken(solicitorUsername, solicitorPassword);
        systemIdamToken = systemUserService.getSysUserToken();
        judgeIdamToken = idamClient.getAccessToken(judgeUserName, judgePassword);
        cafcassIdamToken = idamClient.getAccessToken(cafcassUserName, cafcassPassword);
        citizenIdamToken = idamClient.getAccessToken(citizenUsername, citizenPassword);
        courtAdminIdamToken = idamClient.getAccessToken(courtAdminUsername, courtAdminPassword);
    }
}
