package uk.gov.hmcts.reform.prl.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.OtherApplicationType;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;

import java.math.BigDecimal;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NO;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;

@Slf4j
@Component
public class UploadAdditionalApplicationUtils {

    public static final String C2_APPLICATION_WITHIN_PROCEEDINGS = "C2 application within proceedings";
    public static final String C2_AND = "C2 and ";

    private UploadAdditionalApplicationUtils() {

    }

    public String getAwPTaskName(CaseData caseData) {
        String awpTaskNameToBe = null;
        if (caseData.getUploadAdditionalApplicationData() != null
            && caseData.getUploadAdditionalApplicationData().getAdditionalApplicationsApplyingFor() != null
            && isNotEmpty(caseData.getUploadAdditionalApplicationData().getAdditionalApplicationsApplyingFor())) {
            boolean c2OrderListed = false;
            boolean otherOrderListed = false;
            String awpTaskNameForOtherOrder = null;
            if (caseData.getUploadAdditionalApplicationData().getAdditionalApplicationsApplyingFor().contains(
                AdditionalApplicationTypeEnum.c2Order)) {
                c2OrderListed = true;
            }
            if (caseData.getUploadAdditionalApplicationData()
                .getAdditionalApplicationsApplyingFor().contains(AdditionalApplicationTypeEnum.otherOrder)
                && caseData.getUploadAdditionalApplicationData().getTemporaryOtherApplicationsBundle() != null) {
                otherOrderListed = true;
                OtherApplicationType applicationType = getOtherApplicationType(caseData.getUploadAdditionalApplicationData()
                                                                                   .getTemporaryOtherApplicationsBundle());
                awpTaskNameForOtherOrder = applicationType != null ? applicationType.getDisplayedValue() : "";
            }

            if (c2OrderListed && otherOrderListed) {
                awpTaskNameToBe = C2_AND + awpTaskNameForOtherOrder;
            } else if (c2OrderListed) {
                awpTaskNameToBe = C2_APPLICATION_WITHIN_PROCEEDINGS;
            } else if (otherOrderListed) {
                awpTaskNameToBe = awpTaskNameForOtherOrder;
            }
        }
        return awpTaskNameToBe;
    }

    public String getValueOfAwpTaskToBeCreated(CaseData caseData) {
        String taskToBeCreated = YES;
        UploadAdditionalApplicationData uploadAdditionalApplicationData = caseData.getUploadAdditionalApplicationData();
        log.info("uploadAdditionalApplicationData is present");

        log.info("ObjectUtils.isNotEmpty(uploadAdditionalApplicationData.getAdditionalApplicationFeesToPay():: "
                     + isNotEmpty(uploadAdditionalApplicationData.getAdditionalApplicationFeesToPay()));
        log.info("ObjectUtils.isNotEmpty(uploadAdditionalApplicationData.getAdditionalApplicationFeesToPay():: "
                     + isNotEmpty(uploadAdditionalApplicationData.getAdditionalApplicationFeesToPay()));
        if (isNotEmpty(uploadAdditionalApplicationData != null)) {
            log.info("uploadAdditionalApplicationData is present");
            if (isNotEmpty(uploadAdditionalApplicationData.getAdditionalApplicationFeesToPay())) {
                log.info("uploadAdditionalApplicationData.getAdditionalApplicationFeesToPay() is not null");
                String feeAmount = uploadAdditionalApplicationData.getAdditionalApplicationFeesToPay().replace("£", "");
                log.info("uploadAdditionalApplicationData.getAdditionalApplicationFeesToPay() is::"
                             + uploadAdditionalApplicationData.getAdditionalApplicationFeesToPay());
                log.info("feeAmount is::" + feeAmount);
                if (BigDecimal.ZERO.compareTo(BigDecimal.valueOf(Double.valueOf(feeAmount))) < 0) {
                    log.info("Its more than Zero amount AWP");
                    taskToBeCreated = NO;
                }
            }
        }

        return taskToBeCreated;
    }

    public String getValueOfAwpTaskUrgency(CaseData caseData) {
        String urgencyTiemFrame = null;
        int urgencyTiemFrameC2 = 0;
        int urgencyTiemFrameOther = 0;
        OtherApplicationsBundle temporaryOtherApplicationsBundle = caseData.getUploadAdditionalApplicationData()
            .getTemporaryOtherApplicationsBundle();
        C2DocumentBundle c2DocumentBundle = caseData.getUploadAdditionalApplicationData()
            .getTemporaryC2Document();

        if (c2DocumentBundle.getUrgencyTimeFrameType() != null) {
            if (temporaryOtherApplicationsBundle != null
                && c2DocumentBundle != null) {
                if (!c2DocumentBundle.getUrgencyTimeFrameType().toString().replaceAll("\\D", "").equals(EMPTY_STRING)) {
                    urgencyTiemFrameC2 = Integer.parseInt(c2DocumentBundle.getUrgencyTimeFrameType().toString().replaceAll(
                        "\\D",
                        ""
                    ));
                }
                if (!temporaryOtherApplicationsBundle.getUrgencyTimeFrameType().toString().replaceAll("\\D", "").equals(
                    EMPTY_STRING)) {
                    urgencyTiemFrameOther = Integer.parseInt(temporaryOtherApplicationsBundle.getUrgencyTimeFrameType()
                                                                 .toString().replaceAll("\\D", ""));
                }
                if (urgencyTiemFrameC2 > urgencyTiemFrameOther) {
                    return temporaryOtherApplicationsBundle.getUrgencyTimeFrameType().toString();
                } else {
                    return c2DocumentBundle.getUrgencyTimeFrameType().toString();
                }
            } else if (temporaryOtherApplicationsBundle != null) {
                urgencyTiemFrame = temporaryOtherApplicationsBundle.getUrgencyTimeFrameType().toString();
            } else if (c2DocumentBundle != null) {
                urgencyTiemFrame = c2DocumentBundle.getUrgencyTimeFrameType().toString();
            }
        }

        return urgencyTiemFrame;
    }

    public OtherApplicationType getOtherApplicationType(OtherApplicationsBundle temporaryOtherApplicationsBundle) {
        OtherApplicationType applicationType = null;
        if (null != temporaryOtherApplicationsBundle.getCaApplicantApplicationType()) {
            applicationType = OtherApplicationType.valueOf(temporaryOtherApplicationsBundle.getCaApplicantApplicationType().name());
        } else if (null != temporaryOtherApplicationsBundle.getCaRespondentApplicationType()) {
            applicationType = OtherApplicationType.valueOf(temporaryOtherApplicationsBundle.getCaRespondentApplicationType().name());
        } else if (null != temporaryOtherApplicationsBundle.getDaApplicantApplicationType()) {
            applicationType = OtherApplicationType.valueOf(temporaryOtherApplicationsBundle.getDaApplicantApplicationType().name());
        } else if (null != temporaryOtherApplicationsBundle.getDaRespondentApplicationType()) {
            applicationType = OtherApplicationType.valueOf(temporaryOtherApplicationsBundle.getDaRespondentApplicationType().name());
        }
        return applicationType;
    }
}
