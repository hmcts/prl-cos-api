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
    //public static final String HWF_PATTERN = "^\\w{3}-\\w{3}-\\w{3}$|^[A-Za-z]{2}\\d{2}-\\d{6}$";

    //TODO: Validation is suppressed for the time being as system is not ready for HWF
    /** public boolean validateHelpWithFeesNumber(CallbackRequest callbackRequest) {
        boolean invalidHwfNumber = false;
        Pattern pattern = Pattern.compile(HWF_PATTERN);
        Matcher matcher = null;
        if (Event.SUBMIT_AND_PAY.getId().equalsIgnoreCase(callbackRequest.getEventId())
            && YesOrNo.Yes.equals(callbackRequest.getCaseDetails().getCaseData().getHelpWithFees())) {

            matcher = pattern.matcher(callbackRequest.getCaseDetails().getCaseData().getHelpWithFeesNumber());
        } else if (Event.UPLOAD_ADDITIONAL_APPLICATIONS.getId().equalsIgnoreCase(callbackRequest.getEventId())
            && null != callbackRequest.getCaseDetails().getCaseData().getUploadAdditionalApplicationData()
            && YesOrNo.Yes.equals(callbackRequest.getCaseDetails().getCaseData().getUploadAdditionalApplicationData()
                                      .getAdditionalApplicationsHelpWithFees())) {
            matcher = pattern.matcher(callbackRequest.getCaseDetails().getCaseData()
                                          .getUploadAdditionalApplicationData()
                                          .getAdditionalApplicationsHelpWithFeesNumber());
        }


        if (null != matcher && !matcher.find()) {
            invalidHwfNumber = true;
        }
        return invalidHwfNumber;
    } **/

    public List<String> validateSuppressedHelpWithFeesCheck(CallbackRequest callbackRequest) {
        List<String> errorList = new ArrayList<>();
        if (Event.SUBMIT_AND_PAY.getId().equalsIgnoreCase(callbackRequest.getEventId())
            && YesOrNo.Yes.equals(callbackRequest.getCaseDetails().getCaseData().getHelpWithFees())) {
            errorList.add(HWF_SUPPRESSION_ERROR_MESSAGE);
        } else if (Event.UPLOAD_ADDITIONAL_APPLICATIONS.getId().equalsIgnoreCase(callbackRequest.getEventId())
            && null != callbackRequest.getCaseDetails().getCaseData().getUploadAdditionalApplicationData()
            && YesOrNo.Yes.equals(callbackRequest.getCaseDetails().getCaseData().getUploadAdditionalApplicationData()
                                      .getAdditionalApplicationsHelpWithFees())) {
            errorList.add(HWF_SUPPRESSION_ERROR_MESSAGE);
        }
        return errorList;
    }
}
