package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CA_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CA_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CURRENCY_SIGN_POUND;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DA_RESPONDENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;
import static uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2AdditionalOrdersRequested.REQUESTING_ADJOURNMENT;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITHOUT_NOTICE_AND_FP25;
import static uk.gov.hmcts.reform.prl.models.FeeType.C2_WITH_NOTICE_AND_FC600_FL403;
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
                    && OtherApplicationType.FL403_APPLICATION_TO_VARY_DISCHARGE_OR_EXTEND_AN_ORDER.equals(
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

    public boolean onlyApplyingForAnAdjournment(C2DocumentBundle temporaryC2Bundle) {
        return temporaryC2Bundle.getReasonsForC2Application().size() == 1
            && temporaryC2Bundle.getReasonsForC2Application().contains(REQUESTING_ADJOURNMENT);
    }

    public List<FeeType> getFeeTypes(CaseData caseData) {
        List<FeeType> feeTypes = new ArrayList<>();
        UploadAdditionalApplicationData uploadAdditionalApplicationData = caseData.getUploadAdditionalApplicationData();
        Map<String, Boolean> existingApplicationTypes = checkForExistingApplicationTypes(caseData);
        boolean fl403ApplicationAlreadyPresentForRespondent = existingApplicationTypes.get(
            C2_ALREADY_PRESENT_FOR_RESPONDENT);
        boolean c2ApplicationAlreadyPresentForRespondent = existingApplicationTypes.get(
            FL403_ALREADY_PRESENT_FOR_RESPONDENT);
        if (isNotEmpty(uploadAdditionalApplicationData)) {
            if (isNotEmpty(uploadAdditionalApplicationData.getTypeOfC2Application())
                && !DA_APPLICANT.equals(uploadAdditionalApplicationData.getRepresentedPartyType())) {
                boolean skipPayments = shouldSkipPayments(uploadAdditionalApplicationData);
                boolean applyOrderWithoutGivingNoticeToRespondent = isNotEmpty(caseData.getOrderWithoutGivingNoticeToRespondent())
                    && YesOrNo.Yes.equals(caseData.getOrderWithoutGivingNoticeToRespondent().getOrderWithoutGivingNotice()) ? true : false;
                feeTypes.addAll(getC2ApplicationsFeeTypes(uploadAdditionalApplicationData,
                                                          skipPayments,
                                                          c2ApplicationAlreadyPresentForRespondent,
                                                          applyOrderWithoutGivingNoticeToRespondent
                ));
            }
            if (isNotEmpty(uploadAdditionalApplicationData.getTemporaryOtherApplicationsBundle())) {
                String otherApplicationType = getOtherApplicationType(uploadAdditionalApplicationData);
                fromApplicationType(otherApplicationType).ifPresent(feeTypes::add);
                if (fl403ApplicationAlreadyPresentForRespondent
                    && FL403_APPLICATION_TO_VARY_DISCHARGE_OR_EXTEND_AN_ORDER.equalsIgnoreCase(otherApplicationType)
                    && DA_RESPONDENT.equals(uploadAdditionalApplicationData.getRepresentedPartyType())) {
                    feeTypes.add(C2_WITH_NOTICE_AND_FC600_FL403);
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
                                                    boolean skipPayment,
                                                    boolean c2ApplicationAlreadyPresentForRespondent,
                                                    boolean applyOrderWithoutGivingNoticeToRespondent) {
        log.info("inside getC2ApplicationsFeeTypes");
        List<FeeType> feeTypes = new ArrayList<>();
        boolean additionalCheckForDaRespondent = !applyOrderWithoutGivingNoticeToRespondent
            || (applyOrderWithoutGivingNoticeToRespondent && c2ApplicationAlreadyPresentForRespondent);
        log.info("additionalCheckForDaRespondent => " + additionalCheckForDaRespondent);
        if ((DA_RESPONDENT.equals(uploadAdditionalApplicationData.getRepresentedPartyType())
            && additionalCheckForDaRespondent)
            || CA_APPLICANT.equals(uploadAdditionalApplicationData.getRepresentedPartyType())
            || CA_RESPONDENT.equals(uploadAdditionalApplicationData.getRepresentedPartyType())) {
            fromC2ApplicationType(uploadAdditionalApplicationData.getTypeOfC2Application(), skipPayment).ifPresent(
                feeTypes::add);
        }
        log.info("return getC2ApplicationsFeeTypes feeTypes " + feeTypes);
        return feeTypes;
    }

    private static Optional<FeeType> fromC2ApplicationType(C2ApplicationTypeEnum c2ApplicationType, boolean skipPayment) {
        log.info("c2ApplicationType ==> " + c2ApplicationType);
        if (c2ApplicationType == C2ApplicationTypeEnum.applicationWithNotice) {
            return Optional.of(C2_WITH_NOTICE_AND_FC600_FL403);
        } else if (c2ApplicationType == C2ApplicationTypeEnum.applicationWithoutNotice && !skipPayment) {
            return Optional.of(C2_WITHOUT_NOTICE_AND_FP25);
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
