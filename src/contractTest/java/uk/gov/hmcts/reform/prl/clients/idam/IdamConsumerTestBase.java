package uk.gov.hmcts.reform.prl.clients.idam;

import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.client.fluent.Executor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "idamApi_oidc", port = "8891")
@PactFolder("pacts")
@SpringBootTest({
    "idam.api.url : localhost:8891"
})
public abstract class IdamConsumerTestBase {

    public static final int SLEEP_TIME = 2000;
    protected static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    @Autowired
    protected IdamApi idamApi;
    @Value("${idam.system-update.username}")
    protected String caseworkerUsername;

    @Value("${idam.system-update.password}")
    protected String caseworkerPwd;

    @Value("${idam.client.secret}")
    protected String clientSecret;

    @BeforeEach
    public void prepareTest() throws Exception {
        Thread.sleep(SLEEP_TIME);
    }

    @AfterEach
    void teardown() {
        Executor.closeIdleConnections();
    }

}
