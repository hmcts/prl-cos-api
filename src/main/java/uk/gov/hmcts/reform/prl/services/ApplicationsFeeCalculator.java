package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.FeesData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CURRENCY_SIGN_POUND;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITH_NOTICE;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationsFeeCalculator {
    private static final String AMOUNT_TO_PAY = "additionalApplicationFeesToPay";

    private final FeeService feeService;


    public Map<String, Object> calculateAdditionalApplicationsFee(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        try {
            final List<FeeType> feeTypes = getFeeTypes(caseData.getUploadAdditionalApplicationData());
            log.info("feeTypes lookup {} ", feeTypes);
            final FeesData feesData = feeService.getFeesDataForAdditionalApplications(feeTypes);
            log.info("calculated fees {} ", feesData.getTotalAmount());
            data.put(AMOUNT_TO_PAY, CURRENCY_SIGN_POUND +String.valueOf(feesData.getTotalAmount()));
        } catch (Exception e) {
            log.error("Case id {} ", caseData.getId(), e);
        }
        return data;
    }

    private List<FeeType> getFeeTypes(UploadAdditionalApplicationData uploadAdditionalApplicationData) {
        List<FeeType> feeTypes = new ArrayList<>();

        if (isNotEmpty(uploadAdditionalApplicationData)
            && CollectionUtils.isNotEmpty(uploadAdditionalApplicationData.getAdditionalApplicationsApplyingFor())
            && uploadAdditionalApplicationData.getAdditionalApplicationsApplyingFor().contains(
            AdditionalApplicationTypeEnum.c2Order)) {
            feeTypes.addAll(getC2ApplicationsFeeTypes(uploadAdditionalApplicationData));
        }

        return feeTypes;
    }

    private List<FeeType> getC2ApplicationsFeeTypes(UploadAdditionalApplicationData uploadAdditionalApplicationData) {
        List<FeeType> feeTypes = new ArrayList<>();

        feeTypes.add(fromC2ApplicationType(uploadAdditionalApplicationData.getTypeOfC2Application()));

        return feeTypes;
    }

    public static FeeType fromC2ApplicationType(C2ApplicationTypeEnum c2ApplicationType) {
        if (c2ApplicationType == C2ApplicationTypeEnum.applicationWithNotice) {
            return C2_WITH_NOTICE;
        }
        return C2_WITHOUT_NOTICE;
    }
}
