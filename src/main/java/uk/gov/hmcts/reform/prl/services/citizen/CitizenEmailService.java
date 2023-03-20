package uk.gov.hmcts.reform.prl.services.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenCaseSubmissionEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitizenEmailService {

    public static final String CITIZEN_DASHBOARD = "/dashboard";

    @Autowired
    EmailService emailService;

    @Autowired
    UserService userService;

    @Value("${citizen.url}")
    private String citizenSignUpLink;

    public EmailTemplateVars buildCitizenCaseSubmissionEmail(UserDetails userDetails, String caseId, String caseName) {
        return new CitizenCaseSubmissionEmail(String.valueOf(caseId),
                                              citizenSignUpLink + CITIZEN_DASHBOARD, userDetails.getFullName(), caseName
        );
    }

    public void sendCitizenCaseSubmissionEmail(String authorisation, String caseId) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        EmailTemplateVars email = buildCitizenCaseSubmissionEmail(userDetails, caseId, null);
        sendEmail(userDetails.getEmail(), email);
    }

    public void sendEmail(String address, EmailTemplateVars email) {
        emailService.send(
            address,
            EmailTemplateNames.CITIZEN_CASE_SUBMISSION,
            email,
            LanguagePreference.english
        );
    }

    public void sendCitizenCaseWithdrawalEmail(String authorisation, CaseData caseData) {
        log.info("Inside sendCitizenCaseWithdrawalEmail");
        UserDetails userDetails = userService.getUserDetails(authorisation);
        EmailTemplateVars emailTemplate = buildCitizenCaseSubmissionEmail(
            userDetails, String.valueOf(caseData.getId()), caseData.getApplicantCaseName());
        sendWithdrawalEmail(userDetails.getEmail(), emailTemplate, caseData);
    }

    private void sendWithdrawalEmail(String address, EmailTemplateVars email, CaseData caseData) {
        emailService.send(
            address,
            EmailTemplateNames.CITIZEN_CASE_WITHDRAWN,
            email,
            LanguagePreference.getLanguagePreference(caseData)
        );
    }
}
