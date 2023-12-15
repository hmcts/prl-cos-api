package uk.gov.hmcts.reform.prl.clients.roleassignment;

import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.clients.idam.IdamApiConsumerApplication;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "role_assignment", port = "8881")
@ContextConfiguration(
        classes = {AmRoleAssignmentApiApplication.class, IdamApiConsumerApplication.class}
)

@PactFolder("pacts")
public class AmRoleAssignmentApiTest {
}
