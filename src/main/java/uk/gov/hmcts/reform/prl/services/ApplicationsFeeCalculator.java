package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CURRENCY_SIGN_POUND;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITH_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.applicationToFeeMap;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationsFeeCalculator {
    private static final String ADDITIONAL_APPLICATION_FEES_TO_PAY = "additionalApplicationFeesToPay";

    private final FeeService feeService;


    public Map<String, Object> calculateAdditionalApplicationsFee(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        try {
            final List<FeeType> feeTypes = getFeeTypes(caseData.getUploadAdditionalApplicationData());
            log.info("feeTypes lookup {} ", feeTypes);
            FeeResponse feeResponse = feeService.getFeesDataForAdditionalApplications(feeTypes);
            if (null != feeResponse && BigDecimal.ZERO.compareTo(feeResponse.getAmount()) != 0) {
                log.info("calculated fees {} ", feeResponse.getAmount());
                data.put(ADDITIONAL_APPLICATION_FEES_TO_PAY, CURRENCY_SIGN_POUND + feeResponse.getAmount());
            }
        } catch (Exception e) {
            log.error("Case id {} ", caseData.getId(), e);
        }
        return data;
    }

    public List<FeeType> getFeeTypes(UploadAdditionalApplicationData uploadAdditionalApplicationData) {
        List<FeeType> feeTypes = new ArrayList<>();

        if (isNotEmpty(uploadAdditionalApplicationData)) {
            if (isNotEmpty(uploadAdditionalApplicationData.getTemporaryC2Document())) {
                feeTypes.addAll(getC2ApplicationsFeeTypes(uploadAdditionalApplicationData));
            }
            if (isNotEmpty(uploadAdditionalApplicationData.getTemporaryOtherApplicationsBundle())) {
                if (isNotEmpty(uploadAdditionalApplicationData.getTemporaryOtherApplicationsBundle().getCaApplicantApplicationType())
                    || isNotEmpty(uploadAdditionalApplicationData.getTemporaryOtherApplicationsBundle().getCaRespondentApplicationType())) {
                    feeTypes.addAll(getCaOtherApplicationsFeeTypes(uploadAdditionalApplicationData.getTemporaryOtherApplicationsBundle()));
                } else if (isNotEmpty(uploadAdditionalApplicationData.getTemporaryOtherApplicationsBundle().getDaApplicantApplicationType())
                    || isNotEmpty(uploadAdditionalApplicationData.getTemporaryOtherApplicationsBundle().getDaRespondentApplicationType())) {
                    feeTypes.addAll(getDaOtherApplicationsFeeTypes(uploadAdditionalApplicationData.getTemporaryOtherApplicationsBundle()));
                }
            }
        }

        return feeTypes;
    }

    private List<FeeType> getC2ApplicationsFeeTypes(UploadAdditionalApplicationData uploadAdditionalApplicationData) {
        log.info("inside getC2ApplicationsFeeTypes");
        List<FeeType> feeTypes = new ArrayList<>();
        feeTypes.add(fromC2ApplicationType(uploadAdditionalApplicationData.getTypeOfC2Application()));
        log.info("return getC2ApplicationsFeeTypes feeTypes " + feeTypes);
        return feeTypes;
    }

    private static FeeType fromC2ApplicationType(C2ApplicationTypeEnum c2ApplicationType) {
        if (c2ApplicationType == C2ApplicationTypeEnum.applicationWithNotice) {
            return C2_WITH_NOTICE;
        }
        return C2_WITHOUT_NOTICE;
    }

    private List<FeeType> getCaOtherApplicationsFeeTypes(OtherApplicationsBundle applicationsBundle) {
        List<FeeType> feeTypes = new ArrayList<>();
        log.info("inside getCaOtherApplicationsFeeTypes");
        if (isNotEmpty(applicationsBundle.getCaApplicantApplicationType())) {
            fromApplicationType(applicationsBundle.getCaApplicantApplicationType().getId()).ifPresent(feeTypes::add);
        } else if (isNotEmpty(applicationsBundle.getCaRespondentApplicationType())) {
            fromApplicationType(applicationsBundle.getCaRespondentApplicationType().getId()).ifPresent(feeTypes::add);
        }
        log.info("return getCaOtherApplicationsFeeTypes feeTypes " + feeTypes);
        return feeTypes;
    }

    private List<FeeType> getDaOtherApplicationsFeeTypes(OtherApplicationsBundle applicationsBundle) {
        List<FeeType> feeTypes = new ArrayList<>();
        log.info("inside getDaOtherApplicationsFeeTypes");
        if (isNotEmpty(applicationsBundle.getDaApplicantApplicationType())) {
            fromApplicationType(applicationsBundle.getDaApplicantApplicationType().getId()).ifPresent(feeTypes::add);
        } else if (isNotEmpty(applicationsBundle.getDaRespondentApplicationType())) {
            fromApplicationType(applicationsBundle.getDaRespondentApplicationType().getId()).ifPresent(feeTypes::add);
        }
        log.info("return getDaOtherApplicationsFeeTypes feeTypes " + feeTypes);
        return feeTypes;
    }

    private static Optional<FeeType> fromApplicationType(String applicationType) {
        if (!applicationToFeeMap.containsKey(applicationType)) {
            return Optional.empty();
        }
        return Optional.of(applicationToFeeMap.get(applicationType));
    }

}
