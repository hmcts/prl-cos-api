package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.OtherApplicationType;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CURRENCY_SIGN_POUND;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_REPRESENTING_DARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2AdditionalOrdersRequested.REQUESTING_ADJOURNMENT;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITH_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.applicationToFeeMap;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationsFeeCalculator {
    private static final String ADDITIONAL_APPLICATION_FEES_TO_PAY = "additionalApplicationFeesToPay";
    public static final String FL403_APPLICATION_TO_VARY_DISCHARGE_OR_EXTEND_AN_ORDER = "FL403_APPLICATION_TO_VARY_DISCHARGE_OR_EXTEND_AN_ORDER";

    private final FeeService feeService;


    public Map<String, Object> calculateAdditionalApplicationsFee(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        try {
            boolean fl403ApplicationAlreadyPresent = isFl403ApplicationAlreadyPresent(caseData);
            final List<FeeType> feeTypes = getFeeTypes(caseData.getUploadAdditionalApplicationData(), fl403ApplicationAlreadyPresent);
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

    public static boolean isFl403ApplicationAlreadyPresent(CaseData caseData) {
        boolean fl403ApplicationAlreadyPresent = false;
        if (CollectionUtils.isNotEmpty(caseData.getAdditionalApplicationsBundle())) {
            for (Element<AdditionalApplicationsBundle> additionalApplicationsBundle : caseData.getAdditionalApplicationsBundle()) {
                if (null != additionalApplicationsBundle.getValue().getOtherApplicationsBundle()
                    && OtherApplicationType.FL403_APPLICATION_TO_VARY_DISCHARGE_OR_EXTEND_AN_ORDER.equals(
                    additionalApplicationsBundle.getValue().getOtherApplicationsBundle().getApplicationType())) {
                    fl403ApplicationAlreadyPresent = true;
                    break;
                }
            }
        }
        return fl403ApplicationAlreadyPresent;
    }

    public boolean onlyApplyingForAnAdjournment(C2DocumentBundle temporaryC2Bundle) {
        return temporaryC2Bundle.getReasonsForC2Application().size() == 1
            && temporaryC2Bundle.getReasonsForC2Application().contains(REQUESTING_ADJOURNMENT);
    }

    public List<FeeType> getFeeTypes(UploadAdditionalApplicationData uploadAdditionalApplicationData, boolean fl403ApplicationAlreadyPresent) {
        List<FeeType> feeTypes = new ArrayList<>();

        if (isNotEmpty(uploadAdditionalApplicationData)) {
            if (isNotEmpty(uploadAdditionalApplicationData.getTemporaryC2Document()) && isNotEmpty(
                uploadAdditionalApplicationData.getTypeOfC2Application())) {
                boolean skipPayments = shouldSkipPayments(uploadAdditionalApplicationData);
                feeTypes.addAll(getC2ApplicationsFeeTypes(uploadAdditionalApplicationData, skipPayments));
            }
            if (isNotEmpty(uploadAdditionalApplicationData.getTemporaryOtherApplicationsBundle())) {
                String otherApplicationType = getOtherApplicationType(uploadAdditionalApplicationData);
                fromApplicationType(otherApplicationType).ifPresent(feeTypes::add);
                if (fl403ApplicationAlreadyPresent
                    && FL403_APPLICATION_TO_VARY_DISCHARGE_OR_EXTEND_AN_ORDER.equalsIgnoreCase(otherApplicationType)
                    && SOLICITOR_REPRESENTING_DARESPONDENT.equals(uploadAdditionalApplicationData.getSolicitorRepresentingPartyType())) {
                    feeTypes.add(C2_WITH_NOTICE);
                }
            }
        }

        return feeTypes;
    }

    private boolean shouldSkipPayments(UploadAdditionalApplicationData uploadAdditionalApplicationData) {
        C2DocumentBundle temporaryC2Bundle = uploadAdditionalApplicationData.getTemporaryC2Document();
        boolean skipPayments = false;
        if (null != temporaryC2Bundle.getHearingList()) {
            DynamicListElement selectedHearingElement = temporaryC2Bundle.getHearingList().getValue();
            if (isNotEmpty(selectedHearingElement)
                && StringUtils.isNotEmpty(selectedHearingElement.getCode())
                && selectedHearingElement.getCode().contains(HYPHEN_SEPARATOR)) {
                String selectedHearingDate = selectedHearingElement.getCode().split(HYPHEN_SEPARATOR)[1];
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDateTime selectedHearingLocalDateTime = LocalDate.parse(
                    selectedHearingDate,
                    formatter
                ).atStartOfDay();
                skipPayments = (Duration.between(LocalDateTime.now(), selectedHearingLocalDateTime).toDays() >= 14L)
                    && onlyApplyingForAnAdjournment(temporaryC2Bundle);
            }
        }
        return skipPayments;
    }

    private static String getOtherApplicationType(UploadAdditionalApplicationData uploadAdditionalApplicationData) {
        String otherApplicationType;
        OtherApplicationsBundle applicationsBundle = uploadAdditionalApplicationData.getTemporaryOtherApplicationsBundle();
        if (isNotEmpty(applicationsBundle.getCaApplicantApplicationType())) {
            otherApplicationType = applicationsBundle.getCaApplicantApplicationType().getId();
        } else if (isNotEmpty(applicationsBundle.getCaRespondentApplicationType())) {
            otherApplicationType = applicationsBundle.getCaRespondentApplicationType().getId();
        } else if (isNotEmpty(applicationsBundle.getDaApplicantApplicationType())) {
            otherApplicationType = applicationsBundle.getDaApplicantApplicationType().getId();
        } else if (isNotEmpty(applicationsBundle.getDaRespondentApplicationType())) {
            otherApplicationType = applicationsBundle.getDaRespondentApplicationType().getId();
        } else {
            otherApplicationType = EMPTY_SPACE_STRING;
        }
        return otherApplicationType;
    }

    private List<FeeType> getC2ApplicationsFeeTypes(UploadAdditionalApplicationData uploadAdditionalApplicationData, boolean skipPayment) {
        log.info("inside getC2ApplicationsFeeTypes");
        List<FeeType> feeTypes = new ArrayList<>();
        fromC2ApplicationType(uploadAdditionalApplicationData.getTypeOfC2Application(), skipPayment).ifPresent(feeTypes::add);
        log.info("return getC2ApplicationsFeeTypes feeTypes " + feeTypes);
        return feeTypes;
    }

    private static Optional<FeeType> fromC2ApplicationType(C2ApplicationTypeEnum c2ApplicationType, boolean skipPayment) {
        if (c2ApplicationType == C2ApplicationTypeEnum.applicationWithNotice) {
            return Optional.of(C2_WITH_NOTICE);
        } else if (c2ApplicationType == C2ApplicationTypeEnum.applicationWithoutNotice && !skipPayment) {
            return Optional.of(C2_WITHOUT_NOTICE);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<FeeType> fromApplicationType(String applicationType) {
        if (!applicationToFeeMap.containsKey(applicationType)) {
            return Optional.empty();
        }
        return Optional.of(applicationToFeeMap.get(applicationType));
    }

}
