package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.util.CosApiClient;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest(classes = {Application.class})
public class GetWelcomeTest extends IntegrationTest {

    @Autowired
    private CosApiClient cosApiClient;

    @DisplayName("Should welcome upon root request with 200 response code")
    @Test
    public void welcomeRootEndpoint() {
        String response = cosApiClient.welcome();

        assertThat(response, startsWith("Welcome"));
    }
}
