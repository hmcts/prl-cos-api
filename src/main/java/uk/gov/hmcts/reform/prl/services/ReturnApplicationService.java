package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.FL401RejectReasonEnum;
import uk.gov.hmcts.reform.prl.enums.RejectReasonEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReturnApplicationService {

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

        returnMsgStr.append("""
                Case name: %s
                Reference code: %s
                
                Dear %s,
                
                Thank you for your application. Your application has been reviewed and is being returned for the following reasons:
                
                """.formatted(caseData.getApplicantCaseName(),caseData.getId(),getLegalFullName(caseData)));
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            for (RejectReasonEnum reasonEnum : caseData.getRejectReason()) {
                returnMsgStr.append(reasonEnum.getReturnMsgText());
            }
        } else if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            for (FL401RejectReasonEnum reasonEnum : caseData.getFl401RejectReason()) {
                returnMsgStr.append(reasonEnum.getReturnMsgText());
            }
        }

        returnMsgStr.append("Please resolve these issues and resubmit your application.\n\n")
            .append("Kind regards,\n")
            .append(userDetails.getFullName());

        return returnMsgStr.toString();

    }

    public String getReturnMessageForTaskList(CaseData caseData) {
        StringBuilder returnMsgStr = new StringBuilder();
        returnMsgStr.append("""
                                                

                        <div class='govuk-warning-text'><span class='govuk-warning-text__icon'>\
                        !</span><strong class='govuk-warning-text__text'>Application has been returned</strong></div>
                                        
                        Your application has been  returned for the following reasons:
                                        
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

        returnMsgStr.append("Resolve these concerns and resend your application."
                                + "You have been emailed the full details of your application return.");

        return returnMsgStr.toString();

    }
}
