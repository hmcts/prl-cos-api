package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeAndPayServiceRequestService {
    public static final String HWF_SUPPRESSION_ERROR_MESSAGE
        = "Help with Fees is not yet available in Family Private Law digital service. Select 'No' to continue with your application";

    public List<String> validateSuppressedHelpWithFeesCheck(CallbackRequest callbackRequest) {
        List<String> errorList = new ArrayList<>();
        boolean hwfSelectedForSubmitAndPay = Event.SUBMIT_AND_PAY.getId().equalsIgnoreCase(callbackRequest.getEventId())
            && YesOrNo.Yes.equals(callbackRequest.getCaseDetails().getCaseData().getHelpWithFees());
        boolean hwfSelectedForAwP = Event.UPLOAD_ADDITIONAL_APPLICATIONS.getId().equalsIgnoreCase(callbackRequest.getEventId())
            && null != callbackRequest.getCaseDetails().getCaseData().getUploadAdditionalApplicationData()
            && YesOrNo.Yes.equals(callbackRequest.getCaseDetails().getCaseData().getUploadAdditionalApplicationData()
                                      .getAdditionalApplicationsHelpWithFees());
        if (hwfSelectedForSubmitAndPay || hwfSelectedForAwP) {
            errorList.add(HWF_SUPPRESSION_ERROR_MESSAGE);
        }
        return errorList;
    }
}
