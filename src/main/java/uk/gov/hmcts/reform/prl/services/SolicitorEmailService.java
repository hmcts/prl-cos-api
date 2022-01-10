package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.service.notify.NotificationClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SolicitorEmailService {

    private final NotificationClient notificationClient;
    private final EmailTemplatesConfig emailTemplatesConfig;
    private final ObjectMapper objectMapper;
    @Autowired
    private EmailService emailService;

    @Value("${xui.url}")
    String manageCaseUrl;

    public EmailTemplateVars buildEmail(CaseDetails caseDetails, UserDetails userDetails) {
        List<PartyDetails> applicants = caseDetails.getCaseData()
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> applicantNamesList = applicants.stream()
            .map(element -> element.getFirstName() + " " + element.getLastName())
            .collect(Collectors.toList());

        String applicantNames = String.join(", ", applicantNamesList);

        EmailTemplateVars emailTemplateVars = SolicitorEmail.builder()
            .caseReference(caseDetails.getCaseId())
            .caseName(caseDetails.getCaseData().getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName("court name")
            .fullName(userDetails.getFullName())
            .courtEmail("C100applications@justice.gov.uk")
            .caseLink(manageCaseUrl + caseDetails.getCaseId())
            .build();

        return emailTemplateVars;

    }

    public void sendEmail(CaseDetails caseDetails, UserDetails userDetails) {

        emailService.send(
            getRecipientEmail(userDetails),
            EmailTemplateNames.SOLICITOR,
            buildEmail(caseDetails, userDetails),
            LanguagePreference.ENGLISH
        );

    }

    public String getRecipientEmail(UserDetails userDetails) {
        return userDetails.getEmail() != null ? userDetails.getEmail() : "prl_caseworker_solicitor@mailinator.com";
    }

}
