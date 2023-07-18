package uk.gov.hmcts.reform.prl.services.restrictedcaseaccess;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static org.springframework.http.ResponseEntity.ok;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RestrictedCaseAccessService {

    public static final String SERVICE_REQUEST = "/work/my-work/list";
    public static final String CONFIRMATION_HEADER = "# Case marked as restricted";
    public static final String CONFIRMATION_SUBTEXT = "\n\n ## Only those with allocated roles on this case can access it";
    public static final String CONFIRMATION_BODY = "</br> You can return to " + "<a href=\"" + SERVICE_REQUEST + "\">My Work</a>" + ".";

    public ResponseEntity<SubmittedCallbackResponse> restrictedCaseConfirmation() {
        return ok(SubmittedCallbackResponse.builder().confirmationHeader(
            CONFIRMATION_HEADER + CONFIRMATION_SUBTEXT)
                      .confirmationBody(CONFIRMATION_BODY)
                      .build());
    }
}

