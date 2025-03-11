package uk.gov.hmcts.reform.prl.clients.cafcass.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.spring6.Spring6MockMvcTestTarget;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.controllers.cafcass.CafCassController;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.cafcass.CaseDataService;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Provider("prl_cafcass_search_cases")
@PactBroker(
    url = "${PACT_BROKER_URL:https://pact-broker.platform.hmcts.net}",
    consumerVersionSelectors = {
        @VersionSelector(tag = "${PACT_BRANCH_NAME:Dev}")
    },
    providerTags = "${pactbroker.providerTags:master}",
    enablePendingPacts = "${pactbroker.enablePending:true}"
)

public class CafcassControllerProviderTest {

    @MockBean
    CaseDataService caseDataService;

    @MockBean
    AuthorisationService authService;

    @MockBean
    EventService eventService;

    ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        Spring6MockMvcTestTarget testTarget = new Spring6MockMvcTestTarget();
        testTarget.setControllers(new CafCassController(objectMapper, eventService, caseDataService, authService));
        context.setTarget(testTarget);
    }

    @State({"Search cases with valid credentials"})
    public void toSetUpValidMicroservice() {
        CafCassResponse cafCassResponse;
        try {
            cafCassResponse = objectMapper.convertValue(ResourceLoader.loadJson("response/cafcass-search-response.json"), CafCassResponse.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(authService.authoriseUser(anyString())).thenReturn(Boolean.TRUE);
        when(authService.authoriseService(anyString())).thenReturn(Boolean.TRUE);
        try {
            when(caseDataService.getCaseData(anyString(), anyString(), anyString())).thenReturn(cafCassResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
