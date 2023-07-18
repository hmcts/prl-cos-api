package uk.gov.hmcts.reform.prl.services.restrictedcaseaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static org.springframework.http.ResponseEntity.ok;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RestrictedCaseAccessService {

    public static final String SERVICE_REQUEST = "/work/my-work/list";
    public static final String XUI_CASE_URL = "/cases/case-details/";
    public static final String SUMMARY_TAB = "#Summary";
    public static final String RESTRICTED_CONFIRMATION_HEADER = "# Case marked as restricted";
    public static final String RESTRICTED_CONFIRMATION_SUBTEXT = "\n\n ## Only those with allocated roles on this case can access it";
    public static final String RESTRICTED_CONFIRMATION_BODY = "</br> You can return to " + "<a href=\"" + SERVICE_REQUEST + "\">My Work</a>" + ".";
    public static final String PUBLIC_CONFIRMATION_HEADER = "# Case marked as public";
    public static final String PUBLIC_CONFIRMATION_SUBTEXT = "\n\n ## This case will now appear in search results "
        + "and any previous access restrictions will be removed";

    public ResponseEntity<SubmittedCallbackResponse> restrictedCaseConfirmation() {
        return ok(SubmittedCallbackResponse.builder().confirmationHeader(
            RESTRICTED_CONFIRMATION_HEADER + RESTRICTED_CONFIRMATION_SUBTEXT)
                      .confirmationBody(RESTRICTED_CONFIRMATION_BODY)
                      .build());
    }

    public ResponseEntity<SubmittedCallbackResponse> publicCaseConfirmation(CallbackRequest callbackRequest) {
        String serviceRequestUrl = XUI_CASE_URL + callbackRequest.getCaseDetails().getId() + SUMMARY_TAB;
        String publicConfirmationBody = "</br>" + "<a href=\"" + serviceRequestUrl + "\">Return to the case</a>" + " or " + "<a href=\"" + SERVICE_REQUEST + "\">see roles and access</a>" + ".";
        return ok(SubmittedCallbackResponse.builder().confirmationHeader(
                PUBLIC_CONFIRMATION_HEADER + PUBLIC_CONFIRMATION_SUBTEXT)
                      .confirmationBody(publicConfirmationBody)
                      .build());
    }
}

