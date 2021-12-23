package uk.gov.hmcts.reform.prl.controllers;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.util.CosApiClient;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(classes = {Application.class, CallbackController.class})
public class CallbackControllerTest extends IntegrationTest {

    @Autowired
    private CosApiClient cosApiClient;

    @DisplayName("temporary test to verify gov UK notifications integration")
    @Test
    public void sendEmail() {
        cosApiClient.sendEmail(CallbackRequest.builder()
                                   .caseDetails(CaseDetails.builder().build())
                                   .build());
    }
}
