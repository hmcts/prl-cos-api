package uk.gov.hmcts.reform.prl.clients.idam;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.google.common.collect.Lists;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@TestPropertySource(
    properties = {"idam.api.url=localhost:5000"}
)
@EnableAutoConfiguration
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "Idam_api", port = "5000")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class IdamApiConsumerTest {

    public static final String TOKEN_REGEXP = "[a-zA-Z0-9._-]+";
    public static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";

    @Autowired
    private IdamApi idamClient;

    private static final String IDAM_OPENID_USERINFO_URL = "/o/userinfo";

    @BeforeEach
    public void beforeEach() throws Exception {
        Thread.sleep(4000);
    }

    @Pact(provider = "Idam_api", consumer = "prl_cos")
    public RequestResponsePact executeGetUserInfo(PactDslWithProvider builder) {

        Map<String, Object> params = new HashMap<>();
        params.put("redirect_uri", "http://www.dummy-pact-service.com/callback");
        params.put("client_id", "pact");
        params.put("client_secret", "pactsecret");
        params.put("scope", "openid profile roles");
        params.put("username", "prl_aat_solicitor@mailinator.com");
        params.put("password", "generic");

        return builder.given("I have obtained an access_token as a user", params)
            .uponReceiving("IDAM returns user info to the client")
            .path(IDAM_OPENID_USERINFO_URL)
            .headerFromProviderState("Authorization", "Bearer ${access_token}",
                                     BEARER_TOKEN)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(createUserInfoResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetUserInfo")
    void verifyUserInfo() {
        UserInfo actualUserInfo = idamClient.retrieveUserInfo(BEARER_TOKEN);

        UserInfo expectedUserInfo = UserInfo.builder()
            .familyName("Smith")
            .givenName("John")
            .name("John Smith")
            .roles(Lists.newArrayList("caseworker-privatelaw-solicitor"))
            .sub("prl_aat_solicitor@mailinator.com")
            .uid("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
            .build();

        assertThat(actualUserInfo)
            .isEqualTo(expectedUserInfo);
    }


    private PactDslJsonBody createAuthResponse() {

        return new PactDslJsonBody()
            .stringMatcher("access_token", TOKEN_REGEXP,
                           "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FI.AL_JD-")
            .stringMatcher("refresh_token", TOKEN_REGEXP,
                           "eyJ0eXAiOiJKV1QiLCJ6aXAiO.iJOT05FIiwia2lkIjoi_i9PN-k92V")
            .stringType("scope", "openid roles profile search-user")
            .stringMatcher("id_token", TOKEN_REGEXP,
                           "eyJ0e.XAiOiJKV1QiLCJra-WQiOiJiL082_T3ZWdjEre")
            .stringType("token_type", "Bearer")
            .stringMatcher("expires_in", "[0-9]+", "28798");
    }

    private PactDslJsonBody createUserInfoResponse() {
        return new PactDslJsonBody()
            .stringType("sub", "prl_aat_solicitor@mailinator.com")
            .stringType("uid", "33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
            .minArrayLike("roles", 1, PactDslJsonRootValue.stringType("caseworker-privatelaw-solicitor"), 1)
            .stringType("name", "John Smith")
            .stringType("given_name", "John")
            .stringType("family_name", "Smith");
    }
}
