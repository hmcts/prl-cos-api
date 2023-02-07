package uk.gov.hmcts.reform.prl.services.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
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

    public EmailTemplateVars buildCitizenCaseSubmissionEmail(UserDetails userDetails, String caseId) {
        return new CitizenCaseSubmissionEmail(String.valueOf(caseId),
                                              citizenSignUpLink + CITIZEN_DASHBOARD, userDetails.getFullName()
        );
    }

    public void sendCitizenCaseSubmissionEmail(String authorisation, String caseId) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        EmailTemplateVars email = buildCitizenCaseSubmissionEmail(userDetails, caseId);
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
}
