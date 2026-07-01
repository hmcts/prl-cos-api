package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_CASE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NAME_HMCTS_INTERNAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Service
@RequiredArgsConstructor
public class CaseNameService {
    public static final String CA_CASE_NAME_FORMAT = "%s V %s";
    public static final String DA_CASE_NAME_FORMAT = "%s %s & %s %s";

    public String getCaseNameForCA(String applicantLastName, String respondentLastName) {
        return String.format(CA_CASE_NAME_FORMAT, applicantLastName, respondentLastName);
    }

    public String getCaseNameForDA(String applicantFirstName, String applicantLastName, String respondentFirstName, String respondentLastName) {
        return String.format(DA_CASE_NAME_FORMAT, applicantFirstName, applicantLastName, respondentFirstName, respondentLastName);
    }

    public void setFinalCaseName(Map<String, Object> updatedCaseData, CaseData caseData) {
        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            setCaseNameForCA(updatedCaseData, caseData);
        } else if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            setCaseNameForDA(updatedCaseData, caseData);
        } else {
            throw new RuntimeException("Invalid caseTypeOfApplication found for case " + caseData.getId());
        }
    }

    private void setCaseNameForCA(Map<String, Object> updatedCaseData, CaseData caseData) {
        if (!isEmpty(caseData.getApplicants()) && !isEmpty(caseData.getRespondents())) {
            PartyDetails applicant = caseData.getApplicants().stream().findFirst().orElseThrow().getValue();
            PartyDetails respondent = caseData.getRespondents().stream().findFirst().orElseThrow().getValue();
            String caseName = getCaseNameForCA(applicant.getLastName(), respondent.getLastName());
            updateCaseName(updatedCaseData, caseName);
        }
    }

    private void setCaseNameForDA(Map<String, Object> updatedCaseData, CaseData caseData) {
        PartyDetails applicant = caseData.getApplicantsFL401();
        PartyDetails respondent = caseData.getRespondentsFL401();
        if (applicant != null && respondent != null) {
            String caseName = getCaseNameForDA(applicant.getFirstName(), applicant.getLastName(),
                                            respondent.getFirstName(), respondent.getLastName());
            updateCaseName(updatedCaseData, caseName);
        }
    }

    private void updateCaseName(Map<String, Object> updatedCaseData, String caseName) {
        updatedCaseData.put(APPLICANT_CASE_NAME, caseName);
        updatedCaseData.put(CASE_NAME_HMCTS_INTERNAL, caseName);
    }
}
