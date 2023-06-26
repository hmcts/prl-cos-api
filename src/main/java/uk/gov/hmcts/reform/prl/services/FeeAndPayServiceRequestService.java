package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeAndPayServiceRequestService {
    public static final String HWF_PATTERN = "^\\w{3}-\\w{3}-\\w{3}$|^[A-Za-z]{2}\\d{2}-\\d{6}$";

    public boolean validateHelpWithFeesNumber(CallbackRequest callbackRequest) {
        boolean invalidHwfNumber = false;
        Pattern pattern = Pattern.compile(HWF_PATTERN);
        Matcher matcher = pattern.matcher(callbackRequest.getCaseDetails().getCaseData().getHelpWithFeesNumber());

        if (!matcher.find()) {
            invalidHwfNumber = true;
        }
        return invalidHwfNumber;
    }
}
