package uk.gov.hmcts.reform.prl.services.transfercase;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.TransferToAnotherCourtEmail;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMM_YYYY;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TransferCaseContentProvider {

    public EmailTemplateVars buildCourtTransferEmail(CaseData caseData,
                                                    String confidentialityText) {
        return TransferToAnotherCourtEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .issueDate(CommonUtils.formatDate(D_MMM_YYYY, caseData.getIssueDate()))
            .applicationType(caseData.getCaseTypeOfApplication())
            .courtName(caseData.getTransferredCourtFrom())
            .confidentialityText(confidentialityText)
            .build();
    }

}
