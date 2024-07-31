package uk.gov.hmcts.reform.prl.clients.refdata;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.clients.JudicialUserDetailsApi;
import uk.gov.hmcts.reform.prl.clients.idam.IdamApiConsumerApplication;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "judicialUserDetailsApi", port = "8899")
@ContextConfiguration(
    classes = {JudicialUsersApiConsumerApplication.class, IdamApiConsumerApplication.class}
)
@TestPropertySource(
    properties = {"bundle.api.url=","idam.api.url=localhost:5000","commonData.api.url=http://localhost:8899",
        "fis_hearing.api.url=localhost:5000",
        "refdata.api.url=",
        "courtfinder.api.url=",
        "prl-dgs-api.url=",
        "fees-register.api.url=",
        "fis_hearing.api.url=",
        "judicialUsers.api.url=http://localhost:8899",
        "locationfinder.api.url=",
        "rd_professional.api.url=",
        "payments.api.url=",
        "pba.validation.service.api.baseurl=",
        "staffDetails.api.url=",
        "amRoleAssignment.api.url="
    }
)
@PactFolder("pacts")
public class JudicialUsersApiConsumerTest {

    @Autowired
    JudicialUserDetailsApi judicialUserDetailsApi;

    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private static final String SERVICE_AUTHORIZATION_HEADER = "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";

    private final String validResponseBody = "gatekeeping/JudgeDetailsResponseBody.json";

    private JudicialUsersApiRequest judicialUsersApiRequest;

    @Pact(provider = "judicialUserDetailsApi", consumer = "prl_cos")
    public V4Pact generatePactFragmentForAllocateJudge(PactDslWithProvider builder) throws Exception {
        String[] personalCodes = new String[2];
        personalCodes[0] = "49933940";
        judicialUsersApiRequest = JudicialUsersApiRequest.builder().personalCode(personalCodes).build();
        // @formatter:off
        return builder
            .given("Allocating Judge")
            .uponReceiving("A Request for allocating judge")
            .method("POST")
            .headers("ServiceAuthorization", SERVICE_AUTHORIZATION_HEADER)
            .headers("Authorization", BEARER_TOKEN)
            .headers("Content-Type", "application/json")
            .path("/refdata/judicial/users")
            .body(new ObjectMapper().writeValueAsString(judicialUsersApiRequest), "application/json")
            .willRespondWith()
            .status(200)
            .body(ResourceLoader.loadJson(validResponseBody),"application/json")
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForAllocateJudge")
    public void verifyAllocatedJudgeDetails() {
        List<JudicialUsersApiResponse>
            judicialUsersList = judicialUserDetailsApi.getAllJudicialUserDetails(BEARER_TOKEN,SERVICE_AUTHORIZATION_HEADER,judicialUsersApiRequest);

        assertNotNull(judicialUsersList);
        JudicialUsersApiResponse judicialUsersApiResponse = judicialUsersList.get(0);
        assertNotNull(judicialUsersApiResponse);
        assertEquals(judicialUsersApiResponse.getPersonalCode(),"49933940");
        assertEquals(judicialUsersApiResponse.getEmailId(),"49933940EMP-@ejudiciary.net");
        assertEquals(judicialUsersApiResponse.getSurname(), "Prakasscs");
    }

}
