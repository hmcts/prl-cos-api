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
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DaApplicantOtherApplicationType;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CA_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CURRENCY_SIGN_POUND;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;
import static uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2AdditionalOrdersRequestedCa.REQUESTING_ADJOURNMENT;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITH_NOTICE;
import static uk.gov.hmcts.reform.prl.models.FeeType.FL403_EXTEND_AN_ORDER;
import static uk.gov.hmcts.reform.prl.models.FeeType.applicationToFeeMap;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationsFeeCalculator {
    private static final String ADDITIONAL_APPLICATION_FEES_TO_PAY = "additionalApplicationFeesToPay";
    public static final String C2_ALREADY_PRESENT_FOR_RESPONDENT = "c2AlreadyPresentForRespondent";
    public static final String FL403_ALREADY_PRESENT_FOR_RESPONDENT = "fl403AlreadyPresentForRespondent";
    public static final String N161_APPELLANT_NOTICE = "N161_APPELLANT_NOTICE";
    public static final String D89_COURT_BAILIFF = "D89_COURT_BAILIFF";
    public static final String C79_CHILD_ORDER = "C79_CHILD_ORDER";
    public static final String FC600_COMMITTAL_APPLICATION = "FC600_COMMITTAL_APPLICATION";
    public static final String FP25_WITNESS_SUMMONS = "FP25_WITNESS_SUMMONS";

    private final FeeService feeService;

    public Map<String, Object> calculateAdditionalApplicationsFee(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        try {
            final List<FeeType> feeTypes = getFeeTypes(caseData);
            FeeResponse feeResponse = feeService.getFeesDataForAdditionalApplications(feeTypes);
            if (null != feeResponse && BigDecimal.ZERO.compareTo(feeResponse.getAmount()) != 0) {
                data.put(ADDITIONAL_APPLICATION_FEES_TO_PAY, CURRENCY_SIGN_POUND + feeResponse.getAmount());
            }
        } catch (Exception e) {
            log.error("Case id {} ", caseData.getId(), e);
        }
        return data;
    }

    private static Map<String, Boolean> checkForExistingApplicationTypes(CaseData caseData) {
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
                    && OtherApplicationType.FL403_EXTEND_AN_ORDER.equals(
                    additionalApplicationsBundle.getValue().getOtherApplicationsBundle().getApplicationType())
                    && PartyEnum.respondent.equals(additionalApplicationsBundle.getValue().getPartyType())) {
                    fl403ApplicationAlreadyPresentForRespondent = true;
                }
            }
        }
        existingApplicationTypes.put(C2_ALREADY_PRESENT_FOR_RESPONDENT, c2ApplicationAlreadyPresentForRespondent);
        existingApplicationTypes.put(FL403_ALREADY_PRESENT_FOR_RESPONDENT, fl403ApplicationAlreadyPresentForRespondent);
        return existingApplicationTypes;
    }

    private boolean onlyApplyingForAnAdjournment(C2DocumentBundle temporaryC2Bundle) {
        return ((temporaryC2Bundle.getCaReasonsForC2Application().size() == 1
            && temporaryC2Bundle.getCaReasonsForC2Application().contains(REQUESTING_ADJOURNMENT))
            || (temporaryC2Bundle.getDaReasonsForC2Application().size() == 1
            && temporaryC2Bundle.getDaReasonsForC2Application().contains(REQUESTING_ADJOURNMENT)));
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
            && YesOrNo.Yes.equals(caseData.getOrderWithoutGivingNoticeToRespondent().getOrderWithoutGivingNotice());

        boolean skipC2PaymentForDaApplicant = DA_APPLICANT.equals(uploadAdditionalApplicationData.getRepresentedPartyType());
        boolean skipC2PaymentForDaRespondent = DA_RESPONDENT.equals(uploadAdditionalApplicationData.getRepresentedPartyType())
            && !c2ApplicationAlreadyPresentForRespondent && applyOrderWithoutGivingNoticeToRespondent;
        if (isNotEmpty(uploadAdditionalApplicationData)) {
            if (isNotEmpty(uploadAdditionalApplicationData.getTypeOfC2Application())
                && !skipC2PaymentForDaApplicant && !skipC2PaymentForDaRespondent) {
                boolean skipC2PaymentsBasedOnHearingDate = shouldSkipPayments(uploadAdditionalApplicationData);
                feeTypes.addAll(getC2ApplicationsFeeTypes(
                    uploadAdditionalApplicationData,
                    skipC2PaymentsBasedOnHearingDate
                ));
            }
            if (isNotEmpty(uploadAdditionalApplicationData.getTemporaryOtherApplicationsBundle())) {
                String otherApplicationType = getOtherApplicationType(uploadAdditionalApplicationData);
                fromApplicationType(otherApplicationType, CaseUtils.getCaseTypeOfApplication(caseData),
                                    uploadAdditionalApplicationData.getRepresentedPartyType()).ifPresent(
                    feeTypes::add);
                if (fl403ApplicationAlreadyPresentForRespondent
                    && DaApplicantOtherApplicationType.FL403_EXTEND_AN_ORDER.getId().equalsIgnoreCase(otherApplicationType)
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
            if (isNotEmpty(selectedHearingElement)
                && StringUtils.isNotEmpty(selectedHearingElement.getLabel())
                && selectedHearingElement.getLabel().contains(HYPHEN_SEPARATOR)) {
                String selectedHearingDate = selectedHearingElement.getLabel().split(HYPHEN_SEPARATOR)[1];
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

    private List<FeeType> getC2ApplicationsFeeTypes(UploadAdditionalApplicationData uploadAdditionalApplicationData,
                                                    boolean skipPaymentsBasedOnHearingDate) {
        List<FeeType> feeTypes = new ArrayList<>();
        fromC2ApplicationType(
            uploadAdditionalApplicationData.getTypeOfC2Application(),
            skipPaymentsBasedOnHearingDate
        ).ifPresent(feeTypes::add);
        return feeTypes;
    }

    private static Optional<FeeType> fromC2ApplicationType(C2ApplicationTypeEnum c2ApplicationType, boolean skipPaymentsBasedOnHearingDate) {
        if (c2ApplicationType == C2ApplicationTypeEnum.applicationWithNotice) {
            return Optional.of(C2_WITH_NOTICE);
        } else if (c2ApplicationType == C2ApplicationTypeEnum.applicationWithoutNotice && !skipPaymentsBasedOnHearingDate) {
            return Optional.of(C2_WITHOUT_NOTICE);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<FeeType> fromApplicationType(String applicationType, String caseTypeOfApplication, String representedPartyType) {
        if (applicationToFeeMap.containsKey(applicationType)) {
            return Optional.of(applicationToFeeMap.get(applicationType));
        } else if (N161_APPELLANT_NOTICE.equalsIgnoreCase(applicationType)) {
            return PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseTypeOfApplication)
                ? Optional.of(FeeType.N161_APPELLANT_NOTICE_CA) : Optional.of(FeeType.N161_APPELLANT_NOTICE_DA);
        } else if (D89_COURT_BAILIFF.equalsIgnoreCase(applicationType)) {
            return PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseTypeOfApplication)
                ? Optional.of(FeeType.D89_BAILIFF_CA) : Optional.empty();
        } else if (C79_CHILD_ORDER.equalsIgnoreCase(applicationType)) {
            return CA_APPLICANT.equals(representedPartyType)
                ? Optional.of(FeeType.CHILD_ARRANGEMENTS_ORDER) : Optional.empty();
        } else if (FC600_COMMITTAL_APPLICATION.equalsIgnoreCase(applicationType)) {
            return getFC600FeeType(representedPartyType);
        } else if (FP25_WITNESS_SUMMONS.equalsIgnoreCase(applicationType)) {
            return getFP25FeeType(representedPartyType);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<FeeType> getFC600FeeType(String representedPartyType) {
        return CA_APPLICANT.equals(representedPartyType) || DA_APPLICANT.equals(representedPartyType)
            ? Optional.of(FeeType.FC600_COMMITTAL_APPLICATION) : Optional.empty();
    }

    private static Optional<FeeType> getFP25FeeType(String representedPartyType) {
        return !DA_APPLICANT.equals(representedPartyType)
            ? Optional.of(FeeType.FP25_WITNESS_SUMMONS) : Optional.empty();
    }

}
