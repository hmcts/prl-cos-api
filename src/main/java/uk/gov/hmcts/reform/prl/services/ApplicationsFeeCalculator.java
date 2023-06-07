package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CURRENCY_SIGN_POUND;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITH_NOTICE;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationsFeeCalculator {
    private static final String ADDITIONAL_APPLICATION_FEES_TO_PAY = "additionalApplicationFeesToPay";

    private final FeeService feeService;


    public Map<String, Object> calculateAdditionalApplicationsFee(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        try {
            FeeType feeType = getFeeTypes(caseData.getUploadAdditionalApplicationData());
            log.info("feeTypes lookup {} ", feeType);
            FeeResponse feeResponse = feeService.fetchFeeDetails(feeType);
            log.info("calculated fees {} ", feeResponse.getAmount());
            data.put(ADDITIONAL_APPLICATION_FEES_TO_PAY, CURRENCY_SIGN_POUND + feeResponse.getAmount());
        } catch (Exception e) {
            log.error("Case id {} ", caseData.getId(), e);
        }
        return data;
    }

    public FeeType getFeeTypes(UploadAdditionalApplicationData uploadAdditionalApplicationData) {
        if (isNotEmpty(uploadAdditionalApplicationData)
            && CollectionUtils.isNotEmpty(uploadAdditionalApplicationData.getAdditionalApplicationsApplyingFor())
            && uploadAdditionalApplicationData.getAdditionalApplicationsApplyingFor().contains(
            AdditionalApplicationTypeEnum.c2Order)) {
            return getC2ApplicationsFeeTypes(uploadAdditionalApplicationData);
        }

        return null;
    }

    private FeeType getC2ApplicationsFeeTypes(UploadAdditionalApplicationData uploadAdditionalApplicationData) {
        if (uploadAdditionalApplicationData.getTypeOfC2Application() == C2ApplicationTypeEnum.applicationWithNotice) {
            return C2_WITH_NOTICE;
        }
        return C2_WITHOUT_NOTICE;
    }
}
