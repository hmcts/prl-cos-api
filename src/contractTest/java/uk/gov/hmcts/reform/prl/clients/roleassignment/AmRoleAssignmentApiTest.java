package uk.gov.hmcts.reform.prl.clients.roleassignment;


import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
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
import org.springframework.util.Assert;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.clients.idam.IdamApiConsumerApplication;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "amRoleAssignment", port = "8999")
@ContextConfiguration(
        classes = {AmRoleAssignmentApiApplication.class, IdamApiConsumerApplication.class}
)
@TestPropertySource(
        properties = {"bundle.api.url=","idam.api.url=localhost:5000","commonData.api.url=localhost:5000",
            "fis_hearing.api.url=",
            "refdata.api.url=",
            "courtfinder.api.url=",
            "prl-dgs-api.url=",
            "fees-register.api.url=",
            "judicialUsers.api.url=",
            "locationfinder.api.url=",
            "rd_professional.api.url=",
            "payments.api.url=",
            "pba.validation.service.api.baseurl=",
            "staffDetails.api.url=",
            "amRoleAssignment.api.url=http://localhost:8999"
        }
)

@PactFolder("pacts")
public class AmRoleAssignmentApiTest {

    @Autowired
    RoleAssignmentApi roleAssignmentApi;

    private static final String X_CORRELATION_ID = "someXCorrelationId";
    private static final String APPLICATION_JSON = "application/json";

    private static final String AUTH = "token";
    private static final String S2S = "token";


    @Pact(provider = "am_role_assignment_service", consumer = "prl_cos")
    public RequestResponsePact generatePactFragmentForRoleAssignment(PactDslWithProvider builder) throws Exception {

        RoleAssignmentRequest roleAssignmentRequest = RoleAssignmentRequest.roleAssignmentRequest().build();
        String roleAssignmentResponseBody = "response/role-assignment.json";

        return builder
                .given("Role Assignment")
                .uponReceiving("A Request to assign a new role")
                .method("POST")
                .headers("x-correlation-id", X_CORRELATION_ID)
                .headers("content-type", APPLICATION_JSON)
                .path("/am/role-assignments")
                .body(new ObjectMapper().writeValueAsString(roleAssignmentRequest), APPLICATION_JSON)
                .willRespondWith()
                .status(201)
                .body(ResourceLoader.loadJson(roleAssignmentResponseBody),APPLICATION_JSON)
                .toPact();
    }

    @Pact(provider = "am_role_assignment_service", consumer = "prl_cos")
    public RequestResponsePact generatePactFragmentForRoleAssignmentActor(PactDslWithProvider builder) throws Exception {

        RoleAssignmentRequest roleAssignmentRequest = RoleAssignmentRequest.roleAssignmentRequest().build();
        String roleAssignmentResponseBody = "response/role-assignment.json";

        return builder
            .given("Role Assignment Actor")
            .uponReceiving("A Request to assign a new role for a specific actor")
            .method("GET")
            .headers("Authorization", AUTH)
            .headers("ServiceAuthorization", S2S)
            .headers("x-correlation-id", X_CORRELATION_ID)
            .path("/am/role-assignments/actors/")
            .body(new ObjectMapper().writeValueAsString(roleAssignmentRequest), APPLICATION_JSON)
            .willRespondWith()
            .status(200)
            .body(ResourceLoader.loadJson(roleAssignmentResponseBody),APPLICATION_JSON)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForRoleAssignment")
    public void verifyRoleAssignment() {
        RoleAssignmentResponse roleAssignmentResponse = roleAssignmentApi
            .updateRoleAssignment(AUTH, S2S, X_CORRELATION_ID,
                                  RoleAssignmentRequest.roleAssignmentRequest().build()
            );

        Assert.notNull(roleAssignmentResponse, "Api is returning role assignment response");
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForRoleAssignmentActor")
    public void verifyRoleAssignmentActor() {
        RoleAssignmentServiceResponse roleAssignmentResponse = roleAssignmentApi
            .getRoleAssignments(AUTH, S2S, X_CORRELATION_ID, "1"
            );

        Assert.notNull(roleAssignmentResponse, "Api is returning role assignment response");
    }
}
