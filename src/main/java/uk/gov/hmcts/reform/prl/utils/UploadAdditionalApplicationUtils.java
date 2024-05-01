package uk.gov.hmcts.reform.prl.utils;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.OtherApplicationType;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;

import java.math.BigDecimal;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NO;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;

@Component
@NoArgsConstructor
public class UploadAdditionalApplicationUtils {

    public static final String C2_APPLICATION_WITHIN_PROCEEDINGS = "C2 application within proceedings";
    public static final String C2_AND = "C2 and ";

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

    public String getAwPTaskNameWhenPaymentCompleted(AdditionalApplicationsBundle additionalApplicationsBundle) {
        String awpTaskNameToBe = null;
        String awpTaskNameForOtherOrder = null;
        boolean c2OrderListed = false;
        boolean otherOrderListed = false;

        if (additionalApplicationsBundle != null
            && additionalApplicationsBundle.getC2DocumentBundle() != null) {
            c2OrderListed = true;
        }
        if (additionalApplicationsBundle != null
            && additionalApplicationsBundle.getOtherApplicationsBundle() != null) {
            otherOrderListed = true;
            OtherApplicationType applicationType = additionalApplicationsBundle.getOtherApplicationsBundle().getApplicationType();
            awpTaskNameForOtherOrder = applicationType != null ? applicationType.getDisplayedValue() : "";
        }


        if (c2OrderListed && otherOrderListed) {
            awpTaskNameToBe = C2_AND + awpTaskNameForOtherOrder;
        } else if (c2OrderListed) {
            awpTaskNameToBe = C2_APPLICATION_WITHIN_PROCEEDINGS;
        } else if (otherOrderListed) {
            awpTaskNameToBe = awpTaskNameForOtherOrder;
        }
        return awpTaskNameToBe;
    }

    public String getValueOfAwpTaskToBeCreated(CaseData caseData) {
        String taskToBeCreated = YES;
        UploadAdditionalApplicationData uploadAdditionalApplicationData = caseData.getUploadAdditionalApplicationData();
        if (isNotEmpty(uploadAdditionalApplicationData != null)
            && (StringUtils.isNotEmpty(uploadAdditionalApplicationData.getAdditionalApplicationFeesToPay()))) {
            String feeAmount = uploadAdditionalApplicationData.getAdditionalApplicationFeesToPay().replace("£", "");
            if (BigDecimal.ZERO.compareTo(BigDecimal.valueOf(Double.parseDouble(feeAmount))) < 0) {
                taskToBeCreated = NO;
            }
        }

        return taskToBeCreated;
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
