package uk.gov.hmcts.reform.prl.services.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenCaseSubmissionEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.List;
import java.util.stream.Collectors;

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

    public void sendCitizenCaseWithdrawalEmail(String authorisation, String caseId, CaseData caseData) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        EmailTemplateVars emailTemplete = buildCitizenCaseSubmissionEmail(userDetails, caseId);


        List<PartyDetails> applicants = caseData
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> applicantEmailIds = applicants.stream()
            .map(element -> element.getEmail())
            .collect(Collectors.toList());
        applicantEmailIds.stream().forEach(i -> {
            sendWithdrawalEmail(i, emailTemplete); });

    }

    private void sendWithdrawalEmail(String address, EmailTemplateVars email) {
        emailService.send(
            address,
            EmailTemplateNames.CITIZEN_WITHDRAWN,
            email,
            LanguagePreference.english
        );
    }
}
