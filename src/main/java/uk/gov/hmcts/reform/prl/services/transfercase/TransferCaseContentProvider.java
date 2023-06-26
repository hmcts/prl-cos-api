package uk.gov.hmcts.reform.prl.services.transfercase;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.TransferToAnotherCourtEmail;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_DASHBOARD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TransferCaseContentProvider {

    @Value("${xui.url}")
    private String manageCaseUrl;

    @Value("${citizen.url}")
    private String citizenUrl;


    public EmailTemplateVars buildCourtTransferEmailSolicitor(CaseData caseData,
                                                    String solicitorName) {
        return TransferToAnotherCourtEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .solicitorName(solicitorName)
            .caseLink(manageCaseUrl + URL_STRING + caseData.getId())
            .orderLink(manageCaseUrl + URL_STRING + caseData.getId())
            .transferDate(CommonUtils.formatDate(D_MMMM_YYYY, LocalDate.now()))
            .issueDate(CommonUtils.formatDate(D_MMMM_YYYY, caseData.getIssueDate()))
            .courtName(caseData.getCourtName())
            .build();
    }

    public EmailTemplateVars buildCourtTransferEmailCitizen(CaseData caseData,
                                                  String solicitorName,
                                                  boolean isOtherPerson) {
        return TransferToAnotherCourtEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .citizenSignUpLink(citizenUrl)
            .caseLink(isOtherPerson ? String.valueOf(caseData.getId()) : (citizenUrl + CITIZEN_DASHBOARD))
            .orderLink(isOtherPerson ? String.valueOf(caseData.getId()) : (citizenUrl + CITIZEN_DASHBOARD))
            .transferDate(CommonUtils.formatDate(D_MMMM_YYYY, LocalDate.now()))
            .courtName(caseData.getCourtName())
            .build();
    }
}
