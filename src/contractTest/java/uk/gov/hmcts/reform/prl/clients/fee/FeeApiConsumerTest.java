package uk.gov.hmcts.reform.prl.clients.fee;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.prl.clients.FeesRegisterApi;
import uk.gov.hmcts.reform.prl.clients.idam.IdamApiConsumerApplication;
import uk.gov.hmcts.reform.prl.models.FeeResponse;

import java.math.BigDecimal;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "feeRegister_lookUp", port = "8881")
@ContextConfiguration(
    classes = {FeesRegisterApiConsumerApplication.class, IdamApiConsumerApplication.class}
)
@TestPropertySource(
    properties = {"bundle.api.url=http://localhost:8899","idam.api.url=localhost:5000","commonData.api.url=localhost:5000",
        "fis_hearing.api.url=localhost:5000",
        "refdata.api.url=",
        "courtfinder.api.url=",
        "prl-dgs-api.url=",
        "fees-register.api.url=http://localhost:8881",
        "fis_hearing.api.url=",
        "judicialUsers.api.url=",
        "locationfinder.api.url=",
        "rd_professional.api.url=",
        "payments.api.url=",
        "pba.validation.service.api.baseurl=",
        "staffDetails.api.url=",
        "amRoleAssignment.api.url=",
        "core_case_data.api.url="
    }
)

@PactFolder("pacts")
public class FeeApiConsumerTest {

    @Autowired
    FeesRegisterApi feesRegisterApi;

    @Autowired
    IdamApi idamApi;

    @Pact(provider = "feeRegister_lookUp", consumer = "prl_cos")
    private RequestResponsePact generateFeeWithHearingPact(PactDslWithProvider builder) {
        return getRequestResponsePact(builder, "ChildArrangement", "FEE0336",
                                      "Section 8 orders (section 10(1) or (2))", BigDecimal.valueOf(232.00)
        );
    }

    private RequestResponsePact getRequestResponsePact(PactDslWithProvider builder, String keyword, String code,
                                                       String description, BigDecimal feeAmount) {
        return builder
            .given("Fees exist for PRL")
            .uponReceiving("A request for PRL Fees")
            .path("/fees-register/fees/lookup")
            .method("GET")
            .matchQuery("channel", "default", "default")
            .matchQuery("event", "miscellaneous", "miscellaneous")
            .matchQuery("jurisdiction1", "family", "family")
            .matchQuery("jurisdiction2", "family court", "family court")
            .matchQuery("keyword", keyword, keyword)
            .matchQuery("service", "private law", "private law")
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildFeesResponseDsl(code, description, feeAmount))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private PactDslJsonBody buildFeesResponseDsl(String code, String description, BigDecimal feeAmount) {
        return new PactDslJsonBody()
            .stringType("code", code)
            .stringType("description", description)
            .numberType("version", 2)
            .decimalType("fee_amount", feeAmount);
    }

    @Test
    @PactTestFor(pactMethod = "generateFeeWithHearingPact")
    public void verifyFeesWithHearingPact() {
        FeeResponse feeResponse = feesRegisterApi.findFee("default", "miscellaneous", "family",
                                                          "family court", "ChildArrangement",
                                                          "private law");
        Assertions.assertEquals("FEE0336", feeResponse.getCode());
    }
}
