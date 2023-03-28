package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseWithdrawnRequestService {

    private final ObjectMapper objectMapper;
    public static final String APPLICATION_WITHDRAWN_SUCCESS_LABEL = "# Application withdrawn";
    public static final String APPLICATION_WITHDRAWN_STATUS_LABEL = "### What happens next \n\n This case will now display as “withdrawn” in "
        + "your case list.";

    public static final String APPLICATION_WITHDRAWN_REQUEST_LABEL = "# Requested Application Withdrawal";
    public static final String APPLICATION_WITHDRAWN_REQUEST_STATUS_LABEL = "### What happens next \n\n The court will consider your "
        + "withdrawal request.";

    public static final String APPLICATION_WITHDRAWN_CANCEL_REQUEST_LABEL = "# Application withdrawn cancelled";

    public SubmittedCallbackResponse caseWithdrawnRequestSubmitted(CallbackRequest callbackRequest) {

        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        WithdrawApplication withDrawApplicationData = caseData.getWithDrawApplicationData();
        Optional<YesOrNo> withdrawApplication = ofNullable(withDrawApplicationData.getWithDrawApplication());
        String caseWithdrawnConfirmationHeader;
        String caseWithdrawnConfirmationBodyPrefix;
        if ((withdrawApplication.isPresent() && Yes.equals(withdrawApplication.get()))) {
            if (State.CASE_ISSUE.equals(caseData.getState())
                || State.AWAITING_RESUBMISSION_TO_HMCTS.equals(caseData.getState()) || State.GATE_KEEPING.equals(caseData.getState())) {
                caseWithdrawnConfirmationHeader = APPLICATION_WITHDRAWN_REQUEST_LABEL;
                caseWithdrawnConfirmationBodyPrefix = APPLICATION_WITHDRAWN_REQUEST_STATUS_LABEL;
            } else {
                caseWithdrawnConfirmationHeader = APPLICATION_WITHDRAWN_SUCCESS_LABEL;
                caseWithdrawnConfirmationBodyPrefix = APPLICATION_WITHDRAWN_STATUS_LABEL;
            }
        } else {
            caseWithdrawnConfirmationHeader = APPLICATION_WITHDRAWN_CANCEL_REQUEST_LABEL;
            caseWithdrawnConfirmationBodyPrefix = " \n\n ";
        }

        return SubmittedCallbackResponse.builder().confirmationHeader(
            caseWithdrawnConfirmationHeader).confirmationBody(
            caseWithdrawnConfirmationBodyPrefix
        ).build();
    }
}
