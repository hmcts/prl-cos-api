package uk.gov.hmcts.reform.prl.services.restrictedcaseaccess;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static org.springframework.http.ResponseEntity.ok;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RestrictedCaseAccessServiceTest {

    @InjectMocks
    RestrictedCaseAccessService restrictedCaseAccessService;

    private static final String CONFIRMATION_HEADER = "### Case marked as restricted";
    private static final String CONFIRMATION_SUBTEXT = "\n\n Only those with allocated roles on this case can access it";
    private static final String CONFIRMATION_BODY = "\n\n You can return to";

    private ResponseEntity submittedResponseEntity;

    @Test
    public void testRestrictedCaseConfirmation() {
        submittedResponseEntity = ok(SubmittedCallbackResponse.builder().confirmationHeader(
                CONFIRMATION_HEADER + CONFIRMATION_SUBTEXT)
            .confirmationBody(CONFIRMATION_BODY)
            .build());

        Assert.assertEquals(submittedResponseEntity, restrictedCaseAccessService.restrictedCaseConfirmation());
    }
}
