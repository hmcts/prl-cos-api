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

import static uk.gov.hmcts.reform.prl.services.pin.CaseInviteEmailService.CITIZEN_HOME;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitizenEmailService {

    @Autowired
    EmailService emailService;

    @Autowired
    UserService userService;

    @Value("${citizen.url}")
    private String citizenSignUpLink;

    public EmailTemplateVars buildCitizenCaseSubmissionEmail(UserDetails userDetails, String caseId) {
        return new CitizenCaseSubmissionEmail(String.valueOf(caseId),
                                              citizenSignUpLink + CITIZEN_HOME, userDetails.getFullName()
        );
    }

    public void sendCitizenCaseSubmissionEmail(String authorisation, String caseId) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        EmailTemplateVars email = buildCitizenCaseSubmissionEmail(userDetails, caseId);
        sendEmail(userDetails.getEmail(), email, EmailTemplateNames.CITIZEN_CASE_SUBMISSION);
    }

    public void sendCitizenCaseDeletionWarningEmail(CaseData caseData, String authorisation) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        EmailTemplateVars email = buildCitizenCaseSubmissionEmail(userDetails, String.valueOf(caseData.getId()));
        sendEmail(userDetails.getEmail(), email, EmailTemplateNames.CITIZEN_CASE_DELETION);
    }

    public void sendEmail(String address, EmailTemplateVars email, EmailTemplateNames templateName) {
        emailService.send(
            address,
            templateName,
            email,
            LanguagePreference.english
        );
    }
}
