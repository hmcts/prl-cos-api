package uk.gov.hmcts.reform.prl.controllers;

import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;

@SpringBootTest(classes = {Application.class, CallbackController.class})
public class CallbackControllerTest extends IntegrationTest {

}
