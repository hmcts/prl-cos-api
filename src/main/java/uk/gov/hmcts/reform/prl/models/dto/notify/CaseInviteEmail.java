package uk.gov.hmcts.reform.prl.models.dto.notify;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.List;

@Getter
public class CaseInviteEmail extends EmailTemplateVars {

    private final String caseName;
    private final String accessCode;
    private final String respondentFullName;
    private final String caseLink;
    private final String citizenSignUpLink;
    private final String applicantName;

    @Builder
    public CaseInviteEmail(CaseInvite caseInvite, String caseReference, PartyDetails party,
                           String caseLink, String citizenSignUpLink, CaseData caseData) {
        super(caseReference);
        this.accessCode = caseInvite.getAccessCode();
        this.respondentFullName = String.format("%s %s", party.getFirstName(), party.getLastName());
        this.caseLink = caseLink;
        this.citizenSignUpLink = citizenSignUpLink;
        this.caseName = caseData.getApplicantCaseName();
        this.applicantName = getApplicantNames(caseData);
    }

    private static String getApplicantNames(CaseData caseData) {
        List<PartyDetails> applicants = ElementUtils.unwrapElements(caseData.getApplicants());
        StringBuilder applicantNames = new StringBuilder();
        for (int i = 0; i < applicants.size(); i++) {
            applicantNames.append(String.format("%s %s", applicants.get(i).getFirstName(), applicants.get(i).getLastName()));
            if (applicants.size() >= 1 && (i == applicants.size() - 2)) {
                applicantNames.append(" and ");
            } else if (applicants.size() != 1) {
                applicantNames.append(", ");
            }
        }
        return applicantNames.toString();
    }
}
