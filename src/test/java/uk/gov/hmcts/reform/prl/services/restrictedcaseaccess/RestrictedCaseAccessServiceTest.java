package uk.gov.hmcts.reform.prl.services.restrictedcaseaccess;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static org.springframework.http.ResponseEntity.ok;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RestrictedCaseAccessServiceTest {

    @InjectMocks
    RestrictedCaseAccessService restrictedCaseAccessService;

    public static final String SERVICE_REQUEST = "/work/my-work/list";
    public static final String XUI_CASE_URL = "/cases/case-details/";
    public static final String SUMMARY_TAB = "#Summary";
    public static final String RESTRICTED_CONFIRMATION_HEADER = "# Case marked as restricted";
    public static final String RESTRICTED_CONFIRMATION_SUBTEXT = "\n\n ## Only those with allocated roles on this case can access it";
    public static final String RESTRICTED_CONFIRMATION_BODY = "</br> You can return to " + "<a href=\"" + SERVICE_REQUEST + "\">My Work</a>" + ".";
    public static final String PUBLIC_CONFIRMATION_HEADER = "# Case marked as public";
    public static final String PUBLIC_CONFIRMATION_SUBTEXT = "\n\n ## This case will now appear in search results "
        + "and any previous access restrictions will be removed";

    private ResponseEntity submittedResponseEntity;

    @Test
    public void testRestrictedCaseConfirmation() {
        submittedResponseEntity = ok(SubmittedCallbackResponse.builder().confirmationHeader(
                RESTRICTED_CONFIRMATION_HEADER + RESTRICTED_CONFIRMATION_SUBTEXT)
            .confirmationBody(RESTRICTED_CONFIRMATION_BODY)
            .build());

        Assert.assertEquals(submittedResponseEntity, restrictedCaseAccessService.restrictedCaseConfirmation());
    }

    @Test
    public void testPublicCaseConfirmation() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(123L)
                             .build())
            .build();

        String serviceRequestUrl = XUI_CASE_URL + callbackRequest.getCaseDetails().getId() + SUMMARY_TAB;
        String publicConfirmationBody = "</br>" + "<a href=\"" + serviceRequestUrl + "\">Return to the case</a>" + " or " + "<a href=\"" + SERVICE_REQUEST + "\">see roles and access</a>" + ".";

        submittedResponseEntity = ok(SubmittedCallbackResponse.builder().confirmationHeader(
                PUBLIC_CONFIRMATION_HEADER + PUBLIC_CONFIRMATION_SUBTEXT)
                                         .confirmationBody(publicConfirmationBody)
                                         .build());

        Assert.assertEquals(submittedResponseEntity, restrictedCaseAccessService.publicCaseConfirmation(callbackRequest));
    }
}
