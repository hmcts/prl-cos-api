package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITH_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.FL403_EXTEND_AN_ORDER;
import static uk.gov.hmcts.reform.prl.models.FeeType.applicationToFeeMap;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationsFeeCalculator {
    private static final String ADDITIONAL_APPLICATION_FEES_TO_PAY = "additionalApplicationFeesToPay";
    public static final String FL403_APPLICATION_TO_VARY_DISCHARGE_OR_EXTEND_AN_ORDER = "FL403_APPLICATION_TO_VARY_DISCHARGE_OR_EXTEND_AN_ORDER";
    public static final String C2_ALREADY_PRESENT_FOR_RESPONDENT = "c2AlreadyPresentForRespondent";
    public static final String FL403_ALREADY_PRESENT_FOR_RESPONDENT = "fl403AlreadyPresentForRespondent";

    private final FeeService feeService;


    public Map<String, Object> calculateAdditionalApplicationsFee(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        try {
            final List<FeeType> feeTypes = getFeeTypes(caseData);
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

    public static Map<String, Boolean> checkForExistingApplicationTypes(CaseData caseData) {
        Map<String, Boolean> existingApplicationTypes = new HashMap<>();
        boolean c2ApplicationAlreadyPresentForRespondent = false;
        boolean fl403ApplicationAlreadyPresentForRespondent = false;
        if (CollectionUtils.isNotEmpty(caseData.getAdditionalApplicationsBundle())) {
            for (Element<AdditionalApplicationsBundle> additionalApplicationsBundle : caseData.getAdditionalApplicationsBundle()) {
                if (isNotEmpty(additionalApplicationsBundle.getValue().getC2DocumentBundle())
                    && PartyEnum.respondent.equals(additionalApplicationsBundle.getValue().getPartyType())) {
                    c2ApplicationAlreadyPresentForRespondent = true;
                }
                if (null != additionalApplicationsBundle.getValue().getOtherApplicationsBundle()
                    && AdditionalApplicationType.FL403_EXTEND_AN_ORDER.getDisplayValue().equals(
                    additionalApplicationsBundle.getValue().getOtherApplicationsBundle().getOtherApplicationReason())
                    && PartyEnum.respondent.equals(additionalApplicationsBundle.getValue().getPartyType())) {
                    fl403ApplicationAlreadyPresentForRespondent = true;
                }
            }
        }
        existingApplicationTypes.put(C2_ALREADY_PRESENT_FOR_RESPONDENT, c2ApplicationAlreadyPresentForRespondent);
        existingApplicationTypes.put(FL403_ALREADY_PRESENT_FOR_RESPONDENT, fl403ApplicationAlreadyPresentForRespondent);
        return existingApplicationTypes;
    }

    public boolean onlyApplyingForAnAdjournment(C2DocumentBundle temporaryC2Bundle) {
        return isNotEmpty(temporaryC2Bundle.getC2ApplicationTypes())
            && temporaryC2Bundle.getC2ApplicationTypes().getValue().size() == 1
            && AdditionalApplicationType.C2_REQUESTING_ADJOURNMENT.getDisplayValue()
            .equals(temporaryC2Bundle.getC2ApplicationTypes().getValue().get(0).getLabel());
    }

    public List<FeeType> getFeeTypes(CaseData caseData) {
        List<FeeType> feeTypes = new ArrayList<>();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = caseData.getUploadAdditionalApplicationData();
        Map<String, Boolean> existingApplicationTypes = checkForExistingApplicationTypes(caseData);
        boolean fl403ApplicationAlreadyPresentForRespondent = existingApplicationTypes.get(
            FL403_ALREADY_PRESENT_FOR_RESPONDENT);
        boolean c2ApplicationAlreadyPresentForRespondent = existingApplicationTypes.get(
            C2_ALREADY_PRESENT_FOR_RESPONDENT);
        boolean applyOrderWithoutGivingNoticeToRespondent = isNotEmpty(caseData.getOrderWithoutGivingNoticeToRespondent())
            && YesOrNo.Yes.equals(caseData.getOrderWithoutGivingNoticeToRespondent().getOrderWithoutGivingNotice()) ? true : false;

        boolean skipC2PaymentForDaApplicant = DA_APPLICANT.equals(uploadAdditionalApplicationData.getRepresentedPartyType());
        boolean skipC2PaymentForDaRespondent = DA_RESPONDENT.equals(uploadAdditionalApplicationData.getRepresentedPartyType())
            && !c2ApplicationAlreadyPresentForRespondent && applyOrderWithoutGivingNoticeToRespondent;
        log.info("skipPaymentForDaRespondent => " + skipC2PaymentForDaRespondent);
        log.info("skipPaymentForDaApplicant => " + skipC2PaymentForDaApplicant);

        if (isNotEmpty(uploadAdditionalApplicationData)) {
            if (isNotEmpty(uploadAdditionalApplicationData.getTypeOfC2Application())
                && !skipC2PaymentForDaApplicant & !skipC2PaymentForDaRespondent) {
                boolean skipC2PaymentsBasedOnHearingDate = shouldSkipPayments(uploadAdditionalApplicationData);

                feeTypes.addAll(getC2ApplicationsFeeTypes(
                    uploadAdditionalApplicationData,
                    skipC2PaymentsBasedOnHearingDate
                ));
            }
            if (isNotEmpty(uploadAdditionalApplicationData.getTemporaryOtherApplicationsBundle())) {
                String otherApplicationType = getOtherApplicationType(uploadAdditionalApplicationData);
                fromApplicationType(otherApplicationType, CaseUtils.getCaseTypeOfApplication(caseData)).ifPresent(
                    feeTypes::add);
                if (fl403ApplicationAlreadyPresentForRespondent
                    && AdditionalApplicationType.FL403_EXTEND_AN_ORDER.getDisplayValue()
                    .equalsIgnoreCase(otherApplicationType)
                    && DA_RESPONDENT.equals(uploadAdditionalApplicationData.getRepresentedPartyType())) {
                    feeTypes.add(FL403_EXTEND_AN_ORDER);
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
            log.info("selectedHearingElement ==>" + selectedHearingElement.getLabel());
            if (isNotEmpty(selectedHearingElement)
                && StringUtils.isNotEmpty(selectedHearingElement.getLabel())
                && selectedHearingElement.getLabel().contains(HYPHEN_SEPARATOR)) {
                String selectedHearingDate = selectedHearingElement.getLabel().split(HYPHEN_SEPARATOR)[1];
                log.info("selectedHearingDate ==>" + selectedHearingDate);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDateTime selectedHearingLocalDateTime = LocalDate.parse(
                    selectedHearingDate,
                    formatter
                ).atStartOfDay();
                log.info("selectedHearingLocalDateTime ==>" + selectedHearingLocalDateTime);
                log.info("Duration ==>" + Duration.between(LocalDateTime.now(), selectedHearingLocalDateTime).toDays());
                skipPayments = (Duration.between(LocalDateTime.now(), selectedHearingLocalDateTime).toDays() >= 14L)
                    && onlyApplyingForAnAdjournment(temporaryC2Bundle);
            }
        }
        log.info("shouldSkipPayments ==>" + skipPayments);
        return skipPayments;
    }

    private static String getOtherApplicationType(UploadAdditionalApplicationData uploadAdditionalApplicationData) {
        String otherApplicationType;
        OtherApplicationsBundle applicationsBundle = uploadAdditionalApplicationData.getTemporaryOtherApplicationsBundle();
        if (isNotEmpty(applicationsBundle.getOtherApplicationTypes())
            && isNotEmpty(applicationsBundle.getOtherApplicationTypes().getValue())) {
            otherApplicationType = applicationsBundle.getOtherApplicationTypes().getValue().getLabel();
        } else {
            otherApplicationType = EMPTY_SPACE_STRING;
        }
        return otherApplicationType;
    }

    private List<FeeType> getC2ApplicationsFeeTypes(UploadAdditionalApplicationData uploadAdditionalApplicationData,
                                                    boolean skipPaymentsBasedOnHearingDate) {
        log.info("inside getC2ApplicationsFeeTypes");
        List<FeeType> feeTypes = new ArrayList<>();
        fromC2ApplicationType(
            uploadAdditionalApplicationData.getTypeOfC2Application(),
            skipPaymentsBasedOnHearingDate).ifPresent(feeTypes::add);
        log.info("return getC2ApplicationsFeeTypes feeTypes " + feeTypes);
        return feeTypes;
    }

    private static Optional<FeeType> fromC2ApplicationType(C2ApplicationTypeEnum c2ApplicationType,
                                                           boolean skipPaymentsBasedOnHearingDate) {
        log.info("c2ApplicationType ==> " + c2ApplicationType);
        if (c2ApplicationType == C2ApplicationTypeEnum.applicationWithNotice) {
            return Optional.of(C2_WITH_NOTICE);
        } else if (c2ApplicationType == C2ApplicationTypeEnum.applicationWithoutNotice
            && !skipPaymentsBasedOnHearingDate) {
            return Optional.of(C2_WITHOUT_NOTICE);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<FeeType> fromApplicationType(String applicationType, String caseTypeOfApplication) {
        if (applicationToFeeMap.containsKey(applicationType)) {
            return Optional.of(applicationToFeeMap.get(applicationType));
        } else if (AdditionalApplicationType.N161_APPELLANT_NOTICE.getDisplayValue().equalsIgnoreCase(applicationType)) {
            return PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseTypeOfApplication)
                ? Optional.of(FeeType.N161_APPELLANT_NOTICE_CA) : Optional.of(FeeType.N161_APPELLANT_NOTICE_DA);
        } else if (AdditionalApplicationType.D89_BAILIFF.getDisplayValue().equalsIgnoreCase(applicationType)) {
            return PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseTypeOfApplication)
                ? Optional.of(FeeType.D89_BAILIFF_CA) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

}
