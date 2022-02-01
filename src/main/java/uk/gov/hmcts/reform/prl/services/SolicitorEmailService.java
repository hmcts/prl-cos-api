package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
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

    @Value("${uk.gov.notify.email.application.email-id}")
    private String courtEmail;

    @Value("${uk.gov.notify.email.application.court-name}")
    private String courtName;

    @Value("${xui.url}")
    private String manageCaseUrl;

    public EmailTemplateVars buildEmail(CaseDetails caseDetails) {

        List<PartyDetails> applicants = emailService.getCaseData(caseDetails)
            .getApplicants()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<String> applicantNamesList = applicants.stream()
            .map(element -> element.getFirstName() + " " + element.getLastName())
            .collect(Collectors.toList());

        String applicantNames = String.join(", ", applicantNamesList);

        return SolicitorEmail.builder()
            .caseReference(String.valueOf(caseDetails.getId()))
            .caseName(emailService.getCaseData(caseDetails).getApplicantCaseName())
            .applicantName(applicantNames)
            .courtName(courtName)
            //.fullName(userDetails.getFullName())
            .courtEmail(courtEmail)
            .caseLink(manageCaseUrl + caseDetails.getId())
            .build();

    }

    public void sendEmail(CaseDetails caseDetails) {

        emailService.send(
            caseDetails.getData().get("applicantSolicitorEmailAddress").toString(),
            EmailTemplateNames.SOLICITOR,
            buildEmail(caseDetails),
            LanguagePreference.ENGLISH
        );

    }
}
