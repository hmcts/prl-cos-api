package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.RejectReasonEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReturnApplicationService {

    public String getLegalFullName(CaseData caseData) {

        String legalName;

        Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());

        if (applicantsWrapped.isPresent() && applicantsWrapped.get().size() == 1) {
            List<PartyDetails> applicants = applicantsWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            String legalFirstName = applicants.get(0).getRepresentativeFirstName();
            String legalLastName = applicants.get(0).getRepresentativeLastName();

            legalName = legalFirstName + " " + legalLastName;

        } else {
            legalName = caseData.getSolicitorName();
        }
        return legalName;
    }

    public boolean noRejectReasonSelected(CaseData caseData) {

        boolean noOptionSelected = true;

        boolean hasSelectedOption = allNonEmpty(caseData.getRejectReason());

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
            .append("Thank you for your application."
                        + " Your application has been reviewed and is being returned for the following reasons:" + "\n\n");

        for (RejectReasonEnum reasonEnum : caseData.getRejectReason()) {
            returnMsgStr.append(reasonEnum.getReturnMsgText());
        }

        returnMsgStr.append("Please resolve these issues and resubmit your application.\n\n")
            .append("Kind regards,\n")
            .append(userDetails.getFullName());

        return returnMsgStr.toString();

    }
}
