package uk.gov.hmcts.reform.prl.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@TestPropertySource(locations = "classpath:application.yaml")
@Service
public class S2sClient {

    @Value("${idam.s2s-auth.url}")
    private String testUrl;

    private String s2sToken;

    @Autowired private AuthTokenGenerator authTokenGenerator;

    public String serviceAuthTokenGenerator() {
        if (!StringUtils.hasText(this.s2sToken)) {
            this.s2sToken = authTokenGenerator.generate();
        }
        return "Bearer " + this.s2sToken;
    }
}
