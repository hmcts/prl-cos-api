package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.FL401RejectReasonEnum;
import uk.gov.hmcts.reform.prl.enums.RejectReasonEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReturnApplicationService {

    private final MiamPolicyUpgradeFileUploadService miamPolicyUpgradeFileUploadService;
    private final AllTabServiceImpl allTabsService;

    public String getLegalFullName(CaseData caseData) {

        String legalName = "";
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());

            if (applicantsWrapped.isPresent() && applicantsWrapped.get().size() == 1) {
                List<PartyDetails> applicants = applicantsWrapped.get()
                    .stream()
                    .map(Element::getValue)
                    .toList();

                String legalFirstName = applicants.get(0).getRepresentativeFirstName();
                String legalLastName = applicants.get(0).getRepresentativeLastName();

                legalName = legalFirstName + " " + legalLastName;

            } else {
                legalName = caseData.getSolicitorName();
            }
        } else if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            PartyDetails fl401Applicant = caseData
                .getApplicantsFL401();
            String legalFirstName = fl401Applicant.getRepresentativeFirstName();
            String legalLastName = fl401Applicant.getRepresentativeLastName();
            legalName = legalFirstName + " " + legalLastName;
        }

        return legalName;
    }

    public boolean noRejectReasonSelected(CaseData caseData) {

        boolean noOptionSelected = true;

        boolean hasSelectedOption = allNonEmpty(caseData.getRejectReason());
        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            hasSelectedOption = allNonEmpty(caseData.getFl401RejectReason());
        }
        if (hasSelectedOption) {
            noOptionSelected = false;
        }

        return noOptionSelected;
    }

    public String getReturnMessage(CaseData caseData, UserDetails userDetails) {
        StringBuilder returnMsgStr = new StringBuilder();

        returnMsgStr
            .append("Case name: " + caseData.getApplicantCaseName() + "\n")
            .append("Reference code: " + caseData.getId() + "\n\n")
            .append("Dear " + getLegalFullName(caseData) + ",\n\n")
            .append("Thank you for your application. Your application has been reviewed and is being returned for the following reasons:\n\n");

        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            returnMessageC100(caseData, returnMsgStr);
        } else if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            returnMessageFl401(caseData, returnMsgStr);
        }

        returnMsgStr.append("Please resolve these issues and resubmit your application.\n\n")
            .append("Kind regards,\n")
            .append(userDetails.getFullName());

        return returnMsgStr.toString();

    }

    private void returnMessageC100(CaseData caseData, StringBuilder returnMsgStr) {
        List<RejectReasonEnum> sortedRejectReason = caseData.getRejectReason().stream().sorted().toList();
        for (RejectReasonEnum reasonEnum : sortedRejectReason) {
            returnMsgStr.append(reasonEnum.getReturnMsgText());
        }
    }

    private void returnMessageFl401(CaseData caseData, StringBuilder returnMsgStr) {
        List<FL401RejectReasonEnum> sortedFl401RejectReason = caseData.getFl401RejectReason().stream().sorted().toList();
        for (FL401RejectReasonEnum reasonEnum : sortedFl401RejectReason) {
            returnMsgStr.append(reasonEnum.getReturnMsgText());
        }
    }

    public String getReturnMessageForTaskList(CaseData caseData) {
        StringBuilder returnMsgStr = new StringBuilder();
        returnMsgStr.append("\n\n");
        returnMsgStr.append("""
                                <div class='govuk-warning-text'><span class='govuk-warning-text__icon'>!</span>
                                <strong class='govuk-warning-text__text'>Application has been returned</strong></div>

                                """);

        returnMsgStr.append("""
                                Your application has been returned for the following reasons:

                                """);

        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            for (RejectReasonEnum reasonEnum : caseData.getRejectReason()) {
                returnMsgStr.append(reasonEnum.getDisplayedValue());
                returnMsgStr.append("\n\n");
            }

        } else if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            for (FL401RejectReasonEnum reasonEnum : caseData.getFl401RejectReason()) {
                returnMsgStr.append(reasonEnum.getDisplayedValue());
                returnMsgStr.append("\n\n");
            }
        }

        returnMsgStr.append("""
                                Resolve these concerns and resend your application.
                                You have been emailed the full details of your application return.""");

        return returnMsgStr.toString();

    }

    public CaseData updateMiamPolicyUpgradeDataForConfidentialDocument(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (C100_CASE_TYPE.equals(CaseUtils.getCaseTypeOfApplication(caseData))
            && TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())
            && isNotEmpty(caseData.getMiamPolicyUpgradeDetails())) {
            caseData = miamPolicyUpgradeFileUploadService.renameMiamPolicyUpgradeDocumentWithoutConfidential(
                caseData
            );
            allTabsService.getNewMiamPolicyUpgradeDocumentMap(caseData, caseDataUpdated);

        }
        return caseData;
    }
}
